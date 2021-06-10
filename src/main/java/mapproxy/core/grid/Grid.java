package mapproxy.core.grid;
import mapproxy.core.Generator;
import mapproxy.core.Python;

//******************************************************************************
//**  Grid Class
//******************************************************************************
/**
 *   (Meta-)Tile grids (data and calculations).
 *
 ******************************************************************************/

public abstract class Grid {

    public static java.util.List<Integer> geodetic_epsg_codes =
            java.util.Arrays.asList(new Integer[]{4326, 31466, 31467, 31468, 31469});

    //public int[] tile_size = new int[]{256, 256};
    //public java.util.List<Integer[]> grid_sizes;



    public double[] bbox;
    public mapproxy.core.SRS srs;
    

  //**************************************************************************
  //** get_resolution
  //**************************************************************************
  /**  Calculate the highest resolution needed to draw the bbox into an image
   *   with given size.
   <pre>
    >>> get_resolution((-180,-90,180,90), (256, 256))
    0.703125
   </pre>
   * 
   *  @returns the resolution (float)
   */
    public double get_resolution(double[] bbox, int[] size){
        double w = java.lang.Math.abs(bbox[0] - bbox[2]);
        double h = java.lang.Math.abs(bbox[1] - bbox[3]);
        return java.lang.Math.min(w/size[0], h/size[1]) ;
    }



  //**************************************************************************
  //** tile_grid_for_epsg
  //**************************************************************************
  /**  Create a tile grid that matches the given epsg code:
   *  @param epsg the epsg code (e.g. 'EPSG:0000', '0000' or 0000)
   *  @param bbox the bbox of the grid
   *  @param tile_size the size of each tile //tile_size=(256, 256)
   *  @param res a list with all resolutions
   */
    public TileGrid tile_grid_for_epsg(int epsg, double[] bbox, int[] tile_size, String res){

        //epsg = SRS.get_epsg_num(epsg); //<- skip conversion, this should be done elsewhere?

        if (geodetic_epsg_codes.contains(epsg)){
            return new TileGrid(epsg, bbox, tile_size, res, true);
        }
        return new TileGrid(epsg, bbox, tile_size, res);

    }


  //**************************************************************************
  //** _create_tile_list
  //**************************************************************************
  /** Returns an iterator tile_coords for the given tile ranges (`xs` and `ys`).
   *  If the one tile_coord is negative or out of the `grid_size` bound,
   *  the coord is None.
   */    
    public Generator<int[]> _create_tile_list(int[] xs, int[] ys, int level, int[] grid_size){


        final int[] _xs = xs;
        final int[] _ys = ys;
        final int _level = level;
        final int[] _grid_size = grid_size;

        Generator<int[]> iterator = new Generator<int[]>() {

            @Override
            public void run() {

                int[] xs = _xs;
                int[] ys = _ys;
                int level = _level;
                int[] grid_size = _grid_size;
                int x_limit = grid_size[0];
                int y_limit = grid_size[1];

                for (int y : ys){
                    for (int x : xs){
                        if (x < 0 || y < 0 || x >= x_limit || y >= y_limit){
                            //System.out.println("yield null");
                            yield(null);
                        }
                        else{
                            //System.out.println("yield " + Python.cstr(new int[]{x, y, level}));
                            yield(new int[]{x, y, level});
                        }
                    }
                }


            }
        };

        return iterator;
    }







  //**************************************************************************
  //** pyramid_res_level
  //**************************************************************************
  /**  Return resolutions of an image pyramid.
   *  @param initial_res the resolution of the top level (0)
   *  @param factor the factor between each level, for tms access 2
   *  @param levels number of resolutions to generate
    <pre>
    >>> pyramid_res_level(10000, levels=5)
    [10000.0, 5000.0, 2500.0, 1250.0, 625.0]
    >>> pyramid_res_level(10000, factor=1/0.75, levels=5)
    [10000.0, 7500.0, 5625.0, 4218.7500000000009, 3164.0625000000005]
    </pre>
   */
    public double[] pyramid_res_level(double initial_res, Double factor, Integer levels){

        if (factor==null) factor = 2.0;
        if (levels==null) levels = 20;

      //return [initial_res/factor**n for n in range(levels)];
        double[] arr = new double[levels];
        int i=0;
        for (int n: Python.range(levels)){            
            arr[i] = initial_res/java.lang.Math.pow(factor, n); //initial_res/factor**n;
            i++;
        }
        return arr;
    }



    protected boolean is_float(Object x){
        try{
            Double.valueOf((String) x).doubleValue(); //Float.valueOf((String) x).floatValue();
            return true;
        }
        catch(Exception e){
            return false;
        }
    }




  /** Used to convert a string to a double. */
    protected static double cdbl(String d){
        return Double.valueOf(d).doubleValue();
    }

    protected int cint(double d){
        return (int)Math.round(round(d,0));
    }
    protected double round(double value, int decimalPlace){
        double power_of_ten = 1;
        while (decimalPlace-- > 0){
            power_of_ten *= 10.0;
        }
        return Math.round(value * power_of_ten) / power_of_ten;
    }


    
}