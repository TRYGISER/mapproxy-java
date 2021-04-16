package mapproxy.core.cache;

//******************************************************************************
//**  FileCache Class
//******************************************************************************
/**
 *   This class is responsible to store and load the actual tile data.
 *
 ******************************************************************************/

public class FileCache {


    public String cache_dir; 
    public String file_ext;
    public TileFilter[] pre_store_filter;


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of FileCache.
   *
   *  @param cache_dir the path where the tile will be stored
   *
   *  @param file_ext the file extension that will be appended to each tile
   *  (e.g. 'png')
   *
   *  @param pre_store_filter a list with filter. each filter will be called
   *   with a tile before it will be stored to disc. the filter should return
   *   this or a new tile object.
   */
    public FileCache(String cache_dir, String file_ext, TileFilter[] pre_store_filter) {

        this.cache_dir = cache_dir;
        this.file_ext = file_ext;
        if (pre_store_filter==null){
            pre_store_filter = new TileFilter[0];
        }
        this.pre_store_filter = pre_store_filter;
    }

    public FileCache(String cache_dir, String file_ext) {
        this(cache_dir, file_ext, null);
    }


  //**************************************************************************
  //** level_location
  //**************************************************************************
  /**  Return the path where all tiles for `level` will be stored.
    <pre>
        >>> c = FileCache(cache_dir='/tmp/cache/', file_ext='png')
        >>> c.level_location(2)
        '/tmp/cache/02'
    </pre>
   */
    public String level_location(double level){
        int i = javaxt.utils.string.cint(level);
        return new java.io.File(cache_dir, String.format("%02d", i)).toString();
        //return os.path.join(self.cache_dir, "%02d" % level)
    }
    

  //**************************************************************************
  //** tile_location
  //**************************************************************************
  /**  Return the location of the `tile`. Caches the result as ``location``
   *   property of the `tile`.
   *   @param tile: the tile object
   *   @param create_dir: if True, create all necessary directories
   *   @return: the full filename of the tile
    <pre>
        >>> c = FileCache(cache_dir='/tmp/cache/', file_ext='png')
        >>> c.tile_location(_Tile((3, 4, 2))).replace('\\\\', '/')
        '/tmp/cache/02/000/000/003/000/000/004.png'
    </pre>
   */
    public String tile_location(_Tile tile, boolean create_dir){

        if (tile.location==null){
            
          //x, y, z = tile.coord;
            int[] coord = tile.coord;
            int x = coord[0];
            int y = coord[1];
            int z = coord[2];
            
            StringBuffer path = new StringBuffer();
            String pathSeparator = System.getProperty("file.separator");

            path.append(level_location(z) + pathSeparator);
            path.append(String.format("%03d", cint(x / 1000000)) + pathSeparator);
            path.append(String.format("%03d", (cint(x / 1000) % 1000)) + pathSeparator);
            path.append(String.format("%03d", (cint(x) % 1000)) + pathSeparator);
            path.append(String.format("%03d", cint(y / 1000000)) + pathSeparator);
            path.append(String.format("%03d", (cint(y / 1000) % 1000)) + pathSeparator);
            path.append(String.format("%03d", cint(y) % 1000) + "." + this.file_ext);

            tile.location = path.toString();
            
        }
        if (create_dir){
            new java.io.File(tile.location).getParentFile().mkdirs();
        }
        return tile.location;
    }


  //**************************************************************************
  //** timestamp_created
  //**************************************************************************
  /** Return the timestamp of the last modification of the tile.
   *  @param tile
   */
    public Long timestamp_created(_Tile tile){
        tile = _update_tile_metadata(tile);
        return tile.timestamp;
    }

    
  //**************************************************************************
  //** _update_tile_metadata
  //**************************************************************************
  /**  Used to update tile metadata. Needed to add return statement for java.
   */
    public _Tile _update_tile_metadata(_Tile tile){
        String location = tile_location(tile, false);
        java.io.File stats = new java.io.File(location);
        tile.timestamp = stats.lastModified();
        tile.size = stats.length();
        return tile;
    }

    
  //**************************************************************************
  //** is_cached
  //**************************************************************************
  /**  Returns true if the tile data is present.
   */
    public boolean is_cached(_Tile tile){

        if (tile.is_missing()){
            String location = this.tile_location(tile, false);
            if (new java.io.File(location).exists()) //os.path.exists(location)
                return true;
            else
                return false;
        }
        else{
            return true;
        }
    }


  //**************************************************************************
  //** load
  //**************************************************************************
  /**  Fills the `_Tile.source` of the `tile` if it is cached.
   *   If it is not cached or if the ``.coord`` is ``None``, nothing happens.
   *
   *  @return _Tile (vs boolean) because I can't update the input tile AND 
   *   return a boolean. Treat null returns as false, non-null as true.
   */
    public _Tile load(_Tile tile, boolean with_metadata){ //with_metadata=False

        if (!tile.is_missing())
            return tile; //return true;

        String location = this.tile_location(tile, false);
        java.io.File file = new java.io.File(location);

        if (file.exists()){
            if (with_metadata){
                _update_tile_metadata(tile);
            }
            tile.source = new javaxt.io.Image(file);
            return tile; //return true;
        }
        return null; //return false;
    }


  //**************************************************************************
  //** store
  //**************************************************************************
  /**  Add the given `tile` to the file cache. Stores the `_Tile.source` to
   *   `FileCache.tile_location`.  All ``pre_store_filter`` will be called with
   *   the tile, before it will be stored.
   */
    public _Tile store(_Tile tile){

        if (tile.stored) return tile;

        String tile_loc = tile_location(tile, true);


      /*
        pre_store_filter is just a list, there is no code for it.
        The list is initialized in the constructor of the FileCache,
        which are created in the conf_loader file. Look in tilefilter.py for actual functions.
       */
        //for (TileFilter img_filter : pre_store_filter){ //for img_filter in this.pre_store_filter:
        //    tile = img_filter(tile);
        //}


        java.io.File output = new java.io.File(tile_loc);
        try{
            tile.source.saveAs(output);
            output = new java.io.File(tile_loc);
            tile.size = output.length();
            tile.timestamp = output.lastModified();
            tile.stored = true;
        }
        catch(Exception e){}

        /*
        data = tile.source.as_buffer()
        with open(tile_loc, 'wb') as f:
            //log.debug('writing %r to %s' % (tile.coord, tile_loc))
            f.write(data.read())
        */




        return tile;
    }


    private int cint(double d){
        return (int)Math.round(round(d,0));
    }
    public double round(double value, int decimalPlace){
        double power_of_ten = 1;
        while (decimalPlace-- > 0){
            power_of_ten *= 10.0;
        }
        return Math.round(value * power_of_ten) / power_of_ten;
    }
}