package mapproxy.wms.client;
import mapproxy.wms.request.*;

//******************************************************************************
//**  WMSClient
//******************************************************************************
/**
 *   Client for WMS requests.
 *
 ******************************************************************************/

public class WMSClient {

    public WMSMapRequest request_template;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of WMSClient. 
   * 
   * @param request_template a request that will be used as a template for new
   *    requests
   *
   * @param client_request function is called for each client request. gets the
   *    request_template and the according request. should return a new request
   *    object that is used for this request.
   */
    public WMSClient(WMSMapRequest request_template, Object client_request) {
        /*
        if client_request is None:
            client_request = wms_client_request
        self.client_request = client_request
        */
        this.request_template = request_template;
    }

    public WMSClient(WMSMapRequest request_template) {
        this(request_template, null);
    }

    
    public javaxt.io.Image get_map(WMSRequest request){

        //return new javaxt.http.Request(_map_url(request)).getResponse().getImage();
        javaxt.io.Image image = new javaxt.http.Request(_map_url(request)).getResponse().getImage();
        if (image.getBufferedImage()!=null){
            return image;
        }
        else{
            /*
            javaxt.utils.URL url = new javaxt.utils.URL(_map_url(request));
            int width = javaxt.utils.string.cint(url.getParameter("width"));
            int height = javaxt.utils.string.cint(url.getParameter("height"));
            return new javaxt.io.Image(width, height);
            */
            return null;
        }
        
    }

    private String _map_url(WMSRequest request){
        WMSRequest req = this.client_request(this.request_template, request);
        return req.complete_url();
    }

    public String get_info(WMSRequest request){
        return new javaxt.http.Request(_info_url(request)).getResponse().getText();
    }

  
    private String _info_url(WMSRequest request){
        WMSRequest req = this.client_request(this.request_template, request);

        if (this.request_template == null){
            return req.complete_url();
        }

        req.params.set("srs", request.params.get("srs", null)); //#restore requested srs
        this._transform_fi_request(req);
        req.params.set("query_layers", req.params.get("layers",null));
        return req.complete_url();
    }


  //**************************************************************************
  //** _transform_fi_request
  //**************************************************************************

    public WMSRequest _transform_fi_request(WMSRequest request){

        /*
        WMSMapRequestParams params = request.params;
        if (this.request_template.params.srs.equals(params.srs)){
            return request;
        }

        pos = params.pos;
        double[] req_bbox = params.bbox;
        req_pos = make_lin_transf((0, 0) + params.size, req_bbox)(pos);
        SRS req_srs = SRSCache.getSRS(params.srs);
        SRS dst_srs = SRSCache.getSRS(this.request_template.params.srs);
        dst_pos = req_srs.transform_to(dst_srs, req_pos);
        double[] dst_bbox = req_srs.transform_bbox_to(dst_srs, req_bbox);
        dst_pos = make_lin_transf((dst_bbox), (0, 0) + params.size)(dst_pos);

        params.srs = this.request_template.params.srs;
        params.bbox = dst_bbox;
        params.pos = dst_pos;
        */

        return request;
    }


    private WMSRequest wms_client_request(WMSRequest request_template, WMSRequest map_request){
        if (request_template==null){
            return map_request.copy();
        }


      //This port is a little wierd. Maybe WMSRequest should be a WMSMapRequest instead?
        WMSRequest req = request_template.copy_with_request_params(map_request);
        req.url(request_template.url()); //req.url = request_template.url
        req.params.set("bbox", map_request.params.get("bbox", null)); //req.params.bbox = map_request.params.bbox
        return req;
    }

    private WMSRequest client_request(WMSRequest request_template, WMSRequest map_request){
        return wms_client_request(request_template, map_request);
    }

}