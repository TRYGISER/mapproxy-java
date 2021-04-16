package mapproxy.arcgis.server;
import mapproxy.core.Service;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.geotools.referencing.operation.DefaultCoordinateOperationFactory;
import org.geotools.referencing.CRS;

//******************************************************************************
//**  MapService Class
//******************************************************************************
/**
 *   Used to generate responses that mimic ArcGIS Server
 *
 ******************************************************************************/

public class MapServer {

    private Service service;
  //private static final String wgs84 = "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]]";
    private static final String wgs84 = "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433],AUTHORITY[\"EPSG\",4326]]";
  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of MapService.   */

    public MapServer(Service service){
        this.service = service;
    }



  //**************************************************************************
  //** getWSDL
  //**************************************************************************
  /** Used to return an ArcGIS style WSDL
   */
    public String getWSDL(){
        StringBuffer xml = new StringBuffer();
        xml.append("<definitions xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:e=\"http://www.esri.com/schemas/ArcGIS/10.0\" xmlns=\"http://schemas.xmlsoap.org/wsdl/\" targetNamespace=\"http://www.esri.com/schemas/ArcGIS/10.0\"><types><xs:schema targetNamespace=\"http://www.esri.com/schemas/ArcGIS/10.0\" xmlns=\"http://www.esri.com/schemas/ArcGIS/10.0\"><xs:element name=\"GetServiceDescriptions\"><xs:complexType/></xs:element><xs:element name=\"GetServiceDescriptionsResponse\"><xs:complexType><xs:sequence><xs:element name=\"ServiceDescriptions\" type=\"ArrayOfServiceDescription\"/></xs:sequence></xs:complexType></xs:element><xs:element name=\"GetServiceDescriptionsEx\"><xs:complexType><xs:sequence><xs:element name=\"FolderName\" type=\"xs:string\"/></xs:sequence></xs:complexType></xs:element><xs:element name=\"GetServiceDescriptionsExResponse\"><xs:complexType><xs:sequence><xs:element name=\"ServiceDescriptions\" type=\"ArrayOfServiceDescription\"/></xs:sequence></xs:complexType></xs:element><xs:element name=\"GetFolders\"><xs:complexType/></xs:element><xs:element name=\"GetFoldersResponse\"><xs:complexType><xs:sequence><xs:element name=\"FolderNames\" type=\"ArrayOfString\"/></xs:sequence></xs:complexType></xs:element><xs:element name=\"GetMessageVersion\"><xs:complexType/></xs:element><xs:element name=\"GetMessageVersionResponse\"><xs:complexType><xs:sequence><xs:element name=\"MessageVersion\" type=\"esriArcGISVersion\"/></xs:sequence></xs:complexType></xs:element><xs:element name=\"GetMessageFormats\"><xs:complexType/></xs:element><xs:element name=\"GetMessageFormatsResponse\"><xs:complexType><xs:sequence><xs:element name=\"MessageFormats\" type=\"esriServiceCatalogMessageFormat\"/></xs:sequence></xs:complexType></xs:element><xs:element name=\"GetTokenServiceURL\"><xs:complexType/></xs:element><xs:element name=\"GetTokenServiceURLResponse\"><xs:complexType><xs:sequence><xs:element name=\"TokenServiceURL\" type=\"xs:string\"/></xs:sequence></xs:complexType></xs:element><xs:element name=\"RequiresTokens\"><xs:complexType/></xs:element><xs:element name=\"RequiresTokensResponse\"><xs:complexType><xs:sequence><xs:element name=\"Result\" type=\"xs:boolean\"/></xs:sequence></xs:complexType></xs:element><xs:simpleType name=\"esriServiceCatalogMessageFormat\"><xs:annotation><xs:documentation/></xs:annotation><xs:restriction base=\"xs:string\"><xs:enumeration value=\"esriServiceCatalogMessageFormatSoap\"/><xs:enumeration value=\"esriServiceCatalogMessageFormatBin\"/><xs:enumeration value=\"esriServiceCatalogMessageFormatSoapOrBin\"/></xs:restriction></xs:simpleType><xs:complexType name=\"ServiceDescription\"><xs:annotation><xs:documentation/></xs:annotation><xs:sequence><xs:element name=\"Name\" type=\"xs:string\"/><xs:element name=\"Type\" type=\"xs:string\"/><xs:element name=\"Url\" type=\"xs:string\"/><xs:element name=\"ParentType\" type=\"xs:string\"/><xs:element name=\"Capabilities\" type=\"xs:string\"/><xs:element name=\"Description\" type=\"xs:string\"/></xs:sequence></xs:complexType><xs:complexType name=\"ArrayOfServiceDescription\"><xs:annotation><xs:documentation/></xs:annotation><xs:sequence><xs:element minOccurs=\"0\" maxOccurs=\"unbounded\" name=\"ServiceDescription\" type=\"ServiceDescription\"/></xs:sequence></xs:complexType><xs:complexType name=\"ArrayOfString\"><xs:annotation><xs:documentation/></xs:annotation><xs:sequence><xs:element minOccurs=\"0\" maxOccurs=\"unbounded\" name=\"String\" type=\"xs:string\"/></xs:sequence></xs:complexType><xs:simpleType name=\"esriArcGISVersion\"><xs:annotation><xs:documentation/></xs:annotation><xs:restriction base=\"xs:string\"><xs:enumeration value=\"esriArcGISVersion83\"/><xs:enumeration value=\"esriArcGISVersion90\"/><xs:enumeration value=\"esriArcGISVersion92\"/><xs:enumeration value=\"esriArcGISVersion93\"/><xs:enumeration value=\"esriArcGISVersion10\"/></xs:restriction></xs:simpleType></xs:schema></types><message name=\"GetMessageVersionIn\"><part name=\"parameters\" element=\"e:GetMessageVersion\"/></message><message name=\"GetMessageVersionOut\"><part name=\"parameters\" element=\"e:GetMessageVersionResponse\"/></message><message name=\"GetMessageFormatsIn\"><part name=\"parameters\" element=\"e:GetMessageFormats\"/></message><message name=\"GetMessageFormatsOut\"><part name=\"parameters\" element=\"e:GetMessageFormatsResponse\"/></message><message name=\"GetTokenServiceURLIn\"><part name=\"parameters\" element=\"e:GetTokenServiceURL\"/></message><message name=\"GetTokenServiceURLOut\"><part name=\"parameters\" element=\"e:GetTokenServiceURLResponse\"/></message><message name=\"GetFoldersIn\"><part name=\"parameters\" element=\"e:GetFolders\"/></message><message name=\"GetFoldersOut\"><part name=\"parameters\" element=\"e:GetFoldersResponse\"/></message><message name=\"GetServiceDescriptionsIn\"><part name=\"parameters\" element=\"e:GetServiceDescriptions\"/></message><message name=\"GetServiceDescriptionsOut\"><part name=\"parameters\" element=\"e:GetServiceDescriptionsResponse\"/></message><message name=\"RequiresTokensIn\"><part name=\"parameters\" element=\"e:RequiresTokens\"/></message><message name=\"RequiresTokensOut\"><part name=\"parameters\" element=\"e:RequiresTokensResponse\"/></message><message name=\"GetServiceDescriptionsExIn\"><part name=\"parameters\" element=\"e:GetServiceDescriptionsEx\"/></message><message name=\"GetServiceDescriptionsExOut\"><part name=\"parameters\" element=\"e:GetServiceDescriptionsExResponse\"/></message><portType name=\"ServiceCatalogPort\"><documentation/><operation name=\"GetMessageVersion\"><input message=\"e:GetMessageVersionIn\"/><output message=\"e:GetMessageVersionOut\"/></operation><operation name=\"GetMessageFormats\"><input message=\"e:GetMessageFormatsIn\"/><output message=\"e:GetMessageFormatsOut\"/></operation><operation name=\"GetTokenServiceURL\"><input message=\"e:GetTokenServiceURLIn\"/><output message=\"e:GetTokenServiceURLOut\"/></operation><operation name=\"GetFolders\"><input message=\"e:GetFoldersIn\"/><output message=\"e:GetFoldersOut\"/></operation><operation name=\"GetServiceDescriptions\"><input message=\"e:GetServiceDescriptionsIn\"/><output message=\"e:GetServiceDescriptionsOut\"/></operation><operation name=\"RequiresTokens\"><input message=\"e:RequiresTokensIn\"/><output message=\"e:RequiresTokensOut\"/></operation><operation name=\"GetServiceDescriptionsEx\"><input message=\"e:GetServiceDescriptionsExIn\"/><output message=\"e:GetServiceDescriptionsExOut\"/></operation></portType><binding name=\"ServiceCatalogBinding\" type=\"e:ServiceCatalogPort\"><soap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/><operation name=\"GetMessageVersion\"><soap:operation soapAction=\"\" style=\"document\"/><input><soap:body use=\"literal\"/></input><output><soap:body use=\"literal\"/></output></operation><operation name=\"GetMessageFormats\"><soap:operation soapAction=\"\" style=\"document\"/><input><soap:body use=\"literal\"/></input><output><soap:body use=\"literal\"/></output></operation><operation name=\"GetTokenServiceURL\"><soap:operation soapAction=\"\" style=\"document\"/><input><soap:body use=\"literal\"/></input><output><soap:body use=\"literal\"/></output></operation><operation name=\"GetFolders\"><soap:operation soapAction=\"\" style=\"document\"/><input><soap:body use=\"literal\"/></input><output><soap:body use=\"literal\"/></output></operation><operation name=\"GetServiceDescriptions\"><soap:operation soapAction=\"\" style=\"document\"/><input><soap:body use=\"literal\"/></input><output><soap:body use=\"literal\"/></output></operation><operation name=\"RequiresTokens\"><soap:operation soapAction=\"\" style=\"document\"/><input><soap:body use=\"literal\"/></input><output><soap:body use=\"literal\"/></output></operation><operation name=\"GetServiceDescriptionsEx\"><soap:operation soapAction=\"\" style=\"document\"/><input><soap:body use=\"literal\"/></input><output><soap:body use=\"literal\"/></output></operation></binding>");
        xml.append("<service name=\"Catalog\"><port name=\"ServiceCatalogPort\" binding=\"e:ServiceCatalogBinding\">");
        xml.append("<soap:address location=\"" + service.getURL() + "\"/></port></service>");
        xml.append("</definitions>");
        return xml.toString();
    }

