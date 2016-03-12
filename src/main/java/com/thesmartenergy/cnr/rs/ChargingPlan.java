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
import com.thesmartenergy.cnr.skeleton.GetChargingPlansResponse;
import java.io.StringWriter;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXB;

/**
 *
 * @author maxime.lefrancois
 */
@Path("/ChargingPlan/{requestId}")
public class ChargingPlan {

    @Inject
    RequestController controller;

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response postRequestXml(@PathParam("requestId") String requestId) {
        Request request;
        try {
            // find Request object
            request = controller.find(requestId);
        } catch (CNRException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error, " + ex.getMessage()).build();
        }

        // check HTTP promise state
        if (request == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Request " + requestId + " cannot be found on the server").build();
        }
        if (request.getGetChargingPlansResponse() == null) {
            Response response = Response.status(Response.Status.ACCEPTED)
                    .header("Promise-Delay", 5000) // the delay (milliseconds) before the response can be accessed
                    .entity("Result of request " + requestId + " is not available yet.")
                    .build();
            return response;
        }

        // return ChargingPlans in XML
        GetChargingPlansResponse chargingPlans = request.getGetChargingPlansResponse();
        StringWriter xml = new StringWriter();
        JAXB.marshal(chargingPlans, xml);

        return Response.ok(xml.toString()).build();
    }

}
