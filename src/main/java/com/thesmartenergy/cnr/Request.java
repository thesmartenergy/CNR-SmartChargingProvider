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

import com.thesmartenergy.cnr.skeleton.ArrayOfKeyValueOfintArrayOfOrderJORfzFnK;
import com.thesmartenergy.cnr.skeleton.OptimizationRequestSeas;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author maxime.lefrancois
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(namespace = "http://cnr-seas.cloudapp.net/scp/")
public class Request {

    @XmlAttribute(required = true)
    protected String id;
    
    @XmlAttribute(required = true)
    protected Date requestDate;
    
    @XmlElement(required = true)
    protected OptimizationRequestSeas optimizationRequestSeas;
    
    @XmlAttribute
    protected Date responseDate;
    
    @XmlElement(required = true)
    protected ArrayOfKeyValueOfintArrayOfOrderJORfzFnK chargingPlans;


    public Request(String id, Date requestDate, OptimizationRequestSeas optimizationRequestSeas) {
        this.id = id;
        this.requestDate = requestDate;
        this.optimizationRequestSeas = optimizationRequestSeas;
    }


    public Request(String id, Date requestDate, OptimizationRequestSeas optimizationRequestSeas, Date responseDate, ArrayOfKeyValueOfintArrayOfOrderJORfzFnK chargingPlans) {
        this.id = id;
        this.requestDate = requestDate;
        this.optimizationRequestSeas = optimizationRequestSeas;
        this.responseDate = responseDate;
        this.chargingPlans = chargingPlans;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + 17*this.id.hashCode();
    }
    
    

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Request other = (Request) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }
    

    public ArrayOfKeyValueOfintArrayOfOrderJORfzFnK getChargingPlans() {
        return chargingPlans;
    }

    public String getId() {
        return id;
    }

    public OptimizationRequestSeas getOptimizationRequestSeas() {
        return optimizationRequestSeas;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public Date getResponseDate() {
        return responseDate;
    }

    
}