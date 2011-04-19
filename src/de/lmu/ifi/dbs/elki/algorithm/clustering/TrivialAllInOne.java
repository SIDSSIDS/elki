package de.lmu.ifi.dbs.elki.algorithm.clustering;

import de.lmu.ifi.dbs.elki.algorithm.AbstractAlgorithm;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.model.ClusterModel;
import de.lmu.ifi.dbs.elki.data.model.Model;
import de.lmu.ifi.dbs.elki.data.type.SimpleTypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.utilities.documentation.Description;
import de.lmu.ifi.dbs.elki.utilities.documentation.Title;

/**
 * Trivial pseudo-clustering that just considers all points to be one big
 * cluster.
 * 
 * Useful for evaluation and testing.
 * 
 * @author Erich Schubert
 */
@Title("Trivial all-in-one clustering")
@Description("Returns a 'tivial' clustering which just considers all points to be one big cluster.")
public class TrivialAllInOne extends AbstractAlgorithm<Object, Clustering<Model>> implements ClusteringAlgorithm<Clustering<Model>> {
  /**
   * The logger for this class.
   */
  private static final Logging logger = Logging.getLogger(TrivialAllInOne.class);

  /**
   * Constructor, adhering to
   * {@link de.lmu.ifi.dbs.elki.utilities.optionhandling.Parameterizable}
   */
  public TrivialAllInOne() {
    super();
  }

  /**
   * Run the actual clustering algorithm.
   * 
   * @param database The database to process
   */
  @Override
  protected Clustering<Model> runInTime(Database database) throws IllegalStateException {
    Clustering<Model> result = new Clustering<Model>("All-in-one trivial Clustering", "allinone-clustering");
    Cluster<Model> c = new Cluster<Model>(database.getDBIDs(), ClusterModel.CLUSTER);
    result.addCluster(c);
    return result;
  }

  @Override
  public SimpleTypeInformation<Object> getInputTypeRestriction() {
    return TypeUtil.ANY;
  }

  @Override
  protected Logging getLogger() {
    return logger;
  }
}