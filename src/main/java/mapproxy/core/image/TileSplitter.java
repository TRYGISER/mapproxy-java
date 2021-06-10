package mapproxy.core.image;

//******************************************************************************
//**  TileSplitter Class
//******************************************************************************
/**
 *   Splits a large image into multiple tiles.
 *
 ******************************************************************************/

public class TileSplitter {


    private String format;
    private javaxt.io.Image meta_img;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of TileSplitter.   */

    public TileSplitter(javaxt.io.Image meta_tile, String format){

        this.meta_img = meta_tile;
        //if this.meta_img.mode == 'P' and format in ('png', 'gif'):
        //    this.meta_img = self.meta_img.convert('RGBA')
        this.format = format;

    }



  //**************************************************************************
  //** get_tile
  //**************************************************************************
  /** Return the cropped tile.
   *  @param crop_coord the upper left pixel coord to start
   *  @param tile_size width and height of the new tile
   *  @return 'ImageSource'
   */
    public javaxt.io.Image get_tile(int[] crop_coord, int[] tile_size){


        //minx, miny = crop_coord
        int minx = crop_coord[0];
        int miny = crop_coord[1];

        /*
        int maxx = minx + tile_size[0];
        int maxy = miny + tile_size[1];
        */

        //System.out.println(meta_img.getWidth() + "," + meta_img.getHeight());
        //System.out.println("get_tile: " + minx + "," + miny + " " + tile_size[0] + "," + tile_size[1]);
        //this.meta_img.saveAs("/temp/meta_img_" + new java.util.Date().getTime() + ".png");

        
        try{
            javaxt.io.Image meta_img = new javaxt.io.Image(this.meta_img.getBufferedImage());
            meta_img.crop(minx, miny, tile_size[0], tile_size[1]);
            return meta_img;
        }
        catch(Exception e){
            //e.printStackTrace();
            return null; //new javaxt.io.Image(tile_size[0], tile_size[1]);
        }



        //crop = this.meta_img.crop((minx, miny, maxx, maxy))
        //return ImageSource(crop, this.format);
    }

}
