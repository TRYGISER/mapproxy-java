package mapproxy.core.cache;
import mapproxy.core.Generator;

//******************************************************************************
//**  TileCollection Class
//******************************************************************************
/**
 *   Enter class description here
 *
 ******************************************************************************/

public class TileCollection extends java.util.ArrayList<_Tile> {



  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of TileCollection.   */

    public TileCollection(Generator<int[]> tile_coords){

        for (int[] coord : tile_coords){
            this.add(new _Tile(coord));
        }
        

        /*
        self.tiles = [_Tile(coord) for coord in tile_coords]
        self.tiles_dict = {}
        for tile in self.tiles:
            self.tiles_dict[tile.coord] = tile
         */
    }

    public TileCollection(){

    }


    public _Tile get(int[] tile_coord){
        //System.out.println();
        java.util.Iterator<_Tile> it = this.iterator();
        while (it.hasNext()){
            _Tile tile = it.next();
            //System.out.println(mapproxy.core.Python.cstr(tile_coord) + " vs " + mapproxy.core.Python.cstr(tile.coord) );
            if (java.util.Arrays.equals(tile_coord, tile.coord)){
                return tile;
            }
        }
        return null;
    }

}