    public String getServiceDescriptions(){
        StringBuffer xml = new StringBuffer();
        xml.append("<tns:GetServiceDescriptionsResponse>");
        xml.append("<ServiceDescriptions xsi:type=\"tns:ArrayOfServiceDescription\">");

        xml.append("<ServiceDescription xsi:type=\"tns:ServiceDescription\">");
        xml.append("<Name>" + service.getName() + "</Name>");
        xml.append("<Type>MapServer</Type>");
        xml.append("<Url>" + service.getURL() + "/ArcGIS/" + service.getName() + "</Url>"); //<--Need to URL Encode the Name
        xml.append("<ParentType></ParentType>");
        xml.append("<Capabilities>Map</Capabilities>"); //Map,Query,Data
        xml.append("<Description>" + service.getDescription() + "</Description>");
        xml.append("</ServiceDescription>");

        xml.append("</ServiceDescriptions>");
        xml.append("</tns:GetServiceDescriptionsResponse>");
        return getResponse(xml.toString());
    }

    public String getMessageVersion(){
        return getResponse("<tns:GetMessageVersionResponse><MessageVersion>esriArcGISVersion10</MessageVersion></tns:GetMessageVersionResponse>");
    }

    public String getFolders(){
        return getResponse("<tns:GetFoldersResponse><FolderNames xsi:type=\"tns:ArrayOfString\"><String>All</String></FolderNames></tns:GetFoldersResponse>");
    }

