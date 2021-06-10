package mapproxy.test;

import mapproxy.config.Config;
import mapproxy.core.*;
import mapproxy.core.cache.Cache;
import mapproxy.core.cache.CacheManager;
import mapproxy.core.cache.FileCache;
import mapproxy.core.grid.TileGrid;
import mapproxy.wms.cache.WMSTileSource;
import mapproxy.wms.client.WMSClient;
import mapproxy.wms.request.WMS111MapRequest;
import mapproxy.wms.request.WMSMapRequestParams;
import org.junit.Test;

public class TestUtl {

    @Test
    public void testCapabilitiesParser() {
        String url = "http://tilecache.osgeo.org/wms-c/Basic.py/1.0.0/";
        url = "http://geoposer.com:443/server/services/osm.xml";
        url = "http://ewdev.spadac.com/earthwhere-api/cgi-bin/mapserv.exe?map=E:/EarthWhere/Data/BaseMap/world.map&service=WMS&request=getcapabilities";

        Service service = new Service(url);
        System.out.println("Name:  " + service.getName());
        System.out.println("Title:  " + service.getTitle());
        System.out.println("URL:  " + service.getURL());
        for (mapproxy.core.Layer layer : service.getLayers()) {
            System.out.println(" - " + layer.getName());
        }
    }

    public void testTMS() {


/*
        FileCache fileCache = new FileCache("/temp/mapproxy/java/ew/", "png");
        int[] tile_size = new int[]{256,256};
        TileGrid grid = new TileGrid(4326, tile_size);

        WMS111MapRequest req = getWMS111MapRequest(url);
        WMSTileSource wms = new WMSTileSource(grid, new WMSClient[]{new WMSClient(req)});

 */

        FileCache fileCache = new FileCache("/temp/mapproxy/world.map/", "png");
        int[] tile_size = new int[]{256, 256};
        TileGrid grid = new TileGrid(4326, tile_size);


        String url = "http://localhost:9080/tms/Default%20Basemap/2/9/13.png";
        mapproxy.tms.TileRequest req = new mapproxy.tms.TileRequest(url);


/*
        System.out.println(Python.cstr(req.tile));
        System.out.println(fileCache.tile_location(new _Tile(req.tile), false));

        int[] coord = req.tile;
        System.out.println(Python.cstr(grid.tile_bbox(coord[0], coord[1], coord[2])));
*/

        mapproxy.tms.cache.TMSTileSource tms = new mapproxy.tms.cache.TMSTileSource(grid, url);

        CacheManager cacheManager = new CacheManager(fileCache, tms);
        Cache cache = new Cache(cacheManager, grid, true);


        //Update the
        mapproxy.tms.TileServiceGrid g = new mapproxy.tms.TileServiceGrid(grid);
        req.tile = g.internal_tile_coord(req.tile, true);

        int[] coord = req.tile;
        System.out.println(Python.cstr(grid.tile_bbox(coord[0], coord[1], coord[2])));

        //System.out.println(cache.tile(req.tile).source);


        //http://localhost:9080/ArcGIS/?LAYERS=DefaultBasemap&FORMAT=png&BBOX=-12.897806,38.540149,-0.043791,51.394164&SIZE=256,256&F=image&BBOXSR=4326&IMAGESR=4326
        //cache.image(req.params.bbox(), new SRS(req.params.srs()), req.params.size()).saveAs("/temp/ew_cache.png");


        /*
        mapproxy.tms.TileServiceLayer lyr = new mapproxy.tms.TileServiceLayer(null, cache);
        lyr.render(req, false);
        */


//
    }


