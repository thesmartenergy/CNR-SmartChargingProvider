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
import com.thesmartenergy.cnr.CNRException;
import com.thesmartenergy.cnr.Publish;
import com.thesmartenergy.cnr.skeleton.GetChargingPlans;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

/**
 *
 * @author maxime.lefrancois
 */
@Dependent
public class ChargeOptimizationRequestPublisher {

    @Inject
    Logger log;

    @Inject
    @Publish
    String queue;

    @Inject
    @Publish
    ServiceBusContract service;

    public void publish(String id, GetChargingPlans value) throws CNRException {
        try {
            // embed value in a SOAP envelope
            SOAPMessage soapMessage = createSoapMessage(value);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            soapMessage.writeTo(out);
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            String string = IOUtils.toString(in,Charset.forName("UTF-8"));
            log.info("sending message: "+string);
            BrokeredMessage message = new BrokeredMessage(string);
            message.setMessageId(id);
            service.sendQueueMessage(queue, message);
        } catch (CNRException | ServiceException | SOAPException | IOException ex) {
            throw new CNRException(ex);
        }
    }

    private SOAPMessage createSoapMessage(GetChargingPlans value) throws CNRException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(GetChargingPlans.class);
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            jaxbContext.createMarshaller().marshal(value, document);

            SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
            soapMessage.getSOAPBody().addDocument(document);
            Name headername = soapMessage.getSOAPPart().getEnvelope().createName("Action", "", "http://schemas.microsoft.com/ws/2005/05/addressing/none/");
            SOAPHeaderElement soapAction = soapMessage.getSOAPHeader().addHeaderElement(headername);
            soapAction.setMustUnderstand(true);
            soapAction.setTextContent("http://tempuri.org/ISmartCharging/GetChargingPlans");
            return soapMessage;
        } catch (DOMException | JAXBException | ParserConfigurationException | SOAPException ex) {
            throw new CNRException(ex);
        }
    }

}
