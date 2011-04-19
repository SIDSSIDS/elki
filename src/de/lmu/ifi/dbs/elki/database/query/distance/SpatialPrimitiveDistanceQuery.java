package de.lmu.ifi.dbs.elki.database.query.distance;

import de.lmu.ifi.dbs.elki.data.FeatureVector;
import de.lmu.ifi.dbs.elki.data.HyperBoundingBox;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.SpatialPrimitiveDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancevalue.Distance;

/**
 * Distance query for spatial distance functions
 * @author Erich Schubert
 *
 * @apiviz.uses de.lmu.ifi.dbs.elki.distance.distancefunction.SpatialPrimitiveDistanceFunction
 * 
 * @param <V> Vector type to use
 * @param <D> Distance result type
 */
public class SpatialPrimitiveDistanceQuery<V extends FeatureVector<?, ?>, D extends Distance<D>> extends PrimitiveDistanceQuery<V, D> implements SpatialDistanceQuery<V, D> {
  /**
   * The distance function we use.
   */
  final protected SpatialPrimitiveDistanceFunction<? super V, D> distanceFunction;
  
  /**
   * @param rep Representation to use
   * @param distanceFunction Distance function to use
   */
  public SpatialPrimitiveDistanceQuery(Relation<? extends V> rep, SpatialPrimitiveDistanceFunction<? super V, D> distanceFunction) {
    super(rep, distanceFunction);
    this.distanceFunction = distanceFunction;
  }

  @Override
  public D centerDistance(HyperBoundingBox mbr1, HyperBoundingBox mbr2) {
    return distanceFunction.centerDistance(mbr1, mbr2);
  }

  @Override
  public D distance(HyperBoundingBox mbr1, HyperBoundingBox mbr2) {
    return distanceFunction.distance(mbr1, mbr2);
  }

  @Override
  public D minDist(HyperBoundingBox mbr, V v) {
    return distanceFunction.minDist(mbr, v);
  }

  @Override
  public D minDist(HyperBoundingBox mbr, DBID id) {
    return distanceFunction.minDist(mbr, rep.get(id));
  }

  @Override
  public SpatialPrimitiveDistanceFunction<? super V, D> getDistanceFunction() {
    return distanceFunction;
  }
}