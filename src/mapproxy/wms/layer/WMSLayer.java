package mapproxy.wms.layer;
import mapproxy.core.layer.*;
import mapproxy.wms.request.WMSRequest;
import mapproxy.core.cache.Cache;
import mapproxy.core.Generator;

//******************************************************************************
//**  Layer Class
//******************************************************************************
/**
 *   Base class for all renderable layers.
 *
 ******************************************************************************/

public class WMSLayer extends Layer {

    private LayerMetaData md;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of Layer.   */

    public WMSLayer(LayerMetaData md){
        if (md==null)
            this.md = new LayerMetaData();
        this.md = md;
    }

    public Object info(WMSRequest request){
        //raise RequestError('layer %s is not queryable' % self.md.name, request=request) 
        return null;
    }

    public boolean has_info(){
        return false;
    }

    public Cache[] caches(WMSRequest _request){
        return new Cache[]{};
    }



}
