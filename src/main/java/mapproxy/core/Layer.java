package mapproxy.core;
import mapproxy.wms.request.*;
import mapproxy.wms.client.*;
import mapproxy.core.grid.*;
import mapproxy.core.cache.*;
import mapproxy.wms.cache.WMSTileSource;
import mapproxy.arcgis.cache.*;
import mapproxy.arcgis.client.*;
import java.util.concurrent.ConcurrentHashMap;

//******************************************************************************
//**  Layer Class
//******************************************************************************
/**
 *   Used to represent a single map layer
 * 
 *   Note that this class was not part of the original mapproxy baseline.
 *
 ******************************************************************************/

public class Layer implements Comparable {

    private String name;
    private String description;
    private BBox bbox;
    private java.util.HashSet<String> srs = new java.util.HashSet<String>();
    private java.util.List<Layer> layers = new java.util.ArrayList<Layer>();
    private int parentID = -1;
    private java.util.HashMap<String, Object> params = new java.util.HashMap<String, Object>();


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of Layer. */

    public Layer(String name) {
        this.name = name;
    }

    
    public Layer(String name, int parentID) {
        this.name = name;
        this.parentID = parentID;
    }

  //**************************************************************************
  //** getParentID
  //**************************************************************************
  /**  Returns the parent layer ID. The parent ID should be a positive integer.
   *   Returns -1 if the parent ID is undefined or unknown.
   */
    public int getParentID(){
        return parentID;
    }


    public void setParam(String key, Object val){
        params.put(key, val);
    }

    public Object getParam(String key){
        return params.get(key);
    }


  //**************************************************************************
  //** setSource
  //**************************************************************************
  /** Used to set the base url associated with this layer, along with the
   *  protocol. This information is used to execute getMap requests.
   *
   * @param protocol The protocol to use to get a map (e.g. WMS, WCS, ArcGIS,
   *  etc).
   */
    /*
    public void setSource(String url, String[] layers, String protocol){
        //TODO Implement private variables and get methods
    }

    public void setSource(String url, String layer, String protocol){
        this.setSource(url, new String[]{layer}, protocol);
    }
    */

  //**************************************************************************
  //** addLayer
  //**************************************************************************
  /**  Adds a child layer
   */
    public void addLayer(Layer layer){
        layers.add(layer);
    }

  //**************************************************************************
  //** getLayer
  //**************************************************************************
  /**  Returns a child layer with a given name
   */
    public Layer getLayer(String name){
        java.util.Iterator<Layer> it = layers.iterator();
        while (it.hasNext()){
            Layer layer = it.next();
            if (layer.getName().equalsIgnoreCase(name)){
                return layer;
            }
        }
        return null;
    }

  //**************************************************************************
  //** getLayers
  //**************************************************************************
  /**  Returns an array of sub-layers (children) of this layer
   */
    public Layer[] getLayers(){
        return layers.toArray(new Layer[layers.size()]);
    }

  //**************************************************************************
  //** hasLayers
  //**************************************************************************
  /** Used to indicate whether this layer has layers
   */
    public boolean hasLayers(){
        return !layers.isEmpty();
    }


  //**************************************************************************
  //** addSRS
  //**************************************************************************
  /** Used to update the list of supported spatial reference systems associated
   *  with this layer.
   */
    public void addSRS(String srs){
        this.srs.add(srs);
    }

  //**************************************************************************
  //** getSRS
  //**************************************************************************
  /** Returns a list of supported spatial reference systems for this layer. */

    public String[] getSRS(){
        String[] arr = new String[srs.size()];
        int i=0;
        java.util.Iterator<String> it = srs.iterator();
        while (it.hasNext()){
            arr[i] = it.next();
            i++;
        }
        return arr;
    }



  //**************************************************************************
  //** getName
  //**************************************************************************
  /** Returns the name of the layer. */

    public String getName(){
        return name;
    }

  //**************************************************************************
  //** getDescription
  //**************************************************************************
  /** Returns the description of the layer. */

    public String getDescription(){
        return description;
    }

  //**************************************************************************
  //** setDescription
  //**************************************************************************
  /** Used to set/update the description of the layer. */

    public void setDescription(String description){
        this.description = description;
    }
    
  //**************************************************************************
  //** getBBox
  //**************************************************************************
  /** Returns the bbox for this layer. */

    public BBox getBBox(){
        return bbox;
    }


