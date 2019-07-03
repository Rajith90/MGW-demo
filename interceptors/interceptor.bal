import ballerina/io;
import ballerina/http;

public function validateHeader (http:Caller outboundEp, http:Request req) {
    if(!req.hasHeader("x-auth")) {
    	http:Response response= new;
    	response.setJsonPayload({"error":true, "message": "x-auth header is missing in the request"});
    	var outboundResponse = outboundEp->respond(response);
            if (outboundResponse is error) {
                log:printError("Error when sending response", err = outboundResponse);
            }

    }
}

public function sendCustomRespone (http:Caller outboundEp, http:Response res) {
    json|error payload = res.getJsonPayload();
    if(payload is json ) {
    	if(payload.toString().equalsIgnoreCase("{}")) {
	    	http:Response response= new;
	    	response.setJsonPayload({"length":"0"});
	    	var outboundResponse = outboundEp->respond(response);
	            if (outboundResponse is error) {
	                log:printError("Error when sending response", err = outboundResponse);
	            }
        }
    }
}
