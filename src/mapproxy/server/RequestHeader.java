package mapproxy.server;
import java.io.IOException;
import java.util.StringTokenizer;

//******************************************************************************
//**  RequestHeader Class
//******************************************************************************
/**
 *   Used to encapsulate information found in an HTTP Request
 *
 ******************************************************************************/

public class RequestHeader {
    
    private String[] header = null;
    private java.net.URL url = null;
    private String protocol = null;
    private String version = null;
    private String method = null;
    private String path = null;
    private String host = null;
    private int port;
    
    
  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /**  Creates a new instance of RequestHeader */

    public RequestHeader(java.nio.channels.SocketChannel connection) throws IOException {

        try{
            connection.finishConnect();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        int bufSize = 1;
        char lf = "\n".charAt(0);

        java.io.ByteArrayOutputStream bas = new java.io.ByteArrayOutputStream();
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocateDirect(bufSize);


        int totalBytesRead = 0;

        
      //Extract header from the input stream (one byte at a time)
        int numRead = 0;
        while (numRead >= 0) {

            
          //Read bytes from the channel and update the ByteBuffer
            buf.rewind();
            numRead = connection.read(buf);
            if (numRead>0) totalBytesRead+=numRead;
            buf.rewind();


          //Read bytes from ByteBuffer and add them to the output stream
            byte b = buf.get();
            bas.write(b);


          //Stop reading bytes once we've found the end of the http header
            if (((char)b) == lf){
                //System.out.println("Found LF!");
                String str = bas.toString();
                str = str.substring(str.length()-4);
                if (str.equals("\r\n\r\n")){
                    break;
                }
            }
            if (totalBytesRead>0)
            if (numRead<bufSize) break;
        }
        header = bas.toString("UTF-8").split("\r\n");


      //Parse the first line of the header
        String host = getProperty("Host");
        StringTokenizer st = new StringTokenizer(header[0]);
        if (st.hasMoreTokens()) method = st.nextToken().trim().toUpperCase();
        if (st.hasMoreTokens()) path = st.nextToken().trim();
        if (st.hasMoreTokens()) protocol = st.nextToken().trim().toUpperCase();

        if (protocol.contains("/")) {
            String temp = protocol;
            protocol = temp.substring(0,temp.indexOf("/"));
            version = temp.substring(temp.indexOf("/")+1);
        }
        else{
            protocol = "HTTP";
            version = "1.1";
        }

      //Assemble requested url
        url = new java.net.URL(protocol + "://" + host + path);

      //Update host and post
        this.host = url.getHost();
        this.port = url.getPort();
        if (port < 0 || port > 65535) port = 80;
    }
    


  //**************************************************************************
  //** getHttpVersion
  //**************************************************************************
  /**  Returns the HTTP version number passed in as part of the request */

    public String getHttpVersion(){
        return version;
    }
    
    
  //**************************************************************************
  //** getHeader
  //**************************************************************************
  /**  Returns the header portion of the request */
    
    public String[] getHeaders(){
        return header;
    }
    
  //**************************************************************************
  //** getPath
  //**************************************************************************
  /**  Returns the path specified in the first line of the request */
    
    public String getPath(){
        return path;
    }
    

    
  //**************************************************************************
  //** getMethod
  //**************************************************************************
  /**  Returns the methos specified in the first line of the request. 
   *   Examples include GET, POST, PUT, HEAD, etc.
   */
    
    public String getMethod(){
        return method;
    }
    
    
    public String getHost(){
        return host;
    }
    
    public int getPort(){
        return port;
    }
    
    


    
  //**************************************************************************
  //** isKeepAlive
  //**************************************************************************
  /**  Used to determine whether the Connection attribute is set to Keep-Alive 
   */
    
    public boolean isKeepAlive(){
        if (getProperty("Connection").toUpperCase().contains("KEEP-ALIVE")){
            return true;
        }
        else{
            return false;
        }
    }
    

    
    
  //**************************************************************************
  //** getURL
  //**************************************************************************
  /** Used to retrieve the requested url defined in the header */
    
    public java.net.URL getURL(){
        return url;
    }

    protected void setURL(java.net.URL url){
        this.url = url;
    }

    
  //**************************************************************************
  //** toString
  //**************************************************************************
  /** Returns the full HTTP Request Header.
   */
    public String toString(){
        StringBuffer out = new StringBuffer();
        for (int i=0; i<header.length; i++){
            String str = header[i].trim();
            if (str.length()>0)
                out.append(str+"\r\n");
        }
        out.append("\r\n");
        return out.toString();
    }


  //**************************************************************************
  //** splitHeader
  //**************************************************************************
  /** Used to parse the header and return an array
   *  @deprecated Not used anymore. Basic split should suffice.
   */
    private String[] splitHeader(String s) throws IOException {

      //Get the Request Header
        StringBuffer head = new StringBuffer();
        StringBuffer line = new StringBuffer();
        int c; 
        int n = -1; //char representing a new line
        boolean foundBreak = false;
        for (int i = 0; i < s.length(); i++){

            c = s.charAt(i);
            
          //Find Line Break
            if ((c == '\r' || c == '\n') && !foundBreak) {
                foundBreak = true; //break;
                n = c;
            }
                        
            if (c == '\r' || c == '\n'){
                if (c==n){
                    if (line.length()==0) break;
                    head.append(line);
                    head.append((char) '\n');
                    line = new StringBuffer();
                }
            }
            else{
               line.append((char) c);
            }
        }       
        
        return head.toString().split("\n");
    }
  
    
  //**************************************************************************
  //** getProperty
  //**************************************************************************
  /** Used to extract a specific property from the http header. Note that there 
   *  may exist more than one instance of a given property in which case, only 
   *  the first instance is returned.
   */    
    public String getProperty(String key){
      for (int i=0; i<header.length; i++){
           String Property = header[i];
           if (Property.contains(":")){
               String PropertyName = Property.substring(0,Property.indexOf(":"));
               String PropertyValue = Property.substring(PropertyName.length()+1);
               if (PropertyName.trim().equalsIgnoreCase(key)) return LTrim(PropertyValue);
           }
      }
      return "";
    }
    
    private String LTrim(String str){
        while (str.startsWith(" ")) str = str.substring(1);
        return str;
    }
    
}