package mapproxy.config;
import javaxt.utils.Value;

//******************************************************************************
//**  Options Class
//******************************************************************************
/**
 *   Dictionary with attribute style access.
 *
 ******************************************************************************/

public class Options {

    private java.util.HashMap<String, Value> params;


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of Options.   */

    public Options(){
        params = new java.util.HashMap<String, Value>();
    }

    public Value get(String key){
        return params.get(key.toLowerCase());
    }

    public void set(String key, Object value){
        params.put(key.toLowerCase(), new Value(value));
    }


}
