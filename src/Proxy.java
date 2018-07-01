
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*; 
import java.util.HashMap;

/**
 * Remote Object that makes GET and POST requests on the Client's behalf
 * @author anne
 */
public class Proxy extends UnicastRemoteObject implements ProxyServerInterface {
    int maxExpiryTimeMinutes = 1;
    int cacheSize = 5;
    JedisInstance current = new JedisInstance(maxExpiryTimeMinutes, cacheSize);
    public Proxy() throws RemoteException {}

    /**
     * Method that executes a GET request by 
     *  - looking up cached response or
     *  - calling SendHTTPRequest's get_request
     * @param url
     * @return HTTP Response
     */
    public String getRequest(String url) {
        try {
            String cacheResponse = current.findCacheResult(url);
            if (cacheResponse != null){
                return cacheResponse;
            } else {
                String response = SendHTTPRequest.get_request(url);
                current.addToCache(url, response);
                return response;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Request could not be made";
        }
    }
    
    /**
     * Method that executes a POST request by 
     *  - looking up cached response or
     *  - calling SendHTTPRequest's post_request
     * @param url
     * @return HTTP Response
     */
    public String postRequest(String url, HashMap<String, String> params) {
        try {
            String cacheResult = current.findCacheResult(url);
            if (cacheResult != null){
                return cacheResult;
            } else {
                String response = SendHTTPRequest.post_request(url, params);
                current.addToCache(url, response);
                return response;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Request could not be made";
        }
    }
    
    /**
     * Method to clear the cache if needed
     */
    public void clear() {
        current.flushCache();
        return;
    }
    

    public static void main(String args[]) throws Exception {
        System.out.println("RMI server started");

        try { 
            LocateRegistry.createRegistry(1099); 
            System.out.println("java RMI registry created.");
        } catch (RemoteException e) {
            System.out.println("java RMI registry already exists.");
        }
        Proxy obj = new Proxy();

        // Bind this object instance to the name "ProxyConnection"
        Naming.rebind("//localhost/ProxyConnection", obj);
        System.out.println("PeerServer bound in registry");
    }
}