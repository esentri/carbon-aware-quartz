package com.esentri.quartz.camelexample;

import org.apache.camel.main.Main;

/**
 * A demonstration application showcasing Apache Camel integration with Carbon-Aware-Quartz.
 *
 * <p>This example application demonstrates how to run Apache Camel routes with
 * a scheduled shutdown mechanism using Quartz scheduling. The application creates
 * a Camel main instance and configures it to automatically shutdown after a
 * specified time period.</p>
 *
 * <p>The application is designed to run as a standalone Java application and
 * includes automatic cleanup to prevent resource leaks during testing or
 * demonstration scenarios.</p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>{@code
 * java -cp classpath com.esentri.quartz.camelexample.CamelExample
 * }</pre>
 *
 * <p><strong>Configuration:</strong> The application can be configured using
 * system properties, particularly the Quartz properties file location via
 * the {@code org.quartz.properties} system property.</p>
 *
 * @author Carbon-Aware-Quartz Framework
 * @version 1.0
 * @since 1.0
 *
 * @see org.apache.camel.main.Main
 * @see CamelShutdownJob
 */
public class CamelExample {

    /**
     * The main entry point for the Camel integration example.
     *
     * <p>This method initializes an Apache Camel main instance configured
     * with the current class as the configuration source. It also sets up
     * an automatic shutdown mechanism that will terminate the application
     * after 90 seconds (90,000 milliseconds) to prevent indefinite running
     * during demonstrations.</p>
     *
     * <p>The application flow consists of:</p>
     * <ol>
     *   <li>Creating a new Camel Main instance</li>
     *   <li>Configuring a shutdown job with a 90-second delay</li>
     *   <li>Executing the shutdown job (scheduling it)</li>
     *   <li>Starting the Camel context and blocking until shutdown</li>
     * </ol>
     *
     * <p><strong>Implementation Note:</strong> The shutdown mechanism ensures
     * that the application terminates gracefully, making it suitable for
     * demonstration environments where manual intervention might not be
     * available.</p>
     *
     * @param args command line arguments passed to the application.
     *            Currently not processed by this implementation
     *
     * @throws Exception if an error occurs during Camel initialization,
     *                  route configuration, or application execution.
     *                  This includes but is not limited to:
     *                  <ul>
     *                    <li>Camel route configuration errors</li>
     *                    <li>Resource initialization failures</li>
     *                    <li>Shutdown job scheduling errors</li>
     *                  </ul>
     *
     * @see Main#run()
     * @see CamelShutdownJob#excecute()
     */
    public static void main(String[] args) throws Exception {
        Main camel = new Main(CamelExample.class);

        // shutdown application after specified time
        CamelShutdownJob shutdownJob = new CamelShutdownJob(90L * 1000L, camel);

        shutdownJob.excecute();

        camel.run();
    }
}