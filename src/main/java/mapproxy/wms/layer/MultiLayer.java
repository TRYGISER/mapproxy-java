package mapproxy.wms.layer;
import mapproxy.core.SRS;
import mapproxy.core.layer.LayerMetaData;
import mapproxy.wms.request.WMSMapRequest;
import mapproxy.wms.request.WMSRequest;
import mapproxy.core.cache.Cache;

//******************************************************************************
//**  MultiLayer Class
//******************************************************************************
/**
 *   This layer dispatches requests to other layers.
 *
 ******************************************************************************/

public class MultiLayer extends WMSLayer {

    private WMSLayer[] layers;
    //private SRSDispatcher dispatcher;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of MultiLayer.   */

    public MultiLayer(WMSLayer[] layers, LayerMetaData md){ //SRSDispatcher dispatcher

        super(null);
        this.layers = layers;
        /*
        if dispatcher is None:
            dispatcher = srs_dispatcher
        this.dispatcher = dispatcher;
        */
    }



    public static WMSLayer dispatcher(WMSLayer[] layers, String srs){
        boolean latlong = new SRS(srs).is_latlong();
        for (WMSLayer layer : layers){
            if (layer.srs().is_latlong() == latlong)
                return layer;
        }
        return layers[0];
    }

    @Override
    public double[] _bbox(){
        return this.layers[0].bbox();
    }


    @Override
    public SRS _srs(){
        return this.layers[0].srs();
    }


    @Override
  //**************************************************************************
  //** render
  //**************************************************************************
    public Object render(WMSMapRequest map_request){
        String srs = map_request.params.srs();
        WMSLayer layer = this.dispatcher(this.layers, srs);
        return layer.render(map_request);
    }

    @Override
    public Cache[] caches(WMSRequest request){
        WMSLayer layer = this.dispatcher(this.layers, request.params.get("srs", null));
        return layer.caches(request);
    }

    @Override
    public boolean has_info(){
        return this.layers[0].has_info();
    }

    @Override
    public Object info(WMSRequest request){
        String srs = request.params.get("srs", null);
        WMSLayer layer = this.dispatcher(this.layers, srs);
        return layer.info(request);
    }


}