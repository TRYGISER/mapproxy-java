package mapproxy.core;
import org.w3c.dom.*;

//******************************************************************************
//**  Service Class
//******************************************************************************
/**
 *   Used to represent a web map service. Can be used to transform map requests
 *   between different formats.
 *
 *   Note that this class was not part of the original mapproxy baseline.
 *
 ******************************************************************************/

public class Service {


    private String name;
    private String title;
    private String description;
    private String url;
    private String[] formats;
    private java.util.List<Layer> layers = new java.util.ArrayList<Layer>();
    private java.util.HashMap<String, Object> params = new java.util.HashMap<String, Object>();
    private String protocol;



  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new map service from scratch. With this constructor, you can
   *  create your own custom map layers, tailor the list of supported formats, 
   *  etc.
   *
   * @param name Name of this service. The name will be published in a
   * capabilities document.
   *
   * @param url URL associated with this service. This is typically the base
   * url for map requests.
   */
    public Service(String name, String url) {
        this.name = name;
        this.title = name;
        this.url = url;
    }

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new map service using an existing map service. This constructor
   *  will attempt to parse the capabilities document associated with the
   *  service and instantiate local variables using parsed values (e.g. name,
   *  title, description, layers, etc.)
   *
   * @param url Fully qualified URL to a capabilities document associated with
   * an existing map service (e.g. response from an OGC WMS GetCapabilities
   * request).
   */
    public Service(javaxt.utils.URL url){
        javaxt.http.Response response = new javaxt.http.Request(url.toString()).getResponse();
        String[] contentType = response.getHeader("Content-Type").split(";");
        String media = contentType[0].trim().toLowerCase();
        if (media.equals("text/xml") || media.equals("application/xml") ||
            media.equals("application/vnd.ogc.wms_xml")){

            Document doc = response.getXML();
            Node outerNode = javaxt.xml.DOM.getOuterNode(doc);
            String outerNodeName = outerNode.getNodeName();
            if (outerNodeName.equalsIgnoreCase("WMT_MS_Capabilities") ||
                outerNodeName.equalsIgnoreCase("WMS_Capabilities")){
                parseWMSCapabilities(doc);
            }
            else if(outerNodeName.equalsIgnoreCase("TileMapService")){
                parseTMSCapabilities(doc);
            }
            
            
        }
        else if (media.equals("text/plain")){
            String text = response.getText().trim();
            if (text.startsWith("{") && text.endsWith("}")){
                this.url = url.toString();
                if (this.url.contains("?")) this.url = this.url.substring(0, this.url.indexOf("?"));
                parseArcGISCapabilities(text);
            }
        }
        else{
            System.out.println("Media: " + media);
        }
    }

    public Service(String url){
        this(new javaxt.utils.URL(url));
    }


