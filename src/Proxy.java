import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.util.NoSuchElementException;

/**
 * Remote Object that makes GET and POST requests on the Client's behalf
 * @author anne
 */
public class Proxy extends UnicastRemoteObject implements ProxyServerInterface {

    private int maxExpiryTimeMinutes = 1;
    private int cacheSize = 5;
    private JedisInstance current = new JedisInstance(maxExpiryTimeMinutes, cacheSize);

    /**
     * Constructor that starts the server and binds the proxy to a port
     * @throws RemoteException
     * @throws MalformedURLException
     */
    private Proxy() throws RemoteException, MalformedURLException {
        System.out.println("RMI server started");
        LocateRegistry.createRegistry(1099);
        System.out.println("java RMI registry created.");
        Naming.rebind("//localhost/ProxyConnection", this);
        System.out.println("PeerServer bound in registry");
    }

    /**
     * Method that executes a GET request by 
     *  - looking up cached response or
     *  - calling SendHTTPRequest's getRequest
     *      - In this case, the url and response will be stored in the Redis Cache using the addToCache() method
     * @param url the client's intended site
     * @return HTTP InputStream
     */
    public String getRequest(String url) {
        try {
            String response;
            String cacheResponse = current.findCacheResult(url);
            System.out.println(url + " cacheResponse: " + cacheResponse);
            if (cacheResponse != null){
                response = cacheResponse;
            } else {
                response = SendHTTPRequest.getRequest(url);
                current.addToCache(url, response);
            }
            current.printCacheKeys();
            return response;
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return "Malformed URL. Request could not be made.";
        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
            return "Could not obtain a response for "+ url + ". Request could not be made.";
        } catch (Exception ex){
            ex.printStackTrace();
            return "Request could not be made";
        }
    }

    /**
     * Client's method of telling Proxy to clear the cache when it (voluntarily) breaks off its connection
     */
    public void clear() {
        current.flushCache();
    }

    public static void main(String args[]) throws Exception {
        Proxy prox = new Proxy();
    }
}