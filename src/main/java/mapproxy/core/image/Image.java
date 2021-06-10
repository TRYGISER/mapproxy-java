package mapproxy.core.image;

//******************************************************************************
//**  Image Class
//******************************************************************************
/**
 *   Image and tile manipulation (transforming, merging, etc).
 *
 ******************************************************************************/

public class Image {


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of Image.   */

    private Image(){

    }

    
  //**************************************************************************
  //** merge_images
  //**************************************************************************
  /** Merge multiple images into one.
   *
   * @param images List of `ImageSource`, bottom image first
   * @param format The format of the output `ImageSource`
   * @param size Size of the merged image. If null the size of the first image
   *  is used
    :rtype: `ImageSource`
   */
    public static javaxt.io.Image merge_images(java.util.List<javaxt.io.Image> images, String format, int[] size, boolean transparent){
        LayerMerger merger = new LayerMerger();
        merger.add(images);
        return merger.merge(format, size, transparent);
    }

    public static javaxt.io.Image merge_images(java.util.List<javaxt.io.Image> images, boolean transparent){
        return merge_images(images, "png", null, transparent);
    }

}