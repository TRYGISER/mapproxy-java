package mapproxy.core;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.geotools.referencing.operation.DefaultCoordinateOperationFactory;
import org.geotools.referencing.CRS;

//******************************************************************************
//**  SRS Class
//******************************************************************************
/**
 *   This class represents a Spatial Reference System.
 *
 ******************************************************************************/

public class SRS {
    

    private int epsg;
    private String proj;
    public CoordinateReferenceSystem crs; //<-- GeoTools CRS (Added to support ImageTransformer)


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Create a new SRS with the given `srs_code` code. */
    public SRS(String srs_code){


        epsg = 4326;
        try{
            epsg = get_epsg_num(srs_code);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        this.proj = "epsg:" + epsg;


      //Set GeoTools CRS
        try{
            if (epsg == 4326) crs = CRS.decode(this.proj, true);
            else crs = CRS.decode(this.proj);
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }


  //**************************************************************************
  //** Overloaded Constructor
  //**************************************************************************
    public SRS(int srs){
        this("epsg:"+srs);
    }



    public boolean isGeographic(){
        return mapproxy.core.grid.Grid.geodetic_epsg_codes.contains(epsg);
    }




  //**************************************************************************
  //** get_epsg_num
  //**************************************************************************
  /**
   *  get_epsg_num('ePsG:4326') returns 4326
   *  get_epsg_num(4313) returns 4313
   *  get_epsg_num('31466') returns 31466
   */
    public static int get_epsg_num(Object epsg_code){

        if (epsg_code instanceof String){
            String epsg = (String) epsg_code;
            epsg = epsg.trim();
            if (epsg.toLowerCase().startsWith("epsg:")){
                epsg = epsg.substring(5);
            }
            return cint(epsg);
        }
        return cint(epsg_code);
    }




  //**************************************************************************
  //** transform_to
  //**************************************************************************
  /** 
        :type points: ``(x, y)`` or ``[(x1, y1), (x2, y2), etc]``
        
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
    public java.util.ArrayList<Point> transform_to(SRS other_srs, java.util.ArrayList<Point> points){

        if (other_srs.equals(this)){
            return points;
        }


      //Use GeoTools to transform the points
        try{
            
          //Define Input CoordinateReferenceSystem
            CoordinateReferenceSystem sourceCS = this.crs;
                                    
          //Define Output CoordinateReferenceSystem
            CoordinateReferenceSystem targetCS = other_srs.crs;

          //Initialize Transformation
            DefaultCoordinateOperationFactory trFactory = new DefaultCoordinateOperationFactory();
            MathTransform mt = trFactory.createOperation(sourceCS, targetCS).getMathTransform();

            boolean flipPoints = !sourceCS.getCoordinateSystem().getAxis(0).getDirection().equals(targetCS.getCoordinateSystem().getAxis(0).getDirection());


          //Convert the points
            int i = 0;
            for (Point point : points){
                double[] pt = new double[2];
                pt[0] = point.x;
                pt[1] = point.y;


                mt.transform(pt, 0, pt, 0, 1);
                if (flipPoints){
                    point = new Point(pt[1],pt[0]);
                }
                else{
                    point = new Point(pt[0],pt[1]);
                }

                points.set(i, point);
                i++;
            }


        }
        catch (Exception e){
            e.printStackTrace();
        }
        

        /*

        if self == other_srs:
            return points
        if isinstance(points[0], (int, float)) and 2 >= len(points) <= 3:
            return transform(self.proj, other_srs.proj, *points)

        x = [p[0] for p in points]
        y = [p[1] for p in points]
        transf_pts = transform(self.proj, other_srs.proj, x, y)
        return izip(transf_pts[0], transf_pts[1])

         *
         */


        return points;
    }



  //**************************************************************************
  //** transform_bbox_to
  //**************************************************************************
  /**
        :param with_points: the number of points to use for the transformation.
            A bbox transformation with only two or four points may cut off some
            parts due to distortions.

        >>> ['%.3f' % x for x in
        ...  SRS(4326).transform_bbox_to(SRS(900913), (-180.0, -90.0, 180.0, 90.0))]
        ['-20037508.343', '-147730762.670', '20037508.343', '147730758.195']
        >>> ['%.5f' % x for x in
        ...  SRS(4326).transform_bbox_to(SRS(900913), (8.2, 53.1, 8.3, 53.2))]
        ['912819.82450', '7001516.67745', '923951.77358', '7020078.53264']
        >>> SRS(4326).transform_bbox_to(SRS(4326), (8.25, 53.0, 8.5, 53.75))
        (8.25, 53.0, 8.5, 53.75)

   */
    public double[] transform_bbox_to(SRS other_srs, double[] bbox, int with_points){ //with_points=16
        if (this.equals(other_srs)) return bbox;
        bbox = this.align_bbox(bbox);
        java.util.ArrayList<Point> points = generate_envelope_points(bbox, with_points);
        java.util.ArrayList<Point> transf_pts = this.transform_to(other_srs, points);
        double[] result = calculate_bbox(transf_pts);

        /*
        log.debug('transformed from %r to %r (%s -> %s)' %
                  (self, other_srs, bbox, result))
        */
        
        return result;
    }

    public double[] transform_bbox_to(SRS other_srs, double[] bbox){
        return transform_bbox_to(other_srs, bbox, 16);
    }



  //**************************************************************************
  //** align_bbox
  //**************************************************************************
  /**  Align bbox to reasonable values to prevent errors in transformations.
   *   E.g. transformations from EPSG:4326 with lat=90 or -90 will fail, so
   *   we subtract a tiny delta.
   *
   *   At the moment only EPSG:4326 bbox will be modifyed.
   <pre>
        >>> SRS(4326).align_bbox((-180, -90, 180, 90))
        (-180, -89.999999990000006, 180, 89.999999990000006)
   </pre>
   */
    public double[] align_bbox(double[] bbox){
        if (this.equals("EPSG:4326")){
            double delta = 0.00000001;
            //(minx, miny, maxx, maxy) = bbox
            double minx = bbox[0];
            double miny = bbox[1];
            double maxx = bbox[2];
            double maxy = bbox[3];
            if (miny <= -90.0)
                miny = -90.0 + delta;
            if (maxy >= 90.0)
                maxy = 90.0 - delta;
            bbox = new double[]{minx, miny, maxx, maxy};
        }
        return bbox;
    }

    

  //**************************************************************************
  //** is_latlong
  //**************************************************************************
  /**  Unused method. Referenced in the mapproxy.wms.layer.MultiLayer class
   *
        >>> SRS(4326).is_latlong
        True
        >>> SRS(31466).is_latlong
        False
   */

    public boolean is_latlong(){

        return true;
        //return this.proj.is_latlong();
    }



  //**************************************************************************
  //** generate_envelope_points
  //**************************************************************************
  /** Generates points that form a linestring around a given bbox.
   *
   * @param bbox: bbox to generate linestring for
   * @param n: the number of points to generate around the bbox
   *

    >>> generate_envelope_points((10.0, 5.0, 20.0, 15.0), 4)
    [(10.0, 5.0), (20.0, 5.0), (20.0, 15.0), (10.0, 15.0)]
    >>> generate_envelope_points((10.0, 5.0, 20.0, 15.0), 8)
    ... #doctest: +NORMALIZE_WHITESPACE
    [(10.0, 5.0), (15.0, 5.0), (20.0, 5.0), (20.0, 10.0),\
     (20.0, 15.0), (15.0, 15.0), (10.0, 15.0), (10.0, 10.0)]
   */
    public java.util.ArrayList<Point> generate_envelope_points(double[] bbox, int n){


        //(minx, miny, maxx, maxy) = bbox
        double minx = bbox[0];
        double miny = bbox[1];
        double maxx = bbox[2];
        double maxy = bbox[3];
        if (n <= 4)
            n = 0;
        else
            n = cint(Math.ceil((n - 4) / 4.0));

        double width = maxx - minx;
        double height = maxy - miny;

        minx = Math.min(minx, maxx);
        maxx = Math.max(minx, maxx);
        miny = Math.min(miny, maxy);
        maxy = Math.max(miny, maxy);

        n += 1;
        double xstep = width / n;
        double ystep = height / n;
        java.util.ArrayList<Point> result = new java.util.ArrayList<Point>();
        for (int i : Python.range(n+1))
            result.add(new Point(minx + i*xstep, miny));
        for (int i : Python.range(1, n))
            result.add(new Point(maxx, miny + i*ystep));
        for (int i : Python.range(n, -1, -1))
            result.add(new Point(minx + i*xstep, maxy));
        for (int i : Python.range(n-1, 0, -1))
            result.add(new Point(minx, miny + i*ystep));
        return result;
    }


    




  //**************************************************************************
  //** calculate_bbox
  //**************************************************************************
  /**  Calculates the bbox of a list of points.
   <pre>
        >>> calculate_bbox([(-5, 20), (3, 8), (99, 0)])
        (-5, 0, 99, 20)
   </pre>
   *  @param points: list of points [(x0, y0), (x1, y2), ...]
   *  @returns: bbox of the input points.
   */
    public double[] calculate_bbox(java.util.List<Point> points){
        //points = list(points)
        // points can be INF for invalid transformations, filter out
        // INF is not portable for <2.6 so we check against a large value
        double MAX = 1e300;

        /*
        double minx = min(p[0] for p in points if p[0] <= MAX);
        double miny = min(p[1] for p in points if p[1] <= MAX);
        double maxx = max(p[0] for p in points if p[0] <= MAX);
        double maxy = max(p[1] for p in points if p[1] <= MAX);
        */

        java.util.Iterator<Point> it = points.iterator();
        Point point = it.next();

        double minx = point.x;
        double miny = point.y;
        double maxx = point.x;
        double maxy = point.y;

        while (it.hasNext()){
            point = it.next();
            minx = Math.min(minx, point.x);
            miny = Math.min(miny, point.y);
            maxx = Math.max(maxx, point.x);
            maxy = Math.max(maxy, point.y);
        }

        return new double[]{minx, miny, maxx, maxy};

    }







  //**************************************************************************
  //** make_lin_transf
  //**************************************************************************
  /** Compares two bbox and checks if they are equal, or nearly equal.
   * @param x_delta how precise the comparison should be. Should be reasonable
   * small, like a tenth of a pixle :type x_delta: bbox units
    <pre>
    >>> src_bbox = (939258.20356824622, 6887893.4928338043,
    ...             1095801.2374962866, 7044436.5267618448)
    >>> dst_bbox = (939258.20260000182, 6887893.4908000007,
    ...             1095801.2365000017, 7044436.5247000009)
    >>> bbox_equals(src_bbox, dst_bbox, 61.1, 61.1)
    True
    >>> bbox_equals(src_bbox, dst_bbox, 0.0001)
    False
    </pre>
   */
    public static boolean bbox_equals(double[] src_bbox, double[] dst_bbox, double x_delta, double y_delta){
        return (Math.abs(src_bbox[0] - dst_bbox[0]) < x_delta &&
                Math.abs(src_bbox[1] - dst_bbox[1]) < x_delta &&
                Math.abs(src_bbox[2] - dst_bbox[2]) < y_delta &&
                Math.abs(src_bbox[3] - dst_bbox[3]) < y_delta);

    }
    
    public static boolean bbox_equals(double[] src_bbox, double[] dst_bbox, double x_delta){
        return bbox_equals(src_bbox, dst_bbox, x_delta, x_delta);
    }


  //**************************************************************************
  //** make_lin_transf
  //**************************************************************************
  /** Create a transformation function that transforms linear between two
   *  cartesian coordinate systems.
   *
   *  @return function that takes src x/y and returns dest x/y coordinates
   *
    <pre>
        >>> transf = make_lin_transf((7, 50, 8, 51), (0, 0, 500, 400))
        >>> transf((7.5, 50.5))
        (250.0, 200.0)
        >>> transf((7.0, 50.0))
        (0.0, 400.0)
        >>> transf = make_lin_transf((7, 50, 8, 51), (200, 300, 700, 700))
        >>> transf((7.5, 50.5))
        (450.0, 500.0)
    </pre>
   */
    public static transf make_lin_transf(double[] src_bbox, double[] dst_bbox){

        /*
        func = lambda (x, y): (dst_bbox[0] + (x - src_bbox[0]) *
                               (dst_bbox[2]-dst_bbox[0]) / (src_bbox[2] - src_bbox[0]),
                               dst_bbox[1] + (src_bbox[3] - y) *
                               (dst_bbox[3]-dst_bbox[1]) / (src_bbox[3] - src_bbox[1]))
        return func
        */
        return new transf(src_bbox, dst_bbox);
    }

    public static class transf{
        private double[] src_bbox;
        private double[] dst_bbox;

        protected transf(double[] src_bbox, double[] dst_bbox){
            this.src_bbox = src_bbox;
            this.dst_bbox = dst_bbox;
        }
        public double[] transf(double x, double y){
            return new double[]{
                dst_bbox[0] + (x - src_bbox[0]) * (dst_bbox[2]-dst_bbox[0]) / (src_bbox[2] - src_bbox[0]),
                dst_bbox[1] + (src_bbox[3] - y) * (dst_bbox[3]-dst_bbox[1]) / (src_bbox[3] - src_bbox[1])
            };    
        }

    }


    @Override
  //**************************************************************************
  //** equals
  //**************************************************************************
  /**
        >>> SRS(4326) == SRS("EpsG:4326")
        True
        >>> SRS(4326) == SRS("4326")
        True
        >>> SRS(4326) == SRS(900913)
        False
   */
    public boolean equals(Object obj){        

        if (obj instanceof SRS){
            return this.proj.equalsIgnoreCase(((SRS) obj).proj);
          //return self.proj.srs == other.proj.srs
        }
        else if (obj instanceof String){
            return this.proj.equalsIgnoreCase( new SRS((String) obj).proj  );
        }
        else if (obj instanceof Integer){
            return this.proj.equalsIgnoreCase( new SRS((Integer) obj).proj  );
        }
        else{
            return this.proj.equalsIgnoreCase( new SRS(obj + "").proj  );
        }

        //return false;
    }


    @Override
  //**************************************************************************
  //** toString
  //**************************************************************************
  /** Used to return the srs_code used to instantiate this class. */

    public String toString(){
        return this.proj;
    }


    private static int cint(Object obj){
        return Integer.valueOf((String) obj).intValue();
    }
    
    private int cint(Double d){
        return javaxt.utils.string.toInt(d);
    }

}