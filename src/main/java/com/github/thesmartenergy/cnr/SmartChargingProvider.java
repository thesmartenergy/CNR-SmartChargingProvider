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

import com.github.thesmartenergy.cnr.skeleton.GetChargingPlans;
import com.github.thesmartenergy.pep.ContainerPath;
import com.github.thesmartenergy.pep.PEPException;
import com.github.thesmartenergy.pep.ProcessExecution;
import com.github.thesmartenergy.pep.ProcessExecutorDataset;
import com.github.thesmartenergy.rdfp.BaseURI;
import com.github.thesmartenergy.rdfp.DevelopmentBaseURI;
import com.github.thesmartenergy.rdfp.RDFPException;
import com.github.thesmartenergy.rdfp.ResourceDescription;
import com.github.thesmartenergy.rdfp.jersey.PresentationUtils;
import com.github.thesmartenergy.rdfp.preneg.handlers.STTLHandler;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXB;
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
import org.apache.jena.rdf.model.Model;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

/**
 *
 * @author maxime.lefrancois
 */
@ContainerPath("SmartChargingExecutionContainer")
public class SmartChargingProvider extends ProcessExecutorDataset {

    private static final Logger LOG = Logger.getLogger(SmartChargingProvider.class.getSimpleName());

    @Inject
    @PubSub(PubSub.Type.PUBLISH)
    String queue;

    @Inject
    @PubSub(PubSub.Type.PUBLISH)
    ServiceBusContract service;

    @Inject
    @DevelopmentBaseURI
    String DEV_BASE;

    @Inject
    @BaseURI
    String BASE;

    @Override
    public Future<Model> execute(Model input) throws PEPException {
        return null;
    }

    @Override
    public void create(ProcessExecution processExecution) throws PEPException {
        super.create(processExecution);
        String id = processExecution.getId();
        Model input = processExecution.getInput();
        try {
            // get XML document
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                ResourceDescription presentation = new ResourceDescription(DEV_BASE + "inputGraph");
                PresentationUtils presentationUtils = new PresentationUtils();
                STTLHandler lowerer = new STTLHandler(BASE, presentationUtils);
                lowerer.lower(input, out, MediaType.APPLICATION_XML_TYPE, presentation);
            } catch (RDFPException ex) {
                throw new PEPException("Error while lowering input", ex);
            }
            String xml = new String(out.toByteArray(), "UTF-8");
            GetChargingPlans getChargingPlans = JAXB.unmarshal(xml, GetChargingPlans.class);
            
            LOG.info("sending message: " + xml);

            // embed value in a SOAP envelope
            SOAPMessage soapMessage = createSoapMessage(getChargingPlans);
            out = new ByteArrayOutputStream();
            soapMessage.writeTo(out);
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            String string = IOUtils.toString(in, Charset.forName("UTF-8"));
            LOG.info("sending message: " + string);
            BrokeredMessage message = new BrokeredMessage(string);
            message.setMessageId(id);
            service.sendQueueMessage(queue, message);
        } catch (ServiceException | SOAPException | IOException ex) {
            throw new PEPException(ex);
        }
    }

    private SOAPMessage createSoapMessage(GetChargingPlans value) throws PEPException {
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
            throw new PEPException(ex);
        }
    }
}
