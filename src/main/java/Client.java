package main.java;


import java.net.ConnectException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that gets a proxy reference (stub) to a remote object and invokes GET request
 * The Proxy needs to be running before the Client can start
 * @author anne
 */
public class Client {
    ProxyServerInterface obj;

    /**
     * Constructor that makes the connection with the Proxy via Java Remote Method Invocation (RMI)
     */
    public Client() {
        try {
            this.obj = (ProxyServerInterface)Naming.lookup("//localhost/ProxyConnection");
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that takes in a client-input webpage and returns
     * @param url site the user is trying to reach
     * @return HTTP InputStream, can be from cache or GET request
     * @throws RemoteException
     */
    public String accessPage(String url) throws RemoteException {
        return obj.accessPage(url);
    }

    /**
     * Method that allows Client to disconnect from the proxy. Clears cache in the process
     * @throws RemoteException
     */
    public void exit() throws RemoteException {
        obj.clear();
    }
    
    
    /**
     * Debugging method for checking cache status from client
     * @return Hashset with pages currently in cache
     * @throws RemoteException 
     */
    public HashSet<String> getCache() throws RemoteException {
        return obj.getCacheSites();
    }
}