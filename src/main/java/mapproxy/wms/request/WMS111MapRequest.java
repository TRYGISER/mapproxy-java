package mapproxy.wms.request;

//******************************************************************************
//**  WMS111MapRequest Class
//******************************************************************************
/**
 *   Used to represent a WMS 1.1.1 Request
 *
 ******************************************************************************/

public class WMS111MapRequest extends WMSMapRequest {

    

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of WMS111MapRequest.
   *
   *  @param param RequestParams (e.g. WMSMapRequestParams)
   *  @param url The url to the WMS minus the typical WMS parameters (bbox, srs,
   *  width, height, service, request, version, etc).
   *  @param validate Specify whether to validate the WMS request. Set to false
   *  by default.
   */
    public WMS111MapRequest(Object param, String url, boolean validate) {


        super(param, url, validate);        

        /*
        fixed_params.set("request", "GetMap");
        fixed_params.set("version", "1.1.1");
        fixed_params.set("service", "WMS");
        */

        params.set("request", "GetMap");
        params.set("version", "1.1.1");
        params.set("service", "WMS");

    }
    


    public WMS111MapRequest(Object param, String url) {
        this(param, url, false);
    }


    public void adapt_to_111(){
        //del self.params['wmtver']
    }

}