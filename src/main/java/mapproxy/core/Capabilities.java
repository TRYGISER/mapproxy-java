package mapproxy.core;

//******************************************************************************
//**  Capabilities Class
//******************************************************************************
/**
 *   Used to construct a capabilities document in various formats including
 *   WMS and ArcGIS.
 *
 *   Note that this class was not part of the original mapproxy baseline.
 *
 ******************************************************************************/

public class Capabilities {

    private Service service;
    

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of Capabilities. */

    public Capabilities(Service service) {
        this.service = service;
    }


  //**************************************************************************
  //** toString
  //**************************************************************************
  /** Returns a capabilities document in the requested format (e.g. WMS,
   *  ArcGIS, etc.)
   */
    public String toString(String format){
        if (format.equalsIgnoreCase("WMS")){
            return WMS();
        }
        else if (format.equalsIgnoreCase("TMS")){
            return TMS();
        }
        else if (format.equalsIgnoreCase("ArcGIS.JSON")){
            return ArcGIS_JSON();
        }
        else if (format.equalsIgnoreCase("ArcGIS.SOAP")){
            return ArcGIS_SOAP();
        }
        return null;
    }

    
  //**************************************************************************
  //** toString
  //**************************************************************************
  /** Returns an OGC WMS Capabilities Document. Use the other toString()
   *  method to get a capabilities document in other formats.
   */
    public String toString(){
        return toString("WMS");
    }


    
  //**************************************************************************
  //** WMS Capabilities
  //**************************************************************************
  /** Returns an XML document suitable for a WMS GetCapabilities response. */

    private String WMS(){

        String url = service.getURL();

        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>");
        xml.append("<!DOCTYPE WMT_MS_Capabilities SYSTEM \"http://schemas.opengeospatial.net/wms/1.1.1/capabilities_1_1_1.dtd\" [");
        xml.append(" <!ELEMENT VendorSpecificCapabilities EMPTY>");
        xml.append(" ]>");
        xml.append("<WMT_MS_Capabilities version=\"1.1.1\">");
        xml.append("<Service>");
        xml.append("<Name>" + service.getName() + "</Name>");
        xml.append("<Title>" + service.getTitle() + "</Title>");
        xml.append("<Abstract>" + service.getDescription() + "</Abstract>");
        xml.append("<OnlineResource xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"" + url + "\"/>");
        xml.append("<ContactInformation>");
        xml.append("</ContactInformation>");
        xml.append("</Service>");
        xml.append("<Capability>");
        xml.append("<Request>");
        xml.append("<GetCapabilities>");
        xml.append("<Format>application/vnd.ogc.wms_xml</Format>");
        xml.append("<DCPType>");
        xml.append("<HTTP>");
        xml.append("<Get><OnlineResource xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"" + url + "\"/></Get>");
        xml.append("<Post><OnlineResource xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"" + url + "\"/></Post>");
        xml.append("</HTTP>");
        xml.append("</DCPType>");
        xml.append("</GetCapabilities>");
        xml.append("<GetMap>");
        for (String format : service.getFormats()){
            xml.append("<Format>image/" + format.toLowerCase() + "</Format>");
        }
        xml.append("<DCPType>");
        xml.append("<HTTP>");
        xml.append("<Get><OnlineResource xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"" + url + "\"/></Get>");
        xml.append("<Post><OnlineResource xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"" + url + "\"/></Post>");
        xml.append("</HTTP>");
        xml.append("</DCPType>");
        xml.append("</GetMap>");
        xml.append("<GetFeatureInfo>");
        xml.append("<Format>text/plain</Format>");
        xml.append("<Format>application/vnd.ogc.gml</Format>");
        xml.append("<DCPType>");
        xml.append("<HTTP>");
        xml.append("<Get><OnlineResource xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"" + url + "\"/></Get>");
        xml.append("<Post><OnlineResource xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"" + url + "\"/></Post>");
        xml.append("</HTTP>");
        xml.append("</DCPType>");
        xml.append("</GetFeatureInfo>");
        xml.append("</Request>");
        xml.append("<Exception>");
        xml.append("<Format>application/vnd.ogc.se_xml</Format>");
        xml.append("<Format>application/vnd.ogc.se_inimage</Format>");
        xml.append("<Format>application/vnd.ogc.se_blank</Format>");
        xml.append("</Exception>");
        xml.append("<VendorSpecificCapabilities/>");
        xml.append("<UserDefinedSymbolization SupportSLD=\"0\" UserLayer=\"0\" UserStyle=\"1\" RemoteWFS=\"0\"/>");

        for (Layer layer : service.getLayers()){
            xml.append(WMSLayer(layer));
        }

        xml.append("</Capability>");
        xml.append("</WMT_MS_Capabilities>");

        return xml.toString();

    }


