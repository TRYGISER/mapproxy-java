package mapproxy.core;
import mapproxy.core.grid.*;
import mapproxy.core.cache.*;
import org.w3c.dom.*;
import javaxt.xml.DOM;
import mapproxy.kml.server.KMLServer;

//******************************************************************************
//**  ServiceResponse Class
//******************************************************************************
/**
 *   Used to generate a response to a service request. The response might
 *   include an image or a capabilities document.
 *
 *   Note that this class was not part of the original mapproxy baseline.
 *
 ******************************************************************************/

public class ServiceResponse {

    private javaxt.utils.Date date;
    private javaxt.utils.URL url;
    private Service service;
    private String body;
    private String contentType;
    private String key;
    private byte[] rsp;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of ServiceResponse.   */

    protected ServiceResponse(String requestURL, String requestBody, Service service) throws Exception {

      //Clean up any duplicate values in the query string (wierd bug in ArcMap 9.2)
        javaxt.utils.URL url = new javaxt.utils.URL(requestURL);
        java.util.HashMap<String, java.util.List<String>> params = url.getParameters();
        java.util.Iterator<String> it = params.keySet().iterator();
        while (it.hasNext()){
            String key = it.next();
            java.util.List<String> val = params.get(key);
            if (val==null){
                url.setParameter(key, null);
            }
            else{
                java.util.HashSet<String> uniqueValues = new java.util.HashSet<String>();
                for (int i=0; i<val.size(); i++){
                    uniqueValues.add(val.get(i));
                }
                String vals = "";
                java.util.Iterator<String> it2 = uniqueValues.iterator();
                while (it2.hasNext()){
                    vals += it2.next();
                    if (it2.hasNext()) vals +=",";
                }
                url.setParameter(key, vals);
            }
        }


        this.url = new javaxt.utils.URL(url.toString());

        if (requestBody!=null){
            requestBody = requestBody.trim();
            if (requestBody.length()==0) requestBody = null;
        }
        this.body = requestBody;

        this.service = service;

        javaxt.utils.Date date = new javaxt.utils.Date();
        date.setTimeZone("GMT");
        this.date = date;

        init(false);
    }


  //**************************************************************************
  //** init
  //**************************************************************************
  /**  Used to initialize three class variables: key, contentType, and rsp.
   * 
   *   @param getBytes Used to initialize the rsp variable. If set to true,
   *   rsp will be populated with a byte array. If set to false, the rsp 
   *   variable will not be set. This fine grain control is useful for http
   *   applications where a server may only need the key to implement http
   *   cache control.
   */
    private void init(boolean getBytes){

        if (getBytes && this.rsp!=null) return;

        boolean responseSent = true; 
        String path = url.getPath().toLowerCase();
        if (!path.contains(".") && !path.endsWith("/")) path+="/";


      //ArcGIS Server Response
        if (path.contains("/ImageServer/".toLowerCase()) ||
            path.contains("/MapServer/".toLowerCase()) ||
            path.contains("/ArcGIS/".toLowerCase())){

            if (url.getQueryString().equalsIgnoreCase("WSDL") || body!=null){
                try{
                    getArcGISSoapResponse(getBytes);
                }
                catch(java.io.UnsupportedEncodingException e){
                    //This error should never be thrown. Only occurs if the
                    //string can't be encoded into UTF-8.
                }
            }
            else{

                String f = url.getParameter("f");
                if (f.equalsIgnoreCase("json")){
                    getArcMapCapabilities(getBytes);
                }
                else if (f.equalsIgnoreCase("image")){
                    getArcMapImage(getBytes);
                }
                else{
                    responseSent = false;
                }

            }
        }

      //WMS Server Response
        else if (path.contains("/wms/")){
            String action = url.getParameter("request");
            if (action.equalsIgnoreCase("GetMap")){
                getMap(getBytes);
            }
            else if (action.equalsIgnoreCase("GetCapabilities")){
                getWMSCapabilities(getBytes);
            }
            else{
                responseSent = false;
            }
        }

      //TMS Server Response
        else if (path.contains("/tms/")){

            if (path.endsWith("/tms/")){
                getTMSCapabilities(getBytes);
            }
            else{
                path = java.net.URLDecoder.decode(path);
                boolean getLayerInfo = false;
                Layer requestedLayer = null;
                for (Layer layer : service.getLayers()){
                    String layerName = layer.getName();
                    if (path.toLowerCase().endsWith("/tms/" + layerName.toLowerCase() + "/")){
                        requestedLayer = layer;
                        getLayerInfo = true;
                        break;
                    }
                }

                if (getLayerInfo){
                    getTMSLayerInfo(requestedLayer, getBytes);
                }
                else{
                    getTile(getBytes);
                }
            }
        }

      //KML Server Response
        else if (path.contains("/kml/")){

            if (path.endsWith("/kml/")){
                getKMLCapabilities(getBytes);
            }
            else{

                if (path.contains(".")){
                    String format = path.substring(path.lastIndexOf(".")+1).toLowerCase();
                    if (format.equals("jpg") || format.equals("png")){
                        getTile(getBytes);
                    }
                    else if (format.equals("kml")){
                        getKML(getBytes);
                    }
                    else{
                        responseSent = false;
                    }
                }
                else{
                    responseSent = false;
                }
            }
        }
        else{
            responseSent = false;
        }

        if (!responseSent) getLandingPage(getBytes);
    }


