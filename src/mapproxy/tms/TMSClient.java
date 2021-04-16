package mapproxy.tms;

//******************************************************************************
//**  TMSClient Class
//******************************************************************************
/**
 *   Enter class description here
 *
 ******************************************************************************/

public class TMSClient {

    public String url;
    private String format = "png";

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of TMSClient.   */

    public TMSClient(String url, String format){
        if (!url.endsWith("/")) url+="/";
        if (format.toLowerCase().startsWith("image/")) format = format.substring(6);
        this.url = url;
        this.format = format;
    }

    public javaxt.io.Image get_tile(int[] tile_coord){
        //x, y, z = tile_coord
        int x = tile_coord[0];
        int y = tile_coord[1];
        int z = tile_coord[2];
        return new javaxt.http.Request(url + z + "/" + x + "/" + y + "." + format).getResponse().getImage();
    }

    
}