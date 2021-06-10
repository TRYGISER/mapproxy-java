package mapproxy.wms.layer;
import mapproxy.core.layer.LayerMetaData;
import mapproxy.core.SRS;
import mapproxy.core.cache.Cache;
import mapproxy.wms.request.WMSRequest;
import mapproxy.wms.request.WMSMapRequest;
import mapproxy.core.Generator;

//******************************************************************************
//**  VLayer Class
//******************************************************************************
/**
 *   A layer with multiple sources.
 *
 ******************************************************************************/

public class VLayer extends WMSLayer {

    protected WMSLayer[] sources;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of VLayer.
   *  @param md the layer metadata
   *  @param sources a list with layers. Type = [`WMSLayer`]
   */
    public VLayer(LayerMetaData md, WMSLayer[] sources){
        super(md);
        this.sources = sources;
    }


    @Override
  //**************************************************************************
  //** _bbox
  //**************************************************************************
    protected double[] _bbox(){
        return this.sources[0].bbox();
    }

    
    @Override
  //**************************************************************************
  //** _srs
  //**************************************************************************
    protected SRS _srs(){
        return this.sources[0].srs();
    }

    @Override
  //**************************************************************************
  //** render
  //**************************************************************************
    public Generator<javaxt.io.Image> render(WMSMapRequest request){

        final WMSMapRequest r = request;
        Generator<javaxt.io.Image> iterator = new Generator<javaxt.io.Image>() {

            @Override
            public void run() {
                WMSMapRequest request = r;
                WMSLayer[] sources = getSources();
                for (WMSLayer source : sources) {
                    //yield (source.render(request));
                    Object img = source.render(request);
                    if (img == null || img instanceof javaxt.io.Image){
                        yield ((javaxt.io.Image) img);
                    }
                    else{
                        Generator<javaxt.io.Image> it = (Generator<javaxt.io.Image>) img;
                        for (javaxt.io.Image i : it)
                            yield (i);
                    }
                }
            }
        };

        return iterator;
        
    }

    private WMSLayer[] getSources(){
        return this.sources;
    }


    @Override
  //**************************************************************************
  //** caches
  //**************************************************************************
    public Cache[] caches(WMSRequest request){
        java.util.Vector<Cache> list = new java.util.Vector<Cache>();
        for (WMSLayer source : this.sources){
            Cache[] cache = source.caches(request);
            for (int i=0; i<cache.length; i++){
                list.add(cache[i]);
            }
        }
        Cache[] result = new Cache[list.size()];
        for (int i=0; i<list.size(); i++){
            result[i] = list.get(i);
        }
        return result;
    }


    @Override
  //**************************************************************************
  //** has_info
  //**************************************************************************
    public boolean has_info(){
        //return any(source.has_info() for source in self.sources);
        for (WMSLayer source : this.sources)
            if (source.has_info())
                return true;
        return false;
    }


    @Override
  //**************************************************************************
  //** info
  //**************************************************************************
    public Generator<String> info(WMSRequest request){

        final WMSRequest r = request;
        Generator<String> iterator = new Generator<String>() {

            @Override
            public void run() {
                WMSRequest request = r;
                WMSLayer[] sources = getSources();
                for (WMSLayer source : sources) {
                    //yield (source.render(request));
                    Object info = source.info(request);
                    if (info == null || info instanceof String){
                        yield ((String) info);
                    }
                    else{
                        Generator<String> it = (Generator<String>) info;
                        for (String i : it)
                            yield (i);
                    }
                }
            }
        };

        return iterator;

    }

}
