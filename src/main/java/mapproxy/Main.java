package mapproxy;
import mapproxy.wms.request.*;
import mapproxy.wms.client.*;
import mapproxy.core.grid.*;
import mapproxy.core.cache.*;
import mapproxy.core.*;
import mapproxy.wms.cache.WMSTileSource;
import mapproxy.config.Config;
import mapproxy.server.HttpServer;

import java.util.concurrent.ConcurrentHashMap;

import static jdk.nashorn.internal.runtime.regexp.joni.Syntax.Java;


//******************************************************************************
//**  Main Application
//******************************************************************************
/**
 *   Command line app used to test MapProxy.
 *
 ******************************************************************************/

public class Main {

  //**************************************************************************
  //** Main
  //**************************************************************************
  /**  Entry point for the application. Accepts command line arguments to...
   *
   *   @param args the command line arguments
   */
    public static void main(String[] args) throws Exception {
        new Main(args);
    }




  //**************************************************************************
  //** Constructor
  //**************************************************************************
    public Main(String[] args) throws Exception {


        Config.base_config().set("cache.max_tile_limit", 500);
        Config.base_config().set("image.resampling_method", "bicubic");


        String url =
                "http://localhost:6277/cgi-bin/mapserv.exe?map=/data/world.map&" +
                "layers=grid,polboundaries&" +
                "request=GetMap&service=WMS&version=1.1.1&" +
                //"srs=EPSG:3395&" + //3857, 3395, 900913 (Mercator Projections)
                //"bbox=-25,63,-13,67&" + //"bbox=-95.625,-5.625,-78.75,11.25&" +
                "srs=EPSG:2238&" +
                "bbox=967478.5103,110223.1030,2749590.4261,740927.1811&"+
                "height=256&width=256&" +
                "format=image/png";

        //url = "http://data1.geoposer.com:443/server/services/request.php?jname=/wms.img&service=WMS&srs=EPSG:4326&width=683&styles=&height=330&bbox=-107.05227272727272,40.75,-103.94772727272728,42.25&request=GetMap&layers=osm&version=1.1.1&format=image/png";

        //testCache(url);
        //testSRS();

        args = new String[]{"E:\\Documents\\Java\\IDEA\\gis-oss\\mapproxy-java\\src\\main\\resources\\config.xml"};
        startServer(args);
        //seedTest(args);
        //testWebServices();
        //testArcWebServices();

        //testTMS();

        //testWebServices();

    }





  //**************************************************************************
  //** startServer
  //**************************************************************************
  /** Used to start a HTTP Server used the service Map Requests
   */
    private void startServer(String[] args) throws Exception {

        Config.load(getConfig(args));
        int numThreads = Config.base_config().get("threads").toInteger();
        int port = Config.base_config().get("port").toInteger();

        HttpServer mapserver = new HttpServer(port, numThreads);
        mapserver.start();

    }


    private org.w3c.dom.Document getConfig(String[] args) throws Exception {
        if (args[args.length-1].toLowerCase().endsWith(".xml")){
            javaxt.io.File file = new javaxt.io.File(args[args.length-1]);
            if (!file.exists()) throw new Exception("Config file not found: " + file);
            else {
                return file.getXML();
            }
        }
        else{
            throw new Exception("Config file is required");
        }
    }



}