  //**************************************************************************
  //** getName
  //**************************************************************************
  /** Returns the name of the service. */

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }


  //**************************************************************************
  //** getProtocol
  //**************************************************************************
  /** Returns the type of service. */

    public String getProtocol(){
        return this.protocol;
    }


  //**************************************************************************
  //** getTitle
  //**************************************************************************
  /** Returns the title of the service. */

    public String getTitle(){
        return title;
    }


  //**************************************************************************
  //** getDescription
  //**************************************************************************
  /** Returns the description of the service. */

    public String getDescription(){
        return description;
    }


    public void setDescription(String description){
        this.description = description;
    }


  //**************************************************************************
  //** getURL
  //**************************************************************************
  /** Returns the base URL for this service. */

    public String getURL(){
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

  //**************************************************************************
  //** getFormats
  //**************************************************************************
  /** Returns a list of supported formats. */

    public String[] getFormats(){
        if (formats==null){
            formats = javaxt.io.Image.OutputFormats;
        }
        return formats;
    }
    
  //**************************************************************************
  //** getParam
  //**************************************************************************
  /** Returns parameter with given key. */

    public Object getParam(String key){
        return params.get(key);
    }


  //**************************************************************************
  //** setParam
  //**************************************************************************
  /** Sets a parameter with given key. */

    public void setParam(String key, Object val){
        params.put(key, val);
    }


  //**************************************************************************
  //** addLayer
  //**************************************************************************
  /** Used to add a layer to the list of layers in this service. */

    public void addLayer(Layer layer){
        layers.add(layer);
    }


  //**************************************************************************
  //** getLayer
  //**************************************************************************
  /** Returns a layer with a given name. */

    public Layer getLayer(String name){
        java.util.Iterator<Layer> it = layers.iterator();
        while (it.hasNext()){
            Layer layer = getLayer(it.next(), name);
            if (layer!=null) return layer;
        }
        return null;
    }

    private Layer getLayer(Layer layer, String name){
        if (layer.getName().equalsIgnoreCase(name)){
            return layer;
        }
        if (layer.hasLayers()){
            for (Layer subLayer : layer.getLayers()){
                layer = getLayer(subLayer, name);
                if (layer!=null) return layer;
            }
        }
        return null;
    }


  //**************************************************************************
  //** getLayers
  //**************************************************************************
  /** Returns a complete list of layers associated with this service. */

    public Layer[] getLayers(){
        return layers.toArray(new Layer[layers.size()]);
    }


  //**************************************************************************
  //** getResponse
  //**************************************************************************
  /** Used to generate a response to a service request. The response might
   *  include an image or a capabilities document.
   *  @param requestURL Requested URL (e.g. WMS GetMap request)
   *  @param requestBody Body of the http request. Typically contains the
   *  contents of an HTTP Post request, including any SOAP messages.
   */
    public ServiceResponse getResponse(String requestURL, String requestBody) throws Exception {
        return new ServiceResponse(requestURL, requestBody, this);
    }

  //**************************************************************************
  //** getResponse
  //**************************************************************************
  /** Used to generate a response to a service request. The response might
   *  include an image or a capabilities document.
   *  @param requestURL Requested URL (e.g. WMS GetMap request)
   */
    public ServiceResponse getResponse(String requestURL) throws Exception {
        return this.getResponse(requestURL, null);
    }

    /*
    public Map getMap(Layer[] layers, BBox bbox, String srs, int width, int height, String format){
        return new Map(layers, bbox, srs, width, height, format);
    }
    */



  //**************************************************************************
  //** getCapabilities
  //**************************************************************************
  /** Returns a WMS capabilities document. */

    public Capabilities getCapabilities(){
        return new Capabilities(this);
    }


// <editor-fold defaultstate="collapsed" desc="Parse TMS Capabilities. Click on the + sign on the left to edit the code.">


  //**************************************************************************
  //** parseTMSCapabilities
  //**************************************************************************
  /** Used to parse a TMS Capabilities Document. */

    private void parseTMSCapabilities(Document doc){

        protocol = "TMS";

      //Get Service Metadata
        NodeList serviceInfo = javaxt.xml.DOM.getOuterNode(doc).getChildNodes();
        for (int i=0; i<serviceInfo.getLength(); i++){
            Node node = serviceInfo.item(i);
            String nodeName = node.getNodeName();
            String nodeValue = node.getTextContent();
            if (nodeName.equalsIgnoreCase("Title")){
                this.title = nodeValue;
            }
            else if (nodeName.equalsIgnoreCase("Abstract")){
                this.description = nodeValue;
            }
        }

      //Get Layers
        NodeList tileMaps = doc.getElementsByTagName("TileMap");
        for (int i=0; i<tileMaps.getLength(); i++){
            Node tileMap = tileMaps.item(i);
            NamedNodeMap map = tileMap.getAttributes();
            for (int j=0; j<map.getLength(); j++){
                Node attr = map.item(j);
                String attrName = attr.getNodeName();
                if (attrName.contains(":")) attrName = attrName.substring(attrName.indexOf(":")+1);
                if (attrName.equalsIgnoreCase("href")){
                    url = attr.getNodeValue();
                    Layer layer = getTMSLayer(url);
                    if (layer==null && url.contains("/1.0.0/1.0.0/")){
                        layer = getTMSLayer(url.replace("/1.0.0/1.0.0/", "/1.0.0/"));
                    }
                    if (layer!=null) layers.add(layer);
                    break;
                }
            }
        }

    }

    private Layer getTMSLayer(String url){
        javaxt.http.Response response = new javaxt.http.Request(url).getResponse();
        if (response.getStatus()==200){
            Document doc = response.getXML();
            if (doc!=null){
                String name = null;
                String description = null;
                String srs = "EPSG:4326";
                double minX, minY, maxX, maxY;
                minX = minY = maxX = maxY = 0;
                int[] tile_size = new int[]{256,256};
                String format = "png";

                NodeList nodes = javaxt.xml.DOM.getOuterNode(doc).getChildNodes();
                for (int i=0; i<nodes.getLength(); i++){
                    Node node = nodes.item(i);
                    String nodeName = node.getNodeName();
                    String nodeValue = node.getTextContent();
                    if (nodeName.equalsIgnoreCase("Title")){
                        name = nodeValue;
                    }
                    else if (nodeName.equalsIgnoreCase("Abstract")){
                        description = nodeValue;
                    }
                    else if (nodeName.equalsIgnoreCase("SRS")){
                        srs = nodeValue;
                    }
                    else if (nodeName.equalsIgnoreCase("BoundingBox")){

                        NamedNodeMap map = node.getAttributes();
                        for (int j=0; j<map.getLength(); j++){
                            Node attr = map.item(j);
                            String coordName = attr.getNodeName();
                            String coord = attr.getNodeValue();

                            if (coordName.equalsIgnoreCase("minx")){
                                minX = Double.parseDouble(coord);
                            }
                            if (coordName.equalsIgnoreCase("miny")){
                                minY = Double.parseDouble(coord);
                            }
                            if (coordName.equalsIgnoreCase("maxx")){
                                maxX = Double.parseDouble(coord);
                            }
                            if (coordName.equalsIgnoreCase("maxy")){
                                maxY = Double.parseDouble(coord);
                            }
                        }


                    }
                    else if (nodeName.equalsIgnoreCase("TileFormat")){

                        int w = 256;
                        int h = 256;
                        
                        try{
                          //<TileFormat width="256" height="256" mime-type="image/png" extension="png"/>
                            NamedNodeMap map = node.getAttributes();
                            for (int j=0; j<map.getLength(); j++){
                                Node attr = map.item(j);
                                String attrName = attr.getNodeName();
                                String attrValue = attr.getNodeValue();
                                if (attrName.equalsIgnoreCase("width")){
                                    w = Integer.parseInt(attrValue);
                                }
                                else if (attrName.equalsIgnoreCase("height")){
                                    h = Integer.parseInt(attrValue);
                                }
                                else if (attrName.equalsIgnoreCase("extension")){
                                    format = attrValue;
                                }
                            }
                        }
                        catch(Exception e){}

                        tile_size = new int[]{w,h};
                    }
                }

                if (name!=null){
                    Layer layer = new Layer(name);
                    layer.setDescription(description);
                    layer.setBBox(new BBox(minX, minY, maxX, maxY, srs));
                    layer.addSRS(srs);
                    layer.setParam("host", url);
                    //layer.setParam("layers", layers);
                    layer.setParam("protocol", protocol);

                    int epsg = SRS.get_epsg_num(srs);
                    layer.setParam("grid", new mapproxy.core.grid.TileGrid(epsg, tile_size));
                    layer.setParam("format", format);
                    

                    return layer;
                }

            }
        }
        return null;
    }
  // </editor-fold>


// <editor-fold defaultstate="collapsed" desc="Parse WMS Capabilities. Click on the + sign on the left to edit the code.">

  //**************************************************************************
  //** parseWMSCapabilities
  //**************************************************************************
  /** Used to parse a WMS Capabilities Document. */

    private void parseWMSCapabilities(Document doc){

        protocol = "WMS";

        NodeList serviceInfo = null;
        NodeList capabilities = null;
        NodeList outerNodes = javaxt.xml.DOM.getOuterNode(doc).getChildNodes();
        for (int i=0; i<outerNodes.getLength(); i++){
            Node node = outerNodes.item(i);
            if (node.getNodeType()==1){
                String nodeName = node.getNodeName();
                if (nodeName.contains(":")) nodeName = nodeName.substring(nodeName.indexOf(":")+1);
                if (nodeName.equalsIgnoreCase("Service")){
                    serviceInfo = node.getChildNodes();
                }
                if (nodeName.equalsIgnoreCase("Capability")){
                    capabilities = node.getChildNodes();
                }
            }
        }

      //Parse service info
        if (serviceInfo!=null){
            for (int i=0; i<serviceInfo.getLength(); i++){
                Node node = serviceInfo.item(i);
                String nodeName = getNodeName(node);
                if (nodeName.equalsIgnoreCase("Name")){
                    name = getNodeValue(node);
                }
                if (nodeName.equalsIgnoreCase("Title")){
                    title = getNodeValue(node);
                }
                if (nodeName.equalsIgnoreCase("Abstract")){
                    description = getNodeValue(node);
                }
            }
        }




      //Parse capabilities
        if (capabilities!=null){
            for (int i=0; i<capabilities.getLength(); i++){
                Node node = capabilities.item(i);
                String nodeName = getNodeName(node);

              //Get Methods
                if (nodeName.equalsIgnoreCase("Request")){
                    NodeList methods = node.getChildNodes();
                    for (int j=0; j<methods.getLength(); j++){
                        Node method = methods.item(j);
                        String methodName = getNodeName(method);
                        if (methodName.equalsIgnoreCase("GetMap")){
                            this.url = getMapURL(method.getChildNodes());
                            this.formats = this.getWMSFormats(method.getChildNodes());
                            break;
                        }
                    }
                }

              //Get Layers
                else if (nodeName.equalsIgnoreCase("Layer")){
                    getLayers(node, null);
                }
            }
        }

    }


  //**************************************************************************
  //** getWMSFormats
  //**************************************************************************
  /** Used to extract a list of supported formats from a WMS Capabilities
   *  document.
   */
    private String[] getWMSFormats(NodeList GetMap){
        java.util.ArrayList<String> formats = new java.util.ArrayList<String>();
        for (int i=0; i<GetMap.getLength(); i++){
            Node node = GetMap.item(i);
            String nodeName = getNodeName(node);
            if (nodeName.equalsIgnoreCase("Format")){
                formats.add(getNodeValue(node));
            }
        }
        return formats.toArray(new String[formats.size()]);
    }

    
  //**************************************************************************
  //** getMapURL
  //**************************************************************************
  /** Used to extract the url associated with a GetMap request.
   */
    private String getMapURL(NodeList GetMap){

        for (int i=0; i<GetMap.getLength(); i++){
            Node node = GetMap.item(i);
            if (getNodeName(node).equalsIgnoreCase("DCPType")){
                NodeList protocols = node.getChildNodes();
                for (int j=0; j<protocols.getLength(); j++){
                    Node protocol = protocols.item(j);
                    if (getNodeName(protocol).equalsIgnoreCase("HTTP")){
                        NodeList methods = protocol.getChildNodes();
                        for (int k=0; k<methods.getLength(); k++){
                            Node method = methods.item(k);
                            if (getNodeName(method).equalsIgnoreCase("GET")){
                                NodeList resources = method.getChildNodes();
                                for (int l=0; l<resources.getLength(); l++){
                                    Node resource = resources.item(l);
                                    if (getNodeName(resource).equalsIgnoreCase("OnlineResource")){
                                        NamedNodeMap map = resource.getAttributes();
                                        for (int m=0; m<map.getLength(); m++){
                                            Node attr = map.item(m);
                                            String attrName = attr.getNodeName();
                                            if (attrName.contains(":")) attrName = attrName.substring(attrName.indexOf(":")+1);
                                            if (attrName.equalsIgnoreCase("href")){
                                                return attr.getNodeValue();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


  //**************************************************************************
  //** getLayers
  //**************************************************************************
  /** Used to extract layer info from an XML node in a WMS Capabilities
   *  Document and update the list of layers associated with this service.
   */
    private void getLayers(Node layerNode, Layer parentLayer){
        
        String name = null;
        String title = null;
        String description = null;
        BBox bbox = null;
        java.util.HashSet<String> srs = new java.util.HashSet<String>();
        java.util.Vector<Node> layerNodes = new java.util.Vector<Node>();
        NodeList childNodes = layerNode.getChildNodes();
        for (int i=0; i<childNodes.getLength(); i++){
            Node node = childNodes.item(i);
            String nodeName = getNodeName(node);
            if (nodeName.equalsIgnoreCase("Name")){
                name = getNodeValue(node);
            }
            if (nodeName.equalsIgnoreCase("Title")){
                title = getNodeValue(node);
            }
            if (nodeName.equalsIgnoreCase("Abstract")){
                description = getNodeValue(node);
            }
            if (nodeName.equalsIgnoreCase("SRS")){
                srs.add(getNodeValue(node));
            }
            if (nodeName.equalsIgnoreCase("Layer")){
                layerNodes.add(node);
            }
            if (nodeName.equalsIgnoreCase("LatLonBoundingBox")){
                double minX, minY, maxX, maxY;
                minX = minY = maxX = maxY = 0;
                NamedNodeMap map = node.getAttributes();
                for (int j=0; j<map.getLength(); j++){
                    Node attr = map.item(j);
                    String attrName = attr.getNodeName();
                    if (attrName.contains(":")) attrName = attrName.substring(attrName.indexOf(":")+1);
                    if (attrName.equalsIgnoreCase("minx")){
                        minX = Double.parseDouble(attr.getNodeValue());
                    }
                    if (attrName.equalsIgnoreCase("miny")){
                        minY = Double.parseDouble(attr.getNodeValue());
                    }
                    if (attrName.equalsIgnoreCase("maxx")){
                        maxX = Double.parseDouble(attr.getNodeValue());
                    }
                    if (attrName.equalsIgnoreCase("maxy")){
                        maxY = Double.parseDouble(attr.getNodeValue());
                    }
                }
                bbox = new BBox(minX, minY, maxX, maxY, "EPSG:4326");
            }
            if (nodeName.equalsIgnoreCase("BoundingBox")){
                double minX, minY, maxX, maxY;
                minX = minY = maxX = maxY = 0;
                String boxSRS = "EPSG:4326";
                NamedNodeMap map = node.getAttributes();
                for (int j=0; j<map.getLength(); j++){
                    Node attr = map.item(j);
                    String attrName = attr.getNodeName();
                    if (attrName.contains(":")) attrName = attrName.substring(attrName.indexOf(":")+1);
                    if (attrName.equalsIgnoreCase("minx")){
                        minX = Double.parseDouble(attr.getNodeValue());
                    }
                    if (attrName.equalsIgnoreCase("miny")){
                        minY = Double.parseDouble(attr.getNodeValue());
                    }
                    if (attrName.equalsIgnoreCase("maxx")){
                        maxX = Double.parseDouble(attr.getNodeValue());
                    }
                    if (attrName.equalsIgnoreCase("maxy")){
                        maxY = Double.parseDouble(attr.getNodeValue());
                    }
                    if (attrName.equalsIgnoreCase("SRS")){
                        boxSRS = attr.getNodeValue();
                    }
                }
                bbox = new BBox(minX, minY, maxX, maxY, boxSRS);
            }
        }


      //Encapsulate layer info in a class and update the list of layers
        Layer layer = new Layer(name);
        //layer.setSource(url, name, "WMS");
        layer.setDescription(description);
        layer.setBBox(bbox);
        java.util.Iterator<String> it = srs.iterator();
        while (it.hasNext()){
            layer.addSRS(it.next());
        }


        if (parentLayer!=null){
            parentLayer.addLayer(layer);
        }
        else{
            this.addLayer(layer);
        }
        
        for (int i=0; i<layerNodes.size(); i++){
            getLayers(layerNodes.get(i), layer);
        }
    }



  // </editor-fold>


// <editor-fold defaultstate="collapsed" desc="Parse ArcGIS Capabilities. Click on the + sign on the left to edit the code.">

  //**************************************************************************
  //** parseArcGISCapabilities
  //**************************************************************************
  /** Used to parse an ArcGIS capabilities document returned from a REST
   *  service.
   */
    private void parseArcGISCapabilities(String json){

        protocol = "MapServer";

        try{

          //Convert the JSON string into XML
            org.json.JSONObject jsonobject = new org.json.JSONObject(json);
            StringBuffer xml = new StringBuffer();
            xml.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\" ?>");
            xml.append("<json>");
            xml.append(org.json.XML.toString(jsonobject));
            xml.append("</json>");
            Document doc = javaxt.xml.DOM.createDocument(xml.toString());


            BBox bbox = null;
            java.util.HashSet<String> srs = new java.util.HashSet<String>();
            java.util.Vector<Node> layerNodes = new java.util.Vector<Node>();
            NodeList outerNodes = javaxt.xml.DOM.getOuterNode(doc).getChildNodes();
            for (int i=0; i<outerNodes.getLength(); i++){
                Node node = outerNodes.item(i);
                String nodeName = getNodeName(node);
                
                if (nodeName.equalsIgnoreCase("supportedImageFormatTypes")){
                    this.formats = getNodeValue(node).split(",");
                }
                if (nodeName.equalsIgnoreCase("description")){
                    this.description = getNodeValue(node);
                }
                if (nodeName.equalsIgnoreCase("serviceDescription")){
                    if (this.description==null)
                    this.description = getNodeValue(node);
                }
                if (nodeName.equalsIgnoreCase("mapName")){
                    this.name = getNodeValue(node);
                }
                if (nodeName.equalsIgnoreCase("layers")){
                    layerNodes.add(node);
                }
                if (nodeName.equalsIgnoreCase("documentInfo")){
                    NodeList documentInfo = node.getChildNodes();
                    for (int j=0; j<documentInfo.getLength(); j++){
                        Node info = documentInfo.item(j);
                        if (getNodeName(info).equalsIgnoreCase("Title")){
                            this.title = getNodeValue(info);
                        }
                    }
                }
                if (nodeName.equalsIgnoreCase("spatialReference")){
                    NodeList spatialReference = node.getChildNodes();
                    for (int j=0; j<spatialReference.getLength(); j++){
                        Node srsNode = spatialReference.item(j);
                        if (getNodeName(srsNode).equalsIgnoreCase("wkid")){
                            String srsValue = getNodeValue(srsNode);
                            if (!srsValue.contains(":")) srsValue = "EPSG:" + srsValue;
                            srs.add(srsValue);
                        }
                    }
                }
                if (nodeName.equalsIgnoreCase("fullExtent")){
                    double minX, minY, maxX, maxY;
                    minX = minY = maxX = maxY = 0;
                    String boxSRS = "EPSG:4326";
                    NodeList extents = node.getChildNodes();
                    for (int j=0; j<extents.getLength(); j++){
                        Node coord = extents.item(j);
                        String coordName = getNodeName(coord);
                        if (coordName.equalsIgnoreCase("xmin")){
                            minX = Double.parseDouble(getNodeValue(coord));
                        }
                        if (coordName.equalsIgnoreCase("ymin")){
                            minY = Double.parseDouble(getNodeValue(coord));
                        }
                        if (coordName.equalsIgnoreCase("xmax")){
                            maxX = Double.parseDouble(getNodeValue(coord));
                        }
                        if (coordName.equalsIgnoreCase("ymax")){
                            maxY = Double.parseDouble(getNodeValue(coord));
                        }
                        if (coordName.equalsIgnoreCase("spatialReference")){
                            NodeList spatialReference = coord.getChildNodes();
                            for (int k=0; k<spatialReference.getLength(); k++){
                                Node srsNode = spatialReference.item(k);
                                if (getNodeName(srsNode).equalsIgnoreCase("wkid")){
                                    boxSRS = getNodeValue(srsNode);
                                    if (!boxSRS.contains(":")) boxSRS = "EPSG:" + boxSRS;
                                    break;
                                }
                            }
                        }
                    }
                    bbox = new BBox(minX, minY, maxX, maxY, boxSRS);
                }

            }


          //Parse layers
            java.util.HashMap<Integer, Layer> layers = new java.util.HashMap<Integer, Layer>();
            java.util.Iterator<Node> it = layerNodes.iterator();
            while (it.hasNext()){
                NodeList layerAttributes = it.next().getChildNodes();
                String name = null;
                Integer id = -1;
                Integer parentID = -1;
                for (int i=0; i<layerAttributes.getLength(); i++){
                    Node node = layerAttributes.item(i);
                    String nodeName = getNodeName(node);
                    if (nodeName.equalsIgnoreCase("name")){
                        name = getNodeValue(node);
                    }
                    if (nodeName.equalsIgnoreCase("id")){
                        try{
                            id = Integer.parseInt(getNodeValue(node));
                        }
                        catch(Exception e){}
                    }
                    if (nodeName.equalsIgnoreCase("parentLayerId")){
                        try{
                            parentID = Integer.parseInt(getNodeValue(node));
                        }
                        catch(Exception e){}
                    }
                }
                Layer layer = new Layer(name, parentID);
                //layer.setSource(url, name, "ArcGIS");
                layer.setBBox(bbox);
                java.util.Iterator<String> it2 = srs.iterator();
                while (it2.hasNext()){
                    layer.addSRS(it2.next());
                }

                layers.put(id, layer);
            }



          //Sort the layer IDs
            Object[] key = layers.keySet().toArray();
            java.util.Arrays.sort(key);


          //Add layers to this service
            for (int i=0; i<key.length; i++) {
                Layer layer = layers.get(key[i]);
                int parentID = layer.getParentID();
                if (parentID==-1){
                    this.layers.add(layer);
                }
                else{
                    this.layers.get(parentID).addLayer(layer);
                }
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }



// </editor-fold>


  //**************************************************************************
  //** getNodeName
  //**************************************************************************
  /** Used to return a name of an XML Node. Removes any name space prefix from
   *  the node name.
   */
    private String getNodeName(Node node){
        if (node.getNodeType()==1){
            String nodeName = node.getNodeName();
            if (nodeName.contains(":")) nodeName = nodeName.substring(nodeName.indexOf(":")+1);
            return nodeName;
        }
        return "";
    }


  //**************************************************************************
  //** getNodeValue
  //**************************************************************************
  /** Used to return the value of an XML Node.
   */
    private String getNodeValue(Node node){
        return javaxt.xml.DOM.getNodeValue(node);
    }
}