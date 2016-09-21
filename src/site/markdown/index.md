# CNR Smart Charging Provider

This is a pilot implementation for the SEAS project.
It enables a client to request for Electric Vehicle charging optimizations.

This pilot implementation is a [Core Entity](https://w3id.org/seas/ProcessExecutor).

It uses the [SPARQL-Generate Protocol](https://w3id.org/sparql-generate/protocol-api.html), and the 


## Overview

The CNR Smart Charging Provider is a [Process Executor](https://w3id.org/seas/ProcessExecutor), that implements a Smart Charging Optimization [Process](https://w3id.org/seas/Process) and generates [Process Executions](https://w3id.org/seas/ProcessExecution).

Following a resource-centric approache, Process Executions are contained in a [Process Execution Container](https://w3id.org/seas/ProcessExecutionContainer), whose location is [http://cnr-seas.cloudapp.net/scp/ProcessExecution](http://cnr-seas.cloudapp.net/scp/ProcessExecution).

One may request for an optimized charge plan by posting the charging needs for an Electric Vehicle Charging Station pool at the Process Execution Container. The HTTP request payload contains among other:
    - the estimated time the user of each Electric Vehicle (EV) will want to unplug its vehicle;
    - the level of charge the user estimated its EV has;
    - the maximal charge power the EV accepts.

Then if everything goes well and the request is accepted, the server will respond with HTTP code 201 CREATED, indicating the location of the Process Execution. For instance, [http://cnr-seas.cloudapp.net/scp/ProcessExecution/1fc4ltccsrsun9mdijrgaruhe5](http://cnr-seas.cloudapp.net/scp/ProcessExecution/1fc4ltccsrsun9mdijrgaruhe5). The optimized charge plan is computed using:
    - the charge needs;
    - the [EPEX Spot](https://www.epexspot.com), day ahead electric energy prices (FR, DE/AT, CH, ELIX);
    - the CNR Weather Forecast algorithms (that influences the price of the energy produced by the CNR).

One may then operate a HTTP GET at some Process Execution, and retrieve a description of the Process Execution: retrieve the input, and potentially the output: the location of a document that contains the optimized plan of charge for every Electric Vehicle. For instance, 
[http://cnr-seas.cloudapp.net/scp/ProcessExecution/1fc4ltccsrsun9mdijrgaruhe5/output](http://cnr-seas.cloudapp.net/scp/ProcessExecution/1fc4ltccsrsun9mdijrgaruhe5/output)


## Description of the input

The input of the Smart Charging Optimization [Process](https://w3id.org/seas/Process) is a representation of a RDF Graph such that:

- The RDF Graph must simply entail [this graph](http://cnr-seas.cloudapp.net/scp/input);
- The representation may be of any RDF syntax (Turtle, RDF/XML, JSON-LD), or be a XML document and use the SPARQL-Genrate protocol with Lifting rule [http://cnr-seas.cloudapp.net/scp/liftingRule](http://cnr-seas.cloudapp.net/scp/liftingRule)

## Description of the output

The output of the Smart Charging Optimization [Process](https://w3id.org/seas/Process) is a representation of a RDF Graph such that:

- The RDF Graph must simply entail [this graph](http://cnr-seas.cloudapp.net/scp/output);
- The representation may be of any RDF syntax (Turtle, RDF/XML, JSON-LD), or be a XML document and use the SPARQL-Genrate protocol with Lowering rule [http://cnr-seas.cloudapp.net/scp/loweringRule](http://cnr-seas.cloudapp.net/scp/loweringRule)


# Cite this work
 
Maxime Lefrançois, Guillaume Habault, Caroline Ramondou and Eric Françon, 2016, [**Outsourcing Electric Vehicle Smart Charging on the Web of Data**](16-LefrancoisHabaultRamondouFrancon-GREEN-Outsourcing.pdf), [*The First International Conference on Green Communications, Computing and Technologies*](http://www.iaria.org/conferences2016/GREEN16.html), (GREEN'16), IARIA XPS ISBN 978-1-61208-524-1, July 24 - 28, 2016, Nice, France

See also our presentation at the ITEA 2016 Event, colocated with the EUREKA Innovation Week 2016 (see [the presentation](16-LefrancoisHabaultRamondouFrancon-ITEAEvent-Outsourcing.pptx)) 
