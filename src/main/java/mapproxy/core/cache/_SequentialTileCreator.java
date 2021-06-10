package mapproxy.core.cache;

//******************************************************************************
//**  _SequentialTileCreator Class
//******************************************************************************
/**
 *   This `_TileCreator` creates one requested tile after the other.
 *
 ******************************************************************************/

public class _SequentialTileCreator implements _TileCreator {

    public TileSource tile_source;
    public CacheManager cache;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of _SequentialTileCreator.   */
    public _SequentialTileCreator(TileSource tile_source, CacheManager cache) {

        this.tile_source = tile_source;
        this.cache = cache;
    }

    public TileCollection create_tiles(TileCollection tiles, TileCollection tile_collection){
        TileCollection created_tiles = new TileCollection();
        
        for (_Tile tile : tiles){ //for tile in tiles:            
            //with (this.tile_source.tile_lock(tile)){ //<--TOD0 Implement tile lock
                if (!this.cache.is_cached(tile)){
                    TileCollection new_tiles = this.tile_source.create_tile(tile, tile_collection);
                    new_tiles = this.cache.store_tiles(new_tiles);
                    created_tiles.addAll(new_tiles);
                }
            //}
        }
        return created_tiles;

        /*
    def create_tiles(self, tiles, tile_collection):
        created_tiles = []
        for tile in tiles:
            with self.tile_source.tile_lock(tile):
                if not self.cache.is_cached(tile):
                    new_tiles = self.tile_source.create_tile(tile, tile_collection)
                    self.cache.store_tiles(new_tiles)
                    created_tiles.extend(new_tiles)
        return created_tiles
         */
    }



}