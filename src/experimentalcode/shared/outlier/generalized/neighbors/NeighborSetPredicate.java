package experimentalcode.shared.outlier.generalized.neighbors;

import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.Parameterizable;

/**
 * Predicate to obtain the neighbors of a reference object as set.
 * 
 * @author Erich Schubert
 */
// TODO: differentiate similarly to KNNQuery? Make this a super-interface of KNNQuery?
public interface NeighborSetPredicate {
  /**
   * Get the neighbors of a reference object for DBSCAN.
   * 
   * @param reference Reference object
   * @return Neighborhood
   */
  public DBIDs getNeighborDBIDs(DBID reference);

  /**
   * Factory interface to produce instances.
   * 
   * @author Erich Schubert
   *
   * @param <O> Object type
   */
  public static interface Factory<O> extends Parameterizable {
    /**
     * Instantiation method.
     * 
     * @param rep Database to instantiate for.
     * 
     * @return instance
     */
    public NeighborSetPredicate instantiate(Relation<? extends O> database);
  }
}