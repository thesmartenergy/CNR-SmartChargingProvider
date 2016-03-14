/*
 * Copyright 2016 ITEA 12004 SEAS Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thesmartenergy.cnr;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.thesmartenergy.cnr.scp.ChargingPlanSubscriber;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.servlet.ServletContext;

/**
 *
 * @author maxime.lefrancois
 */
public class App {

    private static int number = 0;
    private static final Logger LOG = Logger.getLogger(App.class.getSimpleName());

    public final static String DIRNAME = "com.thesmartenergy.cnr.dirname";
    public final static String PUBLISH_ENDPOINT_KEY = "com.thesmartenergy.cnr.publish.endpoint";
    public final static String PUBLISH_QUEUE_KEY = "com.thesmartenergy.cnr.publish.queue";
    public final static String PUBLISH_SERVICE_KEY = "com.thesmartenergy.cnr.publish.service";
    public final static String SUBSCRIBE_ENDPOINT_KEY = "com.thesmartenergy.cnr.subscribe.endpoint";
    public final static String SUBSCRIBE_QUEUE_KEY = "com.thesmartenergy.cnr.subscribe.queue";
    public final static String SUBSCRIBE_SERVICE_KEY = "com.thesmartenergy.cnr.subscribe.service";

    private static File DIR;
    private static ExecutorService EXECUTOR;
    private static String PUBLISH_QUEUE;
    private static String SUBSCRIBE_QUEUE;
    private static ServiceBusContract PUBLISH_SERVICE;
    private static ServiceBusContract SUBSCRIBE_SERVICE;

    public static void readProperties(ServletContext context) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("conf.properties");
        Properties properties = new Properties();
        try {
            properties.load(input);
            for (String key : properties.stringPropertyNames()) {
                String prop = properties.getProperty(key);
                context.setAttribute(key, prop);
                Configuration config;
                switch (key) {
                    case DIRNAME:
                        DIR = new File(prop);
                        LOG.info("Writing files in: " + DIR.getAbsolutePath());
                        DIR.mkdir();
                        break;
                    case PUBLISH_QUEUE_KEY:
                        PUBLISH_QUEUE = prop;
                        break;
                    case SUBSCRIBE_QUEUE_KEY:
                        SUBSCRIBE_QUEUE = prop;
                        break;
                    case PUBLISH_ENDPOINT_KEY:
                        config = new Configuration();
                        ServiceBusConfiguration.configureWithConnectionString(null, config, prop);
                        PUBLISH_SERVICE = ServiceBusService.create(config);
                        break;
                    case SUBSCRIBE_ENDPOINT_KEY:
                        config = new Configuration();
                        ServiceBusConfiguration.configureWithConnectionString(null, config, prop);
                        SUBSCRIBE_SERVICE = ServiceBusService.create(config);
                        break;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("can't load properties");
        }
    }

    public static void startWorkers() {
        EXECUTOR = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 1; i++) {
            BeanManager manager = CDI.current().getBeanManager();
            Bean bean = manager.resolve(manager.getBeans(ChargingPlanSubscriber.class));
            if (bean != null) {
                CreationalContext creationalContext = manager.createCreationalContext(null);
                if (creationalContext != null) {
                    ChargingPlanSubscriber instance = (ChargingPlanSubscriber) bean.create(creationalContext);
                    EXECUTOR.execute(instance);
                }
            }
        }
    }

    public static void stopWorkers() {
        EXECUTOR.shutdownNow();
        try {
            EXECUTOR.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Produces
    private Logger createLogger(InjectionPoint injectionPoint) {
        return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getSimpleName());
    }

    @Produces
    private File createDir() {
        return DIR;
    }

    @Produces
    @Publish
    private String getPublishQueue() {
        return PUBLISH_QUEUE;
    }

    @Produces
    @Subscribe
    private String getSubscribeQueue() {
        return SUBSCRIBE_QUEUE;
    }

    @Produces
    @Publish
    private ServiceBusContract getPublishService() {
        return PUBLISH_SERVICE;
    }

    @Produces
    @Subscribe
    private ServiceBusContract getSubscribeService() {
        return SUBSCRIBE_SERVICE;
    }

}