  //**************************************************************************
  //** WMSLayer
  //**************************************************************************
  /** Returns an XML fragment used to represent a layer in a WMS Capabilities
   *  document.
   */
    private String WMSLayer(Layer layer){
        StringBuffer xml = new StringBuffer();
        xml.append("<Layer>");
        xml.append("<Name>" + layer.getName() + "</Name>");
        xml.append("<Title>" + layer.getName() + "</Title>");

        if (layer.getDescription()!=null){
            xml.append("<Description>" + layer.getDescription() + "</Description>");
        }

        for (String srs : layer.getSRS()){
            xml.append("<SRS>" + srs + "</SRS>");
        }
        BBox bbox = layer.getBBox();
        if (bbox!=null){
            xml.append("<LatLonBoundingBox  minx=\"" + bbox.getMinX() + "\" miny=\"" + bbox.getMinY() + "\" maxx=\"" + bbox.getMaxX() + "\" maxy=\"" + bbox.getMaxY() + "\"/>");
        }

        if (layer.hasLayers()){
            for (Layer subLayer : layer.getLayers()){
                xml.append(WMSLayer(subLayer));
            }
        }

        xml.append("</Layer>");
        return xml.toString();
    }


  //**************************************************************************
  //** TMS Capabilities
  //**************************************************************************
  /** Returns an XML document suitable for a TileMapService request. */

    private String TMS(){
        return new mapproxy.tms.TileServer(service).getCapabilities();
    }


  //**************************************************************************
  //** ArcGIS REST Capabilities
  //**************************************************************************
  /** Returns a JSON document suitable for a ArcGIS REST service. */

