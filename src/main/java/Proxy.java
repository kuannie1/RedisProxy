package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Remote Object that makes GET and POST requests on the Client's behalf
 * @author anne
 */
public class Proxy extends UnicastRemoteObject implements ProxyServerInterface {

    private int maxExpiryTimeInSec = 15;
    private int cacheSize = 4;
    private JedisInstance current = new JedisInstance(maxExpiryTimeInSec, cacheSize);

    /**
     * Constructor that starts the server and binds the proxy to a port
     * @throws RemoteException
     * @throws MalformedURLException
     */
    public Proxy() throws RemoteException, MalformedURLException {
        LocateRegistry.createRegistry(1099);
        Naming.rebind("//localhost/ProxyConnection", this);
    }

    /**
     * Method that executes a GET request by 
     *  - looking up cached response or
     *  - making a GET request to retrieve a page's fresh contents
     *      - In this case, the url and response will be stored in 
     *          Redis Cache using the addToCache() method
     *      - Any problems with the cache or URL will return an error message to the Client, but won't be cached
     * @param url the client's intended site
     * @return HTTP InputStream
     */
    public String accessPage(String url) {
        String malURLMsg = "Malformed URL. Request could not be made. Try another site that starts with https or http.";
        String noElementMsg = "Could not obtain a response for "+ url + ". Request could not be made.";
        String genExceptionMsg = "Request could not be made";
        try {
            String response;
            String cacheResponse = current.findCacheResult(url);
            if (cacheResponse != null){
                response = cacheResponse;
            } else {
                response = getRequest(url);
                current.addToCache(url, response);
            }
            return response;
        } catch (MalformedURLException ex) {
            return malURLMsg;
        } catch (NoSuchElementException ex) {
            return noElementMsg;
        } catch (Exception ex){
            return genExceptionMsg;
        }
    }
    
    
    /**
     * Method that opens a connection with the uncached user-typed URL
     * @param url to be accessed
     * @return HTTP InputStream from GET request
     * @throws Exception 
     */
    String getRequest(String url) throws IOException {
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
    

    /**
     * Client's method of telling Proxy to clear the cache when it (voluntarily) breaks off its connection
     */
    public void clear() {
        current.flushCache();
    }
    
    /**
     * Calculate set of cached sites for testing purposes
     * @return HashSet (Serializable Set) of the current cached sites
     */
    public HashSet<String> getCacheSites(){
        return (HashSet)current.getCacheKeys();
    }

}
