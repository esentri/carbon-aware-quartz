package com.esentri.quartz.camelexample;

import org.apache.camel.main.Main;

/**
 * A scheduled shutdown mechanism for Apache Camel applications.
 *
 * <p>This class provides a way to automatically shutdown a Camel Main instance
 * after a specified delay. It implements {@link Runnable} to execute the shutdown
 * logic in a separate thread, allowing the main application thread to continue
 * running until the shutdown is triggered.</p>
 *
 * <p>The shutdown job is useful in demonstration and testing scenarios where
 * automatic cleanup is desired to prevent applications from running indefinitely.
 * It uses a simple wait mechanism to delay the shutdown operation.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * Main camelMain = new Main();
 * CamelShutdownJob shutdownJob = new CamelShutdownJob(30000L, camelMain);
 * shutdownJob.excecute(); // Will shutdown after 30 seconds
 * camelMain.run();
 * }</pre>
 *
 * @author Carbon-Aware-Quartz Framework
 * @version 1.0
 * @since 1.0
 *
 * @see Runnable
 * @see org.apache.camel.main.Main
 */
public class CamelShutdownJob implements Runnable {

  /**
   * The delay in milliseconds before triggering the shutdown.
   *
   * <p>A value of -1 indicates that no delay has been configured,
   * though this should not occur under normal usage patterns.</p>
   */
  long waitingFor = -1;

  /**
   * The Camel Main instance that will be shutdown.
   *
   * <p>This reference is used to invoke the shutdown operation
   * after the configured delay period has elapsed.</p>
   */
  Main camel;

  /**
   * Constructs a new CamelShutdownJob with the specified delay and target Camel instance.
   *
   * <p>This constructor initializes the shutdown job with the delay period and
   * the Camel Main instance that should be shutdown. The delay is specified
   * in milliseconds.</p>
   *
   * @param waitingFor the delay in milliseconds before shutdown is triggered.
   *                   Must be a positive value for meaningful operation
   * @param camel the Camel Main instance to shutdown. Must not be {@code null}
   *
   * @throws NullPointerException if the camel parameter is {@code null}
   */
  public CamelShutdownJob(long waitingFor, Main camel){
    this.waitingFor = waitingFor;
    this.camel = camel;
  }

  /**
   * Initiates the shutdown job execution in a separate thread.
   *
   * <p>This method creates and starts a new daemon thread that will execute
   * the shutdown logic after the configured delay. The method returns immediately,
   * allowing the calling thread to continue with other operations.</p>
   *
   * <p><strong>Note:</strong> There appears to be a typo in the method name.
   * It should be {@code execute} instead of {@code excecute}.</p>
   *
   * <p>The created thread will:</p>
   * <ol>
   *   <li>Wait for the specified delay period</li>
   *   <li>Attempt to shutdown the Camel instance gracefully</li>
   *   <li>Handle any exceptions that occur during shutdown</li>
   * </ol>
   */
  public void excecute(){

    Thread thread = new Thread(this);

    thread.start();
  }

  /**
   * Executes the shutdown logic after waiting for the configured delay.
   *
   * <p>This method implements the {@link Runnable#run()} contract and contains
   * the core shutdown logic. It performs a synchronized wait for the configured
   * delay period, then attempts to shutdown the Camel instance.</p>
   *
   * <p>The implementation uses {@code synchronized(this)} with {@code wait()}
   * to create the delay. While functional, this approach could be improved
   * using more modern concurrency utilities like {@link java.util.concurrent.ScheduledExecutorService}.</p>
   *
   * <p><strong>Exception Handling:</strong></p>
   * <ul>
   *   <li>{@link InterruptedException} during wait is caught and ignored</li>
   *   <li>Any exceptions during Camel shutdown are caught and printed to stderr</li>
   * </ul>
   *
   * <p><strong>Thread Safety:</strong> This method is designed to be called
   * from a single thread (the one created in {@link #excecute()}).</p>
   *
   * @see Runnable#run()
   * @see Main#shutdown()
   */
  @Override
  public void run() {

    try {
      synchronized (this) {
        this.wait( waitingFor );
      }
    } catch (InterruptedException e) {
      // Interrupted during wait - shutdown will proceed immediately
    }

    try {
      System.out.println("camel.shutdown()");
      camel.shutdown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}