    private String ArcGIS_JSON(){

        String callback = null;
        /*
        javaxt.utils.URL url = new javaxt.utils.URL(request.getURL());
        callback = url.getParameter("callback");
        if (callback!=null){
            callback = callback.trim();
            if (callback.length()==0) callback = null;
        }
        */



        StringBuffer json = new StringBuffer();

        if (callback!=null) json.append(callback+"(");
        json.append("{\r\n");
        json.append("  \"serviceDescription\" : \"" + service.getDescription() + "\", \r\n");
        json.append("  \"mapName\" : \"Layers\", \r\n");
        if (service.getDescription()!=null){
            json.append("  \"description\" : \"" + service.getDescription() + "\", \r\n");
        }
        //json.append("  \"copyrightText\" : \"(c) None\", \r\n");
        json.append("  \"layers\" : [\r\n");

        int x = 0;
        for (Layer layer : service.getLayers()){
            json.append("    {\r\n");
            json.append("      \"id\" : "+ x +", \r\n");
            json.append("      \"name\" : \""+ layer.getName() +"\", \r\n");
            json.append("      \"parentLayerId\" : -1, \r\n");
            json.append("      \"defaultVisibility\" : true, \r\n");
            json.append("      \"subLayerIds\" : null\r\n");
            json.append("    }, \r\n");
            x++;
        }

        json.append("  ], \r\n");
        json.append("  \"spatialReference\" : {\r\n");
        json.append("    \"wkid\" : 4326\r\n");
        json.append("  }, \r\n");
        json.append("  \"singleFusedMapCache\" : false, \r\n");
        json.append("  \"initialExtent\" : {\r\n");
        json.append("    \"xmin\" : -180.0, \r\n");
        json.append("    \"ymin\" : -90.0, \r\n");
        json.append("    \"xmax\" : 180.0, \r\n");
        json.append("    \"ymax\" : 90.0, \r\n");
        json.append("    \"spatialReference\" : {\r\n");
        json.append("      \"wkid\" : 4326\r\n");
        json.append("    }\r\n");
        json.append("  }, \r\n");
        json.append("  \"fullExtent\" : {\r\n");
        json.append("    \"xmin\" : -180.0, \r\n");
        json.append("    \"ymin\" : -90.0, \r\n");
        json.append("    \"xmax\" : 180.0, \r\n");
        json.append("    \"ymax\" : 90.0, \r\n");
        json.append("    \"spatialReference\" : {\r\n");
        json.append("      \"wkid\" : 4326\r\n");
        json.append("    }\r\n");
        json.append("  }, \r\n");
        json.append("  \"units\" : \"esriDecimalDegrees\", \r\n");


        json.append("  \"supportedImageFormatTypes\" : \"");
        String[] formats = service.getFormats();
        for (int i=0; i<formats.length; i++){
            json.append(formats[i]);
            if (i<formats.length-1) json.append(",");
        }
        json.append("\", \r\n");


        json.append("  \"documentInfo\" : {\r\n");
        json.append("    \"Title\" : \"" + service.getName() + "\", \r\n");
        json.append("    \"Author\" : \"\", \r\n");
        json.append("    \"Comments\" : \"\", \r\n");
        json.append("    \"Subject\" : \"\", \r\n");
        json.append("    \"Category\" : \"\", \r\n");
        json.append("    \"Keywords\" : \"\"\r\n");
        json.append("  }\r\n");
        json.append("}\r\n");
        if (callback!=null) json.append(");");

        return json.toString();
    }

  //**************************************************************************
  //** ArcGIS Capabilities
  //**************************************************************************
  /** Returns an XML document suitable for a ArcGIS SOAP service. */

    private String ArcGIS_SOAP(){

        String wgs84 = "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]]";
        ///wgs84 = wgs84.replace("\"", "&quot;");

        StringBuffer xml = new StringBuffer();

        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
        xml.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:tns=\"http://www.esri.com/schemas/ArcGIS/9.0\">");
        xml.append("<soap:Body>");
        xml.append("<tns:GetServerInfoResponse>");
        xml.append("<Result xsi:type=\"tns:MapServerInfo\">");

        xml.append("<Name>Layers</Name>");
        xml.append("<Description>" + service.getDescription() + "</Description>");

        xml.append("<FullExtent xsi:type=\"tns:EnvelopeN\">");
        xml.append("<XMin>" + (-180D) + "</XMin><YMin>" + (-90D) + "</YMin>");
        xml.append("<XMax>" + (+180D) + "</XMax><YMax>" + (+90D) + "</YMax>");
        xml.append("<SpatialReference xsi:type=\"tns:GeographicCoordinateSystem\">");
        xml.append("<WKT>" + wgs84 + "</WKT>");
        xml.append("</SpatialReference>");
        xml.append("</FullExtent>");

        xml.append("<Extent xsi:type=\"tns:EnvelopeN\">");
        xml.append("<XMin>" + (-180D) + "</XMin><YMin>" + (-90D) + "</YMin>");
        xml.append("<XMax>" + (+180D) + "</XMax><YMax>" + (+90D) + "</YMax>");
        xml.append("<SpatialReference xsi:type=\"tns:GeographicCoordinateSystem\">");
        xml.append("<WKT>" + wgs84 + "</WKT>");
        xml.append("</SpatialReference>");
        xml.append("</Extent>");

        xml.append("<SpatialReference xsi:type=\"tns:GeographicCoordinateSystem\">");
        xml.append("<WKT>" + wgs84 + "</WKT>");
        xml.append("</SpatialReference>");

