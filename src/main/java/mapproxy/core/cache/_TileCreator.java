package mapproxy.core.cache;

//******************************************************************************
//**  _TileCreator Class
//******************************************************************************
/**
 *   Base class for the creation of new tiles.
 *   Subclasses can implement different strategies how multiple tiles should
 *   be created (e.g. threaded).
 *
 ******************************************************************************/

public interface _TileCreator {


    /*
    public TileSource tile_source;
    public Cache cache;
     */

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of _TileCreator. */
    /*
    public _TileCreator(TileSource tile_source, Cache cache) {

        this.tile_source = tile_source;
        this.cache = cache;
    }
    */


  //**************************************************************************
  //** create_tiles
  //**************************************************************************
  /**  Create the given tiles (`_Tile.source` will be set). Returns a list with
   *   all created tiles.
   * 
   *   Note: The returned list may contain more tiles than requested. This
   *   allows the `TileSource` to create multiple tiles in one pass.
   */

    public TileCollection create_tiles(TileCollection tiles, TileCollection tile_collection);



    
    //public TileCollection create_tiles(TileCollection tiles, TileCollection tile_collection, TileSource tile_source, FileCache cache);



/*

 def threaded_tile_creator(tiles, tile_collection, tile_source, cache):
    """
    This tile creator creates a thread pool to create multiple tiles in parallel.
    """
    return _ThreadedTileCreator(tile_source, cache).create_tiles(tiles, tile_collection)

 */


/*

def sequential_tile_creator(tiles, tile_collection, tile_source, cache):
    """
    This tile creator creates a thread pool to create multiple tiles in parallel.
    """
    return _SequentialTileCreator(tile_source, cache).create_tiles(tiles, tile_collection)
 */
}