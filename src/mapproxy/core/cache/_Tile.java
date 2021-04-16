package mapproxy.core.cache;
import mapproxy.core.grid.TileCoordinate;

//******************************************************************************
//**  _Tile Class
//******************************************************************************
/**
 *   Internal data object for all tiles. Stores the tile-"coord" and the tile
 *   data.
 *
 ******************************************************************************/

public class _Tile {

    public int[] coord;
    public javaxt.io.Image source;
    public boolean stored;
    public String location;
    public Long size;
    public Long timestamp;


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** 
   *  @param source the data of this tile (type ImageSource)
   */

    public _Tile(int[] coord, javaxt.io.Image source) {

        this.coord = coord;
        this.source = source;
        this.location = null;
        this.stored = false;
        this.size = null;
        this.timestamp = null;
    }

    public _Tile(int x, int y, int z) {
        this(new int[]{x,y,z}, null);
    }

    public _Tile(int[] coord) {
        this(coord, null);
    }

    public _Tile(TileCoordinate coord){
        this(coord.tile_coord);
    }

    /*
    def source_buffer(self, *args, **kw):
        if self.source is not None:
            return self.source.as_buffer(*args, **kw)
        else:
            return None

    def source_image(self, *args, **kw):
        if self.source is not None:
            return self.source.as_image(*args, **kw)
        else:
            return None
    */

  //**************************************************************************
  //** is_missing
  //**************************************************************************
  /**
        Returns ``True`` when the tile has no ``data``, except when the ``coord``
        is ``None``. It doesn't check if the tile exists.

        >>> _Tile((1, 2, 3)).is_missing()
        True
        >>> _Tile((1, 2, 3), './tmp/foo').is_missing()
        False
        >>> _Tile(None).is_missing()
        False
   */
    public boolean is_missing(){

        if (this.coord ==null) return false;
        return (this.source ==null);
    }


  //**************************************************************************
  //** equals
  //**************************************************************************
  /**
   <pre>
        >>> _Tile((0, 0, 1)) == _Tile((0, 0, 1))
        True
        >>> _Tile((0, 0, 1)) == _Tile((1, 0, 1))
        False
        >>> _Tile((0, 0, 1)) == None
        False
   </pre>
   */
    public boolean equals(Object other){ //In python, this is called __eq__

        if (other==null) return false;
        if (other instanceof _Tile){
            _Tile tile = (_Tile) other;
            if ( tile.coord.equals(this.coord) && tile.source.equals(this.source)){
                return true;
            }
        }
        
        return false;

    }




  //**************************************************************************
  //** notEquals
  //**************************************************************************
  /**
   *
        >>> _Tile((0, 0, 1)) != _Tile((0, 0, 1))
        False
        >>> _Tile((0, 0, 1)) != _Tile((1, 0, 1))
        True
        >>> _Tile((0, 0, 1)) != None
        True
   */
    public boolean notEquals(Object other){ //def __ne__(self, other):
        return !this.equals(other);
    }


    public String toString(){ //def __repr__(self):

      //return '_Tile(%r, source=%r)' % (self.coord, self.source)
        return "_Tile({" + coord[0] + "," + coord[1] + "," + coord[2] + "}, " +
                "source=" + source.getBufferedImage().hashCode() + ")";
    }


    
    public int hashCode(){
        return toString().hashCode();
    }




}