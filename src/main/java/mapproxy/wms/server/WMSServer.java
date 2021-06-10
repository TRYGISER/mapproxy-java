package mapproxy.wms.server;
//import mapproxy.core.Server;
import mapproxy.core.image.*;
import mapproxy.core.layer.*;
import mapproxy.core.request.NoCaseMultiDict;
import mapproxy.wms.request.WMSRequest;
import mapproxy.wms.request.WMSMapRequest;
import mapproxy.wms.request.WMSMapRequestParams;

//******************************************************************************
//**  WMSServer Class
//******************************************************************************
/**
 *   Enter class description here
 *
 ******************************************************************************/

public class WMSServer { //extends Server


    //names = ("service",)
    private String[] request_methods = new String[]{"map", "capabilities", "featureinfo"};
    private LayerMerger merger;
    private java.util.HashMap<String, Layer> layers;
    private NoCaseMultiDict md;
    private WMSRequest request_parser;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of WMSServer. 
   *  @param md proxy_conf.service_md
   *  @param request_parser request_parser or wms_request
   */
    public WMSServer(java.util.HashMap<String, Layer> layers, NoCaseMultiDict md, LayerMerger layer_merger, WMSRequest request_parser){
        //Server.__init__(self)
        //this.request_parser = request_parser or wms_request

        this.layers = layers;
        if (layer_merger==null) layer_merger = new LayerMerger();
            
        this.merger = layer_merger;
        this.md = md;

    }


  //**************************************************************************
  //** map
  //**************************************************************************

    public javaxt.io.Image map(WMSMapRequest map_request){

        //merger = this.merger();
        this.check_request(map_request);


        for (String layer: map_request.params.layers()){
            merger.add(this.layers.get(layer).render(map_request));
        }

        WMSMapRequestParams params = map_request.params;
        javaxt.io.Image result = merger.merge(params.format(), params.size(),
                params.bgcolor(), params.transparent());

        return result;
        //return Response(result.getByteArray(params.format), params.format_mime_type);
    }



    public javaxt.io.Image capabilities(WMSMapRequest map_request){
        /*
        if (map_request.params.contains("__debug__"))//'__debug__' in map_request.params:
            layers = this.layers.values();
        else:
            layers = [layer for name, layer in self.layers.iteritems()
                      if name != '__debug__']

        service = this._service_md(map_request);
        result = Capabilities(service, layers).render(map_request);
        return Response(result, mimetype=map_request.mime_type);
        */
        return null;
    }

    public String featureinfo(WMSMapRequest request){
        /*
        infos = []
        this.check_request(request);
        for (Layer layer : request.params.query_layers){
            if not self.layers[layer].has_info():
                raise RequestError('layer %s is not queryable' % layer, request=request)
            info = this.layers[layer].info(request);
            if info is None:
                continue
            if isinstance(info, basestring):
                infos.append(info)
            else:
                [infos.append(i) for i in info if i is not None];
        }
        return Response('\n'.join(infos), mimetype='text/plain');
        */
        return null;
    }






  //**************************************************************************
  //** check_request
  //**************************************************************************
  /** Check whether there are query layers in the request. Throw an error if the
   *  layer is not found.
   */
    public void check_request(WMSMapRequest request){
        /*
        query_layers = request.params.query_layers if hasattr(request, 'query_layers') else []
        for layer in chain(request.params.layers, query_layers):
            if layer not in self.layers:
                raise RequestError('unknown layer: ' + str(layer), code='LayerNotDefined',
                                   request=request)

        */
    }

    public NoCaseMultiDict _service_md(WMSMapRequest map_request){
        md = new NoCaseMultiDict(md);// dict(self.md)
        md.set("url", map_request.url());
        return md;
    }
}