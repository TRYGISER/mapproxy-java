package mapproxy.tms;

//******************************************************************************
//**  TileRequest Class
//******************************************************************************
/**
 *   Used to represent a TMS request. 
 *
 ******************************************************************************/

public class TileRequest {

    private String url;
    public String layer;
    public String format = "png";
    public int[] tile;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of TileRequest. Assumes the url ends with a tms
   *  request. Example: "/layer/z/x/y.format"
   */
    public TileRequest(String url){

        this.url = url;
        format = url.substring(url.lastIndexOf(".")+1);
        url = url.substring(0, url.lastIndexOf("."));
        int y = cint(url.substring(url.lastIndexOf("/")+1));
        url = url.substring(0, url.lastIndexOf("/"));
        int x = cint(url.substring(url.lastIndexOf("/")+1));
        url = url.substring(0, url.lastIndexOf("/"));
        int z = cint(url.substring(url.lastIndexOf("/")+1));
        url = url.substring(0, url.lastIndexOf("/"));
        tile = new int[]{x,y,z};
        layer = url.substring(url.lastIndexOf("/")+1);

        if (layer.contains("%")){
            try{
                layer = new java.net.URLDecoder().decode(layer, "UTF-8");
            }
            catch(Exception e){
              //Try to decode the string manually
                String find[] = new String[]{"%20","%2C","%2F","%3A"};
                String replace[] = new String[]{" ",",","/",":"};
                for (int i=0; i<find.length; i++){
                     layer = layer.replace(find[i],replace[i]);
                }
            }
        }


        //System.out.println("format: " + format);
        //System.out.println("layer: " + layer);
        //System.out.println("tile: " + mapproxy.core.Python.cstr(tile));
    }

    public String id(){
        int x = tile[0];
        int y = tile[1];
        int z = tile[2];
        return "/" + layer + "/" + z + "/" + x + "/" + y + "." + format;
    }

    private int cint(String s){
        return javaxt.utils.string.cint(s);
    }

    
    public String toString(){
        return this.url;
    }

}