    public String getMessageFormats(){
        return getResponse("<tns:GetMessageFormatsResponse><MessageFormats>esriServiceCatalogMessageFormatSoap</MessageFormats></tns:GetMessageFormatsResponse>");
    }

    public String getDefaultMapName(){
        return getResponse("<tns:GetDefaultMapNameResponse><Result>Layers</Result></tns:GetDefaultMapNameResponse>");
    }

    public String getSupportedImageReturnTypes(){
        //esriImageReturnMimeData vs esriImageReturnURL ?
        return getResponse("<tns:GetSupportedImageReturnTypesResponse><Result>esriImageReturnURL</Result></tns:GetSupportedImageReturnTypesResponse>");
    }

    public String hasSingleFusedMapCache(){
        boolean hasSingleFusedMapCache = false;
        return getResponse("<tns:HasSingleFusedMapCacheResponse><Result>" + hasSingleFusedMapCache + "</Result></tns:HasSingleFusedMapCacheResponse>");
    }

    public String isFixedScaleMap(){
        return getResponse("<tns:IsFixedScaleMapResponse><Result>false</Result></tns:IsFixedScaleMapResponse>");
    }

    public String getTokenServiceURL(){
        return getResponse("<tns:GetTokenServiceURLResponse><TokenServiceURL></TokenServiceURL></tns:GetTokenServiceURLResponse>");
    }
    

