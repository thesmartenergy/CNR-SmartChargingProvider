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
package com.github.thesmartenergy.cnr;

import com.github.thesmartenergy.rdfp.BaseURI;
import com.github.thesmartenergy.rdfp.DevelopmentBaseURI;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
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
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;

/**
 *
 * @author maxime.lefrancois
 */
public class App {

    private static final Logger LOG = Logger.getLogger(App.class.getSimpleName());

    static final boolean DEV = true;

    @Produces
    @BaseURI
    private static final String BASE = "http://cnr-seas.cloudapp.net/scp/";

    @Produces
    @DevelopmentBaseURI
    static final String DEV_BASE = DEV ? "http://localhost:8081/scp/" : BASE;

    @Produces
    private static Dataset DATASET;

    @Produces
    @PubSub(PubSub.Type.PUBLISH)
    private static String PUBLISH_QUEUE;

    @Produces
    @PubSub(PubSub.Type.SUBSCRIBE)
    private static String SUBSCRIBE_QUEUE;

    @Produces
    @PubSub(PubSub.Type.PUBLISH)
    private static ServiceBusContract PUBLISH_SERVICE;

    @Produces
    @PubSub(PubSub.Type.SUBSCRIBE)
    private static ServiceBusContract SUBSCRIBE_SERVICE;

    @WebListener
    public static class AppListener implements ServletContextListener {

        private final static String TRIPLESTORE_KEY = "com.thesmartenergy.cnr.triplestore";
        private final static String PUBLISH_ENDPOINT_KEY = "com.thesmartenergy.cnr.publish.endpoint";
        private final static String PUBLISH_QUEUE_KEY = "com.thesmartenergy.cnr.publish.queue";
        private final static String PUBLISH_SERVICE_KEY = "com.thesmartenergy.cnr.publish.service";
        private final static String SUBSCRIBE_ENDPOINT_KEY = "com.thesmartenergy.cnr.subscribe.endpoint";
        private final static String SUBSCRIBE_QUEUE_KEY = "com.thesmartenergy.cnr.subscribe.queue";
        private final static String SUBSCRIBE_SERVICE_KEY = "com.thesmartenergy.cnr.subscribe.service";

        private static ExecutorService EXECUTOR;

        @Override
        public void contextInitialized(ServletContextEvent sce) {
            ServletContext context = sce.getServletContext();
            readProperties(context);
            startWorkers();
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce) {
            stopWorkers();
        }

        private void readProperties(ServletContext context) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try (InputStream input = classLoader.getResourceAsStream("conf.properties")) {
                Properties properties = new Properties();
                properties.load(input);
                for (String key : properties.stringPropertyNames()) {
                    String prop = properties.getProperty(key);
                    context.setAttribute(key, prop);
                    Configuration config;
                    switch (key) {
                        case TRIPLESTORE_KEY:
                            (new File(prop)).mkdir();
                            DATASET = TDBFactory.createDataset(prop);
                            LOG.info("Graph Store is at: " + prop);
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
                throw new RuntimeException("can't load properties:", ex);
            }
        }

        private void startWorkers() {
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

        private void stopWorkers() {
            EXECUTOR.shutdownNow();
            try {
                EXECUTOR.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

}
