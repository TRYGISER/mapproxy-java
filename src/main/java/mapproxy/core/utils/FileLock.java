package mapproxy.core.utils;

//******************************************************************************
//**  FileLock Class
//******************************************************************************
/**
 *   Enter class description here
 *
 ******************************************************************************/

public class FileLock {

    public String lock_file;
    public double timeout = 60.0;
    public double step = 0.01;
    public boolean _locked = false;
    public int max_lock_time;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of FileLock. */

    public FileLock(String lock_file, double timeout, double step) {
        this.lock_file = lock_file;
        this.timeout = timeout;
        this.step = step;
        this._locked = false;
        this.max_lock_time = 300;
    }

    public FileLock(String lock_file) {
        this(lock_file, 60.0, 0.01);
    }

}