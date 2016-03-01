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

import com.thesmartenergy.cnr.Request;
import com.thesmartenergy.cnr.RequestController;
import com.thesmartenergy.cnr.skeleton.ArrayOfKeyValueOfintArrayOfOrderJORfzFnK;
import com.thesmartenergy.cnr.skeleton.GetChargingPlansResponse;
import com.thesmartenergy.cnr.skeleton.OptimizationRequestSeas;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import javax.jws.WebParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXB;
import javax.xml.ws.ResponseWrapper;

/**
 *
 * @author maxime.lefrancois
 */
@Path("/ChargingPlan/{requestId}")
public class ChargingPlan {

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @ResponseWrapper(localName = "GetChargingPlansResponse", targetNamespace = "http://cnr-seas.cloudapp.net/scp/", className = "com.thesmartenergy.cnr.scp.service.skeleton.GetChargingPlansResponse")
    public Response postRequestXml(@WebParam(name = "requestId") String requestId) throws IOException {

        // find Request object
        RequestController controller = RequestController.get();
        Request request = controller.find(requestId);

        // check HTTP promise state
        if (request == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Request " + requestId + " cannot be found on the server").build();
        }
        if (request.getChargingPlans() == null) {
            Response response = Response.status(Response.Status.ACCEPTED)
                    .header("X-Promise-Delay", 20000) // the delay (milliseconds) before the response can be accessed
                    .entity("Result of request " + requestId + " is not available yet.")
                    .build();
            return response;
        }
        
        // return ChargingPlans in XML
        ArrayOfKeyValueOfintArrayOfOrderJORfzFnK chargingPlans = request.getChargingPlans();
        StringWriter xml = new StringWriter();
        JAXB.marshal(chargingPlans, xml);
        
        return Response.ok(xml.toString()).build();
    }

}