  //**************************************************************************
  //** getID
  //**************************************************************************
  /** Returns an ID for the response. This ID can be stored as key in a cache
   *  map.
   */
    public String getID(){
        return key;
    }


  //**************************************************************************
  //** getContentType
  //**************************************************************************
  /** Returns the HTTP Content-Type for the response. */

    public String getContentType(){
        return contentType;
    }


  //**************************************************************************
  //** getDate
  //**************************************************************************
  /** Returns the date for the response. */

    public javaxt.utils.Date getDate(){
        return date;
    }


  //**************************************************************************
  //** getByteArray
  //**************************************************************************
  /** Returns a byte array containing the response. The byte array can be used
   *  in the body of an http response.
   */
    public byte[] getByteArray(){
        this.init(true); //<-- Inititalize the byte array...
        return rsp;
    }



  //**************************************************************************
  //** getLandingPage
  //**************************************************************************
  /** Used to construct an HTML landing page for the service.
   */
    private void getLandingPage(boolean getBytes) {

      
        key = "HOME";
        contentType = "text/html";

        if (!getBytes) return;


        String url = this.url.toString();
        if (url.contains("?")) url = url.substring(0, url.indexOf("?"));

        String html;
        if (mapproxy.config.Config.base_config().get("html")!=null){
            html = mapproxy.config.Config.base_config().get("html").toString();
        }
        else{
            html = "<html><body><h1>@SERVICENAME</h1><p>@SERVICEDESCRIPTION</p>Layers:<ul>@LAYERS</ul>Services:<ul>@SERVICES</ul></body></html>";
        }


        html = html.replace("@SERVICENAME", service.getName());
        html = html.replace("@SERVICEDESCRIPTION", service.getDescription());
        
        StringBuffer layers = new StringBuffer();
        for (Layer layer : service.getLayers()){
            layers.append("<li>" + layer.getName() + "</li>");
        }
        html = html.replace("@LAYERS", layers.toString());


        StringBuffer services = new StringBuffer();
        services.append("<li><a href=\"/WMS/?service=WMS&request=GetCapabilities&version=1.1.1\">WMS</a></li>");
        services.append("<li><a href=\"/TMS/\">TMS</a></li>");
        services.append("<li><a href=\"/KML/\">KML</a></li>");
        services.append("<li><a href=\"/ArcGIS/?wsdl\">ArcGIS MapServer (SOAP)</a></li>");
        services.append("<li><a href=\"/ArcGIS/?f=json\">ArcGIS MapServer (REST)</a></li>");
        html = html.replace("@SERVICES", services.toString());


        try{
            rsp = html.toString().getBytes("UTF-8");
        }
        catch (java.io.UnsupportedEncodingException e){
        }

    }





