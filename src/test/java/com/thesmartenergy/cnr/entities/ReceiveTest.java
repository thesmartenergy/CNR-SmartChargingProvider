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
//package com.thesmartenergy.cnr.entities;
//
//import com.thesmartenergy.cnr.skeleton.GetChargingPlansResponse;
//import java.io.File;
//import java.io.StringWriter;
//import java.net.URL;
//import java.util.Date;
//import java.util.logging.Logger;
//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.Marshaller;
//import javax.xml.bind.Unmarshaller;
//import javax.xml.soap.MessageFactory;
//import javax.xml.soap.SOAPMessage;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
///**
// *
// * @author maxime.lefrancois
// */
//public class ReceiveTest {
//    
//        public ReceiveTest() {
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
//    @Test
//    public void hello() throws Exception {
//        
//       
//        RequestController controller = new RequestController();
//        controller.log = Logger.getLogger(RequestController.class.getSimpleName());
//        controller.dir = new File("C:\\temp\\cnr");
//       
//        URL path = ReceiveTest.class.getResource("/example/chargingPlan.xml");
//        System.out.println(path.toString());
//
//        SOAPMessage message = MessageFactory.newInstance().createMessage(null,path.openStream());
//
//        JAXBContext jaxbContext = JAXBContext.newInstance(GetChargingPlansResponse.class);
//
//        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//        GetChargingPlansResponse obj = (GetChargingPlansResponse) unmarshaller.unmarshal(message.getSOAPBody().extractContentAsDocument());
//
//        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
//        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//        StringWriter xml = new StringWriter();
//        jaxbMarshaller.marshal(obj, xml);
//        
//        System.out.println(xml);
//        
//        Request req = controller.find("iqm1pbqfumb2qv3f03olbb0b0j");
//        req.setResponseDate(new Date());
//        req.setGetChargingPlansResponse(obj);
//        controller.edit(req);
//        
//    }
//}
