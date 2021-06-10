package mapproxy.core.image;
import mapproxy.config.Config;
import mapproxy.core.Generator;
import mapproxy.core.Python;
import mapproxy.core.SRS;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.processing.Operations;
import org.geotools.geometry.Envelope2D;

//******************************************************************************
//**  ImageTransformer Class
//******************************************************************************
/**
 *   Transform images between different bbox and spatial reference systems.
 *
 *   The transformation doesn't make a real transformation for each pixel,
 *   but a mesh transformation (see `PIL Image.transform`_).
 *   It will divide the target image into rectangles (a mesh). The
 *   source coordinates for each rectangle vertex will be calculated.
 *   The quadrilateral will then be transformed with the source coordinates
 *   into the destination quad (affine).
 * 
 *   This method will perform good transformation results if the number of
 *   quads is high enough (even transformations with strong distortions).
 *   Tests on images up to 1500x1500 have shown that meshes beyond 8x8
 *   will not improve the results.
 *
 *   _PIL Image.transform:
 *   http://www.pythonware.com/library/pil/handbook/image.htm#Image.transform
 * 
 <pre>


                    src quad                   dst quad
                    .----.   <- coord-           .----.
                   /    /       transformation   |    |
                  /    /                         |    |
                 .----.   img-transformation ->  .----.----
                           |                     |    |
            ---------------.
            large src image                   large dst image
 </pre>
 *
 ******************************************************************************/

public class ImageTransformer {

    private SRS src_srs;
    private SRS dst_srs;
    private String resampling;
    private int mesh_div;
    private double[] dst_bbox;
    private double[] dst_size;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of ImageTransformer.
   *  @param src_srs the srs of the source image
   *  @param dst_srs the srs of the target image
   *  @param resampling the resampling method used for transformation:
   *  nearest|bilinear|bicubic
   *  @param mesh_div the number of quads in each direction to use for
   *  transformation (totals to ``mesh_div**2`` quads)
   */
    public ImageTransformer(SRS src_srs, SRS dst_srs, String resampling, int mesh_div){
        this.src_srs = src_srs;
        this.dst_srs = dst_srs;

      //Hack: Need to figure out why src_srs is null...
        if (this.src_srs==null) this.src_srs = this.dst_srs;

        if (resampling==null)
            resampling = Config.base_config().get("image.resampling_method").toString();
        this.resampling = resampling;
        this.mesh_div = mesh_div;
        //this.dst_bbox = this.dst_size = null;
    }

    public ImageTransformer(SRS src_srs, SRS dst_srs){
        this(src_srs, dst_srs, null, 8);
    }


  //**************************************************************************
  //** transform
  //**************************************************************************
  /**  Transforms the `src_img` between the source and destination SRS of this
   *   "ImageTransformer" instance.
   * 
   *   When the ``src_srs`` and ``dst_srs`` are equal the image will be cropped
   *   and not transformed. If the `src_bbox` and `dst_bbox` are equal,
   *   the `src_img` itself will be returned.
   *
   *  @param src_img the source image for the transformation
   *  @param src_bbox the bbox of the src_img
   *  @param dst_size the size of the result image (in pizel). Example: ``(int(width), int(height))``
   *  @param dst_bbox the bbox of the result image
   *  @return the transformed image: 'ImageSource'
   */
    public javaxt.io.Image transform(javaxt.io.Image src_img, double[] src_bbox, int[] dst_size, double[] dst_bbox){
        if (this._no_transformation_needed(new int[]{src_img.getWidth(), src_img.getHeight()}, src_bbox, dst_size, dst_bbox)){
            //System.out.println("_no_transformation_needed");
            return src_img;
        }
        else if (equals(this.src_srs, this.dst_srs)){
            //System.out.println("_transform_simple");                       
            return this._transform_simple(src_img, src_bbox, dst_size, dst_bbox);
        }
        else
            return this._transform(src_img, src_bbox, dst_size, dst_bbox);
    }


