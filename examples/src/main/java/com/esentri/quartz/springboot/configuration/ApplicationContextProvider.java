package com.esentri.quartz.springboot.configuration;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Order(Integer.MIN_VALUE)
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    @Getter
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        setGlobalApplicationContext(applicationContext);
    }

    private static void setGlobalApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }
}
