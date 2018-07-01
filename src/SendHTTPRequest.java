import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL; 
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to make HTTP GET and POST requests for the Proxy
 * @author anne
 */
public class SendHTTPRequest {
        
    /**
     * Method that opens a connection with the uncached user-typed URL
     * @param url to be accessed
     * @return HTTP Response from GET Request
     * @throws Exception 
     */
    public static String get_request(String url) throws Exception {

//      Open Connection
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

//      Request Header
        int responseCode = con.getResponseCode();

//        logic for what to do for these response codes: 400, 401, 403, 404, 409, 500
        if (responseCode == 500){
            return "500: Service Error";
        } else if (responseCode == 400){
            return "400: Bad Request";
        } else if (responseCode == 401){
            return "401: Unauthorized";
        } else if (responseCode == 403){
            return "403: Forbidden";
        } else if (responseCode == 404){
            return "404: Not found";
        } else if (responseCode == 409){
            return "409: Conflict";
        }
        
//      Receive response
        BufferedReader inputStream = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = inputStream.readLine()) != null) {
            response.append(inputLine);
        }
        inputStream.close();
        return response.toString();
   }
    
    /**
     * Method that opens a connection with the uncached user-typed URL
     * @param url to be accessed
     * @param params to submit to the url
     * @return HTTP Response from POST Request
     * @throws Exception 
     */
    public static String post_request(String url, HashMap<String, String> params) throws Exception {

//      Open Connection
        URL obj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection)obj.openConnection();
        conn.setRequestMethod("POST");

//      POST parameters based on params, need to convert to byte array
        StringBuilder POSTReq = new StringBuilder("");
        for (Map.Entry<String,String> entry: params.entrySet()){
            String pair = entry.getKey() + "=" + entry.getValue();
            POSTReq.append(pair + "&");
        }
        byte[] postDataBytes = POSTReq.toString().getBytes("UTF-8");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

//      Receive response
        Reader inputStream = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (int c; (c = inputStream.read()) >= 0;){
            sb.append((char)c);
        }
        String response = sb.toString();
        return response;
    }
}
