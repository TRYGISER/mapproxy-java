package mapproxy.core;

//******************************************************************************
//**  Status Class
//******************************************************************************
/**
 *   Enter class description here
 *
 ******************************************************************************/

public class Status {

    private int numTiles = 0;
    private int totalTiles = -1;
    private long lastUpdate = -1;

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of Status.   */

    public Status(){

    }

    protected void setTotal(int totalTiles){
        this.totalTiles = totalTiles;
        lastUpdate = new java.util.Date().getTime();
    }

    public long getLastUpdate(){
        return lastUpdate;
    }

    public double getPercentComplete(){
        if (totalTiles>0){
            return (double)numTiles/(double)totalTiles;
        }
        else{
            return 0;
        }
    }

    public boolean isComplete(){
        return (numTiles==totalTiles);
    }

    protected void updateTileCount(){
        numTiles++;
        lastUpdate = new java.util.Date().getTime();
    }

    public int getNumTilesProcessed(){
        return this.numTiles;
    }

    public int getTotalTiles(){
        return this.totalTiles;
    }

}
