package mapproxy.kml.server;
import mapproxy.tms.TileRequest;
import mapproxy.tms.TileServiceGrid;
import mapproxy.core.grid.TileGrid;
import mapproxy.core.cache.FileCache;
import mapproxy.core.*;

//******************************************************************************
//**  KMLServer Class
//******************************************************************************
/**
 *   OGC KML 2.2 Server
 *
 ******************************************************************************/

public class KMLServer {

    private Service service;


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of KMLServer.   */

    public KMLServer(Service service){
        this.service = service;
    }


  //**************************************************************************
  //** getCapabilities
  //**************************************************************************
  /**  Returns a KML Document with links to individual layers in this service.
   *   Note that this method was not part of the original mapproxy baseline.
   */
    public String getCapabilities(){
        StringBuffer kml = new StringBuffer();
        kml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        kml.append("<kml xmlns=\"http://earth.google.com/kml/2.0\">");
        kml.append("<Document>");
        kml.append("<name>" + service.getName() + "</name>");
        kml.append("<description>" + service.getDescription() + "</description>");
        kml.append("<open>1</open>");

        kml.append("<Style id=\"checkHideChildrenExample\">");
        kml.append("<ListStyle>");
        kml.append("<listItemType>checkHideChildren</listItemType>");
        kml.append("</ListStyle>");
        kml.append("</Style>");

        for (Layer layer : service.getLayers()){
            kml.append("<Folder>");
            kml.append("<name>" + layer.getName() + "</name>");
            //kml.append("<description>" + layer.getDescription() + "</description>");
            kml.append("<open>0</open>");
            kml.append("<styleUrl>#checkHideChildrenExample</styleUrl>");

            
            double[] bbox = layer.getBBox().toArray();
            TileGrid grid = (TileGrid) layer.getParam("grid");
            if (grid==null) grid = new TileGrid(4326, new int[]{256,256});

            Object[] affected_tiles = grid.get_affected_tiles(bbox, grid.tile_size, grid.srs, false);
            Generator<int[]> tiles = (Generator<int[]>) affected_tiles[2];
            kml.append("<Folder>");

            for (int[] tile_coord : tiles){
                double[] b = _tile_bbox(tile_coord, grid);
                kml.append(this.getNetworkLink(layer, new SubTile(tile_coord, b)));
            }

            kml.append("<GroundOverlay>");
            kml.append("<name>" + layer.getName() + "</name>");
            kml.append("</GroundOverlay>");

            kml.append("</Folder>");

            kml.append("</Folder>");

        }

        kml.append("</Document>");
        kml.append("</kml>");
        return kml.toString();
    }

    


  //**************************************************************************
  //** getKML
  //**************************************************************************
  /**  Returns a KML Superoverlay
   */
    public String getKML(javaxt.utils.URL url){
        //System.out.println(url);

        TileRequest request = new TileRequest(url.getPath());
        Layer layer = service.getLayer(request.layer);
        int[] tile_coord = request.tile;


        tile_coord[2] = tile_coord[2]+1; //<-- Note the the z value is incremented by 1
        //The z value for the tile_coord and the subtile coord is then updated in the kml
        //Maybe we should do this instead:
        //request.tile = new mapproxy.tms.TileServiceGrid(grid).internal_tile_coord(request.tile, true);

        
        TileGrid grid = (TileGrid) layer.getParam("grid");
        if (grid==null) grid = new TileGrid(4326, new int[]{256,256});


        boolean initial_level = false;
        if (tile_coord[2] == 0)
            initial_level = true;

        double[] bbox = _tile_bbox(tile_coord, grid);
        SubTile tile = new SubTile(tile_coord, bbox);
        SubTile[] subtiles = _get_subtiles(tile_coord, grid);


        String format = "jpg";
        FileCache cache = (FileCache) layer.getParam("cache");
        if (cache!=null) format = cache.file_ext;



        StringBuffer kml = new StringBuffer();
        kml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        kml.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
        kml.append("<Document>");
        if (initial_level){
            kml.append("<name>" + layer.getName() + "</name>");
        }
        else{
            kml.append("<name>" + layer.getName() + "-" + tile.coord + "</name>");
        }
        kml.append("<Region>");
        kml.append("<LatLonAltBox>");
        kml.append("<north>" + tile.bbox[3] + "</north><south>" + tile.bbox[1] + "</south>");
        kml.append("<east>" + tile.bbox[2] + "</east><west>" + tile.bbox[0] + "</west>");
        kml.append("</LatLonAltBox>");
        kml.append("<Lod>");
        kml.append("<minLodPixels>100</minLodPixels>");
        if (initial_level){
            kml.append("<maxLodPixels>-1</maxLodPixels>");
        }
        else{
            kml.append("<maxLodPixels>256</maxLodPixels>");
        }

        kml.append("<minFadeExtent>20</minFadeExtent>");
        kml.append("<maxFadeExtent>10</maxFadeExtent>");
        kml.append("</Lod>");
        kml.append("</Region>");


        for (SubTile subtile : subtiles){
            kml.append(this.getNetworkLink(layer, subtile));
        }

        kml.append("<GroundOverlay>");
        kml.append("<name>" + tile.coord + "</name>");
        kml.append("<drawOrder>" + tile.coord[2] + "</drawOrder>");
        kml.append("<Icon>");
        kml.append("<href>" + service.getURL() + "/kml/" + layer.getName() + "/" + (tile.coord[2]-1) + "/" + tile.coord[0] + "/" + tile.coord[1] + "." + format + "</href>");
        kml.append("</Icon>");
        kml.append("<LatLonBox>");
        kml.append("<north>" + tile.bbox[3] + "</north><south>" + tile.bbox[1] + "</south>");
        kml.append("<east>" + tile.bbox[2] + "</east><west>" + tile.bbox[0] + "</west>");
        kml.append("</LatLonBox>");
        kml.append("</GroundOverlay>");


        kml.append("</Document>");
        kml.append("</kml>");
        return kml.toString();
    }


