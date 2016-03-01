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
package com.thesmartenergy.cnr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.xml.bind.JAXB;

/**
 *
 * @author maxime.lefrancois
 */
@WebListener
public class RequestController implements ServletContextListener {

    private static RequestController controller;

    public static RequestController get() throws IOException {
        if (controller == null) {
            controller = new RequestController();
        }
        return controller;
    }

    private final File dir;

    private RequestController() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("conf.properties");
        Properties properties = new Properties();
        properties.load(input);
        dir = new File(properties.getProperty("dirName"));
        // must take care of exceptions
        dir.mkdir();
    }

    public void create(Request request) throws Exception {
        File file = new File(dir, request.getId() + ".xml");
        OutputStream out = new FileOutputStream(file);
        JAXB.marshal(request, out);
        out.close();
    }

    public void edit(Request request) throws Exception {
        File file = new File(dir, request.getId() + ".xml");
        OutputStream out = new FileOutputStream(file, false);
        JAXB.marshal(request, out);
        out.close();
    }

    public Request find(String id) {
        InputStream in = null;
        try {
            File file = new File(dir, id + ".xml");
            in = new FileInputStream(file);
            Request request = JAXB.unmarshal(in, Request.class);
            in.close();
            return request;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RequestController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RequestController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
                return null;
            } catch (IOException ex) {
                Logger.getLogger(RequestController.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
    }

    public void destroy(String id) throws Exception {
        File file = new File(dir, id + ".xml");
        file.delete();
    }

    public int getChargeOptimizationRequestCount() {
        return dir.list().length;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (controller == null) {
            controller = this;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