  //**************************************************************************
  //** getTile
  //**************************************************************************
  /** Used to construct a response to a TMS Map request
   */
    private void getTile(boolean getBytes) {

      //Set local variables
        String url = this.url.toString();

      //Check whether this request is in the http cache
        mapproxy.tms.TileRequest req = new mapproxy.tms.TileRequest(url);
        key = req.id();
        if (!getBytes) return;


      //Set format and content type
        String format = req.format;
        contentType = "image/"+format;


      //Update content type for jpeg
        if (format.equalsIgnoreCase("jpg")) contentType = "image/jpeg";
        

      //Find requested layer in the mapproxy service definition
        Layer layer = service.getLayer(req.layer);
        FileCache fileCache = (FileCache) layer.getParam("cache");
        

      //Get tile grid
        TileGrid grid = (TileGrid) layer.getParam("grid");
        if (grid==null) grid = new TileGrid(4326, new int[]{256,256});


      //Update tile coordinate
        req.tile = new mapproxy.tms.TileServiceGrid(grid).internal_tile_coord(req.tile, true);


      //Get Image
        if (fileCache==null){
            double[] bbox = grid.tile_bbox(req.tile[0], req.tile[1], req.tile[2]);
            rsp = layer.getImage(grid.tile_size[0], grid.tile_size[1], format, bbox, grid.srs.toString()).getByteArray(format);
        }
        else{

          //Instantiate the TMS cache            
            mapproxy.tms.cache.TMSTileSource tms = new mapproxy.tms.cache.TMSTileSource(grid, url);
            CacheManager cacheManager = new CacheManager(fileCache, tms);
            Cache cache = new Cache(cacheManager, grid, true);
            

            try{

              //Find the requested tile in the cache. Note that this will throw an error if the tile doesn't exist
                javaxt.io.File file = new javaxt.io.File(cache.tile(req.tile).location);
                if (file.exists() && file.getSize()>0){
                    if (file.getExtension().equalsIgnoreCase(format)){
                        rsp = file.getBytes().toByteArray();
                    }
                    else{
                        rsp = file.getImage().getByteArray(format);
                    }
                }
            }
            catch(Exception e){

              //Create image and update cache
                double[] bbox = grid.tile_bbox(req.tile[0], req.tile[1], req.tile[2]);
                rsp = layer.getImage(grid.tile_size[0], grid.tile_size[1], fileCache.file_ext, bbox, grid.srs.toString()).getByteArray(format);
            }

        }
    }


  //**************************************************************************
  //** getMap
  //**************************************************************************
  /** Used to construct a response to a WMS GetMap request
   */
    private void getMap(boolean getBytes) {

      //Parse querystring parameters
        String layers = url.getParameter("layers");
        String bbox = url.getParameter("bbox");        
        String srs = url.getParameter("srs");
        int width = javaxt.utils.string.toInt(url.getParameter("width"));
        int height = javaxt.utils.string.toInt(url.getParameter("height"));
        String format = url.getParameter("format");
        if (!format.toLowerCase().startsWith("image/")) format = "image/" + format;

      //Set content type
        if (format.equalsIgnoreCase("image/PNG24")) contentType = "image/png";
        else contentType = format;

      //Create unique key
        key = bbox + "|" + layers + "|" + contentType + "|" + width + "|" + height + "|" + srs;
        
      //Exit method as needed
        if (!getBytes) return;


      //Create image for each layer
        javaxt.io.Image image = new javaxt.io.Image(width, height);
        for (String layerName : layers.split(",")){

            mapproxy.core.Layer layer = service.getLayer(layerName);

            if (layer==null){
                int layerID = -1;
                try{
                    layerID = Integer.parseInt(layerName);
                    if (layerID>=0 && layerID<service.getLayers().length){
                        layer = service.getLayers()[layerID];
                    }
                }
                catch(Exception e){
                }
            }

            if (layer!=null){
                image.addImage(layer.getImage(width, height, format, bbox, srs), 0, 0, false);
                //image.addImage(getImage(layer, width, height, format, bbox, srs), 0, 0, false);
            }
        }


      //Convert the image into a byte array
        if (format.toLowerCase().endsWith("png24")) format = "image/png";
        rsp = image.getByteArray(format);


        //rsp = new javaxt.io.File("/temp/ArcGIS/ExportImage.png").getBytes().toByteArray();
        //rsp = new javaxt.io.Image("/temp/ArcGIS/ExportImage.png").getByteArray("png");

    }


