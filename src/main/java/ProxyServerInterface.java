package main.java;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;

/**
 * Interface used by the client and implemented by the proxy
 * @author anne
 */
public interface ProxyServerInterface extends Remote {
    public String accessPage(String url) throws RemoteException;
    public void clear() throws RemoteException;
    public HashSet<String> getCacheSites() throws RemoteException;
}