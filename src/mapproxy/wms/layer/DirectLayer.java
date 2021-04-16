package mapproxy.wms.layer;
import mapproxy.core.SRS;
import mapproxy.wms.request.WMSRequest;
import mapproxy.wms.request.WMSMapRequest;
import mapproxy.wms.client.*;


//******************************************************************************
//**  DirectLayer Class
//******************************************************************************
/**
 *   A layer that passes the request to a wms.
 *
 ******************************************************************************/

public class DirectLayer extends WMSLayer{

    private WMSClient wms;
    private boolean queryable;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of DirectLayer.   */

    public DirectLayer(WMSClient wms, boolean queryable){
        super(null);//WMSLayer.__init__(self, {})
        this.wms = wms;
        this.queryable = queryable;
    }

    @Override
    public double[] _bbox(){
        return null;
    }
    

    @Override
    public SRS _srs(){
        String srs = this.wms.request_template.params.get("srs", null);
        if (srs !=null)
            return new SRS(srs);
        return null;
    }

    @Override
  //**************************************************************************
  //** render
  //**************************************************************************
    public javaxt.io.Image render(WMSMapRequest request){
        try{
            return this.wms.get_map(request);
        }
        catch(Exception e){
            /*
            log.warn('unable to get map for direct layer: %r', ex);
            raise RequestError('unable to get map for layers: %s' %
                               ','.join(request.params.layers), request=request);

             */
            return null;
        }
    }

    
    @Override
    public boolean has_info(){
        return this.queryable;
    }

    @Override
    public String info(WMSRequest request){
        return this.wms.get_info(request);
    }


}