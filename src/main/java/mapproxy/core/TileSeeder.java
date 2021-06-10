package mapproxy.core;
import mapproxy.core.cache.*;
import mapproxy.core.grid.TileGrid;
import java.util.concurrent.ConcurrentHashMap;

//******************************************************************************
//**  TileSeeder Class
//******************************************************************************
/**
 *   Thread used to generate a tile cache
 *
 ******************************************************************************/

public class TileSeeder implements Runnable {

    private static java.util.List<Object[]> seeds = new java.util.LinkedList<Object[]>();

    private static ConcurrentHashMap<Integer, Layer> layers = new ConcurrentHashMap<Integer, Layer>();

    private static ConcurrentHashMap<Integer, Status> stats = new ConcurrentHashMap<Integer, Status>();



  //**************************************************************************
  //** TileIterator
  //**************************************************************************
  /**  Class used to iterate through tiles and add them to the queue */

    private static class TileIterator implements Runnable {
        int layerID;
        private double[] bbox;
        private int levels;
        private TileIterator(int layerID, double[] bbox, int levels){
            this.layerID = layerID;
            this.bbox = bbox;
            this.levels = levels;
        }
        public void run(){



            Layer layer;
            synchronized (layers) {
                layer = layers.get(layerID);
                layers.notifyAll();
            }

            TileGrid grid = (TileGrid) layer.getParam("grid");


            int numTiles = 0;
            for (int level=0; level<levels; level++){
                Object[] arr = _create_tile_iterator(grid, bbox, level);
                //int est_number_of_tiles = (Integer) arr[0];
                Generator<int[]> tiles = (Generator<int[]>) arr[1];

                
                for (int[] tile : tiles){

                    synchronized (seeds) {
                       seeds.add(seeds.size(), new Object[]{layerID, tile});
                       seeds.notifyAll();
                    }

                    numTiles++;

                }

            }


          //Update the stats for this layer
            synchronized (stats){

                    Status status = stats.get(layerID);
                    synchronized (status){
                        status.setTotal(numTiles);
                        status.notifyAll();
                    }            

                //stats.get(layerID).setTotal(numTiles);
                stats.notifyAll();
            }


            /*
              //Add null tile to notify the threads that we are done
                synchronized (seeds) {
                    seeds.add(seeds.size(), new Object[]{layerID, numTiles});
                    seeds.notifyAll();
                }
            */


        }
    }


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of TileSeeder.   */

    public TileSeeder(){
    }



  //**************************************************************************
  //** addLayer
  //**************************************************************************
  /** Used to add a layer for the TileSeeder to process.   */

    public static Status addLayer(Layer layer, double[] bbox, int levels){


        FileCache cache = (FileCache) layer.getParam("cache");
        TileGrid grid = (TileGrid) layer.getParam("grid");
        if (cache==null || grid==null) return null;


        int layerID;
        synchronized (layers) {
            layerID = layers.size();
            layers.put(layerID, layer);
            layers.notifyAll();
        }


        Status status = new Status();

        synchronized (stats){
            stats.put(layerID, status);
            stats.notifyAll();
        }        

        new Thread(new TileIterator(layerID, bbox, levels)).run();
        
        return status;

    }


    /*
    public static void stop(){
        synchronized (seeds) {
            seeds.add(seeds.size(), null);
            seeds.notifyAll();
        }
    }
    */


  //**************************************************************************
  //** run
  //**************************************************************************
  /** Used to process tiles added to the queue.   */

    public void run(){
        while (true) {

        //Find request in pool
          Object[] arr;
          synchronized (seeds) {
            while (seeds.isEmpty()) {
              try {
                seeds.wait();
              }
              catch (InterruptedException e) {
                  e.printStackTrace();
              }
            }
            arr = (Object[]) seeds.remove(0);
            if (arr==null){
                seeds.add(null);
                seeds.notifyAll();
                return;
            }
          }




          Layer layer;
          int layerID = (Integer) arr[0];
          synchronized (layers) {
              layer = layers.get(layerID);
              layers.notifyAll();
          }

          Object obj = arr[1];
          if (obj instanceof int[]){
              int[] tile = (int[]) obj;
              FileCache cache = (FileCache) layer.getParam("cache");
              TileGrid grid = (TileGrid) layer.getParam("grid");
              layer.getImage(grid.tile_size[0], grid.tile_size[1], cache.file_ext, grid.tile_bbox(tile[0],tile[1],tile[2]), grid.srs.toString());
              /*
              System.out.println(Python.cstr(tile));
              try{
              Thread.sleep(500);
              }
              catch(Exception e){}
              */

                synchronized (stats){
                    //Integer numTiles = (Integer) stats.get(layerID).get("Total Processed");
                    //if (numTiles==null) numTiles = 0;
                    Status status = stats.get(layerID);
                    synchronized (status){
                        status.updateTileCount();
                        status.notifyAll();
                    }
                    
                    stats.notifyAll();
                }


              /*
                synchronized (stats){
                    Integer total = (Integer) stats.get(layerID).get("Total Files");
                    if (total!=null){
                        Integer numTiles = (Integer) stats.get(layerID).get("Total Processed");
                        if (numTiles==null) numTiles = 0;
                        if (numTiles==total){
                            stats.get(layerID).put("Is Complete", true);
                        }
                    }
                    stats.notifyAll();
                }
              */


          }




        } // end while
    }


    


  //**************************************************************************
  //** _create_tile_iterator
  //**************************************************************************
  /**  Return all tiles that intersect the `bbox` on `level`.
   *  @return estimated number of tiles, tile iterator
   */
    private static Object[] _create_tile_iterator(TileGrid grid, double[] bbox, int level){

        double res = grid.resolution(level);
        int[] pixelSize = bbox_pixel_size(bbox, res);
        int w = pixelSize[0];
        int h = pixelSize[1];

        Object[] affected_tiles = grid.get_affected_tiles(bbox, pixelSize, null, false);
        bbox = (double[]) affected_tiles[0];
        //int[] _grid = (int[]) affected_tiles[1];
        Generator<int[]> tiles = (Generator<int[]>) affected_tiles[2];

        int est_number_of_tiles = cint((w/grid.tile_size[0] *
                                            h/grid.tile_size[1]));
        return new Object[]{est_number_of_tiles, tiles};
    }


  //**************************************************************************
  //** bbox_pixel_size
  //**************************************************************************
  /**  Return the size of the `bbox` in pixel at the given `res`.
   */
    private static int[] bbox_pixel_size(double[] bbox, double res){
        double w = bbox[2] - bbox[0];
        double h = bbox[3] - bbox[1];
        return new int[]{cint(w/res), cint(h/res)};
    }

    private static int cint(double d){
        return javaxt.utils.string.toInt(d);
    }

}
