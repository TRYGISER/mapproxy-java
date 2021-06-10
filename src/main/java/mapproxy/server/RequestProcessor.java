package mapproxy.server;
import mapproxy.core.*;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.*;

//******************************************************************************
//**  RequestProcessor
//******************************************************************************
/**
 *   Used to process HTTP Requests and send responses back to the client. As 
 *   new http requests come in (via the processRequest method), they are added
 *   to a queue (pool). Requests in the queue are processed by instances of
 *   this class via the run method.
 *
 ******************************************************************************/

public class RequestProcessor implements Runnable {
    
    /** Request pool */
    private static java.util.List<SocketChannel> pool = new java.util.LinkedList<SocketChannel>();

    /** Hashmap used to store requests and http responses. Used to manage/send eTags and 304 responses. */
    private static ConcurrentHashMap<String, String> httpCache = new ConcurrentHashMap<String, String>();

    private static String serverName = "MapProxy Server 0.8.1 RC4";
    
  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of this class.  */    
    
    public RequestProcessor(){}
  
    
  //**************************************************************************
  //** processRequest
  //**************************************************************************
  /** Adds a new http request to the que/pool. */
    
    public static void processRequest(SocketChannel request) {

        synchronized (pool) {
           pool.add(pool.size(), request);
           pool.notifyAll();
        }
    }
    
    
    
  //**************************************************************************
  //** Run
  //**************************************************************************
  /** Used to process remove a connection from the connection pool and invoke the 
   *  HttpClient 
   */
    
    public void run() {
        while (true) {

        //Find request in pool
          SocketChannel connection = null;
          synchronized (pool) {
            while (pool.isEmpty()) {
              try {
                pool.wait();
              }
              catch (InterruptedException e) {
                  e.printStackTrace();
              }
            }
            connection = pool.remove(0);

          }

          if (connection!=null) {
              //System.out.println();
              //System.out.println("New Request From: " + connection.socket().getInetAddress());
              //System.out.println("TimeStamp: " + new java.util.Date());
              parseInputStream(connection);
          }
          

        } // end while
    }
    
    
    
    
  //**************************************************************************
  //** Parse InputStream
  //**************************************************************************
    
