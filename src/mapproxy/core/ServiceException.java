package mapproxy.core;

//******************************************************************************
//**  Service Exception
//******************************************************************************
/**
 *   Used to represent a server exception. Not yet implemented or used.
 *
 *   Note that this class was not part of the original mapproxy baseline.
 *
 ******************************************************************************/

public class ServiceException {


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of Exception. */

    public ServiceException() {

    }
    
    private String getWMSException(String error){
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version='1.0' encoding=\"ISO-8859-1\" standalone=\"no\" ?>");
        xml.append("<!DOCTYPE ServiceExceptionReport SYSTEM \"http://schemas.opengis.net/wms/1.1.1/exception_1_1_1.dtd\">");
        xml.append("<ServiceExceptionReport version=\"1.1.1\"><ServiceException>");
        xml.append(error);
        xml.append("</ServiceException></ServiceExceptionReport>");
        return xml.toString();
    }


    private String getTMSException(String error){
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" ?>");
        xml.append("<TileMapServerError>");
        xml.append("<Message>");
        xml.append(error);
        xml.append("</Message>");
        xml.append("</TileMapServerError>");
        return xml.toString();
    }

}