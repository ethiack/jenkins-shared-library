package com.ethiack
import groovy.json.JsonSlurperClassic


/**
 * HTTP response object
 */
class HttpResponse {

    Map body;    
    String message;    
    Integer statusCode;    
    boolean failure = false;    

    /**
     * Constructor
     *
     * @param connection HttpURLConnection object
     */
    public HttpResponse(HttpURLConnection connection){    
        this.statusCode = connection.responseCode;    
        this.message = connection.responseMessage;  
  
        def jsonSlurper = new JsonSlurperClassic();
        if(statusCode >= 200 && statusCode < 400){
            this.body = jsonSlurper.parseText(connection.content.text);
        } else {    
            this.failure = true;    
            this.body = jsonSlurper.parseText(connection.getErrorStream().text);    
        }

        connection = null; 
    }       
}
