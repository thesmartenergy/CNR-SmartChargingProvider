# CNR Smart Charging Provider

This is a first implementation of a SEAS Server Node.

This Server defines two interactions with SEAS Client Node:

1. Client requests a charge plan of a whole Electric Vehicle Service Equipment (EVSE) pool, given:
    - information from the Client:
        - the estimated time the user of each Electric Vehicle (EV) will want to unplug its vehicle;
        - the level of charge the user estimated its EV has;
        - the maximal charge power the EV accepts;
    - electricity price previsions based on secret CNR incredients:
        - the [EPEX Spot](https://www.epexspot.com), day ahead electric energy prices (FR, DE/AT, CH, ELIX);
        - the CNR Weather Forecast algorithms (that influences the price of the energy produced by the CNR).
1. Client requests for the result of the algorithm, and receive:
    - the Server commitment to fulfill the request, or
    - the algorithm execution result: the optimized plan of charge for every EV connected to an EVSE in the pool.

## Description of interaction 1

Node: **CNR-SmartChargingProvider**

| **interacts as:** | Server | 
| ------------- |:-------------:|
| **with:** | any Client | 
| **processes requests for some:** | algorithm execution | 
| **about/on resource:** | the ChargeOptimization algorithm | 
| **expected message content:** | The charge need. | 
| **preferred format:** | XML document satisfying object GetChargingPlans in xsd schema http://bit.ly/1RZms6O | 
| **alternative format:** | RDF (Turtle, or RDF/XML, or JSON-LD, ...)  document satisfying whatever ontology is to be used | 
| **then sends back response:** | The location where the result of the algorithm execution will be available, which is a IRI of the  form `<ChargingPlan/{id}>`, where id is some string like `qsdf13s5f4az3ef3584fezf` | 

## Description of interaction 2:

Node: **CNR-SmartChargingProvider**

| **interacts as:** | Server |
| ------------- |:-------------:|
| **with:** | any Client |
| **processes requests for some:** | information |
| **about/on resource:** | the `ChargingPlan/{id}`, with `id` is some string like `qsdf13s5f4az3ef3584fezf` | 
| **expected message content:** | void, as one just requests information. |
| **then sends back response:** | The optimized charge plan |
| **preferred format:** | XML document satisfying object GetChargingPlansResponse in xsd schema http://bit.ly/1RZms6O |
| **alternative format:** | RDF (Turtle, or RDF/XML, or JSON-LD, ...)  document satisfying whatever ontology is to be used |

