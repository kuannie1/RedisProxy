package test.java;
import java.rmi.RemoteException;
import java.util.HashSet;
import main.java.Client;
import main.java.Proxy;


/**
 *
 * @author anne
 */
public class RedisProxyDriver implements Runnable {
    public static void main (String[] args) throws Exception {
        Proxy prox = new Proxy();
        System.out.println("prox running");
        Thread thread = new Thread(new RedisProxyDriver());
        thread.start();
//      Exiting the main method
    }

    /**
     * Running a separate thread to run tests
     * Each test makes a new Client instance, 
     *      but the cache from previous tests get cleared before the next one runs
     */
    @Override
    public void run(){
        try {
            System.out.println("Test0 result: " + test0());
            System.out.println("Test1 result: " + test1());
            System.out.println("Test2 result: " + test2());
            System.out.println("Test3 result: " + test3());
            System.out.println("Test4 result: " + test4());
            System.exit(0);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    
    /**
     * First method that takes a url and checks if it's not in the cache
     * @param url site to check in cache
     * @param cache HashSet of key-value pairs
     * @return 
     */
    boolean absenceOf(String url, HashSet<String> cache){
        return !(cache.contains(url));
    }
    
    /**
     * Second method that takes in a url and checks if it's in the cache
     * @param url site to check in cache
     * @param cache HashSet of key-value pairs
     * @return 
     */
    boolean presenceOf(String url, HashSet<String> cache){
        return cache.contains(url);
    }
    
    /**
     * Method that takes in an array of urls and checks if each url isn't in the cache
     * @param urls array of urls to check
     * @param cache HashSet of key-value pairs
     * @return 
     */
    boolean absenceOf(String[] urls, HashSet<String> cache){
        for (String url: urls){
            if (presenceOf(url, cache)){ return false; }
        }
        return true;
    }
    
    /**
     * Method that takes in an array of urls and checks if each url is in the cache
     * @param urls array of urls to check
     * @param cache HashSet of key-value pairs
     * @return 
     */
    boolean presenceOf(String[] urls, HashSet<String> cache){
        for (String url: urls){
            if (absenceOf(url, cache)) { return false; }
        }
        return true;
    }
    
    /**
     * Test for expiry by making sure the oldest elements aren't present in cache after expiry
     * @return boolean that checks for 
     *      - absence of 'https://www.google.com' after waiting 1 minute
     *      - presence of 'https://www.segment.com' and 'http://www.olin.edu'
     */
    boolean test0() throws RemoteException, InterruptedException{
        System.out.println("\nTest0");
        
        String oldURL = "https://www.google.com";
        String[] newURLs = {"https://www.segment.com", "http://www.olin.edu"};
        
        Client cli = new Client();
        cli.getPage(oldURL);
        Thread.sleep(15000);
        
        for (String url:newURLs){
            cli.getPage(url);
        }
        HashSet<String> cachedSites = cli.getCache();
        cli.exit();
        
        boolean oldSiteAbsence = absenceOf(oldURL, cachedSites);
        boolean newSitesPresence = presenceOf(newURLs, cachedSites);
        boolean sizeCheck = (cachedSites.size() == 2);
        
        return oldSiteAbsence && newSitesPresence && sizeCheck;
    }
    
    /**
     * Test for only adding one element, waiting for expiry time to pass, and checking if cache is empty
     * @return boolean that checks for absence of 'https://www.segment.com' after waiting 1 minute
     */
    boolean test1() throws RemoteException, InterruptedException{
        System.out.println("\nTest1");
        
        String oldURL = "https://www.olin.com";
        
        Client cli = new Client();
        cli.getPage(oldURL);
        Thread.sleep(15000);
        HashSet<String> cachedSites = cli.getCache();
        
        cli.exit();
        
        boolean oldSiteAbsence = absenceOf(oldURL, cachedSites);
        boolean sizeCheck = (cachedSites.size() == 0);

        return oldSiteAbsence;
    }
    
    /**
     * Test for adding more than cache-capacity elements and checking absence of first few urls 
     * @return boolean that checks for:
     *      - absence of first two URLs
     *      - presence of four other URLs
     */
    boolean test2() throws RemoteException, InterruptedException{
        System.out.println("\nTest2");
        
        String[] oldURLs = {"https://www.google.com", "https://www.facebook.com", "https://www.instagram.com/"};
        String[] newURLs = {"https://www.segment.com", "http://www.olin.edu", "https://www.linkedin.com", "https://stackoverflow.com/"};
        
        Client cli = new Client();
        for (String url: oldURLs){
            cli.getPage(url);
        }
        Thread.sleep(15000);
        for (String url:newURLs){
            cli.getPage(url);
        }
        HashSet<String> cachedSites = cli.getCache();
       
        cli.exit();
        
        boolean oldSitesAbsence = absenceOf(oldURLs, cachedSites);
        boolean newSitesPresence = presenceOf(newURLs, cachedSites);
        boolean sizeCheck = (cachedSites.size() == 4);
        
        return oldSitesAbsence && newSitesPresence && sizeCheck;
    }
    
    
    /**
     * Test for trying to access malformed url (olin.edu) and a valid
     * @return boolean that makes sure 
     *      - olin.edu isn't in the cache
     *      - https://www.segment.com is in the cache
     */
    boolean test3() throws RemoteException, InterruptedException{
        System.out.println("\nTest3");
        
        String malformedURL = "htp://www.olin.edu";
        String validURL = "https://www.segment.com";
        
        Client cli = new Client();
        cli.getPage(malformedURL);
        cli.getPage(validURL);
        HashSet<String> cachedSites = cli.getCache();
        
        cli.exit();
        
        boolean malformedSiteAbsence = absenceOf(malformedURL, cachedSites);
        boolean validSitePresence = presenceOf(validURL, cachedSites);
        boolean sizeCheck = (cachedSites.size() == 1);

        return malformedSiteAbsence && validSitePresence && sizeCheck;
    }
    
    
    /**
     * Test3 but adding the pages in reverse order
     * @return boolean that makes sure 
     *      - https://www.segment.com is in the cache
     *      - olin.edu isn't in the cache
     */
    boolean test4() throws RemoteException, InterruptedException{
        System.out.println("\nTest4");
        
        String malformedURL = "http://www.olin.adu";
        String validURL = "https://www.segment.com";
        
        Client cli = new Client();
        cli.getPage(malformedURL);
        cli.getPage(validURL);
        HashSet<String> cachedSites = cli.getCache();
        
        cli.exit();
        
        boolean malformedSiteAbsence = absenceOf(malformedURL, cachedSites);
        boolean validSitePresence = presenceOf(validURL, cachedSites);
        boolean sizeCheck = (cachedSites.size() == 1);

        return malformedSiteAbsence && validSitePresence && sizeCheck;
    }   
    
}
