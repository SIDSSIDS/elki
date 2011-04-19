package de.lmu.ifi.dbs.elki.algorithm;

import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.QueryUtil;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancevalue.Distance;
import de.lmu.ifi.dbs.elki.result.Result;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.ObjectParameter;

/**
 * Provides an abstract algorithm already setting the distance function.
 * 
 * @author Arthur Zimek
 * 
 * @apiviz.landmark
 * @apiviz.has DistanceFunction
 * @apiviz.excludeSubtypes
 * 
 * @param <O> the type of objects handled by this Algorithm
 * @param <D> the type of Distance used by this Algorithm
 * @param <R> the type of result to retrieve from this Algorithm
 */
public abstract class AbstractDistanceBasedAlgorithm<O, D extends Distance<D>, R extends Result> extends AbstractAlgorithm<O, R> {
  /**
   * OptionID for {@link #DISTANCE_FUNCTION_ID}
   */
  public static final OptionID DISTANCE_FUNCTION_ID = OptionID.getOrCreateOptionID("algorithm.distancefunction", "Distance function to determine the distance between database objects.");

  /**
   * Holds the instance of the distance function specified by
   * {@link #DISTANCE_FUNCTION_ID}.
   */
  private DistanceFunction<? super O, D> distanceFunction;

  /**
   * Constructor.
   * 
   * @param distanceFunction Distance function
   */
  protected AbstractDistanceBasedAlgorithm(DistanceFunction<? super O, D> distanceFunction) {
    super();
    this.distanceFunction = distanceFunction;
  }

  /**
   * Returns the distanceFunction.
   * 
   * @return the distanceFunction
   */
  public DistanceFunction<? super O, D> getDistanceFunction() {
    return distanceFunction;
  }

  /**
   * Get a distance query for the used distance function
   * 
   * @param database Database to use
   * @return distance query
   */
  public DistanceQuery<O, D> getDistanceQuery(Database database) {
    return QueryUtil.getDistanceQuery(database, distanceFunction);
  }

  /**
   * Parameterization helper class.
   * 
   * @author Erich Schubert
   * 
   * @apiviz.exclude
   */
  public abstract static class Parameterizer<O, D extends Distance<D>> extends AbstractParameterizer {
    protected DistanceFunction<O, D> distanceFunction;

    @Override
    protected void makeOptions(Parameterization config) {
      super.makeOptions(config);
      ObjectParameter<DistanceFunction<O, D>> distanceFunctionP = makeParameterDistanceFunction(EuclideanDistanceFunction.class, DistanceFunction.class);
      if(config.grab(distanceFunctionP)) {
        distanceFunction = distanceFunctionP.instantiateClass(config);
      }
    }
  }
}