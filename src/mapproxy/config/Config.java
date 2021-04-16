package mapproxy.config;
import mapproxy.core.cache.FileCache;
import mapproxy.core.grid.TileGrid;

//******************************************************************************
//**  Config Class
//******************************************************************************
/**
 *   Provides static methods to initialize and access config settings.
 *
 ******************************************************************************/

public class Config {

    public static Options options;

    public static Options base_config(){
        if (options==null) options = new Options();
        return options;
    }


    public static void load(org.w3c.dom.Document xml) throws Exception {


      //Set local variables
        String name = null;
        String description = null;
        int port = -1;
        int numThreads = 1;
        org.w3c.dom.NodeList layerNodes = null;


      //Parse outer nodes (service info and get layer nodes)
        org.w3c.dom.NodeList outerNodes = javaxt.xml.DOM.getOuterNode(xml).getChildNodes();
        for (int i=0; i<outerNodes.getLength(); i++){
            org.w3c.dom.Node outerNode = outerNodes.item(i);
            if (outerNode.getNodeType()==1){
                if (outerNode.getNodeName().equalsIgnoreCase("service")){
                    org.w3c.dom.NodeList serviceInfo = outerNode.getChildNodes();
                    for (int j=0; j<serviceInfo.getLength(); j++){
                        org.w3c.dom.Node node = serviceInfo.item(j);
                        if (node.getNodeType()==1){
                            String nodeName = node.getNodeName().toLowerCase();
                            if (nodeName.equalsIgnoreCase("name")){
                                name = javaxt.xml.DOM.getNodeValue(node);
                            }
                            if (nodeName.equalsIgnoreCase("description")){
                                description = javaxt.xml.DOM.getNodeValue(node);
                            }
                            if (nodeName.equalsIgnoreCase("port")){
                                port = Integer.parseInt(javaxt.xml.DOM.getNodeValue(node).trim());
                                if (port < 1 || port > 65535) throw new Exception("Invalid Port: " + javaxt.xml.DOM.getNodeValue(node));
                            }
                            if (nodeName.equalsIgnoreCase("numThreads")){
                                numThreads = Integer.parseInt(javaxt.xml.DOM.getNodeValue(node).trim());
                                if (numThreads < 1 || numThreads > 100000) throw new Exception("Invalid Number of Threads: " + javaxt.xml.DOM.getNodeValue(node));
                            }
                        }
                    }
                }
                else if (outerNode.getNodeName().equalsIgnoreCase("layers")){
                    layerNodes = outerNode.getChildNodes();
                }
                else if (outerNode.getNodeName().equalsIgnoreCase("html")){

                    Config.base_config().set("html", javaxt.xml.DOM.getNodeValue( outerNode ));
                    //System.out.println( javaxt.xml.DOM.getNodeValue( outerNode ));
                    //layerNodes = outerNode.getChildNodes();

                }
            }
        }


      //Create a new map service
        mapproxy.core.Service mapproxy = new mapproxy.core.Service(name, "http://localhost:" + port);
        mapproxy.setDescription(description);


      //Parse layer info and create service layers
        for (int i=0; i<layerNodes.getLength(); i++){
            org.w3c.dom.Node layerNode = layerNodes.item(i);
            if (layerNode.getNodeType()==1){

                String layerName = null;
                String host = null;
                String layers = null;
                String cacheFormat = "png";
                String cacheDir = null;
                String protocol = "WMS";
                int[] tile_size = new int[]{256,256};


                org.w3c.dom.NodeList params = layerNode.getChildNodes();
                for (int j=0; j<params.getLength(); j++){
                    org.w3c.dom.Node param = params.item(j);
                    if (param.getNodeType()==1){


                      //Get layer name/alias
                        if (param.getNodeName().equalsIgnoreCase("name")){
                            layerName = param.getTextContent();
                            if (layerName==null || layerName.trim().length()==0){
                                break;
                            }
                        }

                      //Get host information
                        if (param.getNodeName().equalsIgnoreCase("host")){
                            org.w3c.dom.NodeList nodes = param.getChildNodes();
                            for (int k=0; k<nodes.getLength(); k++){
                                org.w3c.dom.Node node = nodes.item(k);
                                if (node.getNodeType()==1){
                                    if (node.getNodeName().equalsIgnoreCase("BaseRequest")){
                                        host = node.getTextContent();
                                        if (host==null || host.trim().length()==0){
                                            break;
                                        }
                                        else{
                                            host = host.trim();
                                        }

                                        protocol = javaxt.xml.DOM.getAttributeValue(node.getAttributes(), "protocol");

                                    }
                                    if (node.getNodeName().equalsIgnoreCase("layers")){
                                        layers = node.getTextContent();
                                        if (layers==null || layers.trim().length()==0){
                                            break;
                                        }
                                        else{
                                            if (layers.contains(",")){
                                                String[] arr = layers.split(",");
                                                layers = "";
                                                for (int x=0; x<arr.length; x++){
                                                    layers+=arr[x].trim();
                                                    if (x<arr.length-1) layers+=",";
                                                }
                                            }
                                        }
                                    }
                                }
                            }


                        }


                      //Parse Cache Parameters
                        if (param.getNodeName().equalsIgnoreCase("FileCache")){
                            org.w3c.dom.NodeList nodes = param.getChildNodes();
                            for (int k=0; k<nodes.getLength(); k++){
                                org.w3c.dom.Node node = nodes.item(k);
                                if (node.getNodeType()==1){
                                    if (node.getNodeName().equalsIgnoreCase("CacheDirectory")){
                                        cacheDir = node.getTextContent();
                                    }
                                    if (node.getNodeName().equalsIgnoreCase("Format")){
                                        cacheFormat = node.getTextContent();
                                    }
                                    if (node.getNodeName().equalsIgnoreCase("TileSize")){
                                        String[] arr = node.getTextContent().trim().split(",");
                                        int width = Integer.parseInt(arr[0].trim());
                                        int height = Integer.parseInt(arr[1].trim());
                                        if (width < 1 || height < 1) throw new Exception("Invalid Tile Size: " + arr);
                                    }
                                }
                            }
                        }


                    }
                }

                if (layerName==null || host==null || layers==null) break;



                mapproxy.core.Layer layer = new mapproxy.core.Layer(layerName);
                layer.addSRS("EPSG:4326");
                layer.setBBox(new mapproxy.core.BBox(-180,-90,180,90,"EPSG:4326"));
                layer.setParam("host", host);
                layer.setParam("layers", layers);
                layer.setParam("protocol", protocol);

                if (cacheDir!=null){
                    layer.setParam("cache", new FileCache(cacheDir, cacheFormat));
                    layer.setParam("grid", new TileGrid(4326, tile_size));
                }
                mapproxy.addLayer(layer);


            }
        }



        if (mapproxy.getLayers().length<1) throw new Exception("No Layers to Process!");


      //Add the service object to the global config and start the http server
        Config.base_config().set("mapproxy", mapproxy);
        Config.base_config().set("threads", numThreads);
        Config.base_config().set("port", port);

    }

}
