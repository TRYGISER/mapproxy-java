package mapproxy.core.layer;
import mapproxy.wms.request.WMSMapRequest;
import mapproxy.core.SRS;

//******************************************************************************
//**  Layer Class
//******************************************************************************
/**
 *   Base class for all renderable layers.
 *
 ******************************************************************************/

public abstract class Layer {

    protected SRS srs;
    protected double[] bbox;



  //**************************************************************************
  //** render
  //**************************************************************************
  /** Render the response for the given `request`.
   * @param request the map request
   * @return one or more `ImageSource` with the rendered result.
   *  Return type is an `ImageSource` (javaxt.io.Image) or an iterable
   *  (Generator<javaxt.io.Image>) with multiple `ImageSource`
   */
    public Object render(WMSMapRequest request){
        return null;
    }


  //**************************************************************************
  //** bbox
  //**************************************************************************
    public double[] bbox(){
        double[] bbox = this._bbox();
        if (bbox == null)
            bbox = new double[]{-180, -90, 180, 90};
            if (!this.srs.equals(new SRS(4326)))
                bbox = new SRS(4326).transform_bbox_to(this.srs, bbox);
        return bbox;
    }

  //**************************************************************************
  //** llbbox
  //**************************************************************************
  /** The LatLonBoundingBox in EPSG:4326 */
    public double[] llbbox(){
        double[] bbox = this.bbox;
        if (!this.srs.equals(new SRS(4326))){
            bbox = this.srs.transform_bbox_to(new SRS(4326), bbox);
        }
        return bbox;
    }

  //**************************************************************************
  //** _bbox
  //**************************************************************************
    protected double[] _bbox(){
        return null;
    }


  //**************************************************************************
  //** srs
  //**************************************************************************
    public SRS srs(){
        SRS srs = this._srs();
        if (srs == null)
            srs = new SRS(4326);
        return srs;
    }


  //**************************************************************************
  //** _srs
  //**************************************************************************
    protected SRS _srs(){
        return null;
    }


}