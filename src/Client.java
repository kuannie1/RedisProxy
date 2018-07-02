
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Class that gets a proxy reference to a remote object and invokes GET/POST request
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
    public String getPage(String url) throws RemoteException {
        return obj.getRequest(url);
    }

    /**
     * Method that allows Client to disconnect from the proxy. Clears cache in the process
     * @throws RemoteException
     */
    public void exit() throws RemoteException {
        obj.clear();
        System.exit(0);
    }


    public static void main(String args[]) throws Exception {
        Client cli = new Client();
        cli.getPage("https://www.google.com");
        cli.getPage("https://www.quora.com");
        cli.getPage("https://www.instagram.com");
        cli.getPage("https://www.gmail.com");
        cli.getPage("https://www.stackoverflow.com");
        cli.getPage("http://olin.edu/");
        cli.getPage("https://medium.com/");
        cli.exit();
    }
}