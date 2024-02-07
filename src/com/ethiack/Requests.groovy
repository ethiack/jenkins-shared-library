package com.ethiack
import com.ethiack.HttpResponse


/**
 * Request helper class
 */
class Requests {
    String apiKey;
    String apiSecret;

    public Requests(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }


    /**
    * Make an authorization header for HTTP Basic Auth.
    * 
    * @param username Username or API key
    * @param password Password or API secret
    * @return authorization header string
    */
    public static String MakeAuthorizationHeader(String username, String password) {
        String auth = "${username}:${password}";
        String encodedAuth = auth.bytes.encodeBase64().toString();
        return "Basic ${encodedAuth}";
    }


    /**
    * Do a HTTP GET request to the given url.
    *
    * @param requestUrl Target URL
    * @param failOnBadStatus if true, an error will be raised if the status code is not in the 200-399 range
    * @return HttpResponse object
    */
    public HttpResponse doGetHttpRequest(String requestUrl, Boolean failOnBadStatus = true){
        URL url = new URL(requestUrl);    
        HttpURLConnection connection = url.openConnection();    

        connection.setRequestMethod("GET"); 
        connection.setRequestProperty("Authorization", this.MakeAuthorizationHeader(this.apiKey, this.apiSecret))

        connection.connect();    

        HttpResponse resp = new HttpResponse(connection);    

        if(resp.isFailure() && failOnBadStatus){    
            error("\nGET from URL: $requestUrl\n  HTTP Status: $resp.statusCode\n  Message: $resp.message\n  Response Body: $resp.body");    
        }    

        return resp;    
    }


    /**
    * (Base) Do a HTTP request to the given url with the given JSON payload
    *
    * @param json JSON payload
    * @param requestUrl Target URL
    * @param verb HTTP verb
    * @param failOnBadStatus if true, an error will be raised if the status code is not in the 200-399 range
    * @return HttpResponse object
    */
    public HttpResponse doHttpRequestWithJson(String json, String requestUrl, String verb, Boolean failOnBadStatus = true){    
        URL url = new URL(requestUrl);    
        HttpURLConnection connection = url.openConnection();    

        connection.setRequestMethod(verb);    
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", this.MakeAuthorizationHeader(this.apiKey, this.apiSecret))
        connection.doOutput = true;    

        def writer = new OutputStreamWriter(connection.outputStream);    
        writer.write(json);    
        writer.flush();    
        writer.close();    

        connection.connect();

        HttpResponse resp = new HttpResponse(connection);
        if(resp.isFailure() && failOnBadStatus){    
            error("\n$verb to URL: $requestUrl\n    JSON: $json\n    HTTP Status: $resp.statusCode\n    Message: $resp.message\n    Response Body: $resp.body");    
        }    

        return resp;   
    }


    /**
    * Do a HTTP POST request to the given url.
    *
    * @param requestUrl Target URL
    * @param failOnBadStatus if true, an error will be raised if the status code is not in the 200-399 range
    * @return HttpResponse object
    */
    public HttpResponse doPostHttpRequestWithJson(String json, String requestUrl, Boolean failOnBadStatus = true){    
        return this.doHttpRequestWithJson(json, requestUrl, "POST", failOnBadStatus);    
    }    

    /**
    * Do a HTTP PUT request to the given url.
    *
    * @param requestUrl Target URL
    * @param failOnBadStatus if true, an error will be raised if the status code is not in the 200-399 range
    * @return HttpResponse object
    */
    public HttpResponse doPutHttpRequestWithJson(String json, String requestUrl, Boolean failOnBadStatus = true){    
        return this.doHttpRequestWithJson(json, requestUrl, "PUT",  failOnBadStatus);    
    }
}
