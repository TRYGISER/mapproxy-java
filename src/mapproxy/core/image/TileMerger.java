package mapproxy.core.image;
import mapproxy.core.Python;

//******************************************************************************
//**  TileMerger Class
//******************************************************************************
/**
 *   Merge multiple tiles into one image.
 *
 ******************************************************************************/

public class TileMerger {

    private int[] tile_grid;
    private int[] tile_size;

    
  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of TileMerger.
   *  @param tile_grid the grid size: ``(int(x_tiles), int(y_tiles))``
   *  @param tile_size the size of each tile
   */
    public TileMerger(int[] tile_grid, int[] tile_size){
        this.tile_grid = tile_grid;
        this.tile_size = tile_size;
    }



  //**************************************************************************
  //** merge
  //**************************************************************************
  /**  Merge all tiles into one image.
   *   @param ordered_tiles list of tiles, sorted row-wise (top to bottom)
   *   @return 'ImageSource'
   */
    public javaxt.io.Image merge(javaxt.io.Image[] ordered_tiles, boolean transparent){

        if (this.tile_grid[0]==1 && this.tile_grid[1]==1){ //self.tile_grid == (1, 1):
            //assert len(ordered_tiles) == 1
            if (ordered_tiles[0]!=null){
                return ordered_tiles[0]; /*
                tile = ordered_tiles.pop();
                return ImageSource(tile.source, size=self.tile_size,
                                   transparent=transparent) */
            }
        }
        int[] src_size = this._src_size();
        javaxt.io.Image result = new javaxt.io.Image(src_size[0], src_size[1]); //Image.new("RGBA", src_size, (255, 255, 255, 255)) //mode, size, color
        //System.out.println("size: " + result.getWidth() + ", " + result.getHeight());
        
        for (int i=0; i<ordered_tiles.length; i++){ //i, source in enumerate(ordered_tiles):
            javaxt.io.Image source = ordered_tiles[i];
            if (source!=null){
                try{
                    //tile = source.as_image()
                    //tile.draft('RGBA', self.tile_size)
                    //System.out.println("tile_size: " + this.tile_size[0] + ", " + this.tile_size[1] + " vs " + source.getWidth() + ", " + source.getHeight());
                    int[] pos = this._tile_offset(i);
                    //System.out.println("pos: " + pos[0] + ", " + pos[1]);
                    result.addImage(source, pos[0], pos[1], false); //result.paste(tile, pos)
                }
                catch(Exception e){ //except IOError, e:
                    /*
                    log.warn('unable to load tile %s, removing it (reason was: %s)'
                             % (source, str(e)))
                    if isinstance(source.source, basestring):
                        if os.path.exists(source.source):
                            os.remove(source.source)
                    */
                }
            }
        }
        //System.out.println("size: " + result.getWidth() + ", " + result.getHeight());
        return result;
    }


  //**************************************************************************
  //** _src_size
  //**************************************************************************
    private int[] _src_size(){
        int width = this.tile_grid[0]*this.tile_size[0];
        int height = this.tile_grid[1]*this.tile_size[1];
        return new int[]{width, height};
    }
    

  //**************************************************************************
  //** _tile_offset
  //**************************************************************************
  /** Return the image offset (upper-left coord) of the i-th tile,
   *  where the tiles are ordered row-wise, top to bottom.
   */
    private int[] _tile_offset(int i){
        return new int[]{
            i%this.tile_grid[0]*this.tile_size[0],
            Python.doubleDiv(i, this.tile_grid[0])*this.tile_size[1]
        };
    }
}
