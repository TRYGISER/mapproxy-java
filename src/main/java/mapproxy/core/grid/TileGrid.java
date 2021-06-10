package mapproxy.core.grid;
import mapproxy.core.SRS;
import mapproxy.core.Python;

//******************************************************************************
//**  TileGrid Class
//******************************************************************************
/**
 *   This class represents a regular tile grid. The first level (0) contains a
 *   single tile, the origin is bottom-left.
  <pre>
    >>> grid = TileGrid(epsg=900913)
    >>> [round(x, 2) for x in grid.bbox]
    [-20037508.34, -20037508.34, 20037508.34, 20037508.34]
  </pre>
 *
 ******************************************************************************/

public class TileGrid extends Grid {

    //private SRS srs;
    private double spheroid_a = 6378137.0; //# for 900913
    //private int epsg = 900913;
    //private double[] bbox = null;
    public int[] tile_size = new int[]{256, 256};
    private double[] res = null;
    private double[] resolutions = null;
    private boolean is_geodetic = false;
    public int levels; //<-- flipped from private to public for the TileServiceGrid Class
    public String res_type; //<-- flipped from private to public for the TileServiceGrid Class
    public java.util.List<int[]> grid_sizes; //<-- flipped from protected to public for the TileServiceGrid Class
    private double stretch_factor = 1.15;



    public static String RES_TYPE_SQRT2 = "sqrt2";
    public static String RES_TYPE_GLOBAL = "global";
    public static String RES_TYPE_CUSTOM = "custom";




  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of TileGrid.
   * 
   *  @param res_type the type of the multi-resolution pyramid. res_type:
   *    "RES_TYPE_CUSTOM", "RES_TYPE_GLOBAL", "RES_TYPE_SQRT2"
   *  @param levels the number of levels
   *  @param tile_size the size of each tile in pixel [int(with), int(height)]
   *  @param bbox the bbox of the grid, tiles may overlap this bbox
   */
    public TileGrid(int epsg, double[] bbox, int[] tile_size, String res,
                 boolean is_geodetic, Integer levels){

        this.srs = new SRS(epsg); //SRSCache.getSRS(epsg);
        this.tile_size = tile_size;
        this.is_geodetic = is_geodetic;

        //this.stretch_factor = base_config().image.stretch_factor;
        //'allow images to be scaled by this factor before the next level will be selected'

        if (levels==null)
            this.levels = 20;
        else
            this.levels = levels;

        this.res_type = RES_TYPE_CUSTOM;
        if (bbox==null && res==null && (tile_size[0] == 256 && tile_size[1] == 256)){
            this.res_type = RES_TYPE_GLOBAL;
        }


        if (bbox==null){
            bbox = this._calc_bbox();
        }
        this.bbox = bbox;


      //Compute res levels
        if (res==null){
            this.res = this._calc_res(null);
        }
        else if (res.equalsIgnoreCase("sqrt2")){
            this.res_type = RES_TYPE_SQRT2;
            if (levels==null)
                this.levels = 40;
            this.res = this._calc_res(java.lang.Math.sqrt(2));
        }
        else if (is_float(res)){
            this.res = this._calc_res(cdbl(res));
        }


        this.levels = this.res.length;
        this.resolutions = this.res; //<--this seems redundant...

        this.grid_sizes = this._calc_grids();

    }



    public TileGrid(int epsg, double[] bbox, int[] tile_size, String res){
        this(epsg, bbox, tile_size, res, true, null);
    }

    public TileGrid(int epsg, double[] bbox, int[] tile_size, String res, boolean is_geodetic){
        this(epsg, bbox, tile_size, res, is_geodetic, null);
    }

    public TileGrid(int epsg, int[] tile_size){
        this(epsg, null, tile_size, null, true, null);
    }


