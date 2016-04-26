


function resetForm1() {    
    var date1 = new Date();
    date1.setHours(date1.getHours() + 6);
    var date2 = new Date();
    date2.setHours(date2.getHours() + 9);
    document.getElementById("message").value = "<GetChargingPlans xmlns=\"http://tempuri.org/\" xmlns:s=\"http://schemas.datacontract.org/2004/07/SIGE.WCF\">\n    <value>\n        <s:ChargeNeeds>\n            <s:ChargeNeed>\n                <s:DateReprise>"+ date1.toISOString() +"</s:DateReprise>\n                <s:Energie>14</s:Energie>\n                <s:IdCharge>1</s:IdCharge>\n                <s:MargeDuree>0.0</s:MargeDuree>\n                <s:MargeEnergie>0.0</s:MargeEnergie>\n                <s:PMax>4.0</s:PMax>\n                <s:PMin>3.0</s:PMin>\n                <s:Priorite>0</s:Priorite>\n                <s:TypeCharge>Planifiee</s:TypeCharge>\n            </s:ChargeNeed>\n            <s:ChargeNeed>\n                <s:DateReprise>"+ date2.toISOString() +"</s:DateReprise>\n                <s:Energie>10</s:Energie>\n                <s:IdCharge>2</s:IdCharge>\n                <s:MargeDuree>0.0</s:MargeDuree>\n                <s:MargeEnergie>0.0</s:MargeEnergie>\n                <s:PMax>4.0</s:PMax>\n                <s:PMin>3.0</s:PMin>\n                <s:Priorite>0</s:Priorite>\n                <s:TypeCharge>Planifiee</s:TypeCharge>\n            </s:ChargeNeed>\n        </s:ChargeNeeds>\n        <s:OptimisationPmaxSite>7.0</s:OptimisationPmaxSite>\n    </value>\n</GetChargingPlans>";
}

function requestExecution() {
    var req = new XMLHttpRequest();
    req.open('POST', 'rest/ChargeOptimizationRequest', true); 
    req.setRequestHeader("Content-Type","application/xml");
    req.onreadystatechange = function (aEvt) {
        if (req.readyState === 4) {
            if(req.status === 202) {
                console.log(req);
                console.log(req.getAllResponseHeaders());
                console.log(req.getResponseHeader("Content-Location"));
                id = req.getResponseHeader("Content-Location").substring(51);
                document.getElementById("msg").innerHTML = "The algorithm execution response will be available in a couple of seconds with identifier <strong>"+id+"</strong>.<br> Remember to note this identifier.";
                document.getElementById("rqid").value = id;
            } else {
                document.getElementById("msg").innerHTML = "Error while processing the request, got status " + req.statusText;
            }
        }
    };
    req.send(document.getElementById("message").value);
}

function resetForm2() {    
    document.getElementById("rqid").value = "1fc4ltccsrsun9mdijrgaruhe5";
}


function get() {
    id = document.getElementById("rqid").value;
    accept = document.getElementById("accept").value;
    var req = new XMLHttpRequest();
    req.open('GET', 'rest/ChargingPlan/' + id, true); 
    req.setRequestHeader("Content-Type", accept );
    req.onreadystatechange = function (aEvt) {
        if (req.readyState === 4) {
            if(req.status === 200) {
                console.log(req);
                if(accept === "application/xml") {
                    document.getElementById("msg2").innerHTML = "You can also request a \n\
<a href=\"http://w3id.org/sparql-generate/protocol.html\">SPARQL-Generate Client</a> to interpret this response as RDF for you. \n\
<a href=\"http://w3id.org/sparql-generate/api/fetch?uri=" + encodeURIComponent("http://cnr-seas.cloudapp.net/scp/ChargingPlan/" + id)
                            + "&accept=" + encodeURIComponent("text/turtle") + "&useaccept=" +encodeURIComponent("application/xml") +"\">Click here for a demo</a>.";
                }
                document.getElementById("pre").textContent = req.response;
            } else if(req.status === 202) {
                document.getElementById("msg2").innerHTML = "The algorithm execution result is not yet available. Please retry in a moment";
            } else if(req.status === 404) {
                document.getElementById("msg2").innerHTML = "The algorithm execution identifier "+ id +" is unknown.";
            } else if(req.status === 500) {
                document.getElementById("msg2").innerHTML = "A server exception occured. Please <a href=\"http://github.com/thesmartenergy/CNR-SmartChargingProvider/issues\">let us known about this issue</a>";
            }
        }
    };
    req.send(document.getElementById("message").value);  
}

window.onload = function() {
    document.getElementById('message').style["min-width"] = "60em";
    document.getElementById('message').style["height"] = "50em";
    resetForm1();
};