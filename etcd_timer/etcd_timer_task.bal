import ballerina/io;
import ballerina/runtime;
import ballerina/task;
import ballerina/math;
import ballerina/http;
import ballerina/encoding;

http:Client clientEndpoint = new("http://35.226.63.174:30122");
http:Client clientETCDEndpoint = new("http://localhost:8080");
public function main () {
    future<()> uploadTask = start timerTask();
  
    while(true){

    }
}






function timerTask() {
    task:Timer? timer;
        int timeSpan = 5000;
        (function() returns error?) onTriggerFunction = uploadKeys;
        function(error) onErrorFunction = informError;
            timer = new task:Timer(onTriggerFunction, onErrorFunction, timeSpan, delay = 5000);
            timer.start();
        
    
}

function getKubePorts() returns (json?){

    var response = clientETCDEndpoint->get("/api/v1/namespaces/default/services/books-search");
     json? jsonResp = handleKubeResponse(response);
     io:println(jsonResp.spec);
     io:println(jsonResp.spec.ports[0]);
     string nodePort = <string>jsonResp.spec.ports[0].nodePort;
     string key = "bookSearch";
     string encodedKey = encoding:encodeBase64(key.toByteArray("UTF-8"));
     string value = "http://35.226.63.174:" + nodePort;
     string encodedValue = encoding:encodeBase64(value.toByteArray("UTF-8"));
     json reqpayload = {"key": encodedKey, "value": encodedValue};
     io:println(reqpayload);
    return reqpayload;
    

}


function uploadKeys() returns (error?){
    io:println("Timer task running");
    http:Request req = new;
    json? reqpayload = getKubePorts();
    
    req.setJsonPayload(reqpayload);

    var response = clientEndpoint->post("/v3alpha/kv/put", req);
     handleResponse(response);
     
    return ();
}

function informError(error e) {
    io:println("Timer task error");
}

function handleResponse(http:Response|error response) {
    if (response is http:Response) {
        var msg = response.getJsonPayload();
        if (msg is json) {

            io:println(msg);
        } else {
            io:println("Invalid payload received:" , msg.reason());
        }
    } else {
        io:println("Error when calling the backend: ", response.reason());
    }
}

function handleKubeResponse(http:Response|error response) returns (json?) {
    if (response is http:Response) {
        var msg = response.getJsonPayload();
        if (msg is json) {
            io:println(msg);
            return msg;
            
        } else {
            io:println("Invalid payload received:" , msg.reason());
        }
    } else {
        io:println("Error when calling the backend: ", response.reason());
    }
    return ();
}