package mapproxy.wms.layer;
import mapproxy.core.cache.*;
import mapproxy.core.SRS;
import mapproxy.wms.request.WMSRequest;
import mapproxy.wms.request.WMSMapRequest;
import mapproxy.wms.request.WMSMapRequestParams;
import mapproxy.core.Generator;

//******************************************************************************
//**  WMSCacheLayer Class
//******************************************************************************
/**
 *   This is a layer that caches the data.
 *
 ******************************************************************************/

public class WMSCacheLayer extends WMSLayer {

    private Cache cache;
    private FeatureInfoSource fi_source;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of WMSCacheLayer.   */

    public WMSCacheLayer(Cache cache, FeatureInfoSource fi_source){

        super(null);
        this.cache = cache;
        this.fi_source = fi_source;        
    }


    @Override
    public double[] _bbox(){
        return this.cache.grid.bbox; //self.cache.grid.bbox
    }

    @Override
    public SRS _srs(){
        return this.cache.grid.srs;
    }

    @Override
    public boolean has_info(){
        return (this.fi_source!=null);
    }


    @Override
  //**************************************************************************
  //** info
  //**************************************************************************

    public Generator<String> info(WMSRequest request){
        return this.fi_source.info(request);
    }


    @Override
  //**************************************************************************
  //** caches
  //**************************************************************************

    public Cache[] caches(WMSRequest _request){
        return new Cache[]{this.cache};
    }



    @Override
  //**************************************************************************
  //** render
  //**************************************************************************
  /** Render the request.
   *  @param map_request the map request to render
   */
    public javaxt.io.Image render(WMSMapRequest map_request){

        WMSMapRequestParams params = map_request.params;
        double[] req_bbox = params.bbox();
        int[] size = params.size();
        SRS req_srs = new SRS(params.srs());


        try{
            return this.cache.image(req_bbox, req_srs, size);
        }
        catch(Exception e){
            /*
            except TooManyTilesError:
                raise RequestError('Request too large or invalid BBOX.', request=map_request)
            except TransformationError:
                raise RequestError('Could not transform BBOX: Invalid result.',
                    request=map_request)
            except TileCacheError, e:
                log.error(e)
                raise RequestError(e.message, request=map_request)
             *
             */
            return null;
        }

    }

}
