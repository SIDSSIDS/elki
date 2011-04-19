package de.lmu.ifi.dbs.elki.distance.distancefunction;

import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.distance.distancevalue.Distance;

/**
 * Distance function to proxy computations to another distance (that probably
 * was run before).
 * 
 * @author Erich Schubert
 * 
 * @param <O> object type
 * @param <D> distance type
 */
public class ProxyDistanceFunction<O, D extends Distance<D>> extends AbstractDBIDDistanceFunction<D> {
  /**
   * Distance query
   */
  DistanceQuery<O, D> inner;

  /**
   * Constructor
   * 
   * @param inner Inner distance
   */
  public ProxyDistanceFunction(DistanceQuery<O, D> inner) {
    super();
    this.inner = inner;
  }
  
  /**
   * Static method version.
   * 
   * @param <O> Object type
   * @param <D> Distance type
   * @param inner Inner distance query
   * @return Proxy object
   */
  public static <O, D extends Distance<D>> ProxyDistanceFunction<O, D> proxy(DistanceQuery<O, D> inner) {
    return new ProxyDistanceFunction<O, D>(inner);
  }

  @Override
  public D distance(DBID o1, DBID o2) {
    return inner.distance(o1, o2);
  }

  @Override
  public D getDistanceFactory() {
    return inner.getDistanceFactory();
  }

  /**
   * Get the inner query
   * 
   * @return query
   */
  public DistanceQuery<O, D> getDistanceQuery() {
    return inner;
  }

  /**
   * @param inner the inner distance query to set
   */
  public void setDistanceQuery(DistanceQuery<O, D> inner) {
    this.inner = inner;
  }

  /**
   * Helper function, to resolve any wrapped Proxy Distances
   * 
   * @param <V> Object type
   * @param <D> Distance type
   * @param dfun Distance function to unwrap.
   * @return unwrapped distance function
   */
  @SuppressWarnings("unchecked")
  public static <V, T extends V, D extends Distance<D>> DistanceFunction<? super V, D> unwrapDistance(DistanceFunction<V, D> dfun) {
    if(ProxyDistanceFunction.class.isInstance(dfun)) {
      return unwrapDistance(((ProxyDistanceFunction<V, D>) dfun).getDistanceQuery().getDistanceFunction());
    }
    return dfun;
  }
}