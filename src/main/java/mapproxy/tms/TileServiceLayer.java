package mapproxy.tms;
import mapproxy.core.layer.*;
//import mapproxy.wms.request.WMSRequest;
import mapproxy.core.cache.Cache;

//******************************************************************************
//**  TileServiceLayer Class
//******************************************************************************
/**
 *   Enter class description here
 *
 ******************************************************************************/

public class TileServiceLayer extends Layer {

    private LayerMetaData md;
    private Cache cache;
    private TileServiceGrid grid;


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of TileServiceLayer.   */

    public TileServiceLayer(LayerMetaData md, Cache cache){
        if (md==null)
            this.md = new LayerMetaData();
        this.md = md;
        this.cache = cache;
        this.grid = new TileServiceGrid(cache.grid);
    }




    protected double[] _bbox(){
        return this.grid.bbox();
    }
/*
    private SRS _srs(){
        return this.grid.srs;
    }


    
    public String format(){
        _mime_class, format, _options = split_mime_type(self.format_mime_type)
        return format;
    }
*/
    
    public String format_mime_type(){
        return this.md.get("format", "image/png");
    }

    private int[] _internal_tile_coord(TileRequest tile_request, boolean use_profiles){ //use_profiles=False
        int[] tile_coord = this.grid.internal_tile_coord(tile_request.tile, use_profiles);
        if (tile_coord == null){
            //raise RequestError('The requested tile is outside the bounding box'
            //                   ' of the tile map.', request=tile_request)
        }
        return tile_coord;
    }

    /*
    public TileResponse render(TileRequest tile_request, boolean use_profiles){ //use_profiles=False
        //if (tile_request.format != self.format){
            //raise RequestError('invalid format (%s). this tile set only supports (%s)'
            //                   % (tile_request.format, self.format), request=tile_request)
        //}
        int[] tile_coord = this._internal_tile_coord(tile_request, use_profiles);
        System.out.println(mapproxy.core.Python.cstr(tile_coord));
        //try{
        //System.out.println(this.cache.tile(tile_coord).toString());
            //return new TileResponse(this.cache.tile(tile_coord));
        return null;
        //}
        //catch(Exception e){//except TileCacheError, e:
            //log.error(e)
            //raise RequestError(e.message, request=tile_request, internal=True)
        //}
    }
    */

}
