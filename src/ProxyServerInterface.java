
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
/**
 * This interface is used by the client and implemented by the proxy
 * @author anne
 */
public interface ProxyServerInterface extends Remote {
    public String getRequest(String url) throws RemoteException;
    public String postRequest(String url, HashMap<String,String> params) throws RemoteException;
    public void clear() throws RemoteException;
}