  //**************************************************************************
  //** getArcMapImage
  //**************************************************************************
  /** Used to construct a response to an ArcMap Image Export request. Basically
   *  this method transforms the request into a WMS getMap request.
   */
    private void getArcMapImage(boolean getBytes) {

        javaxt.utils.URL url = new javaxt.utils.URL(this.url.toString());


      //Remove extranous parameters
        url.removeParameter("f");
        url.removeParameter("dpi");
        url.removeParameter("imagesr");


      //Update the layers parameter
        String layers = url.removeParameter("layers");
        if (layers!=null){
            layers = layers.trim();
            if (layers.length()==0){
                layers=null;
            }
        }
        if (layers==null){
            layers="";
            for (int i=0; i<service.getLayers().length; i++){
                layers+= service.getLayers()[i];
                if (i<this.service.getLayers().length-1) layers+=",";
            }
        }
        url.setParameter("layers", layers);



      //Update the SRS parameter
        String srs = url.removeParameter("bboxsr");
        if (!srs.contains(":")) srs = "EPSG:" + srs;
        url.setParameter("srs", srs);


      //Update the BBOX parameter as needed. For example, ArcMap returns the following:
      //bbox={"xmin":-140.90176870508722,"ymin":5.39213971511016,"xmax":-50.75590345473839,"ymax":70.5993603884707,"spatialReference":{"wkid":4326}}
        String bbox = url.removeParameter("bbox").trim();
        if (bbox.contains(":")){
            if (bbox.startsWith("{") && bbox.endsWith("}")){
                bbox = bbox.substring(1, bbox.length()-1).trim();
            }
            java.util.HashMap<String, String> coords = new java.util.HashMap<String, String>();
            for (String param : bbox.split(",")){
                if (param.startsWith("\"xmin\"")){
                    coords.put("minX", param.substring(param.indexOf(":")+1));
                }
                if (param.startsWith("\"ymin\"")){
                    coords.put("minY", param.substring(param.indexOf(":")+1));
                }
                if (param.startsWith("\"xmax\"")){
                    coords.put("maxX", param.substring(param.indexOf(":")+1));
                }
                if (param.startsWith("\"ymax\"")){
                    coords.put("maxY", param.substring(param.indexOf(":")+1));
                }
            }
            bbox = coords.get("minX") + "," + coords.get("minY") + "," + coords.get("maxX") + "," + coords.get("maxY");
        }
        url.setParameter("bbox", bbox);


      //Update the format
        String format = url.getParameter("format");
        if (format.toLowerCase().startsWith("png")) format = "png";
        if (!format.toLowerCase().startsWith("image/")) format = "image/" + format;
        url.setParameter("format", format);


        url.setParameter("request", "GetMap");
        url.setParameter("service", "WMS");
        url.setParameter("version", "1.1.1");


      //Set output width/height
        String[] size = url.removeParameter("size").split(",");
        url.setParameter("width", size[0]);
        url.setParameter("height", size[1]);


        javaxt.utils.URL orgURL = this.url;
        this.url = url;

        this.getMap(getBytes);

        this.url = orgURL;

    }


  //**************************************************************************
  //** getWMSCapabilities
  //**************************************************************************
  /** Used to construct a response to a WMS GetCapabilities request
   */
    private void getWMSCapabilities(boolean getBytes) {

        key = "WMS Capabilities";
        contentType = "text/xml";

        if (!getBytes) return;

        String url = this.url.toString();
        if (url.contains("?")) url = url.substring(0, url.indexOf("?"));
        if (!url.endsWith("/")) url += "/";
        if (!url.toLowerCase().endsWith("/wms/")) url += "wms/";

      //Get Capabilities
        String orgURL = service.getURL();
        service.setURL(url);
        rsp = service.getCapabilities().toString("WMS").getBytes();
        service.setURL(orgURL);
    }