    public void testSRS() {

    /*

            >>> srs1 = SRS(4326)
            >>> srs2 = SRS(900913)
            >>> [str(round(x, 5)) for x in srs1.transform_to(srs2, (8.22, 53.15))]
            ['915046.21432', '7010792.20171']
            >>> srs1.transform_to(srs1, (8.25, 53.5))
            (8.25, 53.5)
            >>> [(str(round(x, 5)), str(round(y, 5))) for x, y in
            ...  srs1.transform_to(srs2, [(8.2, 53.1), (8.22, 53.15), (8.3, 53.2)])]
            ... #doctest: +NORMALIZE_WHITESPACE
            [('912819.8245', '7001516.67745'),
             ('915046.21432', '7010792.20171'),
             ('923951.77358', '7020078.53264')]

     */
        mapproxy.core.SRS srs1 = new mapproxy.core.SRS(4326);
        mapproxy.core.SRS srs2 = new mapproxy.core.SRS(900913);

        java.util.ArrayList<mapproxy.core.Point> points = new java.util.ArrayList<mapproxy.core.Point>();
        //points.add(new mapproxy.core.Point(53.15, 8.22));
        points.add(new mapproxy.core.Point(8.22, 53.15));
        points = srs1.transform_to(srs2, points);
        for (mapproxy.core.Point point : points) {
            System.out.println(point);
        }

        points = new java.util.ArrayList<mapproxy.core.Point>();
        points.add(new mapproxy.core.Point(915046.21432, 7010792.20171));
        points = srs2.transform_to(srs1, points);
        for (mapproxy.core.Point point : points) {
            System.out.println(point);
        }


        //967478.5103, 110223.1030, 2749590.4261, 740927.1811

        srs2 = new mapproxy.core.SRS(2238);
        points = new java.util.ArrayList<mapproxy.core.Point>();
        points.add(new mapproxy.core.Point(967478.5103, 110223.1030));
        points.add(new mapproxy.core.Point(2749590.4261, 740927.1811));
        points = srs2.transform_to(srs1, points);
        for (mapproxy.core.Point point : points) {
            System.out.println(point);
        }

    }

    @Test
    public void testWebServices() throws Exception {

        //String url = "http://localhost:9080/ArcGIS/";
        String url = "http://services.arcgisonline.com/ArcGIS/services/USA_Topo_Maps/MapServer";
        //url = "http://services.arcgisonline.com/ArcGIS/services/";

        //url = "http://sampleserver1.arcgisonline.com/ArcGIS/services/";
        //url += "Specialty/ESRI_StateCityHighway_USA/MapServer";

        String msg = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tns=\"http://www.esri.com/schemas/ArcGIS/9.0\">";
        //msg += "<soap:Body><tns:GetMessageVersion></tns:GetMessageVersion></soap:Body>";
        //msg += "<soap:Body><tns:GetDefaultMapName></tns:GetDefaultMapName></soap:Body>";
        msg += "<soap:Body><tns:GetServerInfo><MapName>Layers</MapName></tns:GetServerInfo></soap:Body>";
        //msg += "<soap:Body><tns:GetSupportedImageReturnTypes></tns:GetSupportedImageReturnTypes></soap:Body>";
        //msg += "<soap:Body><tns:HasSingleFusedMapCache><MapName>Layers</MapName><LayerID>0</LayerID></tns:HasSingleFusedMapCache></soap:Body>";
        //msg += "<soap:Body><tns:GetLegendInfo><MapName>Layers</MapName><ImageType xsi:type=\"tns:ImageType\"><ImageFormat>esriImageBMP</ImageFormat><ImageReturnType>esriImageReturnMimeData</ImageReturnType></ImageType></tns:GetLegendInfo></soap:Body>";
        //msg += "<soap:Body><tns:GetTokenServiceURL></tns:GetTokenServiceURL></soap:Body>";
        //msg += "<soap:Body><tns:GetLegendInfo><MapName>Layers</MapName><ImageType xsi:type=\"tns:ImageType\"><ImageFormat>esriImageBMP</ImageFormat><ImageReturnType>esriImageReturnMimeData</ImageReturnType></ImageType></tns:GetLegendInfo></soap:Body>";
        //msg += "<soap:Body><tns:IsFixedScaleMap><MapName>Layers</MapName></tns:IsFixedScaleMap></soap:Body>";
        //msg += "<soap:Body><tns:GetTileCacheInfo><MapName>Layers</MapName></tns:GetTileCacheInfo></soap:Body>";
        //msg += "<soap:Body><tns:GetTileImageInfo><MapName>Layers</MapName></tns:GetTileImageInfo></soap:Body>";
        msg += "</soap:Envelope>";


        //String fileName = "MapProxy-2781";
        //msg = new javaxt.io.File("/temp/ArcGIS/" + fileName + ".xml").getText("UTF-8");
        javaxt.http.Request request = new javaxt.http.Request(url);
        request.setHeader("Accept", "*/*");
        request.setHeader("Content-Type", "text/xml");
        request.setHeader("User-Agent", "ArcGIS Client Using WinInet");
        request.setHeader("SOAPAction", "\"\"");
        request.write(msg);
        javaxt.http.Response response = request.getResponse();
        //System.out.println(response);
        //System.out.println(response.getStatus() + ":  " + response.getMessage());
        System.out.println(response.getText());
        //new javaxt.io.File("/temp/ArcGIS/" + fileName + "_response.xml").write(response.getText(), "UTF-8");

    }

