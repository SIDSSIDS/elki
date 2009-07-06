package de.lmu.ifi.dbs.elki.preprocessing;

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.dbs.elki.data.RealVector;
import de.lmu.ifi.dbs.elki.database.AssociationID;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.DistanceResultPair;
import de.lmu.ifi.dbs.elki.distance.Distance;
import de.lmu.ifi.dbs.elki.distance.DoubleDistance;
import de.lmu.ifi.dbs.elki.distance.similarityfunction.kernel.ArbitraryKernelFunctionWrapper;
import de.lmu.ifi.dbs.elki.distance.similarityfunction.kernel.LinearKernelFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.pca.CompositeEigenPairFilter;
import de.lmu.ifi.dbs.elki.math.linearalgebra.pca.LimitEigenPairFilter;
import de.lmu.ifi.dbs.elki.math.linearalgebra.pca.NormalizingEigenPairFilter;
import de.lmu.ifi.dbs.elki.math.linearalgebra.pca.PCAFilteredResult;
import de.lmu.ifi.dbs.elki.math.linearalgebra.pca.PCAFilteredRunner;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.DoubleParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.Flag;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.ListParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.ParameterException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.WrongParameterValueException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.GlobalParameterConstraint;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.GreaterEqualConstraint;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.LessEqualConstraint;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.ParameterConstraint;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.ParameterFlagGlobalConstraint;

/**
 * Preprocessor for kernel 4C local dimensionality, neighbor objects and strong
 * eigenvector matrix assignment to objects of a certain database.
 * 
 * @author Simon Paradies
 * @param <D> Distance type
 * @param <V> Vector type
 */
public class KernelFourCPreprocessor<D extends Distance<D>, V extends RealVector<V, ?>> extends ProjectedDBSCANPreprocessor<D, V> {

  /**
   * The default kernel function class name.
   */
  public static final String DEFAULT_KERNEL_FUNCTION_CLASS = LinearKernelFunction.class.getName();

  /**
   * Parameter for preprocessor.
   */
  public static final String KERNEL_FUNCTION_CLASS_P = "kernel";

  /**
   * Description for parameter preprocessor.
   */
  public static final String KERNEL_FUNCTION_CLASS_D = "the kernel function which is used to compute the epsilon neighborhood." + "Default: " + DEFAULT_KERNEL_FUNCTION_CLASS;

  /**
   * Flag for marking parameter delta as an absolute value.
   */
  private final Flag ABSOLUTE_PARAM = new Flag(LimitEigenPairFilter.EIGENPAIR_FILTER_ABSOLUTE);

  /**
   * Option string for parameter delta.
   */
  private final DoubleParameter DELTA_PARAM = new DoubleParameter(LimitEigenPairFilter.EIGENPAIR_FILTER_DELTA, new GreaterEqualConstraint(0), DEFAULT_DELTA);

  /**
   * The default value for delta.
   */
  public static final double DEFAULT_DELTA = 0.1;

  /**
   * Threshold for strong eigenpairs, can be absolute or relative.
   */
  private double delta;

  /**
   * Indicates whether delta is an absolute or a relative value.
   */
  private boolean absolute;

  /**
   * PCA utility object
   */
  private PCAFilteredRunner<V, DoubleDistance> pca = new PCAFilteredRunner<V, DoubleDistance>();

  /**
   * Default Constructor
   */
  public KernelFourCPreprocessor() {
    super();
    addOption(DELTA_PARAM);
    addOption(ABSOLUTE_PARAM);

    // parameter constraints are only valid if delta is a relative value! Thus
    // they are
    // dependent on the absolute flag, that is they are global constraints!
    final ArrayList<ParameterConstraint<Number>> deltaCons = new ArrayList<ParameterConstraint<Number>>();
    // TODO: I moved the constraint up to the parameter itself, since it applies
    // in both cases, right? -- erich
    // deltaCons.add(new GreaterEqualConstraint(0));
    deltaCons.add(new LessEqualConstraint(1));

    GlobalParameterConstraint gpc = new ParameterFlagGlobalConstraint<Number, Double>(DELTA_PARAM, deltaCons, ABSOLUTE_PARAM, false);
    optionHandler.setGlobalParameterConstraint(gpc);
  }