  //**************************************************************************
  //** getKMLCapabilities
  //**************************************************************************
  /** Used to construct a response to a KML Capabilities request
   */
    private void getKMLCapabilities(boolean getBytes) {

        key = "KML Capabilities";
        contentType = "application/vnd.google-earth.kml+xml";
        //contentType = "text/xml";

        if (!getBytes) return;
        
        rsp = new KMLServer(service).getCapabilities().getBytes();
    }

  //**************************************************************************
  //** getKML
  //**************************************************************************
  /** Used to construct a KML SuperOverlay
   */
    private void getKML(boolean getBytes) {
        
        key = url.getPath();
        contentType = "application/vnd.google-earth.kml+xml";
        //contentType = "text/xml";

        if (!getBytes) return;

        rsp = new KMLServer(service).getKML(url).getBytes();
    }


  //**************************************************************************
  //** getTMSCapabilities
  //**************************************************************************
  /** Used to construct a response to a TMS GetCapabilities request
   */
    private void getTMSCapabilities(boolean getBytes) {

        key = "TMS Capabilities";
        contentType = "text/xml";

        if (!getBytes) return;

        String url = this.url.toString();
        if (url.contains("?")) url = url.substring(0, url.indexOf("?"));
        if (!url.endsWith("/")) url += "/";
        if (!url.toLowerCase().endsWith("/tms/")) url += "tms/";

      //Get Capabilities
        String orgURL = service.getURL();
        service.setURL(url);
        rsp = service.getCapabilities().toString("TMS").getBytes();
        service.setURL(orgURL);
    }


  //**************************************************************************
  //** getTMSLayerInfo
  //**************************************************************************
  /** Used to construct a response to a TMS Capabilities request for a given
   *  layer.
   */
    private void getTMSLayerInfo(Layer layer, boolean getBytes) {

        String layerName = layer.getName();

        key = "TMS Capabilities - " + layerName;
        contentType = "text/xml";

        if (!getBytes) return;

      //Update Service URL
        String url = this.url.toString();
        if (url.contains("?")) url = url.substring(0, url.indexOf("?"));
        if (!url.endsWith("/")) url += "/";

        String lyr = url.substring(url.substring(0, url.length()-1).lastIndexOf("/")+1);
        if (lyr.endsWith("/")) lyr = lyr.substring(0, lyr.length()-1);
        if (java.net.URLDecoder.decode(lyr).equalsIgnoreCase(layerName)){
            url = url.substring(0, url.lastIndexOf(lyr));
        }


      //Get Capabilities
        String orgURL = service.getURL();
        service.setURL(url);
        rsp = getBytes(new mapproxy.tms.TileServer(service).getLayerInfo(layer));
        service.setURL(orgURL);
    }


    private byte[] getBytes(String str){
        try{
            return str.getBytes("UTF-8");
        }
        catch(Exception e){
            return str.getBytes();
        }
    }

    
  //**************************************************************************
  //** getArcMapCapabilities
  //**************************************************************************
  /** Used to construct a response to an ArcMap REST Capabilities request
   */
    private void getArcMapCapabilities(boolean getBytes) {
        
        key = "ArcMap Capabilities";
        contentType = "text/plain";


        String callback = url.getParameter("callback");
        if (callback!=null){
            callback = callback.trim();
            if (callback.length()==0) callback = null;
        }


        if (callback!=null){
            key += " + Callback";
        }

        if (!getBytes) return;
        

        StringBuffer json = new StringBuffer();
        if (callback!=null) json.append(callback+"(");
        json.append(this.service.getCapabilities().toString("ArcGIS.JSON"));
        if (callback!=null) json.append(");");


      //Convert the xml into a byte array
        rsp = json.toString().getBytes();

        
    }



