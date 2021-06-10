package mapproxy.core.image;
import mapproxy.core.SRS;
import mapproxy.core.Python;

//******************************************************************************
//**  TiledImage Class
//******************************************************************************
/**
 *   An image built-up from multiple tiles.
 *
 ******************************************************************************/

public class TiledImage {

    private javaxt.io.Image[] tiles;
    private int[] tile_grid;
    private int[] tile_size;
    private double[] src_bbox;
    private SRS src_srs;
    private boolean transparent;


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of TiledImage.
   *  @param tiles all tiles (sorted row-wise, top to bottom)
   *  @param tile_grid The tile grid size. Type tile_grid: ``(int(x_tiles), int(y_tiles))``
   *  @param tile_size the size of each tile
   *  @param src_bbox the bbox of all tiles
   *  @param src_srs: the srs of the bbox
   *  @param transparent: if the sources are transparent
   */
    public TiledImage(javaxt.io.Image[] tiles, int[] tile_grid, int[] tile_size,
                            double[] src_bbox, SRS src_srs, boolean transparent){

        this.tiles = tiles;
        this.tile_grid = tile_grid;
        this.tile_size = tile_size;
        this.src_bbox = src_bbox;
        this.src_srs = src_srs;
        this.transparent = transparent;
    }


  //**************************************************************************
  //** image
  //**************************************************************************
  /** Return the tiles as one merged image.
   *  @return 'ImageSource'
   */
    public javaxt.io.Image image(){
        TileMerger tm = new TileMerger(this.tile_grid, this.tile_size);
        return tm.merge(this.tiles, this.transparent);
    }

    
  //**************************************************************************
  //** transform
  //**************************************************************************
  /** Return the the tiles as one merged and transformed image.
   *  @param req_bbox the bbox of the output image
   *  @param req_srs the srs of the req_bbox
   *  @param out_size the size in pixel of the output image
   *  @return 'ImageSource'
   */
    public javaxt.io.Image transform(double[] req_bbox, SRS req_srs, int[] out_size){
        ImageTransformer transformer = new ImageTransformer(this.src_srs, req_srs);
        javaxt.io.Image src_img = this.image();
        return transformer.transform(src_img, this.src_bbox, out_size, req_bbox);
    }


}