    @Test
    public void testArcWebServices() throws Exception {


        String url = "http://services.arcgisonline.com/ArcGIS/services/USA_Topo_Maps/MapServer/?wsdl";
        url = "http://sampleserver1.arcgisonline.com/arcgis/services/Specialty/ESRI_StateCityHighway_USA/MapServer?wsdl";
        javaxt.webservices.WSDL wsdl = new javaxt.webservices.WSDL(new javaxt.http.Request(url).getResponse().getXML());
        javaxt.webservices.Soap soap = new javaxt.webservices.Soap(wsdl);
        soap.setHeader("Accept", "*/*");
        soap.setHeader("Content-Type", "text/xml");
        soap.setHeader("User-Agent", "ArcGIS Client Using WinInet");
        //soap.setHeader("SOAPAction", "\"\"");


        //Flow: GetMessageVersion, GetFolders, GetServiceDescriptions
        for (javaxt.webservices.Service service : wsdl.getServices()) {
            //service.setURL("http://services.arcgisonline.com/ArcGIS/services/USA_Topo_Maps/MapServer/");
            System.out.println(service.getName());
            for (javaxt.webservices.Method method : service.getMethods()) {


                if (method.getName().equalsIgnoreCase("GetLegendInfo")) {
                    //

                    System.out.println(" - " + method.getName());
                    javaxt.webservices.Parameters parameters = method.getParameters();
                    if (parameters != null) {
                        for (javaxt.webservices.Parameter parameter : parameters.getArray()) {
                            System.out.println(parameter.getName());
                        }
                    }

                    parameters.setValue("MapName", "Layers");

                    new javaxt.io.File("/temp/GetLegendInfo.xml").write(soap.execute(service, method, parameters).getText(), "UTF-8");

                }


            }
        }

    }


    private org.w3c.dom.Document getConfig(String[] args) throws Exception {
        if (args[args.length - 1].toLowerCase().endsWith(".xml")) {
            javaxt.io.File file = new javaxt.io.File(args[args.length - 1]);
            if (!file.exists()) throw new Exception("Config file not found: " + file);
            else {
                return file.getXML();
            }
        } else {
            throw new Exception("Config file is required");
        }
    }

    @Test
    public void seedTest() throws Exception {

        String[] args = new String[]{"E:\\Documents\\Java\\IDEA\\gis-oss\\mapproxy-java\\src\\main\\resources\\config.xml"};
        double[] bbox = new double[]{-180, -90, 180, 90};
        String srs = "EPSG:4326";
        int levels = 12;

        Config.load(getConfig(args));
        Service service = ((mapproxy.core.Service) mapproxy.config.Config.base_config().get("mapproxy").toObject());
        Layer layer = service.getLayers()[0];
        Status status = layer.seed(2, bbox, srs, levels);


        long lastUpdate = -1;
        double percentComplete = -1;

        while (true) {
            synchronized (status) {

                //while(status.getLastUpdate()<=lastUpdate){
                while (status.getPercentComplete() == percentComplete || !status.isComplete()) {
                    try {
                        status.wait();
                    } catch (InterruptedException e) {
                    }
                }

                lastUpdate = status.getLastUpdate();
                percentComplete = status.getPercentComplete();
                status.notifyAll();

                System.out.println(status.getPercentComplete());


                if (status.getPercentComplete() == 1) {
                    System.out.println("Done!");
                    return;
                }
            }

        }


    }