  //**************************************************************************
  //** _calc_grids
  //**************************************************************************
    private java.util.List<int[]> _calc_grids(){
        double width = this.bbox[2] - this.bbox[0];
        double height = this.bbox[3] - this.bbox[1];
        java.util.List<int[]> grids = new java.util.Vector<int[]>();
        for (double res : this.resolutions){
            double x = java.lang.Math.ceil(width / res / this.tile_size[0]);
            double y = java.lang.Math.ceil(height / res / this.tile_size[1]);
            grids.add(new int[]{cint(x), cint(y)});
        }
        return grids;
    }



  //**************************************************************************
  //** _calc_bbox
  //**************************************************************************
    private double[] _calc_bbox(){
        if (this.is_geodetic)
            return new double[]{-180.0, -90.0, 180.0, 90.0};
        else{
            double circum = 2 * java.lang.Math.PI * this.spheroid_a;
            double offset = circum / 2.0;
            return new double[]{-offset, -offset, offset, offset};
        }
    }


  //**************************************************************************
  //** _calc_res
  //**************************************************************************
    private double[] _calc_res(Double factor){
        double width = bbox[2] - bbox[0];
        double height = bbox[3] - bbox[1];
        double initial_res = java.lang.Math.max(width/this.tile_size[0], height/this.tile_size[1]);
        return pyramid_res_level(initial_res, factor, (Integer) this.levels);
    }


  //**************************************************************************
  //** resolution
  //**************************************************************************
  /** Returns the resolution of the `level` in units/pixel.
   *  @param level the zoom level index (zero is top)
   *
    <pre>
        >>> grid = TileGrid(epsg=900913)
        >>> grid.resolution(0)
        156543.03392804097
        >>> grid.resolution(1)
        78271.516964020484
        >>> grid.resolution(4)
        9783.9396205025605
    </pre>
   */
    public double resolution(int level){
        return this.res[level];
    }


  //**************************************************************************
  //** closest_level
  //**************************************************************************
  /**  Returns the level index that offers the required resolution.
   *  @param res the required resolution
   *  @return the level with the requested or higher resolution
   <pre>
        >>> grid = TileGrid(epsg=900913)
        >>> grid.stretch_factor = 1.1
        >>> l1_res = grid.resolution(1)
        >>> [grid.closest_level(x) for x in (320000.0, 160000.0, l1_res+50, l1_res, \
                                             l1_res-50, l1_res*0.91, l1_res*0.89, 8000.0)]
        [0, 0, 1, 1, 1, 1, 2, 5]
   </pre>
   */
    public int closest_level(double res){
        int level = 0;
        for (double l_res : this.resolutions){ //for level, l_res in enumerate(this.res):
            if (l_res <= res*this.stretch_factor){
                return level;
            }
            level++;
        }
        level = level-1;
        return level;
        /*
        for level, l_res in enumerate(self.resolutions):
            if l_res <= res*self.stretch_factor:
                return level
        return level
         */
    }

    
  //**************************************************************************
  //** tile
  //**************************************************************************
  /** Returns the tile id for the given point.

        >>> grid = TileGrid(epsg=900913)
        >>> grid.tile(1000, 1000, 0)
        (0, 0, 0)
        >>> grid.tile(1000, 1000, 1)
        (1, 1, 1)
        >>> grid = TileGrid(epsg=900913, tile_size=(512, 512))
        >>> grid.tile(1000, 1000, 2)
        (2, 2, 2)
   */
    public int[] tile(double x, double y, int level){
        double res = this.resolution(level);
        x = x - this.bbox[0];
        y = y - this.bbox[1];
        double tile_x = x/(res*this.tile_size[0]);
        double tile_y = y/(res*this.tile_size[1]);
        return new int[]{cint(Math.floor(tile_x)), cint(Math.floor(tile_y)), level};
    }


