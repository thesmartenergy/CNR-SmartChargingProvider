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

import com.thesmartenergy.cnr.skeleton.GetChargingPlans;
import com.thesmartenergy.cnr.skeleton.GetChargingPlansResponse;
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
    protected GetChargingPlans getChargingPlans;
    
    @XmlAttribute
    protected Date responseDate;
    
    @XmlElement(required = true)
    protected GetChargingPlansResponse getChargingPlansResponse;


    public Request() {
    }

    public Request(String id, Date requestDate, GetChargingPlans getChargingPlans) {
        this.id = id;
        this.requestDate = requestDate;
        this.getChargingPlans = getChargingPlans;
    }


    public Request(String id, Date requestDate, GetChargingPlans getChargingPlans, Date responseDate, GetChargingPlansResponse getChargingPlansResponse) {
        this.id = id;
        this.requestDate = requestDate;
        this.getChargingPlans = getChargingPlans;
        this.responseDate = responseDate;
        this.getChargingPlansResponse = getChargingPlansResponse;
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
    

    public GetChargingPlansResponse getGetChargingPlansResponse() {
        return getChargingPlansResponse;
    }

    public String getId() {
        return id;
    }

    public GetChargingPlans getGetChargingPlans() {
        return getChargingPlans;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public Date getResponseDate() {
        return responseDate;
    }

    public void setGetChargingPlans(GetChargingPlans getChargingPlans) {
        this.getChargingPlans = getChargingPlans;
    }

    public void setGetChargingPlansResponse(GetChargingPlansResponse getChargingPlansResponse) {
        this.getChargingPlansResponse = getChargingPlansResponse;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public void setResponseDate(Date responseDate) {
        this.responseDate = responseDate;
    }
    
    

}