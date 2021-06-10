package mapproxy.core.request;

//******************************************************************************
//**  RequestParams Class
//******************************************************************************
/**
 *   This class represents key-value request parameters. It allows case-
 *   insensitive access to all keys. Multiple values for a single key will be
 *   concatenated (eg. to "layers=foo&layers=bar" becomes "layers: foo,bar").
 *   All values can be accessed as a property.
 *
 ******************************************************************************/

public class RequestParams {

    public NoCaseMultiDict params;


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of RequestParams. 
   *  @param param A dict or ``NoCaseMultiDict``.
   */

    public RequestParams(NoCaseMultiDict param) {
        if (param==null)
            this.params = new NoCaseMultiDict();
        else
            this.params = param; //NoCaseMultiDict(param);
    }

    public RequestParams() {
        this(null);
    }


  //**************************************************************************
  //** get
  //**************************************************************************

    public String get(String key, String defaultValue){ //type_func=None
        return this.params.get(key, defaultValue);
    }

    
  //**************************************************************************
  //** set
  //**************************************************************************

    public void set(String key, String value, boolean append, boolean unpack){
        this.params.set(key, value, append);
    }

    public void set(String key, String value, boolean append){
        this.params.set(key, value, append);
    }

    public void set(String key, String value){
        this.params.set(key, value);
    }


  //**************************************************************************
  //** copy
  //**************************************************************************
    
    public RequestParams copy(){
        return new RequestParams(params);
    }

    
  //**************************************************************************
  //** getKeys
  //**************************************************************************

    public java.util.HashSet<String> getKeys(){
        return this.params.getKeys();
    }


  //**************************************************************************
  //** contains
  //**************************************************************************

    public boolean contains(String key){
        return this.params.contains(key);
    }


  //**************************************************************************
  //** query_string
  //**************************************************************************
  /** The map request as a query string (the order is not guaranteed).
   <pre>
        >>> RequestParams(dict(foo='egg', bar='ham%eggs', baz=100)).query_string
        'baz=100&foo=egg&bar=ham%25eggs'
   </pre>
   */
    public String query_string(){

        StringBuffer str = new StringBuffer();
        java.util.HashSet<String> keys = params.getKeys();
        java.util.Iterator<String> it = keys.iterator();
        while (it.hasNext()){
            String key = it.next();
            String value = params.get(key, "");
            //str.append(key + "=" + value);
            str.append(key + "=" + java.net.URLEncoder.encode(value));
            if (it.hasNext()) str.append("&");
        }
        return str.toString();

    }


  //**************************************************************************
  //** with_defaults
  //**************************************************************************
  /** Return this MapRequest with all values from `defaults` overwritten. */
    public RequestParams with_defaults(RequestParams defaults){        
        RequestParams n = this.copy();        
        java.util.Iterator<String> it = defaults.params.getKeys().iterator();
        while (it.hasNext()){
            String key = it.next();
            String value = defaults.params.get(key);
            if (value!=null){
                if (n.get(key, null)==null) //<-- this is a bit odd...
                    n.set(key, value, false, true); //n.set(key, value, unpack=True)
            }
        }
        //System.out.println(n.get("bbox", null));
        return n;
        /*
        new = self.copy()
        for key, value in defaults.params.iteritems():
            if value != [None]:
                new.set(key, value, unpack=True)
        return new
         */
    }

  //**************************************************************************
  //** split_mime_type
  //**************************************************************************
  /** @return Array containing mime_class, mime_type, options
   <pre>
        >>> split_mime_type('text/xml; charset=utf-8')
        ('text', 'xml', 'charset=utf-8')
   </pre>
   */
    public static String[] split_mime_type(String mime_type){
        String options = null;
        String mime_class = null;
        if (mime_type.contains("/")){ //if '/' in mime_type:
            String[] arr = mime_type.split("/");
            mime_class = arr[0];
            mime_type = arr[1];
        }
        if (mime_type.contains(";")) {//if ';' in mime_type:
            String[] arr = mime_type.split(";");
            mime_type = arr[0];
            options = arr[1];
        }

        return new String[]{ mime_class, mime_type, options };
    }


}