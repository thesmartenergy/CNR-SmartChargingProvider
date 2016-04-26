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
package com.thesmartenergy.cnr.query;

import com.thesmartenergy.cnr.entities.Queries;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author maxime.lefrancois
 */
@Path("")
public class QueryResource {

    static final Logger LOG = Logger.getLogger(QueryResource.class.getSimpleName());

    @Inject
    HttpServletRequest request;
    
    @Inject
    Queries queries;
    
    @GET
    @Produces("application/sparql-generate")
    @Path("{id}")
    public Response getAsTurtle(@PathParam("id") String id) {
        try {
            String requestedUri = queries.getBase() + id;
            System.out.println(requestedUri );
            if(!queries.getQueries().containsKey(requestedUri)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            File queryFile = queries.getQueries().get(requestedUri);
            Response.ResponseBuilder res = Response.ok(IOUtils.toString(new FileInputStream(queryFile)), "application/sparql-generate");
            res.header("Content-Disposition", "filename= " + FilenameUtils.removeExtension(id) + "." + "rqg;");
            return res.build();
        } catch (Exception ex) {
            Logger.getLogger(QueryResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
