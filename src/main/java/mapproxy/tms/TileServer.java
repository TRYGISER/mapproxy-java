package mapproxy.tms;
import mapproxy.core.*;
import mapproxy.core.cache.FileCache;
import mapproxy.core.grid.TileGrid;

//******************************************************************************
//**  TileServer Class
//******************************************************************************
/**
 *   A Tile Server. Supports strict TMS and non-TMS requests. The difference is
 *   the support for profiles. The our internal tile cache starts with one tile
 *   at the first level (like KML, etc.), but the global-geodetic and
 *   global-mercator start with two and four tiles. The ``tile_request`` should
 *   set ``use_profiles`` accordingly (eg. False if first level is one tile)
 *
 ******************************************************************************/

public class TileServer {

    private Service service;


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of TileServer.   */

    public TileServer(Service service){
        this.service = service;
    }

  //**************************************************************************
  //** TMS Capabilities
  //**************************************************************************
  /** Returns an XML document suitable for a TileMapService request. */

    public String getCapabilities(){

        String url = service.getURL();

        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        xml.append("<TileMapService version=\"1.0.0\">");
        xml.append("<Title>" + service.getTitle() + "</Title>");
        xml.append("<Abstract>" + service.getDescription() + "</Abstract>");
        xml.append("<TileMaps>");
        for (Layer layer : service.getLayers()){

            xml.append(
                "<TileMap title=\"" + layer.getName() + "\" " +
                 "srs=\"" + layer.getSRS()[0] + "\" " + //layer.grid.srs_name
                 "profile=\"global-geodetic\" " +  //layer.grid.profile
                 "href=\"" + url + layer.getName() + "/\"/>");
        }
        xml.append("</TileMaps>");
        xml.append("</TileMapService>");
        return xml.toString();
    }


  //**************************************************************************
  //** getLayerInfo
  //**************************************************************************
  /** Used to construct a response to a TMS Capabilities request for a given
   *  layer.
   */
    public String getLayerInfo(Layer layer) {

        String layerName = layer.getName();


        String url = service.getURL();
        if (url.contains("?")) url = url.substring(0, url.indexOf("?"));
        if (!url.endsWith("/")) url += "/";

        
        url += layerName + "/";


        TileGrid grid = (TileGrid) layer.getParam("grid");
        if (grid!=null){
            grid = new TileGrid(4326, new double[]{-180,-90,180,90}, new int[]{256,256}, null, true, 21);
            //SRS srs = new SRS(layer.getSRS()[0]);
            //grid = new TileGrid(srs.get_epsg_num(srs), layer.getBBox().toArray(), new int[]{256,256}, null, srs.isGeographic(), 21);
        }

        int[] tileSize = grid.tile_size;
        int tileWidth = tileSize[0];
        int tileHeight = tileSize[1];

        String tileFormat = "png";
        FileCache fileCache = (FileCache) layer.getParam("cache");
        if (fileCache!=null){
            tileFormat = fileCache.file_ext;
        }

        String mimeType = "image/"+tileFormat.toLowerCase();
        if (tileFormat.equalsIgnoreCase("jpg")) mimeType = "image/jpeg";


      //Get Capabilities
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        xml.append("<TileMap version=\"1.0.0\" tilemapservice=\"" + url + "1.0.0/\">");
        xml.append("<Title>" + layerName + "</Title>");
        xml.append("<Abstract>" + layer.getDescription() + "</Abstract>");
        xml.append("<SRS>EPSG:4326</SRS>");
        xml.append("<BoundingBox minx=\"-180.000000\" miny=\"-90.000000\" maxx=\"180.000000\" maxy=\"90.000000\" />");
        xml.append("<Origin x=\"-180.000000\" y=\"-90.000000\" />");
        xml.append("<TileFormat width=\"" + tileWidth + "\" height=\"" + tileHeight + "\" mime-type=\"" + mimeType + "\" extension=\"" + tileFormat + "\" />");
        xml.append("<TileSets>");

        for (int i=0; i<grid.levels; i++){
            if (i>0){
                int level = i-1;
                xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/" + level + "\" units-per-pixel=\"" + formatNum(grid.resolution(i)) + "\" order=\"" + level + "\" />");
            }
        }

        /*
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/0\" units-per-pixel=\"0.70312500000000000000\" order=\"0\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/1\" units-per-pixel=\"0.35156250000000000000\" order=\"1\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/2\" units-per-pixel=\"0.17578125000000000000\" order=\"2\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/3\" units-per-pixel=\"0.08789062500000000000\" order=\"3\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/4\" units-per-pixel=\"0.04394531250000000000\" order=\"4\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/5\" units-per-pixel=\"0.02197265625000000000\" order=\"5\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/6\" units-per-pixel=\"0.01098632812500000000\" order=\"6\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/7\" units-per-pixel=\"0.00549316406250000000\" order=\"7\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/8\" units-per-pixel=\"0.00274658203125000000\" order=\"8\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/9\" units-per-pixel=\"0.00137329101562500000\" order=\"9\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/10\" units-per-pixel=\"0.00068664550781250000\" order=\"10\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/11\" units-per-pixel=\"0.00034332275390625000\" order=\"11\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/12\" units-per-pixel=\"0.00017166137695312500\" order=\"12\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/13\" units-per-pixel=\"0.00008583068847656250\" order=\"13\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/14\" units-per-pixel=\"0.00004291534423828125\" order=\"14\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/15\" units-per-pixel=\"0.00002145767211914062\" order=\"15\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/16\" units-per-pixel=\"0.00001072883605957031\" order=\"16\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/17\" units-per-pixel=\"0.00000536441802978516\" order=\"17\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/18\" units-per-pixel=\"0.00000268220901489258\" order=\"18\" />");
        xml.append("<TileSet href=\"" + url + "1.0.0/" + layerName + "/19\" units-per-pixel=\"0.00000134110450744629\" order=\"19\" />");

        */
        xml.append("</TileSets>");
        xml.append("</TileMap>");

        return xml.toString();

    }


    private static final int MAX_LENGTH = ("0.70312500000000000000".length());

    private static String formatNum(double number) {
        int digitsAvailable = MAX_LENGTH - 2;
        if (Math.abs(number) < Math.pow(10, digitsAvailable)
                && Math.abs(number) > Math.pow(10, -digitsAvailable)) {
            String format = "0.";
            double temp = number;
            for (int i = 0; i < digitsAvailable; i++) {
                if ((temp /= 10) < 1) {
                    format += "#";
                }
            }
            String r = new java.text.DecimalFormat(format).format(number);
            while (r.length() < MAX_LENGTH){
                r+= "0";
            }
            return r;
        }
        String format = "0.";
        for (int i = 0; i < digitsAvailable; i++) {
            format += "#";
        }
        String r = new java.text.DecimalFormat(format + "E0").format(number);
        int lastLength = r.length() + 1;
        while (r.length() > MAX_LENGTH && lastLength > r.length()) {
            lastLength = r.length();
            r = r.replaceAll("\\.?[0-9]E", "E");
        }

        while (r.length() < MAX_LENGTH){
            r+= "0";
        }

        return r;
    }


}
