package com.esentri.quartz.carbonaware.plugins;

import com.esentri.quartz.carbonaware.clients.opendata.EnergyChartsForecastProvider;
import com.esentri.quartz.carbonaware.clients.opendata.OpenDataUpdateJob;
import com.esentri.quartz.carbonaware.plugins.listeners.CarbonStatisticsTriggerListener;
import com.esentri.quartz.carbonaware.plugins.listeners.TimeShiftingTriggerListener;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.*;
import org.quartz.core.ListenerManagerImpl;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarbonAwarePluginTest {
    private static final String PERSISTENCE_CLIENT_REF = "com.esentri.quartz.carbonaware.testsupport.PersistenceClient";
    private static final String REST_CLIENT_REF = "com.esentri.quartz.carbonaware.testsupport.CarbonForecastClient";

    @Mock
    private Scheduler scheduler;

    private final ListenerManagerImpl listenerManager = new ListenerManagerImpl();

    private CarbonAwarePlugin sut;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() throws Exception {
        when(scheduler.getListenerManager()).thenReturn(listenerManager);
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @Test
    void shouldInitializeTimeShiftingTriggerListenerByDefault() throws Exception {
        sut = new CarbonAwarePlugin();

        sut.initialize("name", scheduler, null);

        assertThat(listenerManager.getTriggerListeners())
                .singleElement()
                .isInstanceOf(TimeShiftingTriggerListener.class);
    }

    @Test
    void shouldNotInitializeStatisticsListener_IfValuesAreConfiguredButNotEnabled() throws Exception {
        sut = new CarbonAwarePlugin();
        sut.setPersistenceClientImplementationClass(PERSISTENCE_CLIENT_REF);
        sut.setRestClientImplementationClass(REST_CLIENT_REF);
        sut.setDryrun(false);

        sut.setEnableStatistics(false);
        sut.initialize("name", scheduler, null);

        assertThat(listenerManager.getTriggerListeners())
                .singleElement()
                .isInstanceOf(TimeShiftingTriggerListener.class);
    }

    @Test
    void shouldInitializeStatisticsListener_IfValuesAreConfiguredAndEnabled() throws Exception {
        sut = new CarbonAwarePlugin();
        sut.setPersistenceClientImplementationClass(PERSISTENCE_CLIENT_REF);
        sut.setRestClientImplementationClass(REST_CLIENT_REF);
        sut.setDryrun(false);

        sut.setEnableStatistics(true);
        sut.initialize("name", scheduler, null);

        assertThat(listenerManager.getTriggerListeners())
                .filteredOn(CarbonStatisticsTriggerListener.class::isInstance)
                .singleElement()
                .isInstanceOf(CarbonStatisticsTriggerListener.class);
    }

    @Test
    void shouldInitializeOpenDataProvider_WhenUseOpenDataProviderIsTrue() throws Exception {
        // Given
        sut = new CarbonAwarePlugin();
        sut.setUseOpenDataProvider(true);
        sut.setOpenDataLocations("de,fr");

        // Set up mocks for job scheduling
        ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);

        // Set up WireMock to mock the API responses
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);

        // Mock responses for the locations
        String mockResponseDe = "{\"unix_seconds\":[1626432000,1626435600],\"co2eq\":[300.5,290.2],\"co2eq_forecast\":[null,null]}";
        stubFor(get(urlEqualTo("/co2eq?country=de"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponseDe)));

        String mockResponseFr = "{\"unix_seconds\":[1626432000,1626435600],\"co2eq\":[200.5,190.2],\"co2eq_forecast\":[null,null]}";
        stubFor(get(urlEqualTo("/co2eq?country=fr"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponseFr)));

        // Use reflection to set the apiUrlTemplate field in EnergyChartsForecastProvider
        java.lang.reflect.Field apiUrlTemplateField = EnergyChartsForecastProvider.class.getDeclaredField("apiUrlTemplate");
        apiUrlTemplateField.setAccessible(true);
        apiUrlTemplateField.set(null, "http://localhost:8089/co2eq?country=%s");

        // When
        sut.initialize("name", scheduler, null);
        sut.start(); // Need to call start() as the job scheduling happens in the start() method

        // Then
        // Verify that the scheduler.scheduleJob was called with the correct job and trigger
        // The actual implementation uses scheduleJob(JobDetail, Set<Trigger>, boolean)
        verify(scheduler).scheduleJob(jobDetailCaptor.capture(), any(), eq(true));

        JobDetail capturedJobDetail = jobDetailCaptor.getValue();

        // Verify job details
        assertThat(capturedJobDetail.getKey().getName()).isEqualTo(OpenDataUpdateJob.JOB_NAME);
        assertThat(capturedJobDetail.getKey().getGroup()).isEqualTo(OpenDataUpdateJob.JOB_GROUP_NAME);
        assertThat(capturedJobDetail.getJobClass()).isEqualTo(OpenDataUpdateJob.class);
    }

    @Test
    void shouldPassDryrunParameterToTimeShiftingTriggerListener() throws Exception {
        // Given
        sut = new CarbonAwarePlugin();
        sut.setDryrun(true);

        // When
        sut.initialize("name", scheduler, null);

        // Then
        // Get the TimeShiftingTriggerListener from the listener manager
        TimeShiftingTriggerListener listener = (TimeShiftingTriggerListener) listenerManager.getTriggerListeners().get(0);

        // Use reflection to check the dryRun field value
        java.lang.reflect.Field dryRunField = TimeShiftingTriggerListener.class.getDeclaredField("dryRun");
        dryRunField.setAccessible(true);
        boolean dryRunValue = (boolean) dryRunField.get(listener);

        // Verify that dryRun is set to true
        assertThat(dryRunValue).isTrue();
    }
}
