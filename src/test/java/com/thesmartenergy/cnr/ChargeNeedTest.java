///*
// * Copyright 2016 ITEA 12004 SEAS Project.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.thesmartenergy.cnr;
//
//import com.thesmartenergy.cnr.skeleton.GetChargingPlans;
//import java.io.ByteArrayOutputStream;
//import java.io.StringWriter;
//import java.net.URL;
//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.JAXBException;
//import javax.xml.bind.Marshaller;
//import javax.xml.bind.Unmarshaller;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.soap.MessageFactory;
//import javax.xml.soap.Name;
//import javax.xml.soap.SOAPEnvelope;
//import javax.xml.soap.SOAPException;
//import javax.xml.soap.SOAPHeaderElement;
//import javax.xml.soap.SOAPMessage;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.w3c.dom.DOMException;
//import org.w3c.dom.Document;
//
///**
// *
// * @author maxime.lefrancois
// */
//public class ChargeNeedTest {
//
//    public ChargeNeedTest() {
//    }
//
//    @BeforeClass
//    public static void setUpClass() {
//    }
//
//    @AfterClass
//    public static void tearDownClass() {
//    }
//
//    @Before
//    public void setUp() {
//    }
//
//    @After
//    public void tearDown() {
//    }
//
////    @Test
//    public void hello() throws Exception {
//        URL path = ChargeNeedTest.class.getResource("/example/chargeNeed.xml");
//        System.out.println(path.toString());
//
//        SOAPMessage message = MessageFactory.newInstance().createMessage(null, path.openStream());
//
//        JAXBContext jaxbContext = JAXBContext.newInstance(GetChargingPlans.class);
//
//        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//        GetChargingPlans obj = (GetChargingPlans) unmarshaller.unmarshal(message.getSOAPBody().extractContentAsDocument());
//
//        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
//        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//        StringWriter xml = new StringWriter();
//        jaxbMarshaller.marshal(obj, xml);
//
//        System.out.println(xml);
//    }
//
//    @Test
//    public void createSoapMessage() throws Exception {
//        URL path = ChargeNeedTest.class.getResource("/example/chargeNeed.xml");
//        System.out.println(path.toString());
//        
//        SOAPMessage message = MessageFactory.newInstance().createMessage(null, path.openStream());
//        JAXBContext jaxbContext = JAXBContext.newInstance(GetChargingPlans.class);
//        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//        GetChargingPlans obj = (GetChargingPlans) unmarshaller.unmarshal(message.getSOAPBody().extractContentAsDocument());
//
//        // marshal in a document
//        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
//        jaxbContext.createMarshaller().marshal(obj, document);
//
//        // create envelope
//        SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
//        soapMessage.getSOAPBody().addDocument(document);
//        Name headername = soapMessage.getSOAPPart().getEnvelope().createName("Action", "", "http://schemas.microsoft.com/ws/2005/05/addressing/none/");
//        SOAPHeaderElement soapAction = soapMessage.getSOAPHeader().addHeaderElement(headername);
//        soapAction.setMustUnderstand(true);
//        soapAction.setTextContent("http://cnr-seas.cloudapp.net/scp/ISmartCharging/GetChargingPlans");
//
//        soapMessage.writeTo(System.out);
//
//    }
//    
//    
//}
