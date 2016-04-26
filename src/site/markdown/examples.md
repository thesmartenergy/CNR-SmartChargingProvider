# API use example

This API defines only two operations, as described [here](index.html). 

It is planed that the Server will to consume requests with any Content-Type, provided that the Client uses the [SPARQL-Generate](http://w3id.org/sparql-generate/) Protocol. This Protocol gives the Server the opportunity to `interpret` the content as an [RDF Graph](https://www.w3.org/TR/rdf11-concepts/#section-rdf-graph). The RDF Graph interpretation of the content needs to be conformant with the Electric Vocabulary of the [Multidimensional Quantity Ontology](http://w3id.org/multidimensional-quantity/), and the SEAS Knowledge Model.

In this version, the Server consumes, and produces XML documents as exemplified below.


## Example of interaction 1

In the following HTTP request, the Client (EVSE pool owner) requests the CNR server to optimize the charge of an Electric Vehicle Service Equipment (EVSE) pool, with:

- the Electric Vehicle connected to EVSE 1 user will unplugged at 7 pm (vs. 8 pm for EVSE 2);
- the estimated energy need of EVSE 1 is 14 kWh (vs. EVSE 2 10 kWh);
- the optimal state of charge should be reached before 7 pm for EVSE 1 (vs. 8 pm for EVSE 2);
- the planned energy charge should be equal to the estimated energy need for both EVSE;
- the maximal charging power is 4 kWh for each EVSE;
- the minimal charging power is 3 kWh for each EVSE;
- no EVSE has the priority over the other;
- the maximal charging power of the EVSE pool is 7 kWh.

```
POST /scp/rest/ChargeOptimizationRequest HTTP/1.1
Host: cnr-seas.cloudapp.net
Content-type: application/xml

<GetChargingPlans xmlns="http://tempuri.org/" xmlns:s="http://schemas.datacontract.org/2004/07/SIGE.WCF">
    <value>
        <s:ChargeNeeds>
            <s:ChargeNeed>
                <s:DateReprise>2016-04-26T21:26:25.574Z</s:DateReprise>
                <s:Energie>14</s:Energie>
                <s:IdCharge>1</s:IdCharge>
                <s:MargeDuree>0.0</s:MargeDuree>
                <s:MargeEnergie>0.0</s:MargeEnergie>
                <s:PMax>4.0</s:PMax>
                <s:PMin>3.0</s:PMin>
                <s:Priorite>0</s:Priorite>
                <s:TypeCharge>Planifiee</s:TypeCharge>
            </s:ChargeNeed>
            <s:ChargeNeed>
                <s:DateReprise>2016-04-27T00:26:25.574Z</s:DateReprise>
                <s:Energie>10</s:Energie>
                <s:IdCharge>2</s:IdCharge>
                <s:MargeDuree>0.0</s:MargeDuree>
                <s:MargeEnergie>0.0</s:MargeEnergie>
                <s:PMax>4.0</s:PMax>
                <s:PMin>3.0</s:PMin>
                <s:Priorite>0</s:Priorite>
                <s:TypeCharge>Planifiee</s:TypeCharge>
            </s:ChargeNeed>
        </s:ChargeNeeds>
        <s:OptimisationPmaxSite>7.0</s:OptimisationPmaxSite>
    </value>
</GetChargingPlans>
```

Then the following Server response means that the server did not finish processing the charge optimization, but it promises the result will be available in 5000 ms, at a certain URI, containing identifier `1fc4ltccsrsun9mdijrgaruhe5`.

```
202 Accepted
Content-Location: http://cnr-seas.cloudapp.net/scp/rest/ChargingPlan/1fc4ltccsrsun9mdijrgaruhe5
Promise-Delay: 5000
```

Other HTTP responses may be returned by the server, among which:

- `20O OK`: the algorithm already ended, here is the response;
- `404 NOT FOUND`: either the Server is down, or the API endpoint you use is wrong. Check it is equal to `http://cnr-seas.cloudapp.net/scp/rest/ChargeOptimizationRequest`.
- `500 INTERNAL SERVER ERROR`: an error occurred. 

## Example of interaction 2

In the following HTTP request, the Client requests the CNR Server for the response of some algorithm execution with id `1fc4ltccsrsun9mdijrgaruhe5`, and it would prefer the response to be in XML:

```
GET /scp/rest/ChargingPlan/1fc4ltccsrsun9mdijrgaruhe5
Accept: application/xml
```

Then if the server answers with HTTP code:

- `202 Accepted Promise-Delay: 5000` the server did not finish processing the charge optimization, but it promises the result will be available in 5000 ms;
- `200 OK`: the response body represents the algorithm execution result. The response describes the optimal EV charge per EVSE, every hour. [See it live on the example described above](http://cnr-seas.cloudapp.net/scp/rest/ChargingPlan/1fc4ltccsrsun9mdijrgaruhe5).

The returned XML document satisfies object GetChargingPlansResponse in the [xsdl soap message description](http://bit.ly/1RZms6O).
