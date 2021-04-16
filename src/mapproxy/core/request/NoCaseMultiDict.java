package mapproxy.core.request;

//******************************************************************************
//**  NoCaseMultiDict Class
//******************************************************************************
/**
 *   This is a dictionary that allows case insensitive access to values.
 *
 ******************************************************************************/

public class NoCaseMultiDict {

    private java.util.HashMap<String, java.util.List<String>> parameters;


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of NoCaseMultiDict.   */

    public NoCaseMultiDict(){
        parameters = new java.util.HashMap<String, java.util.List<String>>();
    }

    public NoCaseMultiDict(NoCaseMultiDict dict){
        this.parameters = dict.parameters;
    }


  //**************************************************************************
  //** getKeys
  //**************************************************************************
  /** Used to retrieve a list of keys associated with the params. */

    public java.util.HashSet<String> getKeys(){
        java.util.HashSet<String> keys = new java.util.HashSet<String>();
        java.util.Iterator<String> it = parameters.keySet().iterator();
        while(it.hasNext()){
            keys.add(it.next());
        }
        return keys;
    }

    public boolean contains(String key){
        return parameters.keySet().contains(key.toLowerCase());
    }

  //**************************************************************************
  //** set
  //**************************************************************************
  /**  Set a "value" for the "key". If "append" is "True" the value will be
   *   added to other values for this "key". If "unpack" is True, "value" will
   *   be unpacked and each item will be added.
   */
    public void set(String key, String value, boolean append, boolean unpack){

        key = key.toLowerCase();
        if (append){
            java.util.List<String> values = parameters.get(key);
            java.util.Iterator<String> it = values.iterator();
            while(it.hasNext()){
                if (it.next().equalsIgnoreCase(value)){
                    append = false;
                    break;
                }
            }
            if (append) {
                values.add(value);
                parameters.put(key, values);
            }
            
        }
        else{
            if (value!=null){
                java.util.List<String> values = new java.util.LinkedList<String>();
                values.add(value);
                parameters.put(key, values);
            }
        }

    }

    public void set(String key, String value, boolean append){
        set(key, value, append, false);
    }

    public void set(String key, String value){
        set(key, value, false, false);
    }


  //**************************************************************************
  //** get
  //**************************************************************************

    public String get(String key, String defaultValue){ //type_func=None


        StringBuffer str = new StringBuffer();
        java.util.List<String> values = parameters.get(key.toLowerCase());
        if (values!=null){
            for (int i=0; i<values.size(); i++){
                str.append(values.get(i));
                if (i<values.size()-1) str.append(",");
            }
            return str.toString();
        }
        else{
            return defaultValue;
        }
        
    }

    public String get(String key){
        return get(key, null);
    }



}
