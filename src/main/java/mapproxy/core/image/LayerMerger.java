package mapproxy.core.image;
import mapproxy.core.Generator;

//******************************************************************************
//**  LayerMerger Class
//******************************************************************************
/**
 *   Merge multiple layers into one image.
 *
 ******************************************************************************/

public class LayerMerger {

    private java.util.List<javaxt.io.Image> layers;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of LayerMerger.   */

    public LayerMerger(){
        layers = new java.util.LinkedList<javaxt.io.Image>();
    }


  //**************************************************************************
  //** add
  //**************************************************************************
  /** @param layer A single javaxt.io.Image or a Generator<javaxt.io.Image>
   */
    public void add(Object layer){

        java.util.Iterator<javaxt.io.Image> layers;
        try{
            layers = iter(layer);
          //[self.layers.append(layer) for layer in layers if layer is not None]
            while (layers.hasNext()){
                javaxt.io.Image img = layers.next();
                if (img!=null) this.layers.add(img);
            }            
        }
        catch(Exception e){
            if (layer!=null)
                this.layers.add((javaxt.io.Image) layer);
        }
    }



  //**************************************************************************
  //** iter
  //**************************************************************************
  /** Returns an iterator for a given Generator */

    private java.util.Iterator<javaxt.io.Image> iter(Object layer){
        return ((Generator<javaxt.io.Image>) layer).iterator();
    }





  //**************************************************************************
  //** merge
  //**************************************************************************
  /** Merge the layers. If the format is not 'png' just return the last image. 
   *  @param format The image format for the result.
   *  @param size The size for the merged output.
   *  @param bgcolor '#ffffff'
   *  @return `ImageSource`
   */
    public javaxt.io.Image merge(String format, int[] size, String bgcolor, boolean transparent){
        if (this.layers.size() == 1){
            //if (this.layers.get(0).getBufferedImage().getTransparency() == transparent)
                return this.layers.get(0);
        }

        /*
        # TODO optimizations
        #  - layer with non transparency
        #         if not format.endswith('png'): #TODO png8?
        #             return self.layers[-1]
        */

        
        if (size==null){
            size = new int[]{ this.layers.get(0).getWidth(), this.layers.get(0).getHeight() };
        }
        /*
        bgcolor = ImageColor.getrgb(bgcolor)
        if transparent:
            img = Image.new('RGBA', size, bgcolor+(0,))
        else:
            img = Image.new('RGB', size, bgcolor)
        for layer in self.layers:
            layer_img = layer.as_image()
            if layer_img.mode == 'RGBA':
                # paste w transparency mask from layer
                img.paste(layer_img, (0, 0), layer_img)
            else:
                img.paste(layer_img, (0, 0))
        return ImageSource(img, format);
        */

        javaxt.io.Image img = new javaxt.io.Image(size[0], size[1]);
        for (javaxt.io.Image layer : layers){
            img.addImage(layer, 0, 0, false);
        }
        return img;

    }


    public javaxt.io.Image merge(String format, int[] size, boolean transparent){
        return merge(format, size, "#ffffff", transparent);
    }


}