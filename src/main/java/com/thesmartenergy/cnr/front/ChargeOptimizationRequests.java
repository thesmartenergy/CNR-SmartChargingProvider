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
package com.thesmartenergy.cnr.front;

import com.microsoft.windowsazure.exception.ServiceException;
import com.thesmartenergy.cnr.Request;
import com.thesmartenergy.cnr.RequestController;
import com.thesmartenergy.cnr.scp.ChargeOptimizationRequestPublisher;
import com.thesmartenergy.cnr.skeleton.OptimizationRequestSeas;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;
import javax.xml.ws.RequestWrapper;
import org.apache.http.client.config.RequestConfig;

/**
 * resource "/ChargingRequest" processes POST that contain a charging request it
 * accepts XML or Turtle requests it processes the request and transfers it to
 * the CNR.
 *
 * in the meantime, it returns a HTTP 205 Promise code to the client, with the
 * location where the client will be able to retrieve the answer, and the delay
 * before the client will be able to retrieve the answer
 *
 * @author maxime.lefrancois
 */
@Path("/ChargeOptimizationRequest")
public class ChargeOptimizationRequests {

    private final SecureRandom random = new SecureRandom();

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @RequestWrapper(localName = "GetChargingPlans", targetNamespace = "http://cnr-seas.cloudapp.net/scp/", className = "com.thesmartenergy.cnr.scp.service.skeleton.GetChargingPlans")
    public Response postRequestXml(@WebParam(name = "value", targetNamespace = "http://cnr-seas.cloudapp.net/scp/") OptimizationRequestSeas value)  {

        try {
            // publish message to SCP
            String requestId = new BigInteger(130, random).toString(32);
            ChargeOptimizationRequestPublisher.get().publish(requestId, value);
            
            // create and persist request object
            Request request = new Request(requestId, new Date(), value);
            RequestController.get().create(request);

            return respondPromise(requestId, value);
        } catch (IOException ex) {
            Logger.getLogger(ChargeOptimizationRequests.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error, " + ex.getMessage()).build();
        } catch (JAXBException ex) {
            Logger.getLogger(ChargeOptimizationRequests.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error, " + ex.getMessage()).build();
        } catch (ServiceException ex) {
            Logger.getLogger(ChargeOptimizationRequests.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error, " + ex.getMessage()).build();
        } catch (Exception ex) {
            Logger.getLogger(ChargeOptimizationRequests.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error, " + ex.getMessage()).build();
        }
    }

    public Response respondPromise(String requestId, OptimizationRequestSeas value) throws URISyntaxException {
        Response response = Response.status(Status.ACCEPTED)
                .contentLocation(new URI("http://cnr-seas.cloudapp.net/scp/ChargingPlan/" + requestId))
                .header("X-Promise-Delay", 5000) // the delay (milliseconds) before the response can be accessed
                .build();
        return response;
    }

}
