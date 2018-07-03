package main.java;

import java.net.MalformedURLException;
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
     *  - calling SendHTTPRequest's getRequest
     *      - In this case, the url and response will be stored in 
     *          Redis Cache using the addToCache() method
     *      - Any problems with the cache or URL will return an error message to the Client
     * @param url the client's intended site
     * @return HTTP InputStream
     */
    public String getRequest(String url) {
        String malURLMsg = "Malformed URL. Request could not be made. Try another site that starts with https or http.";
        String noElementMsg = "Could not obtain a response for "+ url + ". Request could not be made.";
        String genExceptionMsg = "Request could not be made";
        try {
            String response;
            String cacheResponse = current.findCacheResult(url);
            if (cacheResponse != null){
                response = cacheResponse;
            } else {
                response = SendHTTPRequest.getRequest(url);
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