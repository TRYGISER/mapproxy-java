package mapproxy.core;

//******************************************************************************
//**  Point Class
//******************************************************************************
/**
 *   Enter class description here
 *
 ******************************************************************************/

public class Point {


    public double x;
    public double y;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of Point.   */

    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString(){
        return x + ", " + y;
    }
    
    @Override
    public boolean equals(Object obj){
        if (obj instanceof Point){
            Point point = (Point) obj;
            return (this.x == point.x && this.y == point.y);
        }
        return false;
    }


}
