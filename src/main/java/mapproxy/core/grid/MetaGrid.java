package mapproxy.core.grid;
import mapproxy.core.Python;
import mapproxy.core.Generator;

//******************************************************************************
//**  MetaGrid Class
//******************************************************************************
/**
 *   This class contains methods to calculate bbox, etc. of metatiles.
 *
 ******************************************************************************/

public class MetaGrid extends Grid {

    private TileGrid grid;
    private int meta_buffer;
    private int[] _meta_size;


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of MetaGrid.
   *
   * @param grid the grid to use for the metatiles
   * @param meta_size the number of tiles a metatile consist (x_size, y_size)
   * @param meta_buffer the buffer size in pixel that is added to each metatile.
   *    the number is added to all four borders.
   *    this buffer may improve the handling of lables overlapping (meta)tile borders.
   * :type meta_buffer: pixel
   * 
   */
    public MetaGrid(TileGrid grid, int[] meta_size, int meta_buffer){
        this.grid = grid;
        this._meta_size = meta_size;
        this.meta_buffer = meta_buffer;
    }

    public MetaGrid(TileGrid grid, int[] meta_size){
        this(grid, meta_size, 0);
    }


  //**************************************************************************
  //** meta_bbox
  //**************************************************************************
  /**  Returns the bbox of the metatile that contains `tile_coord`.
    <pre>
        >>> mgrid = MetaGrid(grid=TileGrid(), meta_size=(2, 2))
        >>> [round(x, 2) for x in mgrid.meta_bbox((0, 0, 2))]
        [-20037508.34, -20037508.34, 0.0, 0.0]
        >>> mgrid = MetaGrid(grid=TileGrid(), meta_size=(2, 2))
        >>> [round(x, 2) for x in mgrid.meta_bbox((0, 0, 0))]
        [-20037508.34, -20037508.34, 20037508.34, 20037508.34]
    </pre>
   *
   * @param tile_coord ``(x, y, z)``
   */
    public double[] meta_bbox(int[] tile_coord){


        //x, y, z = tile_coord
        int[] coord = tile_coord;
        int x = coord[0];
        int y = coord[1];
        int z = coord[2];

                
        int[] meta_size = meta_size(z);

        if (z == 0 && meta_size[0] == 1 && meta_size[1] == 1){ //if (z == 0 && meta_size == (1, 1)){
            return this.grid.tile_bbox(0, 0, 0);
        }

        int meta_x = Python.doubleDiv(x, meta_size[0]);
        int meta_y = Python.doubleDiv(y, meta_size[1]);

        double[] bbox = this.grid.tile_bbox(meta_x * meta_size[0],
                                            meta_y * meta_size[1], z);

        //(minx, miny, maxx, maxy)
        double minx = bbox[0];
        double miny = bbox[1];
        double maxx = bbox[2];
        double maxy = bbox[3];



        double width = (maxx - minx) * meta_size[0];
        double height = (maxy - miny) * meta_size[1];
        maxx = minx + width;
        maxy = miny + height;


        if (this.meta_buffer > 0){
            double res = this.grid.resolution(z);
            minx -= this.meta_buffer * res;
            miny -= this.meta_buffer * res;
            maxx += this.meta_buffer * res;
            maxy += this.meta_buffer * res;
        }

        return new double[]{minx, miny, maxx, maxy};
    }