        xml.append("<MapLayerInfos xsi:type=\"tns:ArrayOfMapLayerInfo\">");

      //Create List of Layers
        int x = 0;
        for (Layer layer : service.getLayers()){

            xml.append("<MapLayerInfo xsi:type=\"tns:MapLayerInfo\">");

            xml.append("<LayerID>" + x + "</LayerID>");
            xml.append("<Name>" + layer.getName() + "</Name>");
            xml.append("<Description>" + layer.getDescription() + "</Description>");
            xml.append("<LayerType>Feature Layer</LayerType>");
            xml.append("<SourceDescription></SourceDescription>");
            xml.append("<HasLabels>false</HasLabels>");
            xml.append("<CanSelect>false</CanSelect>");
            xml.append("<CanScaleSymbols>false</CanScaleSymbols>");
            xml.append("<MinScale>0</MinScale>");
            xml.append("<MaxScale>0</MaxScale>");

            xml.append("<Extent xsi:type=\"tns:EnvelopeN\">");
            xml.append("<XMin>" + (-180D) + "</XMin><YMin>" + (-90D) + "</YMin>");
            xml.append("<XMax>" + (+180D) + "</XMax><YMax>" + (+90D) + "</YMax>");
            xml.append("<SpatialReference xsi:type=\"tns:GeographicCoordinateSystem\">");
            xml.append("<WKT>" + wgs84 + "</WKT>");
            xml.append("</SpatialReference>");
            xml.append("</Extent>");

            xml.append("<HasHyperlinks>false</HasHyperlinks>");
            xml.append("<HasAttributes>false</HasAttributes>");
            xml.append("<CanIdentify>false</CanIdentify>");
            xml.append("<CanFind>false</CanFind>");
            xml.append("<IsFeatureLayer>true</IsFeatureLayer>");

          //Fields
            xml.append("<Fields xsi:type=\"tns:Fields\">");
            xml.append("<FieldArray xsi:type=\"tns:ArrayOfField\">");

                xml.append("<Field xsi:type=\"tns:Field\">");
                xml.append("<Name>OBJECTID</Name>");
                xml.append("<Type>esriFieldTypeOID</Type>");
                xml.append("<IsNullable>false</IsNullable><Length>4</Length><Precision>0</Precision><Scale>0</Scale><Required>true</Required><Editable>false</Editable><AliasName>OBJECTID</AliasName><ModelName>OBJECTID</ModelName>");
                xml.append("</Field>");

                xml.append("<Field xsi:type=\"tns:Field\">");
                xml.append("<Name>Shape</Name>");
                xml.append("<Type>esriFieldTypeGeometry</Type><IsNullable>true</IsNullable><Length>0</Length>");
                xml.append("<Precision>0</Precision><Scale>0</Scale><Required>true</Required>");
                xml.append("<GeometryDef xsi:type=\"tns:GeometryDef\"><AvgNumPoints>0</AvgNumPoints>");
                xml.append("<GeometryType>esriGeometryPolygon</GeometryType>");
                xml.append("<HasM>false</HasM><HasZ>false</HasZ>");
                xml.append("<SpatialReference xsi:type=\"tns:GeographicCoordinateSystem\">");
                xml.append("<WKT>" + wgs84 + "</WKT>");
                xml.append("</SpatialReference>");
                xml.append("<GridSize0>0</GridSize0></GeometryDef><AliasName>Shape</AliasName><ModelName>Shape</ModelName>");
                xml.append("</Field>");

                xml.append("<Field xsi:type=\"tns:Field\">");
                xml.append("<Name>Id</Name><Type>esriFieldTypeInteger</Type><IsNullable>true</IsNullable><Length>4</Length><Precision>0</Precision><Scale>0</Scale><AliasName>Id</AliasName>");
                xml.append("</Field>");

            xml.append("</FieldArray>");
            xml.append("</Fields>");


            xml.append("<DisplayField>Id</DisplayField><IsComposite>false</IsComposite>");
            xml.append("<ParentLayerID>-1</ParentLayerID>");

            xml.append("</MapLayerInfo>");
            x++;
            //break;
        }


