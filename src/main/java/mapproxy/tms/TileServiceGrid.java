package mapproxy.tms;
import mapproxy.core.Python;
import mapproxy.core.grid.Grid;
import mapproxy.core.grid.TileGrid;

//******************************************************************************
//**  TileServiceGrid Class
//******************************************************************************
/**
 *   Wraps a "TileGrid" and adds some "TileService" specific methods.
 *
 ******************************************************************************/

public class TileServiceGrid { //extends TileGrid

    private boolean _skip_first_level = true;
    private boolean _skip_odd_level = false;
    private TileGrid grid;
    private String profile;
    private String srs_name;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of TileServiceGrid.   */

    public TileServiceGrid(TileGrid grid){
        this.grid = grid;
        if (this.grid.res_type.equals(TileGrid.RES_TYPE_GLOBAL) ||
            this.grid.res_type.equals(TileGrid.RES_TYPE_SQRT2)){
            if (this.grid.srs.equals(900913)){
                this.profile = "global-mercator";
                this.srs_name = "OSGEO:41001"; // as required by TMS 1.0.0
                this._skip_first_level = true;
            }
            else if (this.grid.srs.isGeographic()){
                this.profile = "global-geodetic";
                this.srs_name = this.grid.srs.toString(); //"EPSG:4326";
                this._skip_first_level = true;
            }
        }
        else{
            this.profile = "local";
            this.srs_name = this.grid.srs.toString();
            this._skip_first_level = false;
        }

        this._skip_odd_level = false;
        if (this.grid.res_type.equalsIgnoreCase(TileGrid.RES_TYPE_SQRT2))
            this._skip_odd_level = true;
    }


  //**************************************************************************
  //** internal_level
  //**************************************************************************
  /** returns the internal level   */

    public int internal_level(int level){

        if (this._skip_first_level){
            level += 1;
            if (this._skip_odd_level)
                level += 1;
        }
        if (this._skip_odd_level)
            level *= 2;
        return level;
    }

    
  //**************************************************************************
  //** bbox
  //**************************************************************************
  /**
   *   @return the bbox of all tiles of the first level
   */
    public double[] bbox(){
        int first_level = this.internal_level(0);
        int[] grid_size = this.grid.grid_sizes.get(first_level);
        java.util.List<int[]> tiles = new java.util.ArrayList<int[]>();
        tiles.add(new int[]{0, 0, first_level});
        tiles.add(new int[]{grid_size[0]-1, grid_size[1]-1, first_level});
        return this.grid._get_bbox(tiles);
    }
    


  //**************************************************************************
  //** tile_sets
  //**************************************************************************
  /**  Get all public tile sets for this layer. 
   *   @return the order and resolution of each tile set
   */
    public java.util.List<double[]> tile_sets(){

        java.util.List<double[]> tile_sets = new java.util.ArrayList<double[]>(); //tile_sets = []
        int num_levels = this.grid.levels;
        int start = 0;
        int step = 1;
        if (this._skip_first_level){
            if (this._skip_odd_level)
                start = 2;
            else
                start = 1;
        }
        if (this._skip_odd_level)
            step = 2;
        /*
        for order, level in enumerate(range(start, num_levels, step)):
            tile_sets.append((order, self.grid.resolutions[level]))
        */
        int order = 0;
        for (int level : (Python.range(start, num_levels, step)) ){
            tile_sets.add(new double[]{order, this.grid.resolution(level)});
        }

        return tile_sets;
    }


  //**************************************************************************
  //** internal_tile_coord
  //**************************************************************************
  /**  Converts public tile coords to internal tile coords.
   *   @param tile_coord the public tile coord
   *   @param use_profiles True if the tile service supports global
                             profiles (see `mapproxy.core.server.TileServer`)
   */
    public int[] internal_tile_coord(int[] tile_coord, boolean use_profiles){

        //x, y, z = tile_coord
        int x = tile_coord[0];
        int y = tile_coord[1];
        int z = tile_coord[2];

        if (z < 0)
            return null;


        if (use_profiles && this._skip_first_level)
            z += 1;

        if (this._skip_odd_level)
            z *= 2;

        //x = x+2;
        //y = y+1;
        //z = 4;
        //System.out.println("z: " + z);
        return this.grid.limit_tile(new int[]{x, y, z});
    }

}