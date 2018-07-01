
import java.rmi.Naming;
import java.util.HashMap;
/**
 * Class that gets a proxy reference to a remote object and invokes GET/POST request
 * @author anne
 */
public class Client { 
    public static void main(String args[]) throws Exception {

//      GET Request to Proxy
        ProxyServerInterface obj = (ProxyServerInterface)Naming.lookup("//localhost/ProxyConnection");
        String getURL = "http://www.httpbin.com/get";
        System.out.println(obj.getRequest(getURL)); 
        obj.clear();

//      POST Request to Proxy
        ProxyServerInterface server = (ProxyServerInterface)Naming.lookup("//localhost/ProxyConnection");
        String postURL = "http://www.httpbin.com/post";
        HashMap<String, String> params = new HashMap<String,String>();
        params.put("firstname", "Dwayne");
        params.put("lastname", "Johnson");
        System.out.println(server.postRequest(postURL, params)); 
        server.clear();
        
    }
}