  //**************************************************************************
  //** flip_tile_coord
  //**************************************************************************
  /** Flip the tile coord on the y-axis. (Switch between bottom-left and top-
   *  left origin.)

        >>> grid = TileGrid(epsg=900913)
        >>> grid.flip_tile_coord((0, 1, 1))
        (0, 0, 1)
        >>> grid.flip_tile_coord((1, 3, 2))
        (1, 0, 2)
   */
    public int[] flip_tile_coord(int[] coord){ //self, (x, y, z)
        int x = coord[0];
        int y = coord[1];
        int z = coord[2];
        return new int[]{x, this.grid_sizes.get(z)[1]-1-y, z};
    }


  //**************************************************************************
  //** get_affected_tiles
  //**************************************************************************
  /** Get a list with all affected tiles for a bbox and output size.
   *  @return An array containing the bbox, the size, and a list with tile
   *  coordinates, sorted row-wise. rtype: ``bbox, (xs, yz), [(x, y, z), ...]``
   <pre>
        >>> grid = TileGrid()
        >>> bbox = (-20037508.34, -20037508.34, 20037508.34, 20037508.34)
        >>> tile_size = (256, 256)
        >>> grid.get_affected_tiles(bbox, tile_size)
        ... #doctest: +NORMALIZE_WHITESPACE +ELLIPSIS
        ((-20037508.342789244, -20037508.342789244,\
          20037508.342789244, 20037508.342789244), (1, 1),\
          <generator object at ...>)
   </pre>
   */
    public Object[] get_affected_tiles(double[] bbox, int[] size, SRS req_srs, boolean inverse){ //req_srs=None, inverse=False
        double[] src_bbox;
        if (req_srs!=null && !req_srs.equals(this.srs)) //if req_srs and req_srs != self.srs:
            src_bbox = req_srs.transform_bbox_to(this.srs, bbox);
        else
            src_bbox = bbox;

        
        double res = get_resolution(src_bbox, size);
        int level = this.closest_level(res);
        // remove 1/10 of a pixel so we don't get a tiles we only touch
        double x_delta = (src_bbox[2]-src_bbox[0]) / size[0] / 10.0;
        double y_delta = (src_bbox[3]-src_bbox[1]) / size[1] / 10.0;
        int[] t0 = this.tile(src_bbox[0]+x_delta, src_bbox[1]+y_delta, level);
        int[] t1 = this.tile(src_bbox[2]-x_delta, src_bbox[3]-y_delta, level);

        int x0 = t0[0];
        int y0 = t0[1];
        int x1 = t1[0];
        int y1 = t1[1];

        
        //System.err.println("BBOX: " + Python.cstr(src_bbox));
        //System.err.println("coords: " + Python.cstr(new double[]{x0, y0, x1, y1}));
        //System.err.println("res: " + res);
        

        int[] xs = Python.range(x0, x1+1);
        int[] ys;
        if (inverse){
            y0 = cint(this.grid_sizes.get(level)[1]) - 1 - y0;
            y1 = cint(this.grid_sizes.get(level)[1]) - 1 - y1;
            ys = Python.range(y1, y0+1);
        }
        else{
            ys = Python.range(y1, y0-1, -1);
        }
        //ll = (xs[0], ys[-1], level)
        //ur = (xs[-1], ys[0], level)
        int[] ll = new int[]{xs[0], ys[ys.length-1], level};
        int[] ur = new int[]{xs[xs.length-1], ys[0], level};
        if (inverse){
            ll = this.flip_tile_coord(ll);
            ur = this.flip_tile_coord(ur);
        }

        //System.err.println("ll: " + Python.cstr(ll));
        //System.err.println("ur: " + Python.cstr(ur));

        java.util.List<int[]> tiles = new java.util.LinkedList<int[]>();
        tiles.add(ll);
        tiles.add(ur);
        double[] abbox = this._get_bbox(tiles);
        //System.err.println("abbox: " + Python.cstr(abbox));
        return new Object[]{abbox, new int[]{xs.length, ys.length},
                _create_tile_list(xs, ys, level, this.grid_sizes.get(level))}; 
    }


