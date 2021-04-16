package mapproxy.wms.request;

//******************************************************************************
//**  WMSMapRequest Class
//******************************************************************************
/**
 *   Base class for all WMS GetMap requests.
 *
 ******************************************************************************/

public class WMSMapRequest extends WMSRequest {


    //#pylint: disable-msg=E1102
    //xml_exception_handler = None

    public WMSMapRequestParams params;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of WMSMapRequest. */


    public WMSMapRequest(Object param, String url, boolean validate) { 

        super(param, url, validate);


      //Update params variable
        params = new WMSMapRequestParams(super.params.params);

        //request_params = WMSMapRequestParams.class;
        request_handler_name = "map";

        /*
        fixed_params.set("request", "GetMap");
        fixed_params.set("service", "WMS");
        */
        params.set("request", "GetMap");
        params.set("service", "WMS");


        expected_param = new String[]{"version", "request", "layers", "styles",
                                    "srs", "bbox", "width", "height", "format"};
        
    }

    public WMSMapRequest(){
        this(null, null, false);
    }



    public void validate(){

        for (String param : this.expected_param){
            //if (param)
        }

        /*
        missing_param = []
        for param in self.expected_param:
            if param not in self.params:
                missing_param.append(param)

        if (missing_param){
            if 'format' in missing_param:
                self.params['format'] = 'image/png'
            raise RequestError('missing parameters ' + str(missing_param),
                               request=self)
        }

        this.validate_bbox();
        this.validate_format();
        this.validate_srs();
        this.validate_styles();
        */
    }

}