    private void parseInputStream(SocketChannel connection){
                
        try {
          

          //THIS IS THE MAGICAL STATEMENT THAT CAUSES THE SERVER NOT TO HANG!!!
            connection.configureBlocking(false);


          //Extract Header from Request
            RequestHeader request = new RequestHeader(connection);
            //System.out.println(request.toString());



          //Read in any remaining bytes from the connection (body of the http request)
            StringBuffer body = new StringBuffer();
            java.nio.charset.Charset charset = java.nio.charset.Charset.forName("UTF-8");
            java.nio.charset.CharsetDecoder decoder = charset.newDecoder();
            int bufferSize = 21480;
            java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocateDirect(bufferSize);
            int numRead = 0;
            while (numRead >= 0) {

              //Read bytes from the client
                buf.clear();
                numRead = connection.read(buf);
                buf.flip();

                
              //Convert bytes to a string and append it to the body
                int old_position = buf.position();
                try{
                    body.append(decoder.decode(buf).toString());
                }
                catch(Exception e){}
                buf.position(old_position);


                if (numRead<bufferSize) break;
            }
            buf.clear();


          //Send response to back to the client
            try {
              this.getResponse(request, connection, body.toString());
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            try {
              //Close the socket connection
                connection.close();    
            }
            catch (IOException e) {e.printStackTrace();} 
        }
    }


  //**************************************************************************
  //** getResponse
  //**************************************************************************
  /** Used to construct a response to the client
   */
    private void getResponse(RequestHeader request, SocketChannel connection, String body) throws Exception {

        Service service = ((mapproxy.core.Service) mapproxy.config.Config.base_config().get("mapproxy").toObject());
        ServiceResponse response = service.getResponse(request.getURL().toString(), body);
        javaxt.utils.Date date = response.getDate();
        String key = response.getID();

        //System.out.println(key);

        if (useCache(request, connection, key, date)) return;


        byte[] rsp = response.getByteArray();
        String contentType = response.getContentType();
        String eTag = "W/\"" + rsp.length + "-" + date.getTime() + "\"";



      //GZIP compress the output (as needed)
        boolean gzip = request.getProperty("Accept-Encoding").toLowerCase().contains("gzip");
        if (contentType.startsWith("image/")) gzip = false;
        if (gzip){
            try {
                rsp = gzip(rsp);
            }
            catch (IOException e) {
                gzip = false;
            }
        }
        

      //Create header
        String header = getHeader(date, contentType, rsp.length, eTag, gzip);
        byte[] hdr = header.getBytes();


      //Create a buffer and write bytes to the socket
        int bufferSize = hdr.length + rsp.length;
        java.nio.ByteBuffer output = java.nio.ByteBuffer.allocateDirect(bufferSize);
        output.put(hdr);
        output.put(rsp);
        output.flip();
        synchronized(connection) {
            connection.write(output);
        }


      //Update the http cache
        this.updateCache(key, eTag);
    }


  //**************************************************************************
  //** getHeader
  //**************************************************************************
  /**  Used to construct an http response header
   */
    private String getHeader(javaxt.utils.Date date, String format, long length, String eTag, boolean gzip){

        StringBuffer header = new StringBuffer();
        header.append("HTTP/1.1 200 OK\r\n");
        header.append("Date: " + date.toString("EEE, dd MMM yyyy HH:mm:ss zzz") + "\r\n");
        header.append("Server: " + serverName + "\r\n");
        header.append("Content-Type: " + format + "\r\n");
        header.append("Content-Length: " + length + "\r\n");
        header.append("ETag: " + eTag + "\r\n");
        header.append("Last-Modified: " + date.toString("EEE, dd MMM yyyy HH:mm:ss zzz") + "\r\n"); //Sat, 23 Oct 2010 13:04:28 GMT
      //header.append("Cache-Control: " + "max-age=" + (365*24*60*60) + "\r\n"); //vs "no-cache"
        //header.append("Accept-Ranges: bytes\r\n");
        header.append("Connection: Keep-Alive\r\n");
        if (gzip){
            header.append("Content-Encoding: gzip\r\n");
        }
        header.append("\r\n");
        //System.out.print("\r\n" + header.toString());
        return header.toString();
    }


  //**************************************************************************
  //** useCache
  //**************************************************************************
  /** Used to check whether the request is in the http cache. If so, sends a
   *  304 http response and returns true. Otherwise, nothing is sent back to
   *  the client and the method returns false.
   */
    private boolean useCache(RequestHeader request, SocketChannel connection, String key, javaxt.utils.Date date) throws IOException {

        if (key==null) return false;

        String eTag = request.getProperty("if-none-match");
        String cacheControl = request.getProperty("cache-control");

        if (cacheControl==null) cacheControl = "";
        if (cacheControl.equalsIgnoreCase("no-cache")==false && eTag!=null){

            String val = null;
            synchronized (httpCache){
                val = httpCache.get(key);
            }
            if (val!=null){
                if (val.equals(eTag)){

                  //Return 304 Response Code
                    StringBuffer header = new StringBuffer();
                    header.append("HTTP/1.1 304 Not Modified\r\n");
                    header.append("Date: " + date.toString("EEE, dd MMM yyyy HH:mm:ss zzz") + "\r\n");
                    header.append("Server: " + serverName + "\r\n");
                    header.append("ETag: " + eTag + "\r\n");
                    header.append("\r\n");

                    System.out.println("304 Response!");
                    //System.out.println(header);
                    byte[] response = header.toString().getBytes();
                    java.nio.ByteBuffer output = java.nio.ByteBuffer.allocateDirect(response.length);
                    output.put(response);
                    output.flip();
                    synchronized(connection) {
                        connection.write(output);
                    }
                    return true;
                }
            }

        }

        return false;
    }


  //**************************************************************************
  //** updateCache
  //**************************************************************************
  /** Used to update the http cache.
   */
    private void updateCache(String key, String eTag){
        if (key==null || eTag==null) return;
        synchronized (httpCache){
            httpCache.put(key, eTag); //<-- Need to make this smarter so we don't run out of memory...
        }
    }


  //**************************************************************************
  //** gzip
  //**************************************************************************
  /** Used gzip compress a byte array
   */
    private byte[] gzip(byte[] img) throws IOException {

        System.out.println(img.length + " bytes");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream(img.length);
        GZIPOutputStream out = new GZIPOutputStream(bos);
        out.write(img);
        out.finish();
        out.close();
        img = bos.toByteArray();
        System.out.println(img.length + " bytes");
        return img;
    }
}