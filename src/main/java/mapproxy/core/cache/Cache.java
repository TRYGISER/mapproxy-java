package mapproxy.core.cache;
import mapproxy.core.grid.*;
import mapproxy.core.image.TiledImage;
import mapproxy.core.SRS;
import mapproxy.config.Config;
import mapproxy.core.Generator;
import mapproxy.core.Python;

//******************************************************************************
//**  Cache Class
//******************************************************************************
/**   
 *    Easy access to images from cached tiles.
 *
 ******************************************************************************/

public class Cache {

    public CacheManager cache_mgr;
    public TileGrid grid;
    public boolean transparent = false;


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of Cache.
   *  @param cache_mgr the cache manager
   *  @param grid the grid of the tile cache
   */
    public Cache(CacheManager cache_mgr, TileGrid grid, boolean transparent){

        this.cache_mgr = cache_mgr;
        this.grid = grid;
        this.transparent = transparent;
        
    }

    public Cache(CacheManager cache_mgr, TileGrid grid){
        this(cache_mgr, grid, false);
    }

  //**************************************************************************
  //** tile
  //**************************************************************************
  /** Return a single tile.
   *  @return loaded tile or ``None`` 
   */
    public _Tile tile(int[] tile_coord){
        
      //tiles = self.cache_mgr.load_tile_coords([tile_coord], with_metadata=True)
        TileCollection tiles = this.cache_mgr.load_tile_coords(tile_coord, true);
        if (tiles.size()<1) //len(tiles) < 1:
            return null;
        else
            return tiles.get(0);
    }



  //**************************************************************************
  //** _tiles
  //**************************************************************************

    public TileCollection _tiles(Generator<int[]> tile_coords){
        return this.cache_mgr.load_tile_coords(tile_coords, true);
    }



  //**************************************************************************
  //** _tiled_image
  //**************************************************************************
  /** Return a `TiledImage` with all tiles that are within the requested bbox,
   *  for the given out_size.
   *  
   *  Note: The parameters are just hints for the tile cache to load the right
   *  tiles. Usually the bbox and the size of the result is larger. The result
   *  will always be in the native srs of the cache. See `Cache.image`.
   * 
   *  @param req_bbox the requested bbox
   *  @param req_srs the srs of the req_bbox
   *  @param out_size the target output size
   *  @return 'ImageSource`
   */
    private TiledImage _tiled_image(double[] req_bbox, SRS req_srs, int[] out_size){

        Object[] arr = this.grid.get_affected_tiles(req_bbox, out_size, req_srs, false);
        double[] src_bbox = (double[]) arr[0];
        int[] tile_grid = (int[]) arr[1];
        Generator<int[]> affected_tile_coords = (Generator<int[]>) arr[2];

        //System.out.println("src_bbox: " + Python.cstr(src_bbox));
        //System.out.println("tile_grid: " + Python.cstr(tile_grid));

        int num_tiles = tile_grid[0] * tile_grid[1];
        if (num_tiles >= Config.base_config().get("cache.max_tile_limit").toInteger()){
            return null; //raise TooManyTilesError();
        }

      //tile_sources = [tile.source for tile in this._tiles(affected_tile_coords)]
        TileCollection tiles = this._tiles(affected_tile_coords);
        javaxt.io.Image[] tile_sources = new javaxt.io.Image[tiles.size()];
        for (int i=0; i<tiles.size(); i++){
            tile_sources[i] = tiles.get(i).source;
        }

        return new TiledImage(tile_sources, tile_grid, this.grid.tile_size,
                src_bbox, this.grid.srs, this.transparent);
    }



  //**************************************************************************
  //** image
  //**************************************************************************
  /**  Return an image with the given bbox and size. The result will be
   *   cropped/transformed if needed.
   *
   *  @param req_bbox the requested bbox
   *  @param req_srs the srs of the req_bbox
   *  @param out_size the output size
   *  @return `ImageSource`
   */
    public javaxt.io.Image image(double[] req_bbox, SRS req_srs, int[] out_size){
        if (!this.grid.srs.equals(req_srs)){
            System.out.println("src_srs: " + this.grid.srs);
            System.out.println("req_srs: " + req_srs);
        }
        TiledImage tiled_image = this._tiled_image(req_bbox, req_srs, out_size);
        return tiled_image.transform(req_bbox, req_srs, out_size);
    }



  //**************************************************************************
  //** create_dir
  //**************************************************************************
    public void create_dir(String file_name){
        java.io.File dir = new java.io.File(file_name).getParentFile();
        if (!dir.exists()){
            dir.mkdirs();
        }
    }


}