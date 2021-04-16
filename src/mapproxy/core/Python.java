package mapproxy.core;

//******************************************************************************
//**  Python Class
//******************************************************************************
/**
 *   Miscellaneous python functions not found in standard Java
 *
 ******************************************************************************/

public class Python {


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Private constructor - all operations are invoked via static methods. */

    private Python(){

    }

    
  //**************************************************************************
  //** doubleDiv
  //**************************************************************************
  /** Used to execute a double division. In Python this is normally executed
   *  using a double division sign (e.g. 8.0//3). The double division sign is
   *  also known as the "floor" division. It takes the result of the division,
   *  and rounds it down to the nearest integer. If one divides two integers
   *  together, the result is an integer. If one of the numbers (or both) is a
   *  decimal number, the result is also a decimal number (rounded down to the
   *  nearest integer).
   */
    public static int doubleDiv(Integer a, Integer b){
        return (int) Math.floor(a.doubleValue()/b.doubleValue());
    }
    
    public static int doubleDiv(Double a, Double b){
        return (int) Math.floor(a/b);
    }

  //**************************************************************************
  //** range
  //**************************************************************************
  /** This is a versatile function to create lists containing arithmetic
   *  progressions. It is most often used in for loops. The arguments must be
   *  plain integers. If the step argument is omitted, it defaults to 1. If the
   *  start argument is omitted, it defaults to 0. The full form returns a list
   *  of plain integers [start,  start + step, start + 2 *  step, ...]. If step
   *  is positive, the last element is the largest start + i * step  less than
   *  stop; if step is negative, the last element is the smallest start + i *
   *  step greater than stop. step  must not be zero (or else ValueError is
   *  raised). Example:
   <pre>
        >>> range(10)
        [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
        >>> range(1, 11)
        [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
        >>> range(0, 30, 5)
        [0, 5, 10, 15, 20, 25]
        >>> range(0, 10, 3)
        [0, 3, 6, 9]
        >>> range(0, -10, -1)
        [0, -1, -2, -3, -4, -5, -6, -7, -8, -9]
        >>> range(0)
        []
        >>> range(1, 0)
        []
   </pre>
   */
    public static int[] range(int start, int stop, int step){
        java.util.List<Integer> list = new java.util.LinkedList<Integer>();

        if (start>stop){
            if (step<0){
                for (int i=start; i>stop; i=i+step){
                    list.add(i);
                }
            }
        }
        else{
            for (int i=start; i<stop; i=i+step){
                list.add(i);
            }
        }

        int[] arr = new int[list.size()];
        for (int i=0; i<arr.length; i++){
            arr[i] = list.get(i);
        }
        return arr;
    }

    public static int[] range(int start, int stop){
        return range(start, stop, 1);
    }

    public static int[] range(int stop){
        return range(0, stop, 1);
    }


    public static String cstr(double[] arr){
        StringBuffer str = new StringBuffer();
        str.append("[");
        for (int i=0; i<arr.length; i++){
            str.append(arr[i]);
            if (i<arr.length-1) str.append(", ");
        }
        str.append("]");
        return str.toString();
    }

    public static String cstr(int[] arr){
        StringBuffer str = new StringBuffer();
        str.append("[");
        for (int i=0; i<arr.length; i++){
            str.append(arr[i]);
            if (i<arr.length-1) str.append(", ");
        }
        str.append("]");
        return str.toString();
    }
}
