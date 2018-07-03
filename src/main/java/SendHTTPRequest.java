package main.java;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class to make HTTP GET and POST requests for the Proxy
 * @author anne
 */
class SendHTTPRequest {
        
    /**
     * Method that opens a connection with the uncached user-typed URL
     * @param url to be accessed
     * @return HTTP InputStream from GET request
     * @throws Exception 
     */
    static String getRequest(String url) throws IOException {
        HttpURLConnection con;
        StringBuilder response = new StringBuilder();
        
//      Open Connection
        URL obj = new URL(url);
        con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        
//      If all goes well, get the GET Request content
        if (responseCode >= 400){
            response.append(con.getErrorStream().toString());
        } else if (responseCode >= 300){
            String newURL = con.getHeaderField("Location");
            System.out.println("Redirecting to " + newURL);
            return getRequest(newURL);
        }
        BufferedReader inputStream = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = inputStream.readLine()) != null){ response.append(inputLine); }
        inputStream.close();
        
        return response.toString();
    }
}