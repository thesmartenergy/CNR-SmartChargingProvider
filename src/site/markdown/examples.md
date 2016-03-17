# API use example

This API defines only two operations, as described [here](index.html). 

It is planed that the Server will to consume requests with any Content-Type, provided that the Client uses the [SPARQL-Generate](http://thesmartenergy.github.io/sparql-generate-jena/) Protocol. This Protocol gives the Server the opportunity to `interpret` the content as an [RDF Graph](https://www.w3.org/TR/rdf11-concepts/#section-rdf-graph). The RDF Graph interpretation of the content needs to be conformant with the Electric Vocabulary of the [Multidimensional Quantity Ontology](http://w3id.org/multidimensional-quantity/), and the SEAS Knowledge Model.

In this version, the Server consumes, and produces XML documents as exemplified below.


## Example of interaction 1

In the following HTTP request, the Client requests the CNR server to optimize the charge of an Electric Vehicle Service Equipment (EVSE) pool, with:

- the Electric Vehicle (EV) user will unplug the vehicle at 8 am the next morning;
- the estimated energy need of the EV is 20 kWh;
- the EV is plugged to EVSE #1 of the pool;
- the optimal state of charge should be reached before 8 am;
- the planned energy charge should be equal to the estimated energy need;
- the maximal charging power is 4 kWh;
- the minimal charging power is 3 kWh;
- the priority of charging this EV with respect to other EVs of the pool is 1;
- the maximal charging power of the EVSE pool is 10 kWh.

```
POST /scp/rest/ChargeOptimizationRequest HTTP/1.1
Host: cnr-seas.cloudapp.net
Content-type: application/xml

<GetChargingPlans xmlns="http://tempuri.org/">
    <value xmlns:d4p1="http://schemas.datacontract.org/2004/07/SIGE.WCF" xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
        <d4p1:ChargeNeeds>
            <d4p1:ChargeNeed>
                <d4p1:DateReprise>2016-03-04T08:00:00</d4p1:DateReprise>
                <d4p1:Energie>20</d4p1:Energie>
                <d4p1:IdCharge>1</d4p1:IdCharge>
                <d4p1:MargeDuree>0</d4p1:MargeDuree>
                <d4p1:MargeEnergie>0</d4p1:MargeEnergie>
                <d4p1:PMax>4</d4p1:PMax>
                <d4p1:PMin>3</d4p1:PMin>
                <d4p1:Priorite>0</d4p1:Priorite>
                <d4p1:TypeCharge>Planifiee</d4p1:TypeCharge>
            </d4p1:ChargeNeed>
        </d4p1:ChargeNeeds>
        <d4p1:OptimisationPmaxSite>10</d4p1:OptimisationPmaxSite>
    </value>
</GetChargingPlans>
```

Then the following Server response means that the server did not finish processing the charge optimization, but it promises the result will be available in 5000 ms, at a certain URI, containing identifier `qsdf13s5f4az3ef3584fezf`.

```
202 Accepted
Location: http://cnr-seas.cloudapp.net/scp/ChargingPlan/qsdf13s5f4az3ef3584fezf
Promise-Delay: 5000
```

Other HTTP responses may be returned by the server, among which:

- `20O OK`: the algorithm already ended, here is the response;
- `404 NOT FOUND`: either the Server is down, or the API endpoint you use is wrong. Check it is equal to `http://cnr-seas.cloudapp.net/scp/rest/ChargeOptimizationRequest`.
- `500 INTERNAL SERVER ERROR`: an error occurred. 

## Example of interaction 2

In the following HTTP request, the Client requests the CNR Server for the response of some algorithm execution with id `qsdf13s5f4az3ef3584fezf`, and it would prefer the response to be in XML:

```
REQ:
GET /scp/ChargingPlan/qsdf13s5f4az3ef3584fezf
Accept: application/xml
```

Then if the server answers with HTTP code:

- `202 Accepted Promise-Delay: 5000` the server did not finish processing the charge optimization, but it promises the result will be available in 5000 ms;
- `200 OK`: the response body represents the algorithm execution result. The following response describes that the optimal EV charge would be between 1 am and 5 am, with a peak of 6 kWh between 2 am and 3 am.

                
```
200 OK
Content-Type: application/xml 
```

```
 <ns2:GetChargingPlansResponse xmlns="http://schemas.datacontract.org/2004/07/SIGE.WCF" xmlns:ns2="http://tempuri.org/"
 xmlns:ns3="http://schemas.microsoft.com/2003/10/Serialization/>
    <ns2:GetChargingPlansResult>
        <ChargingPlan>
            <IdCharge>1</IdCharge>
            <Orders>
                <Order>
                    <DateDebut>2016-03-03T22:00:00</DateDebut>
                    <DateFin>2016-03-03T23:00:00</DateFin>
                    <Puissance>0</Puissance>
                </Order>
                <Order>
                    <DateDebut>2016-03-03T23:00:00</DateDebut>
                    <DateFin>2016-03-04T00:00:00</DateFin>
                    <Puissance>0</Puissance>
                </Order>
                <Order>
                    <DateDebut>2016-03-04T00:00:00</DateDebut>
                    <DateFin>2016-03-04T01:00:00</DateFin>
                    <Puissance>3</Puissance>
                </Order>
                <Order>
                    <DateDebut>2016-03-04T01:00:00</DateDebut>
                    <DateFin>2016-03-04T02:00:00</DateFin>
                    <Puissance>4</Puissance>
                </Order>
                <Order>
                    <DateDebut>2016-03-04T02:00:00</DateDebut>
                    <DateFin>2016-03-04T03:00:00</DateFin>
                    <Puissance>6</Puissance>
                </Order>
                <Order>
                    <DateDebut>2016-03-04T03:00:00</DateDebut>
                    <DateFin>2016-03-04T04:00:00</DateFin>
                    <Puissance>4</Puissance>
                </Order>
                <Order>
                    <DateDebut>2016-03-04T04:00:00</DateDebut>
                    <DateFin>2016-03-04T05:00:00</DateFin>
                    <Puissance>3</Puissance>
                </Order>
                <Order>
                    <DateDebut>2016-03-04T05:00:00</DateDebut>
                    <DateFin>2016-03-04T06:00:00</DateFin>
                    <Puissance>0</Puissance>
                </Order>
                <Order>
                    <DateDebut>2016-03-04T06:00:00</DateDebut>
                    <DateFin>2016-03-04T07:00:00</DateFin>
                    <Puissance>0</Puissance>
                </Order>
                <Order>
                    <DateDebut>2016-03-04T07:00:00</DateDebut>
                    <DateFin>2016-03-04T08:00:00</DateFin>
                    <Puissance>0</Puissance>
                </Order>
            </Orders>
        </ChargingPlan>
    </ns2:GetChargingPlansResult>
</ns2:GetChargingPlansResponse>
```


The XML document is satisfies object GetChargingPlansResponse in the [xsdl soap message description](http://bit.ly/1RZms6O).