  //**************************************************************************
  //** _get_bbox
  //**************************************************************************
  /** Returns the bbox of multiple tiles. The tiles should be ordered row-wise,
   *  bottom-up.
   *  @param tiles ordered list of tiles
   *  @return Returns the bbox of all tiles
   */
    public double[] _get_bbox(java.util.List<int[]> tiles){
        int[] ll = tiles.get(0);
        int[] ur = tiles.get(tiles.size()-1); //ur = tiles[-1]
        double[] pt0 = this._get_south_west_point(ll);
        double x0 = pt0[0];
        double y0 = pt0[1];
        double[] pt1 = this._get_south_west_point(new int[]{ur[0]+1, ur[1]+1, ur[2]});
        double x1 = pt1[0];
        double y1 = pt1[1];
        return new double[]{x0, y0, x1, y1};
    }


  //**************************************************************************
  //** _get_south_west_point
  //**************************************************************************
  /**  Returns the coordinate of the lower left corner.
   * 
        >>> grid = TileGrid(epsg=900913)
        >>> [round(x, 2) for x in grid._get_south_west_point((0, 0, 0))]
        [-20037508.34, -20037508.34]
        >>> [round(x, 2) for x in grid._get_south_west_point((1, 1, 1))]
        [0.0, 0.0]
   *
   * @param tile_coord the tile coordinate. Type tile_coord: ``(x, y, z)``
   */
    private double[] _get_south_west_point(int[] tile_coord){
        
        //x, y, z = tile_coord
        int x = tile_coord[0];
        int y = tile_coord[1];
        int z = tile_coord[2];

        double res = this.resolution(z);
        double x0 = this.bbox[0] + x * res * this.tile_size[0];
        double y0 = this.bbox[1] + y * res * this.tile_size[1];
        return new double[]{x0, y0};
    }




  //**************************************************************************
  //** tile_bbox
  //**************************************************************************
  /**  Returns the bbox of the given tile.

        >>> grid = TileGrid(epsg=900913)
        >>> [round(x, 2) for x in grid.tile_bbox((0, 0, 0))]
        [-20037508.34, -20037508.34, 20037508.34, 20037508.34]
        >>> [round(x, 2) for x in grid.tile_bbox((1, 1, 1))]
        [0.0, 0.0, 20037508.34, 20037508.34]
   */
    public double[] tile_bbox(int x, int y, int z){ //def tile_bbox(self, (x, y, z)):

        double[] pt = _get_south_west_point(new int[]{x, y, z});
        double x0 = pt[0];
        double y0 = pt[1];

        double res = this.resolution(z);
        double width = res * this.tile_size[0];
        double height = res * this.tile_size[1];
        return new double[]{x0, y0, x0+width, y0+height};
    }


  //**************************************************************************
  //** limit_tile
  //**************************************************************************
  /**  Check if the "tile_coord" is in the grid.
   *  @return the "tile_coord" if it is within the "grid", otherwise null.

        >>> grid = TileGrid(epsg=900913)
        >>> grid.limit_tile((-1, 0, 2)) == None
        True
        >>> grid.limit_tile((1, 2, 1)) == None
        True
        >>> grid.limit_tile((1, 2, 2))
        (1, 2, 2)
   */

    public int[] limit_tile(int[] tile_coord){

        //x, y, z = tile_coord
        int x = tile_coord[0];
        int y = tile_coord[1];
        int z = tile_coord[2];

        int[] grid = this.grid_sizes.get(z);
        //System.out.println(mapproxy.core.Python.cstr(tile_coord));
        //System.out.println(mapproxy.core.Python.cstr(grid));

        if (z < 0 || z >= this.levels)
            return null;
        if (x < 0 || y < 0 || x >= grid[0] || y >= grid[1])
            return null;
        return new int[]{x, y, z};
    }
    
}