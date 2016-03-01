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
import com.thesmartenergy.cnr.skeleton.OptimizationRequestSeas;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 *
 * @author maxime.lefrancois
 */
public class ChargeOptimizationRequestPublisher {

    static private ChargeOptimizationRequestPublisher instance;

    static public ChargeOptimizationRequestPublisher get() throws IOException {
        if (instance == null) {
            instance = new ChargeOptimizationRequestPublisher();
        }
        return instance;
    }
    
    private final String connectionUri, queueName;
    private final ServiceBusContract service;

    private ChargeOptimizationRequestPublisher() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("conf.properties");
        Properties properties = new Properties();
        properties.load(input);

        connectionUri = properties.getProperty("publishRequestConnection");
        queueName = properties.getProperty("publishRequestQueue");
        Configuration config = new Configuration();
        ServiceBusConfiguration.configureWithConnectionString(null, config, connectionUri);
        service = ServiceBusService.create(config);
    }

    public void publish(String id, OptimizationRequestSeas value) throws JAXBException, IOException, ServiceException {
        // do not show configuration in github !
        JAXBContext jaxbContext = JAXBContext.newInstance(OptimizationRequestSeas.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        StringWriter valueXMLString = new StringWriter();
        jaxbMarshaller.marshal(value, valueXMLString);
        BrokeredMessage message = new BrokeredMessage(valueXMLString.toString());
        message.setMessageId(id);
        service.sendQueueMessage(queueName, message);
    }

}
