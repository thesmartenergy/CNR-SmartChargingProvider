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
package com.thesmartenergy.cnr.rs;

import com.thesmartenergy.cnr.CNRException;
import com.thesmartenergy.cnr.entities.Request;
import com.thesmartenergy.cnr.entities.RequestController;
import com.thesmartenergy.cnr.scp.ChargeOptimizationRequestPublisher;
import com.thesmartenergy.cnr.skeleton.GetChargingPlans;
import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * resource "/ChargeOptimizationRequest" processes POST that contain a charging request it
 * accepts XML or Turtle requests it processes the request and transfers it to
 * the CNR.
 *
 * in the meantime, it returns a HTTP 202 Accepted code to the client, with the
 * location where the client will be able to retrieve the answer, and the delay
 * before the client will be able to retrieve the answer
 *
 * @author maxime.lefrancois
 */
@Path("/ChargeOptimizationRequest")
public class ChargeOptimizationRequests {

    @Inject
    ChargeOptimizationRequestPublisher publisher;

    @Inject
    RequestController controller;
    
    @Inject
    Logger log;
    
    @Inject
    File dir;
    
    @HeaderParam("SPARQL-Generate-Uri")
    String sparqlGenerateUri;

    @HeaderParam("SPARQL-Generate-Variable")
    String sparqlGenerateVariable;

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response postRequestXml(GetChargingPlans value) {
        try {
            log.info("SPARQL-Generate-Uri: " + sparqlGenerateUri + " -- SPARQL-Generate-Variable: " + sparqlGenerateVariable);
            
            // publish message to SCP
            String requestId = new BigInteger(130, new SecureRandom()).toString(32);
            publisher.publish(requestId, value);

            // create and persist request object
            Request request = new Request(requestId, new Date(), value);
            controller.create(request);

            return respondPromise(requestId);
        } catch (CNRException | URISyntaxException ex) {
            log.log(Level.SEVERE, null, ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error, " + ex.getMessage()).build();
        }   
    }

    public Response respondPromise(String requestId) throws URISyntaxException {
        Response response = Response.status(Status.ACCEPTED)
                .contentLocation(new URI("http://cnr-seas.cloudapp.net/scp/rest/ChargingPlan/" + requestId))
                .header("Promise-Delay", 5000) // the delay (milliseconds) before the response can be accessed
                .build();
        return response;
    }

}
