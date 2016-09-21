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

import com.github.thesmartenergy.pep.ContainerPath;
import com.github.thesmartenergy.pep.PEPException;
import com.github.thesmartenergy.pep.ProcessExecution;
import com.github.thesmartenergy.pep.impl.ProcessExecutionImpl;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMode;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveQueueMessageResult;
import com.github.thesmartenergy.rdfp.BaseURI;
import com.github.thesmartenergy.rdfp.DevelopmentBaseURI;
import com.github.thesmartenergy.rdfp.RDFPException;
import com.github.thesmartenergy.rdfp.ResourceDescription;
import com.github.thesmartenergy.rdfp.jersey.PresentationUtils;
import com.github.thesmartenergy.rdfp.preneg.handlers.SPARQLGenerateHandler;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;

/**
 *
 * @author maxime.lefrancois
 */
public class ChargingPlanSubscriber implements Runnable {

    private static final Logger LOG = Logger.getLogger(ChargingPlanSubscriber.class.getSimpleName());

    final static ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;

    static {
        opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
    }

    @Inject
    @BaseURI
    String BASE;

    @Inject
    @DevelopmentBaseURI
    String DEV_BASE;

    @Inject
    @PubSub(PubSub.Type.SUBSCRIBE)
    private ServiceBusContract service;

    @Inject
    @PubSub(PubSub.Type.SUBSCRIBE)
    private String queue;

    @Inject
    @ContainerPath("SmartChargingExecutionContainer")
    SmartChargingProvider smartChargingProvider;

    @Override
    public void run() {
        LOG.info("Starting worker.");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ReceiveQueueMessageResult result = service.receiveQueueMessage(queue, opts);
                BrokeredMessage message = result.getValue();
                if (message == null) {
                    continue;
                }
                
                System.out.println(message.getContentType());
                System.out.println(message.getCorrelationId());
                System.out.println(message.getDate());
                System.out.println(message.getDeliveryCount());
                System.out.println(message.getLabel());
                System.out.println(message.getLockLocation());
                System.out.println(message.getLockToken());
                System.out.println(message.getLockedUntilUtc());
                System.out.println(message.getMessageId());
                System.out.println(message.getMessageLocation());
                System.out.println(message.getPartitionKey());
                System.out.println(message.getProperties());
                System.out.println(message.getReplyTo());
                System.out.println(message.getSessionId());
                
                
                
                // SCP response
                String id = message.getMessageId();
                if (id == null) {
                    LOG.log(Level.WARNING, "received message with null id. Try to delete:");
                    try {
                        service.deleteMessage(message);
                    } catch (Exception ex) {
                        LOG.warning("error while trying to delete message: " + ex.getMessage());
                        ex.printStackTrace();
                        LOG.warning("this was the content:");
                        System.out.println(IOUtils.toString(message.getBody(), "UTF-8"));
                    }
                    continue;
                } else {
                    LOG.info("received response " + id);
                }

                byte[] body = IOUtils.toByteArray(message.getBody());
                String bodyAsString = IOUtils.toString(body, "UTF-8");
                LOG.info(bodyAsString);

                // strangely, string starts with "@strin3http://schemas.microsoft.com/2003/10/Serialization/?f<s:Envelope ..." on this side, 
                // although it starts with  "<?xml version="1.0" encoding="UTF-8"?> <s:Envelope ..." on the other side
                // so ... ugly hack
                bodyAsString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + bodyAsString.substring(64, bodyAsString.length() - 1);

                ResourceDescription presentation = new ResourceDescription(DEV_BASE + "outputGraph");
                Model output;
                try {
                    PresentationUtils presentationUtils = new PresentationUtils();
                    SPARQLGenerateHandler lifter = new SPARQLGenerateHandler(BASE, presentationUtils);
                    output = lifter.lift(MediaType.APPLICATION_XML_TYPE, presentation, IOUtils.toInputStream(bodyAsString, "UTF-8"));
                } catch (RDFPException ex) {
                    throw new PEPException("error while lifting output", ex);
                }

                ProcessExecution processExecution = smartChargingProvider.find(id);
                processExecution = new ProcessExecutionImpl(BASE, processExecution.getContainerPath(), processExecution.getId(), processExecution.getInput(), CompletableFuture.completedFuture(output));
                smartChargingProvider.update(processExecution);

                // Delete message from queue
                service.deleteMessage(message);

            } catch (ServiceException | IOException | PEPException ex) {
                LOG.log(Level.WARNING, "error while processing input ", ex);
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        LOG.info("Stopping worker.");
    }
}