        xml.append("</MapLayerInfos>");
        
        xml.append("<BackgroundColor xsi:type=\"tns:RgbColor\"><UseWindowsDithering>false</UseWindowsDithering><AlphaValue>255</AlphaValue><Red>255</Red><Green>255</Green><Blue>255</Blue></BackgroundColor>");
        xml.append("<Bookmarks xsi:type=\"tns:ArrayOfMapServerBookmark\"></Bookmarks>");


        xml.append("<DefaultMapDescription xsi:type=\"tns:MapDescription\">");

        xml.append("<Name>Layers</Name>");
        xml.append("<MapArea xsi:type=\"tns:MapExtent\">");
        xml.append("<Extent xsi:type=\"tns:EnvelopeN\">");
        xml.append("<XMin>" + (-180D) + "</XMin><YMin>" + (-90D) + "</YMin>");
        xml.append("<XMax>" + (+180D) + "</XMax><YMax>" + (+90D) + "</YMax>");
        xml.append("<SpatialReference xsi:type=\"tns:GeographicCoordinateSystem\">");
        xml.append("<WKT>" + wgs84 + "</WKT>");
        xml.append("</SpatialReference>");
        xml.append("</Extent>");
        xml.append("</MapArea>");


        xml.append("<LayerDescriptions xsi:type=\"tns:ArrayOfLayerDescription\">");

      //Create List of Layers
        x = 0;
        for (Layer layer : service.getLayers()){
            xml.append("<LayerDescription xsi:type=\"tns:LayerDescription\">");
            xml.append("<LayerID>" + x + "</LayerID><Visible>true</Visible><ShowLabels>false</ShowLabels><ScaleSymbols>true</ScaleSymbols>");
            xml.append("<SelectionFeatures xsi:type=\"tns:ArrayOfInt\"></SelectionFeatures>");

            xml.append("<SelectionSymbol xsi:type=\"tns:SimpleFillSymbol\"><Outline xsi:type=\"tns:SimpleLineSymbol\">");
            xml.append("   <Color xsi:type=\"tns:RgbColor\"><UseWindowsDithering>true</UseWindowsDithering><AlphaValue>255</AlphaValue><Red>0</Red><Green>255</Green><Blue>255</Blue></Color><Style>esriSLSSolid</Style><Width>2</Width></Outline><Style>esriSFSSolid</Style>");
            xml.append("</SelectionSymbol>");

            xml.append("<SetSelectionSymbol>false</SetSelectionSymbol>");
            xml.append("<SelectionBufferDistance>0</SelectionBufferDistance>");
            xml.append("<ShowSelectionBuffer>false</ShowSelectionBuffer>");
            xml.append("<DefinitionExpression></DefinitionExpression>");
            xml.append("</LayerDescription>");
            x++;
            break;
        }
        xml.append("</LayerDescriptions>");


        xml.append("<Rotation>0</Rotation>");

        xml.append("<SpatialReference xsi:type=\"tns:GeographicCoordinateSystem\">");
        xml.append("<WKT>" + wgs84 + "</WKT>");
        xml.append("</SpatialReference>");

        xml.append("<SelectionColor xsi:type=\"tns:RgbColor\">");
        xml.append("<UseWindowsDithering>true</UseWindowsDithering><AlphaValue>255</AlphaValue><Red>0</Red><Green>255</Green><Blue>255</Blue>");
        xml.append("</SelectionColor>");


        xml.append("</DefaultMapDescription>");

        xml.append("<Units>esriDecimalDegrees</Units>");  //esriDecimalDegrees vs esriMeters

        xml.append("</Result>");
        xml.append("</tns:GetServerInfoResponse>");
        xml.append("</soap:Body>");
        xml.append("</soap:Envelope>");

        return xml.toString();

    }


}