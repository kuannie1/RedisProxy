import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class to make HTTP GET and POST requests for the Proxy
 * @author anne
 */
class SendHTTPRequest {
        
    /**
     * Method that opens a connection with the uncached user-typed URL
     * @param url to be accessed
     * @return HTTP Response from GET Request
     * @throws Exception 
     */
    static String getRequest(String url) throws Exception {

//      Open Connection
        URL obj = new URL(url);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        if (responseCode >= 400){
            return con.getErrorStream().toString();
        } else {
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = inputStream.readLine()) != null) {
                response.append(inputLine);
            }
            inputStream.close();
            return response.toString();
        }
    }
}