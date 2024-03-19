package com.esentri.quartz.camelExample;

import org.apache.camel.main.Main;

public class CamelExample {

    public static void main(String[] args) throws Exception {
        Main camel = new Main(CamelExample.class);

        // shutdown application after specified time
        CamelShutdownJob shutdownJob = new CamelShutdownJob(90L * 1000L, camel);

        shutdownJob.excecute();

        camel.run();
    }
}
