package mapproxy.core;

//******************************************************************************
//**  BBOX Class
//******************************************************************************
/**
 *   Used to represent a bounding box used in map requests.
 *   Note that this class was not part of the original mapproxy baseline.
 *
 ******************************************************************************/

public class BBox {

    private double minX;
    private double minY;
    private double maxX;
    private double maxY;
    private String srs;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of BBOX. */

    public BBox(double minX, double minY, double maxX, double maxY, String srs) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.srs = srs;
    }

    public BBox(double minX, double minY, double maxX, double maxY) {
        this(minX, minY, maxX, maxY, null);
    }


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /**  "bbox" as a tuple (minx, miny, maxx, maxy). */

    public BBox(String bbox){

        if (bbox==null) return; //Throw Exception?

        String[] arr = bbox.split(",");
        double[] box = new double[4];
        for (int i=0; i<box.length; i++){
            box[i] = Double.valueOf(arr[i]);
        }
        minX = box[0];
        minY = box[1];
        maxX = box[2];
        maxY = box[3];
    }



    public double getMinX(){
        return minX;
    }

    public double getMinY(){
        return minY;
    }

    public double getMaxX(){
        return maxX;
    }

    public double getMaxY(){
        return maxY;
    }

    public String getSRS(){
        return srs;
    }


  //**************************************************************************
  //** toString
  //**************************************************************************
  /** Returns a comma delimited representation of the BBOX. */

    public String toString(){
        return minX + "," + minY + "," + maxX + "," + maxY;
    }


    public double[] toArray(){
        double[] box = new double[4];
        box[0] = minX;
        box[1] = minY;
        box[2] = maxX;
        box[3] = maxY;
        return box;
    }
}