  //**************************************************************************
  //** _transform_simple
  //**************************************************************************
  /** Do a simple crop/extend transformation. */
    private javaxt.io.Image _transform_simple(javaxt.io.Image src_img, 
                            double[] src_bbox, int[] dst_size, double[] dst_bbox){

        double[] src_quad = new double[]{0, 0, src_img.getWidth(), src_img.getHeight()};
        SRS.transf to_src_px =SRS.make_lin_transf(src_bbox, src_quad);

      //minx, miny = to_src_px((dst_bbox[0], dst_bbox[3]));
        double[] min = to_src_px.transf(dst_bbox[0], dst_bbox[3]);
        double minx = min[0];
        double miny = min[1];

      //maxx, maxy = to_src_px((dst_bbox[2], dst_bbox[1]));
        double[] max = to_src_px.transf(dst_bbox[2], dst_bbox[1]);
        double maxx = max[0];
        double maxy = max[1];

        double src_res = (src_bbox[0]-src_bbox[2])/src_img.getWidth();
        double dst_res = (dst_bbox[0]-dst_bbox[2])/dst_size[0];

        double tenth_px_res = Math.abs(dst_res/(dst_size[0]*10));
        javaxt.io.Image img = new javaxt.io.Image(src_img.getBufferedImage());
        if (Math.abs(src_res-dst_res) < tenth_px_res){
            img.crop(cint(minx), cint(miny), dst_size[0], dst_size[1]);
            //src_img.crop(cint(minx), cint(miny), cint(minx)+dst_size[0], cint(miny)+dst_size[1]);
            return img; //src_img;
        }
        else{
            img.crop(cint(minx), cint(miny), cint(maxx)-cint(minx), cint(maxy)-cint(miny));
            img.resize(dst_size[0], dst_size[1]);
            //src_img.crop(cint(minx), cint(miny), cint(maxx), cint(maxy));
            //src_img.resize(dst_size[0], dst_size[1]);
            return img; //src_img;
        }
        //ImageSource(result, size=dst_size, transparent=src_img.transparent)
    }


  //**************************************************************************
  //** _transform
  //**************************************************************************
  /** Do a 'real' transformation with a transformed mesh (see above). */
    private javaxt.io.Image _transform(javaxt.io.Image src_img, double[] src_bbox, int[] dst_size, double[] dst_bbox){

      //Use GeoTools to reproject src_img to the output projection.
        try{

          //Compute geodetic w/h in degrees (used to specify the Envelope2D)
            double x1 = x(src_bbox[0]);
            double x2 = x(src_bbox[2]);
            double w = 0;
            if (x1>x2) w = x1-x2;
            else w = x2-x1;
            //System.out.println(x1 + " to " + x2 + " = " + (w));

            double y1 = y(src_bbox[1]);
            double y2 = y(src_bbox[3]);
            double h = 0;
            if (y1>y2) h = y1-y2;
            else h = y2-y1;
            //System.out.println(y1 + " to " + y2 + " = " + (h));


            System.out.println(src_bbox[0] + ", " + src_bbox[1] + ", " + src_bbox[2] + ", " + src_bbox[3]);
            System.out.println(dst_bbox[0] + ", " + dst_bbox[1] + ", " + dst_bbox[2] + ", " + dst_bbox[3]);
            
            Envelope2D envelope =
               new Envelope2D(this.src_srs.crs, src_bbox[0], src_bbox[1], w, h);

            GridCoverage2D gc2d =
               new GridCoverageFactory().create("BMImage", src_img.getBufferedImage(), envelope);

            GridCoverage2D gc2dProj =
                (GridCoverage2D)Operations.DEFAULT.resample(gc2d, this.dst_srs.crs);

          //TODO: Crop Output?
            /*             
            final AbstractProcessor processor = new DefaultProcessor(null);
            final ParameterValueGroup param = processor.getOperation("CoverageCrop").getParameters();
            
            final GeneralEnvelope crop = new GeneralEnvelope( ... ); //<--Define new extents
            param.parameter("Source").setValue( gc2dProj );
            param.parameter("Envelope").setValue( crop );

            gc2dProj = (GridCoverage2D) processor.doOperation(param);             
            */

            src_img = new javaxt.io.Image(gc2dProj.getRenderedImage());
            src_img.resize(dst_size[0], dst_size[1], false);

        }
        catch (Exception e){
            e.printStackTrace();
        }


        /*
      //Start port of original python code...
        src_bbox = this.src_srs.align_bbox(src_bbox);
        dst_bbox = this.dst_srs.align_bbox(dst_bbox);
        int[] src_size = new int[]{src_img.getWidth(), src_img.getHeight()}; //src_img.size
        double[] src_quad = new double[]{0, 0, src_size[0], src_size[1]};
        double[] dst_quad = new double[]{0, 0, dst_size[0], dst_size[1]};
        
        SRS.transf to_src_px = SRS.make_lin_transf(src_bbox, src_quad);
        SRS.transf to_dst_w = SRS.make_lin_transf(dst_quad, dst_bbox);

      //This segment still needs to be ported to java
        meshes = [];        
        def dst_quad_to_src(quad):
            src_quad = []
            for dst_px in [(quad[0], quad[1]), (quad[0], quad[3]),
                           (quad[2], quad[3]), (quad[2], quad[1])]:
                dst_w = to_dst_w(dst_px)
                src_w = self.dst_srs.transform_to(self.src_srs, dst_w)
                src_px = to_src_px(src_w)
                src_quad.extend(src_px)
            return quad, src_quad
        */

        /*
        int mesh_div = this.mesh_div;
        while (mesh_div > 1 && ((dst_size[0] / mesh_div) < 10 || (dst_size[1] / mesh_div) < 10))
            mesh_div -= 1;
        for (int[] quad : griddify(dst_quad, mesh_div))
            meshes.append(dst_quad_to_src(quad));
        result = src_img.as_image().transform(dst_size, Image.MESH, meshes,
                                              image_filter[self.resampling])
        */
        return src_img;
    }