  //**************************************************************************
  //** setBBox
  //**************************************************************************
  /** Used to set/update the bbox for this layer. */

    public void setBBox(BBox bbox){
        this.bbox = bbox;
    }


    @Override
  //**************************************************************************
  //** toString
  //**************************************************************************
  /** Returns the name of the layer. */

    public String toString(){
        return name;
    }

    @Override
    public int hashCode(){
        return name.toUpperCase().hashCode();
    }


    @Override
    public int compareTo(Object obj){
        if (obj==null) return -1;
        else return -obj.toString().compareTo(name.toUpperCase());
    }





  //**************************************************************************
  //** getImage
  //**************************************************************************
  /**  Used to return an image for a given bbox.
   *  @param width Image width
   *  @param height Image height
   *  @param format Requested format
   *  @param bbox Bounding box
   *  @param srs Spatial reference system associated with the bbox.
   */
    public javaxt.io.Image getImage(int width, int height, String format, String bbox, String srs){

        FileCache fileCache = (FileCache) this.getParam("cache");
        TileGrid grid = (TileGrid) this.getParam("grid");

        String host = (String) this.getParam("host");
        if (!host.contains("?")) host += "?";
        //host += url.getQueryString();

        WMSMapRequestParams param = new WMSMapRequestParams();
        param.set("layers", (String) this.getParam("layers"));
        param.set("bbox", bbox);

        WMS111MapRequest req = new WMS111MapRequest(param, host);
        req.params.size(new int[]{width, height});
        req.params.set("srs", srs);



        //if (format.equalsIgnoreCase("image/PNG24")) format = "jpeg"; //<-- This is crazy! If I return a 24 bit PNG, ArcMap doesn't display anything. If I return a JPEG, works like a champ
        if (!format.toLowerCase().startsWith("image/")) format = "image/" + format;
        if (format.equalsIgnoreCase("image/jpg")) format = "image/jpeg";
        if (format.equalsIgnoreCase("image/PNG24") && fileCache!=null) format = "image/png";
        //format = "image/png";
        req.params.set("format", format);

        String protocol = (String) this.getParam("protocol");
        if (protocol.equalsIgnoreCase("WMS")){

            if (fileCache==null){
                return new WMSClient(req).get_map(req);
            }
            else{
                WMSTileSource wms = new WMSTileSource(grid, new WMSClient(req));
                CacheManager cacheManager = new CacheManager(fileCache, wms);
                Cache cache = new Cache(cacheManager, grid, true);
                return cache.image(req.params.bbox(), new SRS(req.params.srs()), req.params.size());
            }
        }
        else if (protocol.equalsIgnoreCase("MapServer")){

            if (fileCache==null){
                return new MapClient(req).get_map(req);
            }
            else{
                MapTileSource arcMapServer = new MapTileSource(grid, new MapClient(req));
                CacheManager cacheManager = new CacheManager(fileCache, arcMapServer);
                Cache cache = new Cache(cacheManager, grid, true);
                return cache.image(req.params.bbox(), new SRS(req.params.srs()), req.params.size());
            }


        }
        else if (protocol.equalsIgnoreCase("TMS")){
            if (fileCache==null){

                Object[] affected_tiles = grid.get_affected_tiles(new BBox(bbox).toArray(), new int[]{width, height}, new SRS(srs), false);
                double[] box = (double[]) affected_tiles[0];
                int[] _grid = (int[]) affected_tiles[1];
                Generator<int[]> tiles = (Generator<int[]>) affected_tiles[2];

            }
            else{

            }
        }

        return null;

    }


    public javaxt.io.Image getImage(int width, int height, String format, double[] bbox, String srs){
        return this.getImage(width, height, format, bbox[0] + "," + bbox[1] + "," + bbox[2] + "," + bbox[3], srs);
    }



  //**************************************************************************
  //** seedLayers
  //**************************************************************************
  /** Used to seed the current layer at a given point
   *
   *  @param bbox Bounding box
   *  @param srs Spatial reference system associated with the bbox.
   *  @param levels Number of resolution levels
   *  @return Returns a HashMap with status information. Keys include
   *  "Total Files", "Total Processed", and "Is Complete".
   */
    public Status seed(int numThreads, double[] bbox, String srs, int levels){
        if (numThreads<1) numThreads = 1;
        for (int i=0; i<numThreads; i++){
            Thread t = new Thread(new TileSeeder());
            t.start();
        }



        return TileSeeder.addLayer(this, bbox, levels);
    }

}