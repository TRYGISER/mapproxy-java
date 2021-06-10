package mapproxy.arcgis.cache;
//import mapproxy.wms.cache.*;
import mapproxy.core.cache.*;
import mapproxy.core.grid.*;
import mapproxy.core.image.*;
import mapproxy.arcgis.client.MapClient;
//import mapproxy.wms.request.*;
import mapproxy.core.Generator;

//******************************************************************************
//**  MapTileSource
//******************************************************************************
/**  
 *   This TileSource retrieves new tiles from a ArcGIS server.
 *   This class is able to request maps that are larger than one tile and split
 *   the large map into multiple tiles. The meta_size defines how many tiles
 *   should be generated per request.
 *
 ******************************************************************************/

public class MapTileSource extends TileSource {

    private int meta_buffer = 0;
    private int[] meta_size = new int[]{2, 2};
    private TileGrid grid;
    private MapClient[] clients;
    private String format;
    private MetaGrid meta_grid;
    private boolean transparent;



  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of the WMSTileSource.
   * 
   *  @param grid the associated grid
   *
   *  @param clients MapClient for each distinct WMS source
   *   ["mapproxy.wms.client.MapClient",...]
   *
   *  @param format internal image format. if not set use format from first WMS
   *   client
   *
   *  @param meta_size the number of tiles to get per request (x_size, y_size)
   *
   *  @param meta_buffer the buffer size in pixel that is added to each grid.
   *   the number is added to all four borders. this buffer may improve the
   *   handling of labels overlapping (meta)tile borders.
        :type meta_buffer: pixel
   */
    public MapTileSource(TileGrid grid, MapClient[] clients, String format,
                                            int meta_buffer, int[] meta_size) {


        this.init(lock_dir); //TileSource.__init__(self)

        this.grid = grid;
        this.clients = clients;
        if (format==null){
            format = "image/png"; //clients[0].request_template.params.format;
        }
        this.format = format;
        this.transparent = this._has_transparent_sources();
        this.meta_grid = new MetaGrid(grid, meta_size, meta_buffer);

    }

    public MapTileSource(TileGrid grid, MapClient[] clients){
        this(grid, clients, null, 0, new int[]{2, 2});
    }

    public MapTileSource(TileGrid grid, MapClient client){
        this(grid, new MapClient[]{client});
    }




  //**************************************************************************
  //** _has_transparent_sources
  //**************************************************************************

    private boolean _has_transparent_sources(){
        /*
        for (MapClient client : this.clients){
            if (client.request_template.params.get("transparent", "false").equalsIgnoreCase("true"))
                return true;
        }
        return false;
        */
        return true;
    }


    @Override
  //**************************************************************************
  //** id
  //**************************************************************************
    public String id(){

        return null; //<-- TODO 
        /*
        StringBuffer str = new StringBuffer();
        for (int i=0; i<clients.length; i++){
            str.append(clients[i].request_template.complete_url());
            if (i<clients.length-1) str.append("|");
        }
        return str.toString();
        */
        
        //return '|'.join(client.request_template.complete_url for client in self.clients);
    }


    @Override
  //**************************************************************************
  //** lock_filename
  //**************************************************************************
  /** Returns a lock for one fixed tile per metatile.
   */
    public String lock_filename(_Tile tile){
        Generator<TileCoordinate> tiles = meta_grid.tiles(tile.coord);        
        TileCoordinate first_tile = tiles.iterator().next(); //first_tile, _ = tiles.next()
        return lock_filename(new _Tile(first_tile));
    }


    @Override
  //**************************************************************************
  //** create_tile
  //**************************************************************************
  /**  Retrieve the metatile that contains the requested tile and save all
   *   tiles.
   *   @param tile_map tile_collection
   */
    public TileCollection create_tile(_Tile tile, TileCollection tile_map){
        javaxt.io.Image meta_tile = _get_meta_tile(tile);
        Generator<TileCoordinate> tiles = meta_grid.tiles(tile.coord);
        return _split_meta_tile(meta_tile, tiles, tile_map);
    }


  //**************************************************************************
  //** _get_meta_tile
  //**************************************************************************

    private javaxt.io.Image _get_meta_tile(_Tile tile){
        double[] bbox = meta_grid.meta_bbox(tile.coord);
        int[] size = meta_grid.tile_size(tile.coord[2]);
        java.util.List<javaxt.io.Image> responses = new java.util.ArrayList<javaxt.io.Image>();
        for (MapClient client : this.clients){
            javaxt.io.Image img = client.get_map(bbox, size);
            responses.add(img);
        }

        if (responses.size() > 1)
            return Image.merge_images(responses, true); //transparent=True
        else
            return responses.get(0);
    }

    
  //**************************************************************************
  //** _split_meta_tile
  //**************************************************************************
    private TileCollection _split_meta_tile(javaxt.io.Image meta_tile, 
                                            Generator<TileCoordinate> tiles,
                                            TileCollection tile_map){

        /*
        System.out.println();
        System.out.println("tile_map");
        java.util.Iterator<_Tile> it = tile_map.iterator();
        while (it.hasNext()){
            _Tile tile = it.next();
            System.out.println(mapproxy.core.Python.cstr(tile.coord) + ", " + tile.source);
        }
        System.out.println();
        */
        
        TileSplitter splitter = new TileSplitter(meta_tile, format);

        
        //System.out.println("_split_meta_tile...");
        TileCollection split_tiles = new TileCollection();
        for (TileCoordinate tile : tiles){
            
            int[] tile_coord = tile.tile_coord;
            int[] crop_coord = tile.crop_coord;
            //System.out.println(mapproxy.core.Python.cstr(tile_coord));
            //System.out.println(mapproxy.core.Python.cstr(crop_coord));

            javaxt.io.Image data = splitter.get_tile(crop_coord, this.grid.tile_size);
            _Tile new_tile = tile_map.get(tile_coord);
            if (new_tile==null) new_tile = new _Tile(tile_coord);
            new_tile.source = data;
            
            split_tiles.add(new_tile);
        }
        return split_tiles;
    }

}