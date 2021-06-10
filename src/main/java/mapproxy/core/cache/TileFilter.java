package mapproxy.core.cache;

//******************************************************************************
//**  TileFilter Class
//******************************************************************************
/**
    Tile filters can manipulate tiles before they are written to the cache.

    A TileFilter needs a ``create_filter`` method that takes ``layer_conf`` and a
    ``priority`` attribute.

    The priority controls in wich order the filters are applied. A higher value
    means that the filter is applied first.

    The ``create_filter`` method should return the filter function or ``None`` if
    the filter does not apply to the layer.
    Each filter should use some layer variable(s) to configure if the filter should
    apply to a specific layer. The ``create_filter`` should check for this
    variable(s) in the given ``layer_conf``.

    The filter function gets a `mapproxy.core.cache._Tile` and must return the
    same (modified) tile.
 *
 ******************************************************************************/

public class TileFilter {


  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /** Creates a new instance of TileFilter.   */

    public TileFilter(_Tile tile){

    }

}
