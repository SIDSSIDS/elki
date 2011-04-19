package de.lmu.ifi.dbs.elki.database.query.knn;

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.dbs.elki.database.ids.ArrayDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.query.AbstractDataBasedQuery;
import de.lmu.ifi.dbs.elki.database.query.DistanceResultPair;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.distance.distancevalue.Distance;
import de.lmu.ifi.dbs.elki.index.preprocessed.knn.AbstractMaterializeKNNPreprocessor;
import de.lmu.ifi.dbs.elki.index.preprocessed.knn.MaterializeKNNPreprocessor;
import de.lmu.ifi.dbs.elki.logging.LoggingUtil;
import de.lmu.ifi.dbs.elki.utilities.exceptions.AbortException;

/**
 * Instance for a particular database, invoking the preprocessor.
 * 
 * @author Erich Schubert
 */
public class PreprocessorKNNQuery<O, D extends Distance<D>> extends AbstractDataBasedQuery<O> implements KNNQuery<O, D> {
  /**
   * The last preprocessor result
   */
  final private MaterializeKNNPreprocessor<O, D> preprocessor;

  /**
   * Warn only once.
   */
  private boolean warned = false;

  /**
   * Constructor.
   * 
   * @param database Database to query
   * @param preprocessor Preprocessor instance to use
   */
  public PreprocessorKNNQuery(Relation<O> database, MaterializeKNNPreprocessor<O, D> preprocessor) {
    super(database);
    this.preprocessor = preprocessor;
  }

  /**
   * Constructor.
   * 
   * @param database Database to query
   * @param preprocessor Preprocessor to use
   */
  public PreprocessorKNNQuery(Relation<O> database, MaterializeKNNPreprocessor.Factory<O, D> preprocessor) {
    this(database, preprocessor.instantiate(database));
  }

  @Override
  public List<DistanceResultPair<D>> getKNNForDBID(DBID id, int k) {
    if(!warned && k > preprocessor.getK()) {
      LoggingUtil.warning("Requested more neighbors than preprocessed!");
    }
    if(!warned && k < preprocessor.getK()) {
      LoggingUtil.warning("FIXME: we're returning too many neighbors!");
    }
    return preprocessor.get(id);
  }

  @Override
  public List<List<DistanceResultPair<D>>> getKNNForBulkDBIDs(ArrayDBIDs ids, int k) {
    if(!warned && k > preprocessor.getK()) {
      LoggingUtil.warning("Requested more neighbors than preprocessed!");
    }
    if(!warned && k < preprocessor.getK()) {
      LoggingUtil.warning("FIXME: we're returning too many neighbors!");
    }
    List<List<DistanceResultPair<D>>> result = new ArrayList<List<DistanceResultPair<D>>>(ids.size());
    for(DBID id : ids) {
      result.add(preprocessor.get(id));
    }
    return result;
  }

  @SuppressWarnings("unused")
  @Override
  public List<DistanceResultPair<D>> getKNNForObject(O obj, int k) {
    throw new AbortException("Preprocessor KNN query only supports ID queries.");
  }

  /**
   * Get the preprocessor instance.
   * 
   * @return preprocessor instance
   */
  public AbstractMaterializeKNNPreprocessor<O, D> getPreprocessor() {
    return preprocessor;
  }

  @Override
  public D getDistanceFactory() {
    return preprocessor.getDistanceFactory();
  }

  @Override
  public DistanceQuery<O, D> getDistanceQuery() {
    // TODO: remove? throw an exception?
    return preprocessor.getDistanceQuery();
  }
}