    public String getTileCacheInfo(){
        return getResponse("<tns:GetTileCacheInfoResponse></tns:GetTileCacheInfoResponse>");
    }

    public String getTileImageInfo(){
        return getResponse("<tns:GetTileImageInfoResponse></tns:GetTileImageInfoResponse>");
    }
    
    
    private String getResponse(String body){
        return "<?xml version=\"1.0\" encoding=\"utf-8\" ?><soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:tns=\"http://www.esri.com/schemas/ArcGIS/10.0\"><soap:Body>" + body + "</soap:Body></soap:Envelope>";
    }

    public String getServerInfo(){
        return service.getCapabilities().toString("ArcGIS.SOAP");
    }

    public mapproxy.core.Capabilities getCapabilities(){
        return service.getCapabilities();
    }

    public String getLegendInfo(){
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
        xml.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:tns=\"http://www.esri.com/schemas/ArcGIS/10.0\">");
        xml.append("<Result xsi:type=\"tns:ArrayOfMapServerLegendInfo\">");
        xml.append("<MapServerLegendInfo xsi:type=\"tns:MapServerLegendInfo\">");
        xml.append("    <LayerID>0</LayerID>");
        xml.append("    <Name>" + this.service.getName() + "</Name>");
        xml.append("    <LegendGroups xsi:type=\"tns:ArrayOfMapServerLegendGroup\">");
        xml.append("        <MapServerLegendGroup xsi:type=\"tns:MapServerLegendGroup\">");
        xml.append("            <Heading></Heading>");
        xml.append("            <LegendClasses xsi:type=\"tns:ArrayOfMapServerLegendClass\">");
        xml.append("                <MapServerLegendClass xsi:type=\"tns:MapServerLegendClass\">");
        xml.append("                    <Label></Label>");
        xml.append("                <Description></Description>");
        xml.append("                <SymbolImage xsi:type=\"tns:ImageResult\">");
        xml.append("                    <ImageURL>http://sampleserver1a.arcgisonline.com/arcgisoutput/_ags_leg967087cc1feb4f7f90b88b44ee72a161.bmp</ImageURL>");
        xml.append("                    <ImageHeight>3</ImageHeight>");
        xml.append("                    <ImageWidth>10</ImageWidth>");
        xml.append("                    <ImageDPI>96</ImageDPI>");
        xml.append("                    </SymbolImage>");
        xml.append("                    <TransparentColor xsi:type=\"tns:RgbColor\">");
        xml.append("                        <UseWindowsDithering>true</UseWindowsDithering>");
        xml.append("                        <AlphaValue>255</AlphaValue><Red>254</Red><Green>255</Green><Blue>255</Blue>");
        xml.append("                    </TransparentColor>");
        xml.append("                </MapServerLegendClass>");
        xml.append("            </LegendClasses>");
        xml.append("        </MapServerLegendGroup>");
        xml.append("    </LegendGroups>");
        xml.append("</MapServerLegendInfo>");
        xml.append("</Result>");
        xml.append("</soap:Envelope>");
        return xml.toString();

    }


    
  //**************************************************************************
  //** getExportMapImageResponse
  //**************************************************************************
  /**  Contructs an XML response to an ExportMap image request. The document
   *   contains a link to a WMS service.
   */
    public String getExportMapImageResponse(javaxt.utils.URL url, String layers, String bbox, String wkt, String width, String height, String format, String returnType){


      //Convert the WKT into an EPSG for the WMS url
        String srs = "EPSG:4326";
        try{
            CoordinateReferenceSystem crs = CRS.parseWKT(wkt);
            srs = CRS.lookupIdentifier( crs, true );
            //System.out.println(crs.getName());
            //System.out.println("SRS:  " + srs);
        }
        catch(Exception e){
            e.printStackTrace();
        }


      //Update the format parameter
        if (format.startsWith("esriImage")) format = format.substring("esriImage".length());
        //if (format.equalsIgnoreCase("PNG24")) format = "jpeg"; //<-- This is crazy! If I return a 24 bit PNG, ArcMap doesn't display anything. If I return a JPEG, works like a champ

        
        
        url.setParameter("BBOX", bbox);
        url.setParameter("SERVICE", "WMS");
        url.setParameter("REQUEST", "GetMap");
        url.setParameter("VERSION", "1.1.1");
        url.setParameter("SRS", srs);
        url.setParameter("WIDTH", width);
        url.setParameter("HEIGHT", height);
        url.setParameter("LAYERS", layers);
        url.setParameter("FORMAT", "image/" + format);
        
        //System.out.println(url);

        
        StringBuffer xml = new StringBuffer();        
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
        xml.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:tns=\"http://www.esri.com/schemas/ArcGIS/10.0\">");
        xml.append("<soap:Body>");
        xml.append("<tns:ExportMapImageResponse>");
        xml.append("<Result xsi:type=\"tns:MapImage\">");
        xml.append("<ImageURL><![CDATA[" + url.toString() + "]]></ImageURL>");

        xml.append("<Extent xsi:type=\"tns:EnvelopeN\">");

        String[] arr = bbox.split(",");
        xml.append("<XMin>" + arr[0] + "</XMin><YMin>" + arr[1] + "</YMin><XMax>" + arr[2] + "</XMax><YMax>" + arr[3] + "</YMax>");

        if (srs.equalsIgnoreCase("EPSG:4326")){
            xml.append("<SpatialReference xsi:type=\"tns:GeographicCoordinateSystem\">");
            xml.append("<WKT>" + wgs84.replace("\"", "&quot;") + "</WKT>");
            xml.append("</SpatialReference>");
        }
        else{
            xml.append("<SpatialReference xsi:type=\"tns:ProjectedCoordinateSystem\">");
            xml.append("<WKT>" + wkt.replace("\"", "&quot;") + "</WKT>");
            xml.append("</SpatialReference>");
        }

        xml.append("</Extent>");

      //Layers
        xml.append("<VisibleLayerIDs xsi:type=\"tns:ArrayOfInt\">");
        for (String layer : layers.split(",")){
            xml.append("<Int>" + layer + "</Int>");
        }
        xml.append("</VisibleLayerIDs>");

        
        xml.append("<MapScale>994187.94491636404</MapScale>");

        xml.append("<ImageHeight>" + height + "</ImageHeight>");
        xml.append("<ImageWidth>" + width + "</ImageWidth>");
        xml.append("<ImageDPI>96</ImageDPI>");

        //xml.append("<ImageType></ImageType>");

        xml.append("</Result>");
        xml.append("</tns:ExportMapImageResponse>");
        xml.append("</soap:Body>");
        xml.append("</soap:Envelope>");

        //new javaxt.io.File("/temp/export.xml").write(xml.toString(), "UTF-8");
        return xml.toString();


    }
    
}