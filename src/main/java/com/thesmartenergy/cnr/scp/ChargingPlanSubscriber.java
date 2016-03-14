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

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMode;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveQueueMessageResult;
import com.thesmartenergy.cnr.CNRException;
import com.thesmartenergy.cnr.Subscribe;
import com.thesmartenergy.cnr.entities.Request;
import com.thesmartenergy.cnr.entities.RequestController;
import com.thesmartenergy.cnr.skeleton.GetChargingPlansResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

/**
 *
 * @author maxime.lefrancois
 */
public class ChargingPlanSubscriber implements Runnable {

    final static ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;

    static {
        opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
    }

    @Inject
    private Logger log;

    @Inject
    @Subscribe
    private ServiceBusContract service;

    @Inject
    @Subscribe
    private String queue;

    @Inject
    private RequestController controller;

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ReceiveQueueMessageResult result = service.receiveQueueMessage(queue, opts);
                BrokeredMessage message = result.getValue();

                // SCP response
                String id = message.getMessageId();
                log.info("received response " + id);
                InputStream body = message.getBody();
                
                String bodyAsString = IOUtils.toString(body);
                
                log.info("message: " + bodyAsString);

                SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, body);

                JAXBContext jaxbContext = JAXBContext.newInstance(GetChargingPlansResponse.class);

                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                Document document = soapMessage.getSOAPBody().extractContentAsDocument();

                // Use a Transformer for output
                TransformerFactory tFactory
                        = TransformerFactory.newInstance();
                Transformer transformer
                        = tFactory.newTransformer();

                DOMSource source = new DOMSource(document);
                StreamResult res = new StreamResult(System.out);
                transformer.transform(source, res);

                GetChargingPlansResponse response = (GetChargingPlansResponse) unmarshaller.unmarshal(document);

                // Delete message from queue
                service.deleteMessage(message);
                // Get original request
                Request request = controller.find(id);
                request.setResponseDate(new Date());
                request.setGetChargingPlansResponse(response);

                controller.edit(request);

            } catch (SOAPException | IOException | ServiceException | JAXBException | CNRException | NullPointerException ex) {
                log.warning("error while processing input " + ex);
                ex.printStackTrace();
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(ChargingPlanSubscriber.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(ChargingPlanSubscriber.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
