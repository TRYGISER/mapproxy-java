package mapproxy.core.cache;
import mapproxy.core.utils.FileLock;

//******************************************************************************
//**  TileSource Class
//******************************************************************************
/**
 *   Base class for tile sources.
 *   A TileSource knows how to get the `_Tile.source` for a given tile.
 *
 ******************************************************************************/

public abstract class TileSource {

    public String lock_dir;
    private String _id;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of TileSource. 
   *  @param lock_dir "Lock" Directory specified in defaults.py
   */

    public void init(String lock_dir) {

        if (lock_dir==null){
            lock_dir = "/temp/tile_locks"; //abspath(base_config().cache.lock_dir)
        }
        this.lock_dir = lock_dir;
        this._id = null;
    }


    
  //**************************************************************************
  //** id
  //**************************************************************************
  /**  Returns a unique but constant id of this TileSource used for locking.
   */
    public String id(){
        return _id; //raise NotImplementedError
    }


  //**************************************************************************
  //** tile_lock
  //**************************************************************************
  /**  Returns a lock object for the given tile.
   */
    public FileLock tile_lock(_Tile tile){
        String lock_file = this.lock_filename(tile);
        return new FileLock(lock_file);
    }


    public String lock_filename(_Tile tile){
        if (this._id == null){

            try{
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
                //md.update((this.id()+"").getBytes());
                this._id = new String( md.digest( (this.id()+"").getBytes() ) );
            }
            catch(Exception e){
            }

            /*
            md5 = hashlib.md5();
            md5.update(str(this.id()));
            this._id = md5.hexdigest();
            */
        }

        String join = tile.coord[0] + "-" + tile.coord[1] + "-" + tile.coord[2]; //"-".join(map(str, tile.coord))

        return new java.io.File(this.lock_dir,
                this._id + "-" + join + ".lck").toString();

    }



  //**************************************************************************
  //** create_tile
  //**************************************************************************
  /**  Create the given tile and set the `_Tile.source`. It doesn't store the
   *   data on disk (or else where), this is up to the cache manager.
   *
   *   Note: This method may return multiple tiles, if it is more effective for the
               ``TileSource`` to create multiple tiles in one pass.
        :rtype: list of ``Tiles``
   */
    
    public TileCollection create_tile(_Tile tile, TileCollection tile_map){

      //See class that extends this class for implementation (WMSTileSource)
      return null;
    }
    

    /*
    def __repr__(self):
        return '%s(%r)' % (self.__class__.__name__)
    */

}