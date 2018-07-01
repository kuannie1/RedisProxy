import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import redis.clients.jedis.Jedis;
import java.util.LinkedList;

/**
 * Connects to a Jedis Database and executes Jedis commands based on
 *  - cache size
 *  - expiry time
 *  - Requested url
 * @author Anne Ku
 */
public class JedisInstance {
    private int expiryTime;
    private int cacheSize;
    private List<String> list;
    private Jedis instance; 
    private Queue<String> urls;
    private int currentSize;
    
    /**
     * Initializes the connection to Jedis
     * 
     * @param expiryTimeInMinutes minutes to wait until http response gets evicted
     * @param cacheSize maximum number of http responses that can be saved at a time
     */
    public JedisInstance(int expiryTimeInMinutes, int cacheSize){
        this.expiryTime = expiryTimeInMinutes*60;
        this.cacheSize = cacheSize;
        this.instance = new Jedis("localhost");
        this.urls = new LinkedList<String>();
        this.currentSize = 0;
    }
    
    /**
     * Looks up url from cache to see if the response is saved and recent
     * @param url key possibly in database
     * @return cache value from Redis key-value database, null if key doesn't exist
     */
    public String findCacheResult(String url){
        return instance.get(url);
    }
    
    /**
     * Inserts url-response (key-value) pair into the Redis cache. 
     * Sets an 'expiry date' for each addition based on proxy's needs
     * @param url the recently visited site
     * @param response the response 
     */
    public void addToCache(String url, String response){
        instance.append(url, response);
        instance.expire(url, expiryTime);
        urls.add(url);
        currentSize++;
        if (currentSize - cacheSize == 1) {
            String lastURL = urls.remove();
            instance.del(lastURL);
        }
    }
    /**
     * Method to clear the cache and each site when client leaves the proxy server
     */
    public void flushCache(){
        instance.flushAll();
        while (!urls.isEmpty()){
            urls.remove();
            currentSize--;
        }
    }

}
