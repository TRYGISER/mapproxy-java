package mapproxy.core.cache;
import mapproxy.core.Generator;

//******************************************************************************
//**  CacheManager Class
//******************************************************************************
/**
 *   Manages tile cache and tile creation.
 *
 ******************************************************************************/

public class CacheManager {

    public FileCache cache;
    public TileSource tile_source;
    //public java.lang.Class<_TileCreator> tile_creator;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of CacheManager. */

    public CacheManager(FileCache cache, TileSource tile_source) { //tile_creator

        this.cache = cache;
        this.tile_source = tile_source;
        //this.tile_creator = tile_creator;
    }


  //**************************************************************************
  //** is_cached
  //**************************************************************************

    public boolean is_cached(_Tile tile){

        Long max_mtime = this.expire_timestamp(tile);
        boolean cached = this.cache.is_cached(tile);
        if (cached && max_mtime!=null){
            boolean stale = this.cache.timestamp_created(tile) < max_mtime;
            if (stale)
                cached = false;
        }
        return cached;
    }

  //**************************************************************************
  //** expire_timestamp
  //**************************************************************************
  /**  Return the timestamp until which a tile should be accepted as up-to-date,
   *   or ``None`` if the tiles should not expire.

        :note: Returns ``None`` by default. Overwrite/change method to enable
            expiration.
   */
    public Long expire_timestamp(_Tile tile){
        return null;
    }



  //**************************************************************************
  //** load_tile_coords
  //**************************************************************************
  /**  Load all given tiles from cache. If they are not present, load them.
   *  @param tile_coords list with tile coordinates (None for out of bounds tiles)
   *  @return list with `ImageSource` for all tiles (None for out of bounds tiles)
   */
    public TileCollection load_tile_coords(Generator<int[]> tile_coords, boolean with_metadata){

        TileCollection tiles = new TileCollection(tile_coords);
        this._load_tiles(tiles, with_metadata);

        return tiles;
    }


  //**************************************************************************
  //** load_tile_coords
  //**************************************************************************
  /** Overloaded member used by Cache.tile();  
   */
    public TileCollection load_tile_coords(int[] tile_coord, boolean with_metadata){
        final int[] tc = tile_coord;
        Generator<int[]> tile_coords = new Generator<int[]>() {
            @Override
            public void run() {
                yield(tc);
            }
        };
        return load_tile_coords(tile_coords, with_metadata);
    }


  //**************************************************************************
  //** _load_tiles
  //**************************************************************************
  /**  Return the given `tiles` with the `_Tile.source` set. If a tile is not
   *   cached, it will be created.
   */
    private TileCollection _load_tiles(TileCollection tiles, boolean with_metadata){

        tiles = this._load_cached_tiles(tiles, with_metadata);
        tiles = this._create_tiles(tiles, with_metadata);
        return tiles;
    }



  //**************************************************************************
  //** _create_tiles
  //**************************************************************************
  /** Create the tile data for all missing tiles. All created tiles will be
   *  added to the cache.
   *  @param tiles
   *  @param with_metadata
   */
    private TileCollection _create_tiles(TileCollection tiles, boolean with_metadata){


/*
        new_tiles = [tile for tile in tiles if tile.is_missing()]
        if new_tiles:
            created_tiles = self.tile_creator(new_tiles, tiles,
                                              self.tile_source, self)

            # load tile that were not created (e.g tiles created by another process)
            not_created = set(new_tiles).difference(created_tiles)
            if not_created:
                self._load_cached_tiles(not_created, with_metadata=with_metadata)
 */


      //new_tiles = [tile for tile in tiles if tile.is_missing()]
        TileCollection new_tiles = new TileCollection();
        for (_Tile tile : tiles){
            if (tile.is_missing()) new_tiles.add(tile);
        }

        
        if (!new_tiles.isEmpty()){

            TileCollection created_tiles =
                    new _SequentialTileCreator(this.tile_source, this).create_tiles(new_tiles, tiles);
                    //this.tile_creator(new_tiles, tiles, this.tile_source, this);


            // load tile that were not created (e.g tiles created by another process)
            // set(new_tiles).difference(created_tiles);
            TileCollection not_created = new TileCollection();
            for (_Tile tile : new_tiles){
                if (!created_tiles.contains(tile)){
                    not_created.add(tile);
                }
            }


            if (!not_created.isEmpty()){
                this._load_cached_tiles(not_created, with_metadata);
            }
        }

        return tiles;
    }




  /** Set the `_Tile.source` for all cached tiles. */
    public TileCollection _load_cached_tiles(TileCollection tiles, boolean with_metadata){

        for (_Tile tile : tiles){
            if (tile.is_missing() && this.is_cached(tile))
                this.cache.load(tile, with_metadata);
        }
        return tiles;
    }


  /** Store the given tiles in the underlying cache. */
    public TileCollection store_tiles(TileCollection tiles){

        for (_Tile tile : tiles){
            this.cache.store(tile);
        }
        return tiles;
    }

}