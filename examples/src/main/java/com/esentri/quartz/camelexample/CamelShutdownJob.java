package com.esentri.quartz.camelexample;

import org.apache.camel.main.Main;

public class CamelShutdownJob implements Runnable {
  long waitingFor = -1;
  Main camel;

  public CamelShutdownJob(long waitingFor, Main camel){
    this.waitingFor = waitingFor;
    this.camel = camel;
  }

  public void excecute(){

    Thread thread = new Thread(this);

    thread.start();
  }

  @Override
  public void run() {

    try {
      synchronized (this) {
        this.wait( waitingFor );
      }
    } catch (InterruptedException e) {
    }

    try {
      System.out.println("camel.shutdown()");
      camel.shutdown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