    /*
    private dst_quad_to_src(int[] quad){
        src_quad = []
        for (dst_px : [(quad[0], quad[1]), (quad[0], quad[3]),
                       (quad[2], quad[3]), (quad[2], quad[1])])
        {
            dst_w = to_dst_w(dst_px);
            src_w = this.dst_srs.transform_to(this.src_srs, dst_w);
            src_px = to_src_px(src_w);
            src_quad.extend(src_px);
        }
        return quad, src_quad;
    }
    */
    
  //**************************************************************************
  //** X
  //**************************************************************************
  /**  Used to convert longitude to pixel coordinates */

    private double x(double pt){
        pt += 180;
        return pt;
    }


  //**************************************************************************
  //** Y
  //**************************************************************************
  /**  Used to convert latitude to pixel coordinates */

    private double y(double pt){
        pt = -pt;
        if (pt<=0) pt = 90 + -pt;
        else pt = 90 - pt;

        pt = 180-pt;
        return pt;
    }




  //**************************************************************************
  //** _no_transformation_needed
  //**************************************************************************
  /**
   <pre>
        >>> src_bbox = (-2504688.5428486541, 1252344.271424327,
        ...             -1252344.271424327, 2504688.5428486541)
        >>> dst_bbox = (-2504688.5431999983, 1252344.2704,
        ...             -1252344.2719999983, 2504688.5416000001)
        >>> from mapproxy.core.srs import SRS
        >>> t = ImageTransformer(SRS(900913), SRS(900913))
        >>> t._no_transformation_needed((256, 256), src_bbox, (256, 256), dst_bbox)
        True
   </pre>
   */
    private boolean _no_transformation_needed(int[] src_size, double[] src_bbox, int[] dst_size, double[] dst_bbox){

        double xres = (dst_bbox[2]-dst_bbox[0])/dst_size[0];
        double yres = (dst_bbox[3]-dst_bbox[1])/dst_size[1];


        return (equals(src_size, dst_size) &&
                equals(this.src_srs, this.dst_srs) &&
                SRS.bbox_equals(src_bbox, dst_bbox, xres/10, yres/10));
         /*
        xres = (dst_bbox[2]-dst_bbox[0])/dst_size[0]
        yres = (dst_bbox[3]-dst_bbox[1])/dst_size[1]
        return (src_size == dst_size and
                self.src_srs == self.dst_srs and
                bbox_equals(src_bbox, dst_bbox, xres/10, yres/10))
         */
    }

    private boolean equals(int[] a, int[] b){
        if (a.length!=b.length) return false;
        for (int i=0; i<a.length; i++){
            if (a[i]!=b[i]) return false;
        }
        return true;
    }


    private boolean equals(SRS src_srs, SRS dst_srs){
        boolean srsEquals = false;
        if (src_srs==null || dst_srs==null){
            if (src_srs==null && dst_srs==null) srsEquals = true;
        }
        else{
            srsEquals = src_srs.equals(dst_srs);
        }
        return srsEquals;
    }



  //**************************************************************************
  //** griddify
  //**************************************************************************
  /** Divides a box (`quad`) into multiple boxes (``steps x steps``).
    <pre>
        >>> list(griddify((0, 0, 500, 500), 2))
        [(0, 0, 250, 250), (250, 0, 500, 250), (0, 250, 250, 500), (250, 250, 500, 500)]
    </pre>
   */
    public Generator<int[]> griddify(final double[] quad, final int steps){

        Generator<int[]> iterator = new Generator<int[]>() {

            @Override
            public void run() {
                double w = quad[2]-quad[0];
                double h = quad[3]-quad[1];
                double x_step = w / steps;
                double y_step = h / steps;

                double y = quad[1];
                for (int a : Python.range(steps)){
                    double x = quad[0];
                    for (int b : Python.range(steps)){
                        yield (new int[]{cint(x), cint(y), cint(x+x_step), cint(y+y_step)});
                        x += x_step;
                    }
                    y += y_step;
                }
            }
        };

        return iterator;
    }

    private int cint(double d){
        return javaxt.utils.string.cint(d);
    }
}