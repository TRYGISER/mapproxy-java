package mapproxy.arcgis.client;
import mapproxy.wms.request.*;

//******************************************************************************
//**  ArcGIS MapServer Client
//******************************************************************************
/**
 *   Used to execute an ArcGIS MapServer export image requests. 
 *
 *   Loosely based on the WMSClient class.
 *
 ******************************************************************************/

public class MapClient {

    private String url;
    private int[] size;
    private double[] bbox;
    private String[] layers;
    private String srs;



  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of ArcServerClient.   */

    public MapClient(WMSMapRequest request_template){
        parseWMSMapRequest(request_template);
    }


    private void parseWMSMapRequest(WMSMapRequest request){
        bbox = request.params.bbox();
        layers = request.params.layers();
        url = request.url();
        if (url.endsWith("?")) url = url.substring(0, url.length()-1);
        if (!url.endsWith("/")) url += "/";
        srs = request.params.srs();
        size = request.params.size();
    }


  //**************************************************************************
  //** get_map
  //**************************************************************************
  /** Used to convert a WMS GetMap Request into a ArcGIS MapServer export image
   *  request.
   */
    public javaxt.io.Image get_map(WMSMapRequest request){
        parseWMSMapRequest(request);
        return get_map();
    }


    public javaxt.io.Image get_map(double[] bbox, int[] size){
        this.bbox = bbox;
        this.size = size;
        return get_map();
    }



  //**************************************************************************
  //** get_map
  //**************************************************************************
  /** Used to execute an ArcGIS MapServer export image request.
   */
    private javaxt.io.Image get_map(){

        //MapServer/export?LAYERS=0%2C1%2C2&FORMAT=png&BBOX=-109.6875%2C28.125%2C-106.875%2C30.9375&SIZE=256%2C256&F=image&BBOXSR=4326&IMAGESR=4326
        //MapServer/export?f=image&bboxsr=4326&bbox={"xmin":-136.4518828300507,"ymin":23.481884805139213,"xmax":-63.82997012850966,"ymax":56.08549514181948,"spatialReference":{"wkid":4326}}&dpi=96&format=png8&imagesr=4326&transparent=true&size=1381,620



        String srs = this.srs.split(":")[1];

        StringBuffer url = new StringBuffer();
        url.append(this.url);
        url.append("export?f=image");
        url.append("&BBOX=" + bbox[0] + "%2C" + bbox[1] + "%2C" + bbox[2] + "%2C" + bbox[3]);
        url.append("&FORMAT=PNG24");
        url.append("&SIZE=" + size[0] + "%2C" + size[1]);
        url.append("&BBOXSR=" + srs + "&IMAGESR=" + srs);
        url.append("&transparent=true");


        javaxt.io.Image image = new javaxt.http.Request(url.toString()).getResponse().getImage();
        if (image.getBufferedImage()!=null){
            return image;
        }
        else{
            return null;
        }
    }

}
