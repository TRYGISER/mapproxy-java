package mapproxy.wms.layer;
import mapproxy.wms.client.WMSClient;
import mapproxy.wms.request.WMSRequest;
import mapproxy.core.Generator;

//******************************************************************************
//**  FeatureInfoSource Class
//******************************************************************************
/**
 *   Enter class description here
 *
 ******************************************************************************/

public class FeatureInfoSource {

    private WMSClient[] fi_sources;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of FeatureInfoSource.   */

    public FeatureInfoSource(WMSClient[] fi_sources){
        this.fi_sources = fi_sources;
    }
    
    public Generator<String> info(WMSRequest request){

        final WMSRequest r = request;
        Generator<String> iterator = new Generator<String>() {

            @Override
            public void run() {
                WMSRequest request = r;
                WMSClient[] fi_sources = getSources();
                for (WMSClient fi_source : fi_sources){
                    yield(fi_source.get_info(request));
                }
            }
        };

        return iterator;

    }

    private WMSClient[] getSources(){
        return this.fi_sources;
    }

}