package com.esentri.quartz.springboot.configuration;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.CompositeRetryPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Configuration
public class RestTemplateConfiguration {

    @Bean
    public SerializableRestTemplate restTemplate(SerializableRetryTemplate retryTemplate) {
        return new RestTemplateBuilder()
                .configure(new SerializableRestTemplate(retryTemplate));
    }

    @Bean
    public SerializableRetryTemplate retryTemplate() {
        return new SerializableRetryTemplate().init();
    }


    public static class SerializableRestTemplate extends RestTemplate implements Serializable {

        private final SerializableRetryTemplate retryTemplate;

        @Serial
        private static final long serialVersionUID = 1L;

        public SerializableRestTemplate(SerializableRetryTemplate retryTemplate) {
            super();
            this.retryTemplate = retryTemplate;
        }

        @Override
        public <T> ResponseEntity<T> exchange(@NonNull String url,@NonNull HttpMethod method, @Nullable HttpEntity<?> requestEntity,@NonNull Class<T> responseType,@NonNull Map<String, ?> uriVariables) throws RestClientException {
            return retryTemplate.execute(retryContext -> {
                log.info("Retry count: {}", retryContext.getRetryCount());
                return super.exchange(url, method, requestEntity, responseType, uriVariables);
            });
        }
    }

    public static class SerializableRetryTemplate extends RetryTemplate implements Serializable {

        private final Set<HttpStatusCode> httpStatusRetry;

        @Serial
        private static final long serialVersionUID = 1L;

        public SerializableRetryTemplate() {
            super();
            this.httpStatusRetry = new HashSet<>();
        }

        public SerializableRetryTemplate init() {

            this.httpStatusRetry.addAll(getDefaults());

            ExceptionClassifierRetryPolicy policy = new ExceptionClassifierRetryPolicy();
            policy.setExceptionClassifier(configureStatusCodeBasedRetryPolicy());


            CompositeRetryPolicy finalPolicy = new CompositeRetryPolicy();
            finalPolicy.setPolicies(new RetryPolicy[]{policy});
            this.setRetryPolicy(finalPolicy);
            this.setBackOffPolicy(new FixedBackOffPolicy());
            return this;
        }

        private Classifier<Throwable, RetryPolicy> configureStatusCodeBasedRetryPolicy() {
            SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(5);
            NeverRetryPolicy neverRetryPolicy = new NeverRetryPolicy();

            return throwable -> {
                if (throwable instanceof HttpStatusCodeException httpException) {
                    return getRetryPolicyForStatus(httpException.getStatusCode(), simpleRetryPolicy, neverRetryPolicy);
                }
                return neverRetryPolicy;
            };
        }

        private RetryPolicy getRetryPolicyForStatus(HttpStatusCode httpStatusCode, SimpleRetryPolicy simpleRetryPolicy, NeverRetryPolicy neverRetryPolicy) {

            if (this.httpStatusRetry.contains(httpStatusCode)) {
                return simpleRetryPolicy;
            }
            return neverRetryPolicy;
        }

        private static Set<HttpStatusCode> getDefaults() {
            return Set.of(
                    HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()),
                    HttpStatusCode.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value()),
                    HttpStatusCode.valueOf(HttpStatus.BAD_GATEWAY.value()),
                    HttpStatusCode.valueOf(HttpStatus.GATEWAY_TIMEOUT.value())
            );
        }
    }


}
