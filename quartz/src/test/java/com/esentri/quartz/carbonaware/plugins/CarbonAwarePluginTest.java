package com.esentri.quartz.carbonaware.plugins;

import com.esentri.quartz.carbonaware.plugins.listeners.CarbonStatisticsTriggerListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.Scheduler;
import org.quartz.core.ListenerManagerImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarbonAwarePluginTest {
    private static final String PERSISTENCE_CLIENT_REF = "com.esentri.quartz.carbonaware.testsupport.PersistenceClient";
    private static final String REST_CLIENT_REF = "com.esentri.quartz.carbonaware.testsupport.CarbonForecastClient";

    @Mock
    private Scheduler scheduler;

    private final ListenerManagerImpl listenerManager = new ListenerManagerImpl();

    private CarbonAwarePlugin sut;

    @BeforeEach
    void setUp() throws Exception {
        when(scheduler.getListenerManager()).thenReturn(listenerManager);
    }

    @Test
    void shouldNotInitializeAnyListener_IfNoListenerIsEnabled() throws Exception {
        sut = new CarbonAwarePlugin();

        sut.initialize("name", scheduler, null);

        assertThat(listenerManager.getTriggerListeners()).isEmpty();
    }

    @Test
    void shouldNotInitializeStatisticsListener_IfValuesAreConfiguredButNotEnabled() throws Exception {
        sut = new CarbonAwarePlugin();
        sut.setPersistenceClientImplementationClass(PERSISTENCE_CLIENT_REF);
        sut.setRestClientImplementationClass(REST_CLIENT_REF);
        sut.setDryrun(false);

        sut.setEnableStatistics(false);
        sut.initialize("name", scheduler, null);

        assertThat(listenerManager.getTriggerListeners()).isEmpty();
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
                .singleElement()
                .isInstanceOf(CarbonStatisticsTriggerListener.class);
    }

}