    //**************************************************************************
    //** testWMSClient
    //**************************************************************************

    /**
     * Used to connect to a WMS and retrieve an image. No cache is involved for
     * this test.
     */
    private void testWMSClient(String url) {

        WMS111MapRequest req = getWMS111MapRequest(url);
        WMSClient wms = new WMSClient(req);
        wms.get_map(req).saveAs("/temp/wms_ew.png");
    }


    //**************************************************************************
    //** testCache
    //**************************************************************************

    /**
     * Used to execute a WMS request. Retrieves an image from the tile cache.
     * Builds the corresponding cache on-the-fly
     */
    private void testCache(String url) {

        FileCache fileCache = new FileCache("/temp/mapproxy/java/ew/", "png");
        int[] tile_size = new int[]{256, 256};
        TileGrid grid = new TileGrid(4326, tile_size);

        WMS111MapRequest req = getWMS111MapRequest(url);
        WMSTileSource wms = new WMSTileSource(grid, new WMSClient[]{new WMSClient(req)});

        CacheManager cacheManager = new CacheManager(fileCache, wms);
        Cache cache = new Cache(cacheManager, grid, true);
        cache.image(req.params.bbox(), new SRS(req.params.srs()), req.params.size()).saveAs("/temp/ew_cache.png");

    }


    //**************************************************************************
    //** getWMS111MapRequest
    //**************************************************************************
    private WMS111MapRequest getWMS111MapRequest() {

        //http://localhost:6277/cgi-bin/mapserv.exe?map=/data/world.map&height=256&bbox=-95.625,-5.625,-78.75,11.25&layers=polboundaries&width=512&styles=&srs=EPSG:4326&format=image/png
        //http://localhost:6277/cgi-bin/mapserv.exe?map=/data/world.map&SERVICE=WMS&REQUEST=GetMap&VERSION=1.1.0&LAYERS=polboundaries&EXCEPTIONS=application/vnd.ogc.se_xml&STYLES=&TRANSPARENT=TRUE&FORMAT=image/png&SRS=EPSG:4326&WIDTH=768&HEIGHT=768&BBOX=-95.625,-5.625,-78.75,11.25
        String TESTSERVER_URL = "http://localhost:6277/cgi-bin/mapserv.exe?map=/data/world.map";
        //String TESTSERVER_URL = "http://localhost:6277/cgi-bin/mapserv.exe?map=/data/world.map&height=256&bbox=-95.625,-5.625,-78.75,11.25&layers=grid,polboundaries&request=GetMap&width=256&service=WMS&styles=&srs=EPSG:4326&format=image/png&version=1.1.1";

        WMSMapRequestParams param = new WMSMapRequestParams();
        param.set("layers", "grid,polboundaries");
        param.set("bbox", "-95.625,-5.625,-78.75,11.25"); //-180.0,-90.0,180.0,90.0

        WMS111MapRequest req = new WMS111MapRequest(param, TESTSERVER_URL);
        req.params.size(new int[]{256, 256});
        req.params.set("format", "image/png");
        req.params.set("srs", "EPSG:4326");

        return req;
    }


    private WMS111MapRequest getWMS111MapRequest(String wmsRequest) {

        javaxt.utils.URL url = new javaxt.utils.URL(wmsRequest);
        String bbox = url.removeParameter("bbox");
        String layers = url.removeParameter("layers");
        String format = url.removeParameter("format");
        String srs = url.removeParameter("srs");
        int width = javaxt.utils.string.toInt(url.removeParameter("width"));
        int height = javaxt.utils.string.toInt(url.removeParameter("height"));

        url.removeParameter("request");
        url.removeParameter("service");
        url.removeParameter("version");


        WMSMapRequestParams param = new WMSMapRequestParams();
        param.set("layers", layers);
        param.set("bbox", bbox);


        WMS111MapRequest req = new WMS111MapRequest(param, url.toString());
        req.params.size(new int[]{width, height});
        req.params.set("format", format);
        req.params.set("srs", srs);

        System.out.println("***********************************");
        System.out.println(url);
        System.out.println("***********************************");

        return req;
    }


}