  //**************************************************************************
  //** getArcGISSoapResponse
  //**************************************************************************
  /** Used to construct a response to an ArcGIS SOAP client
   */
    private void getArcGISSoapResponse(boolean getBytes) throws java.io.UnsupportedEncodingException {

        mapproxy.arcgis.server.MapServer arcgis = new mapproxy.arcgis.server.MapServer(service);

        contentType = "text/xml";

        if (body==null || body.length()==0) body = " ";
        if (body.substring(0,1).equals("<")){
            String resultsNode = "Body"; //soap:Body
            Document xml = DOM.createDocument(body);
            NodeList response = xml.getElementsByTagName(resultsNode);
            if (response!=null){

              //Special Case: Probably Missing Namespace in Soap.resultsNode
                if (response.getLength()==0) {
                    resultsNode = getResultsNode(body, resultsNode);
                    response = xml.getElementsByTagName(resultsNode);
                }

                String msg = DOM.getNodeValue(response.item(0));                
                if (msg.contains("GetMessageVersion")){
                    key = "ArcGIS GetMessageVersion";
                    rsp = arcgis.getMessageVersion().getBytes("UTF-8");
                }
                else if (msg.contains("GetMessageFormats")){
                    key = "ArcGIS GetMessageFormats";
                    rsp = arcgis.getMessageFormats().getBytes("UTF-8");
                }
                else if (msg.contains("GetFolders")){
                    key = "ArcGIS GetFolders";
                    rsp = arcgis.getFolders().getBytes("UTF-8");
                }
                else if (msg.contains("GetServiceDescriptions")){
                    key = "ArcGIS GetServiceDescriptions";
                    rsp = arcgis.getServiceDescriptions().getBytes("UTF-8");
                }
                else if (msg.contains("GetDefaultMapName")){
                    key = "ArcGIS GetDefaultMapName";
                    rsp = arcgis.getDefaultMapName().getBytes("UTF-8");
                }
                else if (msg.contains("GetServerInfo")){
                    key = "ArcGIS GetServerInfo";
                    rsp = arcgis.getServerInfo().getBytes("UTF-8");
                }
                else if (msg.contains("GetSupportedImageReturnTypes")){
                    key = "ArcGIS GetSupportedImageReturnTypes";
                    rsp = arcgis.getSupportedImageReturnTypes().getBytes("UTF-8");
                }
                else if (msg.contains("HasSingleFusedMapCache")){
                    key = "ArcGIS HasSingleFusedMapCache";
                    rsp = arcgis.hasSingleFusedMapCache().getBytes("UTF-8");
                }
                else if (msg.contains("GetTokenServiceURL")){
                    key = "ArcGIS GetTokenServiceURL";
                    rsp = arcgis.getTokenServiceURL().getBytes("UTF-8");
                }
                else if (msg.contains("IsFixedScaleMap")){
                    key = "ArcGIS IsFixedScaleMap";
                    rsp = arcgis.isFixedScaleMap().getBytes("UTF-8");
                }
                else if (msg.contains("GetTileCacheInfo")){
                    key = "ArcGIS GetTileCacheInfo";
                    rsp = arcgis.getTileCacheInfo().getBytes("UTF-8");
                }
                else if (msg.contains("GetTileImageInfo")){
                    key = "ArcGIS GetTileImageInfo";
                    rsp = arcgis.getTileImageInfo().getBytes("UTF-8");
                }
                else if (msg.contains("GetLegendInfo")){
                    key = "ArcGIS GetLegendInfo";
                    //rsp = new javaxt.io.File("/temp/GetLegendInfo.xml").getText("UTF-8").getBytes("UTF-8");
                    //rsp = arcgis.getLegendInfo().getBytes("UTF-8");
                }
                else if (msg.contains("ExportMapImage")){
                    System.out.println("ExportMapImage");
//new javaxt.io.File("/temp/ExportImage.xml").write(body, "UTF-8");

                    String returnType = getNodeValue("ImageReturnType", xml);
                    String width = getNodeValue("ImageWidth", xml);
                    String height = getNodeValue("ImageHeight", xml);
                    String format = getNodeValue("ImageFormat", xml);


                    String layers = "";
                    NodeList nodeList = xml.getElementsByTagName("LayerDescription");
                    for (int i=0; i<nodeList.getLength(); i++ ) {
                        if (nodeList.item(i).getNodeType()==1){
                            String layerID = "";
                            String isVisible = "";
                            NodeList layerAttributes = nodeList.item(i).getChildNodes();
                            for (int j=0; j<layerAttributes.getLength(); j++ ) {
                                Node layerAttribute = layerAttributes.item(j);
                                if (layerAttribute.getNodeType()==1){
                                    if (layerAttribute.getNodeName().equalsIgnoreCase("LayerID")){
                                        layerID = javaxt.xml.DOM.getNodeValue(layerAttribute).trim();
                                    }
                                    else if (layerAttribute.getNodeName().equalsIgnoreCase("Visible")){
                                        isVisible = javaxt.xml.DOM.getNodeValue(layerAttribute).trim();
                                    }
                                }
                            }
                            if (layerID.length()>0){
                                if (isVisible.equalsIgnoreCase("true")){
                                    layers+=layerID + ",";
                                }
                            }
                        }
                    }
                    /*
                    NodeList nodeList = xml.getElementsByTagName("LayerID");
                    for (int i=0; i<nodeList.getLength(); i++ ) {
                         if (nodeList.item(i).getNodeType()==1){
                             String layer = javaxt.xml.DOM.getNodeValue(nodeList.item(i)).trim();
                             if (layer.length()>0){
                                 layers+=layer + ",";
                             }
                        }
                    }
                    */
                    if (layers.endsWith(",")){
                        layers = layers.substring(0, layers.length()-1);
                    }

                    String minX = getNodeValue("XMin", xml);
                    String minY = getNodeValue("YMin", xml);
                    String maxX = getNodeValue("XMax", xml);
                    String maxY = getNodeValue("YMax", xml);
                    String bbox = minX + "," + minY + "," + maxX + "," + maxY;
                    String wkt = getNodeValue("WKT", xml);


                  //Update the request URL to point to the WMS endpoint
                    String wms = this.url.toString();
                    wms = wms.substring(0, wms.indexOf(this.url.getPath())) + "/wms";


                  //Generate ExportImage SOAP Response
                    rsp = arcgis.getExportMapImageResponse(new javaxt.utils.URL(wms), layers, bbox, wkt, width, height, format, returnType).getBytes("UTF-8");


                    /*
                  //Redirect request to ArcGIS Server (Debug Use Only)
                    msg = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tns=\"http://www.esri.com/schemas/ArcGIS/9.0\">" + msg;
                    msg += "</soap:Envelope>";

                    javaxt.http.Request req = new javaxt.http.Request("http://services.arcgisonline.com/ArcGIS/services/USA_Topo_Maps/MapServer");
                    req.setHeader("Accept", "*");
                    req.setHeader("Content-Type", "text/xml");
                    req.setHeader("User-Agent", "ArcGIS Client Using WinInet");
                    req.setHeader("SOAPAction", "\"\"");

                    req.write(msg);

                    String txt = req.getResponse().getText("UTF-8");
                    String img = getNodeValue("ImageURL", DOM.createDocument(txt));
                    System.out.println(img);
                    txt = txt.replace(img, "<![CDATA[http://localhost:9080/wms?height=597&bbox=319898.427173501,-238695.385822158,1250996.29959783,693964.731354558&request=GetMap&width=596&layers=0&service=WMS&srs=EPSG:2781&format=image/jpeg&version=1.1.1&date="+ new java.util.Date().getTime() +"]]>");
                    rsp = txt.getBytes("UTF-8");
                    new javaxt.io.File("/temp/ExportImageResponse.xml").write(rsp);
                    */


                }
                else{
                    System.out.println( msg );
                }

            }
        }


      //Return a WSDL by default
        if (rsp==null){
            key = "ArcGIS WSDL";
            rsp = arcgis.getWSDL().getBytes("UTF-8");
        }

    }


    private String getResultsNode(String ServiceResponse, String resultsNode){
        resultsNode = ServiceResponse.substring(0,
                      ServiceResponse.toLowerCase().indexOf(resultsNode.toLowerCase()) + resultsNode.length());

        resultsNode = resultsNode.substring(resultsNode.lastIndexOf("<")+1);
        return resultsNode;
    }


    private String getNodeValue(String nodeName, Document xml){

        NodeList nodeList = xml.getElementsByTagName(nodeName);
        for (int i=0; i<nodeList.getLength(); i++ ) {
             if (nodeList.item(i).getNodeType()==1){
                 return javaxt.xml.DOM.getNodeValue(nodeList.item(i));
            }
        }
        return "";
    }

}