  /**
   * This method implements the type of variance analysis to be computed for a
   * given point.
   * <p/>
   * Example1: for 4C, this method should implement a PCA for the given point.
   * Example2: for PreDeCon, this method should implement a simple axis-parallel
   * variance analysis.
   * 
   * @param id the given point
   * @param neighbors the neighbors as query results of the given point
   * @param database the database for which the preprocessing is performed
   */
  @Override
  protected void runVarianceAnalysis(final Integer id, final List<DistanceResultPair<D>> neighbors, final Database<V> database) {
    final List<Integer> ids = new ArrayList<Integer>(neighbors.size());
    for(final DistanceResultPair<D> neighbor : neighbors) {
      ids.add(neighbor.getSecond());
    }
    PCAFilteredResult pcares = pca.processIds(ids, database);

    if(logger.isDebugging()) {
      final StringBuffer msg = new StringBuffer();
      msg.append(id).append(" ").append(database.getAssociation(AssociationID.LABEL, id));
      msg.append("\ncorrDim ").append(pcares.getCorrelationDimension());
      logger.debugFine(msg.toString());
    }
    database.associate(AssociationID.LOCAL_DIMENSIONALITY, id, pcares.getCorrelationDimension());
    database.associate(AssociationID.STRONG_EIGENVECTOR_MATRIX, id, pcares.getStrongEigenvectors());
    database.associate(AssociationID.NEIGHBOR_IDS, id, ids);
  }

  /**
   * Sets the values for the parameters alpha, pca and pcaDistancefunction if
   * specified. If the parameters are not specified default values are set.
   * 
   */
  @Override
  public List<String> setParameters(List<String> args) throws ParameterException {
    // add the kernel function wrapper for the distance function
    ArrayList<String> preprocessorParameters = new ArrayList<String>();
    OptionUtil.addParameter(preprocessorParameters, ProjectedDBSCANPreprocessor.DISTANCE_FUNCTION_ID, ArbitraryKernelFunctionWrapper.class.getName());
    preprocessorParameters.addAll(args);
    List<String> remainingParameters = super.setParameters(preprocessorParameters);
    // absolute
    absolute = ABSOLUTE_PARAM.isSet();

    // delta
    delta = DELTA_PARAM.getValue();
    if(absolute && DELTA_PARAM.tookDefaultValue()) {
      throw new WrongParameterValueException("Illegal parameter setting: " + "Flag " + ABSOLUTE_PARAM.getName() + " is set, " + "but no value for " + DELTA_PARAM.getName() + " is specified.");
    }

    // save parameters for pca
    ArrayList<String> pcaParameters = new ArrayList<String>();
    // eigen pair filter
    OptionUtil.addParameter(pcaParameters, PCAFilteredRunner.PCA_EIGENPAIR_FILTER, CompositeEigenPairFilter.class.getName());
    OptionUtil.addParameter(pcaParameters, CompositeEigenPairFilter.EIGENPAIR_FILTER_COMPOSITE_LIST, LimitEigenPairFilter.class.getName() + ListParameter.SPLIT + NormalizingEigenPairFilter.class.getName() + ListParameter.SPLIT + LimitEigenPairFilter.class.getName());
    // parameters for eigenpair filtering
    // FIXME: at some point, the code here used to always set ABSOLUTE, and
    // DELTA=0.0,
    // Then set absolute again and the new value for delta. I've removed this.
    // -- erich
    // abs
    if(absolute) {
      OptionUtil.addFlag(pcaParameters, LimitEigenPairFilter.EIGENPAIR_FILTER_ABSOLUTE);
    }
    // delta
    OptionUtil.addParameter(pcaParameters, LimitEigenPairFilter.EIGENPAIR_FILTER_DELTA, Double.toString(delta));

    // Big and small are not used in this version of KernelFourC
    // as they implicitly take the values 1 (big) and 0 (small),
    // big value
    OptionUtil.addParameter(pcaParameters, PCAFilteredRunner.BIG_ID, "1");

    // small value
    OptionUtil.addParameter(pcaParameters, PCAFilteredRunner.SMALL_ID, "0");

    pca.setParameters(pcaParameters);
    rememberParametersExcept(args, remainingParameters);

    return remainingParameters;
  }

  @Override
  public String shortDescription() {
    final StringBuffer description = new StringBuffer();
    description.append(KernelFourCPreprocessor.class.getName());
    description.append(" computes the local dimensionality and locally weighted matrix of objects of a certain database according to the 4C algorithm.\n");
    description.append("The PCA is based on epsilon range queries.\n");
    return description.toString();
  }

}