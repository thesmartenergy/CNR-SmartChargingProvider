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
package com.thesmartenergy.cnr.scp;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMode;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveQueueMessageResult;
import com.thesmartenergy.cnr.Request;
import com.thesmartenergy.cnr.RequestController;
import com.thesmartenergy.cnr.skeleton.ArrayOfKeyValueOfintArrayOfOrderJORfzFnK;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;

/**
 *
 * @author maxime.lefrancois
 */
@WebListener
public class ChargingPlanSubscriber implements ServletContextListener {

    private final String connectionUri, queueName;
    private final ServiceBusContract service;
    private ExecutorService executor;

    private ChargingPlanSubscriber() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("conf.properties");
        Properties properties = new Properties();
        properties.load(input);

        connectionUri = properties.getProperty("subscribePlanConnection");
        queueName = properties.getProperty("subscribePlanQueue");
        Configuration config = new Configuration();
        ServiceBusConfiguration.configureWithConnectionString(null, config, connectionUri);
        service = ServiceBusService.create(config);
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        ServletContext context = arg0.getServletContext();
        executor = Executors.newFixedThreadPool(10);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
                        opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
                        ReceiveQueueMessageResult result = service.receiveQueueMessage(queueName, opts);
                        executor.execute(new Worker(result));
                    } catch (ServiceException ex) {
                        Logger.getLogger(ChargingPlanSubscriber.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        ServletContext context = arg0.getServletContext();
        executor.shutdownNow();
    }

    private class Worker implements Runnable {

        private final ReceiveQueueMessageResult result;

        public Worker(ReceiveQueueMessageResult result) {
            this.result = result;
        }

        @Override
        public void run() {
            try {
                BrokeredMessage message = result.getValue();
                if (message != null && message.getMessageId() != null) {

                    // SCP response 
                    String id = message.getMessageId();
                    InputStream body = message.getBody();
                    ArrayOfKeyValueOfintArrayOfOrderJORfzFnK response = JAXB.unmarshal(body, ArrayOfKeyValueOfintArrayOfOrderJORfzFnK.class);

                    // Delete message from queue
                    service.deleteMessage(message);

                    // Get original request
                    RequestController controller = RequestController.get();
                    Request request = controller.find(id);

                    // Edit request object
                    Request newRequest = new Request(request.getId(), request.getRequestDate(), request.getOptimizationRequestSeas(), new Date(), response);
                    controller.edit(newRequest);

                } else {
                    System.out.println("Did not receive messages.");
                }
            } catch (ServiceException ex) {
                Logger.getLogger(ChargingPlanSubscriber.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JAXBException ex) {
                Logger.getLogger(ChargingPlanSubscriber.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ChargingPlanSubscriber.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(ChargingPlanSubscriber.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
