import java.util.Iterator;
import java.util.Queue;
import redis.clients.jedis.Jedis;
import java.util.LinkedList;
import java.util.Set;

/**
 * Connects to a Jedis Database and executes Jedis commands based on
 *  - cache size
 *  - expiry time
 *  - Requested url
 * @author Anne Ku
 */
class JedisInstance {
    private int expiryTime;
    private int cacheSize;
    private Jedis instance; 
    private Queue<String> urls;
    private int currentSize;


    /**
     * Initializes the connection to Jedis
     * 
     * @param expiryTimeInMinutes minutes to wait until http response gets evicted
     * @param cacheSize maximum number of http responses that can be saved at a time
     */
    JedisInstance(int expiryTimeInMinutes, int cacheSize){
        this.expiryTime = expiryTimeInMinutes*60;
        this.cacheSize = cacheSize;
        this.instance = new Jedis("localhost");
        this.urls = new LinkedList<String>();
        this.currentSize = 0;
    }


    /**
     * Looks up url from cache to see if the response is saved and recent
     * @param url key possibly in database
     * @return HTTP InputStream from Redis key-value database, null if key doesn't exist
     */
    String findCacheResult(String url){
        return instance.get(url);
    }


    /**
     * Inserts url-response (key-value) pair into the Redis cache. 
     * Sets an 'expiry date' for each addition based on proxy's needs
     * Enqueues url to a QueueList, which will keep track of the urls added in chronological order
     * @param url recently visited site
     * @param response HTTP InputStream from GET Request
     */
    void addToCache(String url, String response){
        instance.append(url, response);
        instance.expire(url, expiryTime);
        urls.add(url);
        removeURLSIfNeeded();
    }

    /**
     * Deletes the latest URL in the cache and url chronological queue.
     * Only needs to delete one at a time since a page is only added one at a time
     */
    void removeURLSIfNeeded(){
        if (instance.dbSize() - cacheSize > 0){
            String oldURL = urls.remove();
            instance.del(oldURL);
        }
    }


    /**
     * Method to clear the cache and each site when client leaves the proxy server
     */
    void flushCache(){
        instance.flushAll();
        while (!urls.isEmpty()){
            urls.remove();
        }
    }

    /**
     * Helper Method to print all the url keys from the cache
     */
    void printCacheKeys(){
        Set<String> savedSites = instance.keys("*");
        System.out.println(savedSites.toString());
    }
}
