package mapproxy.wms.request;
import mapproxy.core.request.*;

//******************************************************************************
//**  WMSMapRequestParams Class
//******************************************************************************
/**
 *   This class represents key-value parameters for WMS map requests. All values
 *   can be accessed as a property. Some properties return processed values.
 *   "size" returns a tuple of the width and height, "layers" returns an
 *   iterator of all layers, etc.
 *
 ******************************************************************************/

public class WMSMapRequestParams extends RequestParams {

    
  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of WMSMapRequestParams. */

    public WMSMapRequestParams(NoCaseMultiDict param) {
        super(param);
    }

    public WMSMapRequestParams() {
        this(null);
    }
 

  //**************************************************************************
  //** layers
  //**************************************************************************
  /** List with all layer names. */
    
    public String[] layers(){
        return this.params.get("layers", "").split(",");
    }


  //**************************************************************************
  //** _get_bbox
  //**************************************************************************
  /**  "bbox" as a tuple (minx, miny, maxx, maxy). */

    public double[] bbox(){
        String bbox = this.params.get("bbox");
        if (bbox==null) return null;

        String[] arr = bbox.split(",");
        double[] box = new double[4];
        for (int i=0; i<box.length; i++){
            box[i] = Double.valueOf(arr[i]);
        }
        return box;
    }



  //**************************************************************************
  //** _set_bbox
  //**************************************************************************

    public void bbox(double[] value){
        if (value==null) this.params.set("bbox", null);
        else this.params.set("bbox", value[0]+","+value[1]+","+value[2]+","+value[3]);
    }
    
    public void bbox(String value){
        this.params.set("bbox", value);
    }


  //**************************************************************************
  //** _get_size
  //**************************************************************************
  /** Size of the request in pixel as a tuple (width, height), or None if one
   *  is missing.
   */

    public int[] size(){
        if (params.get("height")==null || params.get("width")==null)
            return null;
        int width = Integer.valueOf(this.params.get("width"));
        int height = Integer.valueOf(this.params.get("height"));
        return new int[]{width, height};
    }


  //**************************************************************************
  //** _set_size
  //**************************************************************************
    
    public void size(int[] value){
        params.set("width", value[0]+"");
        params.set("height", value[1]+"");
    }


  //**************************************************************************
  //** srs
  //**************************************************************************

    public String srs(){
        return this.params.get("srs", null);
    }

  //**************************************************************************
  //** transparent
  //**************************************************************************
  /** ``True`` if transparent is set to true, otherwise ``False``. */

    public boolean transparent(){
        if (this.params.get("transparent", "false").toLowerCase().equals("true"))
            return true;
        return false;
    }

  //**************************************************************************
  //** bgcolor
  //**************************************************************************
  /** The background color in PIL format (#rrggbb). Defaults to '#ffffff'. */
    
    public String bgcolor(){
        String color = this.params.get("bgcolor", "0xffffff");
        return "#" + color.substring(2);
    }
    
  //**************************************************************************
  //** format
  //**************************************************************************
  /**  The requested format as string (w/o any 'image/', 'text/', etc prefixes)
   */
    public String format(){

        String[] arr = split_mime_type(this.params.get("format", ""));
        String _mime_class = arr[0];
        String format = arr[1];
        String options = arr[2];


        /* //Disabling routine - no such thing as png8 in java...
        if (format.equalsIgnoreCase("png") && (options.equalsIgnoreCase("mode=8bit") || !this.transparent())){
            format = "png8";
        }
        */
        
        return format;
    }

    
    public String format_mime_type(){
        return this.params.get("format");
    }



}