    private String getNetworkLink(Layer layer, SubTile subtile){

        StringBuffer kml = new StringBuffer();

        kml.append("<NetworkLink>");
        kml.append("<name>" + layer.getName() + "-" + subtile.coord + "</name>");
        kml.append("<Region>");
        kml.append("<LatLonAltBox>");
        kml.append("<north>" + subtile.bbox[3] + "</north><south>" + subtile.bbox[1] + "</south>");
        kml.append("<east>" + subtile.bbox[2] + "</east><west>" + subtile.bbox[0] + "</west>");
        kml.append("</LatLonAltBox>");
        kml.append("<Lod>");
        kml.append("<minLodPixels>128</minLodPixels>");
        kml.append("<maxLodPixels>-1</maxLodPixels>");
        kml.append("</Lod>");
        kml.append("</Region>");
        kml.append("<Link>");
        kml.append("<href>" + service.getURL() + "/kml/" + layer.getName() + "/" + (subtile.coord[2]-1) + "/" + subtile.coord[0] + "/" + subtile.coord[1] + ".kml</href>");
        kml.append("<viewRefreshMode>onRegion</viewRefreshMode>");
        kml.append("<viewFormat/>");
        kml.append("</Link>");
        kml.append("</NetworkLink>");

        return kml.toString();

    }





  //**************************************************************************
  //** _get_subtiles
  //**************************************************************************
  /*  Create four `SubTile` for the next level of `tile`.
   */
    private SubTile[] _get_subtiles(int[] tile, TileGrid grid){ //Layer layer

      //x, y, z = tile
        int x = tile[0];
        int y = tile[1];
        int z = tile[2];

        java.util.ArrayList<SubTile> subtiles = new java.util.ArrayList<SubTile>();
        for (int[] coord : new int[][]{new int[]{x*2, y*2, z+1}, new int[]{x*2+1, y*2, z+1},
                      new int[]{x*2+1, y*2+1, z+1}, new int[]{x*2, y*2+1, z+1}}){
            double[] bbox = _tile_bbox(coord, grid);
            if (bbox != null)
                subtiles.add(new SubTile(coord, bbox));
        }
        return subtiles.toArray(new SubTile[subtiles.size()]);
    }


  //**************************************************************************
  //** _tile_bbox
  //**************************************************************************

    private double[] _tile_bbox(int[] tile_coord, TileGrid grid){

        //tile_coord = grid.internal_tile_coord(tile_coord, false);
        tile_coord = new TileServiceGrid(grid).internal_tile_coord(tile_coord, false);


        if (tile_coord == null)
            return null;

        double[] src_bbox = grid.tile_bbox(tile_coord[0], tile_coord[1], tile_coord[2]);
        double[] bbox = grid.srs.transform_bbox_to(new SRS(4326), src_bbox, 4);
        if (grid.srs.equals(new SRS(900913))){
            //bbox = list(bbox)
            if (Math.abs(src_bbox[1] -  -20037508.342789244) < 0.1)
                bbox[1] = -90.0;
            if (Math.abs(src_bbox[3] -  20037508.342789244) < 0.1)
                bbox[3] = 90.0;
        }

        mapproxy.core.Python.cstr(src_bbox);
        mapproxy.core.Python.cstr(bbox);
        return bbox;
    }


  //**************************************************************************
  //** SubTile Class
  //**************************************************************************
  /*  Contains the "bbox" and "coord" of a sub tile.
   */
    private class SubTile{
        public int[] coord;
        public double[] bbox;
        public SubTile(int[] coord, double[] bbox){
            this.coord = coord;
            this.bbox = bbox;
        }
    }
}