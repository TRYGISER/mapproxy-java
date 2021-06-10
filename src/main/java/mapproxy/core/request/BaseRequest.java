package mapproxy.core.request;

//******************************************************************************
//**  BaseRequest Class
//******************************************************************************
/**
 *   This class represents a request with a URL and key-value parameters.
 *
 ******************************************************************************/

public class BaseRequest {


    private String delimiter = ",";
    protected String url;
    public RequestParams params;
    protected boolean validate;
    
  /** the ``RequestParams`` class for this request */
    //protected Object request_params = RequestParams.class;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of BaseRequest.
   *  @param param A dict, `NoCaseMultiDict` or ``RequestParams``.
   *  @param url The service URL for the request.
   *  @param validate True if the request should be validated after initialization.
   */
    public BaseRequest(Object param, String url, boolean validate) {

        if (param == null){
            this.params = new RequestParams(new NoCaseMultiDict()); //self.params = self.request_params(NoCaseMultiDict())
        }
        else{
            if (param instanceof RequestParams){
                this.params = (RequestParams) param; //self.params = self.request_params(param.params)
            }
            else{
                //self.params = self.request_params(NoCaseMultiDict(param))
            }
        }

        this.url = url;
        this.validate = validate;

        if (validate) validate();
    }


  //**************************************************************************
  //** copy
  //**************************************************************************
    
    public BaseRequest copy(){
        return new BaseRequest(params, url, validate);
    }

    public String url(){
        return url;
    }


  //**************************************************************************
  //** query_string
  //**************************************************************************

    public String query_string(){
        return this.params.query_string();
    }


  //**************************************************************************
  //** complete_url
  //**************************************************************************
  /** The complete MapRequest as URL. */
    
    public String complete_url(){

        if (url==null) //not self.url:
            return this.query_string();

        String delimiter = "?";
        if (this.url.contains("?")) delimiter = "&";
        if (this.url.endsWith("?")) delimiter = "";

        System.out.println(this.url + delimiter + this.query_string());
        return this.url + delimiter + this.query_string();
    }


  //**************************************************************************
  //** copy_with_request_params
  //**************************************************************************
  /** Return a copy of this request and overwrite all param values from `req`.
      Use this method for templates
        (``req_template.copy_with_request_params(actual_values)``).
   */
    public BaseRequest copy_with_request_params(BaseRequest req){
        RequestParams new_params = req.params.with_defaults(this.params);
        return new BaseRequest(new_params, this.url, false);
    }



    public void validate(){
    }
    

}