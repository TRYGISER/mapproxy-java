package mapproxy.tms.cache;
import mapproxy.tms.*;
import mapproxy.core.cache.*;
import mapproxy.core.grid.*;
//import mapproxy.core.image.*;
//import mapproxy.wms.request.*;
import mapproxy.core.Python;

//******************************************************************************
//**  TMSTileSource Class
//******************************************************************************
/**
 *   This `TileSource` retrieves new tiles from a TMS server.
 *
 ******************************************************************************/

public class TMSTileSource extends TileSource {


    private int meta_buffer = 0;
    private int[] meta_size = new int[]{2, 2};
    private TileGrid grid;
    private String format;
    private MetaGrid meta_grid;

    private TMSClient tms_client;
    private boolean inverse = false;


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of TMSTileSource.   */

    public TMSTileSource(TileGrid grid, String url, String format, boolean inverse){


        this.init(lock_dir);

        this.grid = grid;
        if (format==null){
            format = "image/png"; //clients[0].request_template.params.format;
        }
        this.format = format;
        this.meta_grid = new MetaGrid(grid, meta_size, meta_buffer);

        
        this.tms_client = new TMSClient(url, format);
        this.inverse = inverse;

    }

    public TMSTileSource(TileGrid grid, String url){
        this(grid, url, "png", false);
    }



    public String id(){
        return this.tms_client.url;
    }


  //**************************************************************************
  //** create_tile
  //**************************************************************************
  /**  Retrieve the requested `tile`.
   */
    /*
    public create_tile(tile, _tile_map){


        if (this.inverse)
            coord = this.grid.flip_tile_coord(tile.coord);
        else
            coord = tile.coord;
        try{
            buf = StringIO(self.tms_client.get_tile(coord).read())
            tile.source = ImageSource(buf)
        except HTTPClientError, e:
            reraise_exception(TileSourceError(e.message), sys.exc_info())
        return [tile];

    }
    */


    @Override
  //**************************************************************************
  //** create_tile
  //**************************************************************************
  /**  Retrieve the metatile that contains the requested tile and save all
   *   tiles.
   *   @param tile_map tile_collection
   */
    public TileCollection create_tile(_Tile tile, TileCollection tile_map){


        System.out.println("TileCollection create_tile:");
        System.out.println(Python.cstr(tile.coord));

        for (_Tile t : tile_map){
            System.out.println(Python.cstr(t.coord));
        }
        return tile_map;

        //req.tile = tms_grid.internal_tile_coord(req.tile, true);

        //javaxt.io.Image meta_tile = _get_meta_tile(tile);
        //Generator<TileCoordinate> tiles = meta_grid.tiles(tile.coord);
        //return _split_meta_tile(meta_tile, tiles, tile_map);
    }



  //**************************************************************************
  //** _get_meta_tile
  //**************************************************************************
  /*
    private javaxt.io.Image _get_meta_tile(_Tile tile){
        double[] bbox = meta_grid.meta_bbox(tile.coord);
        int[] size = meta_grid.tile_size(tile.coord[2]);


        mapproxy.tms.TileServiceGrid g = new mapproxy.tms.TileServiceGrid(grid);
        tms_request.tile = g.internal_tile_coord(tms_request.tile, true);

    }
    */

}
