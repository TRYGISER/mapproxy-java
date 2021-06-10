package mapproxy.core.grid;

//******************************************************************************
//**  MetaTile Class
//******************************************************************************
/**
 *    Contains the ``tile_coord`` and the upper-left
 *   pixel coordinate of the tile in a meta tile image.
 *
 ******************************************************************************/

public class TileCoordinate {


    public int[] tile_coord;
    public int[] crop_coord;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of MetaTile.   */

    protected TileCoordinate(int[] tile_coord, int[] ul_pixel_coord){
        this.tile_coord = tile_coord;
        this.crop_coord = ul_pixel_coord;
    }

}
