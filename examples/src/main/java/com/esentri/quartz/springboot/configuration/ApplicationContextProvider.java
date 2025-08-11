package com.esentri.quartz.springboot.configuration;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * A Spring component that provides static access to the Spring ApplicationContext.
 *
 * <p>This utility class implements {@link ApplicationContextAware} to receive the
 * ApplicationContext during Spring initialization and makes it available statically
 * throughout the application lifecycle. This is particularly useful for accessing
 * Spring beans from non-Spring managed classes or static contexts.</p>
 *
 * <p>The component is ordered with {@link Order} MinValue to ensure it is
 * initialized early in the Spring context lifecycle, making the ApplicationContext
 * available as soon as possible for other components that might need it.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Get a bean from the ApplicationContext
 * MyService myService = ApplicationContextProvider.getApplicationContext()
 *     .getBean(MyService.class);
 *
 * // Check if a bean exists
 * boolean exists = ApplicationContextProvider.getApplicationContext()
 *     .containsBean("myBeanName");
 * }</pre>
 *
 * <p><strong>Warning:</strong> Static access to the ApplicationContext should be
 * used sparingly and only when dependency injection is not feasible. Prefer
 * constructor or setter injection whenever possible for better testability and
 * loose coupling.</p>
 *
 * @author Carbon-Aware-Quartz Framework
 * @version 1.0
 * @since 1.0
 *
 * @see ApplicationContextAware
 * @see ApplicationContext
 * @see Order
 */
@Order(Integer.MIN_VALUE)
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    /**
     * The static reference to the Spring ApplicationContext.
     *
     * <p>This field is populated during Spring initialization and provides
     * access to the ApplicationContext from static contexts throughout the
     * application lifecycle.</p>
     *
     * @return the Spring ApplicationContext instance
     */
    @Getter
    private static ApplicationContext applicationContext;

    /**
     * Callback method invoked by Spring to set the ApplicationContext.
     *
     * <p>This method is called automatically by the Spring framework during
     * the initialization phase. It stores the ApplicationContext reference
     * in a static field to make it accessible throughout the application.</p>
     *
     * <p>The method is marked with {@code @NonNull} to indicate that the
     * applicationContext parameter must not be null, enforcing Spring's
     * contract for ApplicationContextAware implementations.</p>
     *
     * @param applicationContext the ApplicationContext object to be used by this object.
     *                          Must not be {@code null}
     *
     * @throws BeansException if an error occurs during ApplicationContext setup.
     *                       This is a runtime exception that indicates a serious
     *                       configuration problem in the Spring context
     *
     * @see ApplicationContextAware#setApplicationContext(ApplicationContext)
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        setGlobalApplicationContext(applicationContext);
    }

    /**
     * Internal helper method to set the global ApplicationContext reference.
     *
     * <p>This private method encapsulates the logic for setting the static
     * ApplicationContext field. It is separated from the public callback method
     * to provide a clear separation of concerns and potential future extensibility.</p>
     *
     * <p><strong>Implementation Note:</strong> This method is thread-safe as it
     * is only called once during Spring initialization in a controlled manner.</p>
     *
     * @param context the ApplicationContext to store as the global reference.
     *               Should not be {@code null} under normal circumstances
     */
    private static void setGlobalApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }
}