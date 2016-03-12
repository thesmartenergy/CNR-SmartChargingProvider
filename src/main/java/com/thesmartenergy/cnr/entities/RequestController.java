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
package com.thesmartenergy.cnr.entities;

import com.thesmartenergy.cnr.CNRException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXB;

/**
 *
 * @author maxime.lefrancois
 */
@RequestScoped
public class RequestController {

    @Inject
    Logger log;
    
    @Inject
    File dir;

    @PostConstruct
    public void postConstruct() {
        log.fine(dir.list().toString());
    }

    public void create(Request request) throws CNRException {
        try {
            File file = new File(dir, request.getId() + ".xml");
            try (OutputStream out = new FileOutputStream(file)) {
                JAXB.marshal(request, out);
            }
        } catch (IOException ex) {
            throw new CNRException(ex);
        }
    }

    public void edit(Request request) throws CNRException {
        try {
            File file = new File(dir, request.getId() + ".xml");
            try (OutputStream out = new FileOutputStream(file, false)) {
                JAXB.marshal(request, out);
            }
        } catch (IOException ex) {
            throw new CNRException(ex);
        }
    }

    public Request find(String id) throws CNRException {
        try {
            File file = new File(dir, id + ".xml");
            InputStream in = new FileInputStream(file);
            Request request = JAXB.unmarshal(in, Request.class);
            return request;
        } catch (IOException ex) {
            throw new CNRException(ex);
        }
    }

    public void destroy(String id) throws CNRException {
        try {
            File file = new File(dir, id + ".xml");
            file.delete();
        } catch (Exception ex) {
            throw new CNRException(ex);
        }
    }

    public int getChargeOptimizationRequestCount() throws CNRException {
        try {
            return dir.list().length;
        } catch (Exception ex) {
            throw new CNRException(ex);
        }
    }

}
