package mapproxy.wms.request;
import mapproxy.core.request.*;
//******************************************************************************
//**  WMSRequest Class
//******************************************************************************
/**
 *
 ******************************************************************************/

public class WMSRequest extends BaseRequest {



  /** parameters that are fixed for a request */
    protected RequestParams fixed_params = new RequestParams(); //fixed_params = {}

  /** required parameters, used for validating */
    protected String[] expected_param = new String[]{};

  /** the name of the server handler */
    protected String request_handler_name = null;


    /*
    #pylint: disable-msg=E1102
    xml_exception_handler = None
     */



  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of WMSRequest.
   *  @param param A dict, `NoCaseMultiDict` or ``RequestParams``.
   *  @param url The service URL for the request.
   *  @param validate True if the request should be validated after initialization.
   */

    public WMSRequest(Object param, String url, boolean validate) {
        super(param, url, validate);
        adapt_to_111();
    }


  //**************************************************************************
  //** copy - implemented to support WMSClient class
  //**************************************************************************
  /** Creates an exact copy of this class */
    public WMSRequest copy(){
        WMSRequest req = this.copy();
        req.fixed_params = fixed_params;
        req.expected_param = expected_param;
        req.request_handler_name = request_handler_name;
        adapt_to_111();
        return req;
    }


  //**************************************************************************
  //** copy_with_request_params - implemented to support WMSClient class
  //**************************************************************************
  /** Return a copy of this request and overwrite all param values from `req`.
   */
    public WMSRequest copy_with_request_params(WMSRequest req){
        RequestParams new_params = req.params.with_defaults(this.params);        
        return new WMSRequest(new_params, this.url, false);
    }

  //**************************************************************************
  //** url - implemented to support WMSClient class
  //**************************************************************************
    public void url(String url){
        this.url = url;
    }
    

    public void adapt_to_111(){
        //pass
    }
    

    public RequestParams adapt_params_to_version(){        
        
        RequestParams params = this.params.copy();

        java.util.Iterator<String> it = this.fixed_params.getKeys().iterator();
        while (it.hasNext()){
            String key = it.next();
            params.set(key, this.fixed_params.get(key, null), true);
        }

        if (!params.contains("styles")){
            params.set("styles", "", false);
        }
        return params;
        
    }

    
    public String query_string(){
        return this.adapt_params_to_version().query_string();
    }



}