  //**************************************************************************
  //** tiles
  //**************************************************************************
  /**  Returns all tiles that belong to the same metatile as `tile_coord`. 
   *   The result contains for each tile the ``tile_coord`` and the upper-left
   *   pixel coordinate of the tile in the meta tile image.
    <pre>
        >>> mgrid = MetaGrid(grid=TileGrid(), meta_size=(2, 2))
        >>> tiles = list(mgrid.tiles((0, 1, 1)))
        >>> tiles[0], tiles[-1]
        (((0, 1, 1), (0, 0)), ((1, 0, 1), (256, 256)))
        >>> list(mgrid.tiles((0, 0, 0)))
        [((0, 0, 0), (0, 0))]
    </pre>
   */
    public Generator<TileCoordinate> tiles(int[] tile_coord){



        //x, y, z = tile_coord
        int x = tile_coord[0];
        int y = tile_coord[1];
        final int z = tile_coord[2];


        /*
        meta_size = self.meta_size(z)
        if z == 0 and meta_size == (1, 1):
            yield ((0, 0, 0), (0, 0))
            raise StopIteration
        */
        final int[] meta_size = this.meta_size(z);
        if (z == 0 && (meta_size[0] == 1 && meta_size[1] == 1)){

            return new Generator<TileCoordinate>() {

                @Override
                public void run() {
                    TileCoordinate coord = new TileCoordinate( new int[]{0, 0, 0}, new int[]{0, 0} );
                    yield (coord);
                    //raise StopIteration
                }
            };
        }



      //x0 = x//meta_size[0] * meta_size[0]
      //y0 = y//meta_size[1] * meta_size[1]

        final int x0 = Python.doubleDiv(x, meta_size[0]) * meta_size[0];
        final int y0 = Python.doubleDiv(y, meta_size[1]) * meta_size[1];
/*
System.out.println(x + "//" + meta_size[0] + " * " + meta_size[0]);
System.out.println(">>>" + x0);
System.out.println(y + "//" + meta_size[1] + " * " + meta_size[1]);
System.out.println(">>>" + y0);
*/
        /*
        for i, y in enumerate(range(y0+(meta_size[1]-1), y0-1, -1)):
            for j, x in enumerate(range(x0, x0+meta_size[0])):
                yield (x, y, z), (j*self.grid.tile_size[0] + self.meta_buffer,
                                  i*self.grid.tile_size[1] + self.meta_buffer)
        */
        Generator<TileCoordinate> iterator = new Generator<TileCoordinate>(){
            @Override
            public void run() {

                //System.out.println(y0+(meta_size[1]-1) + "," + (y0-1) + "," + -1);
                //System.out.println(x0 + "," + (x0+meta_size[0]));
                int i = 0;
                for (int y : (Python.range(y0+(meta_size[1]-1), y0-1, -1)) ){
                    int j = 0;
                    for (int x : Python.range(x0, x0+meta_size[0]) ){
                        yield (new TileCoordinate(new int[] {x, y, z}, new int[]{
                                    j*grid.tile_size[0] + meta_buffer,
                                    i*grid.tile_size[1] + meta_buffer
                        }));

                        j++;
                    }
                    i++;
                }
            }
        };


        return iterator;
    }





  //**************************************************************************
  //** tile_size
  //**************************************************************************
  /** Returns the size of a metatile (includes ``meta_buffer`` if present).
   <pre>
      >>> mgrid = MetaGrid(grid=TileGrid(), meta_size=(2, 2), meta_buffer=10)
      >>> mgrid.tile_size(2)
      (532, 532)
      >>> mgrid.tile_size(0)
      (256, 256)
   </pre>
   *
   *  @param level the zoom level
   */
    public int[] tile_size(int level){

        int[] meta_size = this.meta_size(level);

        if (level == 0 && meta_size[0] == 1 && meta_size[1] == 1){
            return this.grid.tile_size;
        }

        return new int[]{this.grid.tile_size[0] * meta_size[0] + 2*this.meta_buffer,
                this.grid.tile_size[1] * meta_size[1] + 2*this.meta_buffer};
    }


  //**************************************************************************
  //** meta_size
  //**************************************************************************
    public int[] meta_size(int level){
        int[] grid_size = this.grid.grid_sizes.get(level);
        return new int[]{
            java.lang.Math.min(this._meta_size[0], grid_size[0]),
            java.lang.Math.min(this._meta_size[1], grid_size[1])
        };
    }

}