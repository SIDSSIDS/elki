package de.lmu.ifi.dbs.elki.datasource.filter;

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.SimpleTypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeInformation;
import de.lmu.ifi.dbs.elki.data.type.VectorFieldTypeInformation;
import de.lmu.ifi.dbs.elki.datasource.bundle.MultipleObjectsBundle;
import de.lmu.ifi.dbs.elki.utilities.exceptions.AbortException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.ListGreaterEqualConstraint;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.IntListParameter;

/**
 * Split an existing column into two types.
 * 
 * @author Erich Schubert
 */
public class SplitNumberVectorFilter<V extends NumberVector<V, ?>> implements ObjectFilter {
  /**
   * Selected dimensions.
   */
  final int[] dims;

  /**
   * Constructor.
   * 
   * @param dims Dimensions to use.
   */
  public SplitNumberVectorFilter(int[] dims) {
    super();
    this.dims = dims;
  }

  @Override
  public MultipleObjectsBundle filter(MultipleObjectsBundle objects) {
    if(objects.dataLength() == 0) {
      return objects;
    }
    MultipleObjectsBundle bundle = new MultipleObjectsBundle();

    for(int r = 0; r < objects.metaLength(); r++) {
      @SuppressWarnings("unchecked")
      SimpleTypeInformation<Object> type = (SimpleTypeInformation<Object>) objects.meta(r);
      @SuppressWarnings("unchecked")
      final List<Object> column = (List<Object>) objects.getColumn(r);
      if(!getInputTypeRestriction().isAssignableFromType(type)) {
        bundle.appendColumn(type, column);
        continue;
      }
      // Should be a vector type after above test.
      @SuppressWarnings("unchecked")
      final VectorFieldTypeInformation<V> vtype = (VectorFieldTypeInformation<V>) type;

      // Get the replacement type informations
      VectorFieldTypeInformation<V> type1 = new VectorFieldTypeInformation<V>(type.getRestrictionClass(), dims.length, dims.length);
      VectorFieldTypeInformation<V> type2 = new VectorFieldTypeInformation<V>(type.getRestrictionClass(), vtype.dimensionality() - dims.length, vtype.dimensionality() - dims.length);
      final List<V> col1 = new ArrayList<V>(column.size());
      final List<V> col2 = new ArrayList<V>(column.size());
      bundle.appendColumn(type1, col1);
      bundle.appendColumn(type2, col2);

      // Build other dimensions array.
      int[] odims = new int[vtype.dimensionality() - dims.length];
      {
        int i = 0;
        for(int d = 1; d <= vtype.dimensionality(); d++) {
          boolean found = false;
          for(int j = 0; j < dims.length; j++) {
            if(dims[j] == d) {
              found = true;
              break;
            }
          }
          if(!found) {
            if(i >= odims.length) {
              throw new AbortException("Dimensionalities not proper!");
            }
            odims[i] = d;
            i++;
          }
        }
      }
      // Normalization scan
      for(int i = 0; i < objects.dataLength(); i++) {
        @SuppressWarnings("unchecked")
        final V obj = (V) column.get(i);
        double[] part1 = new double[dims.length];
        double[] part2 = new double[obj.getDimensionality() - dims.length];
        for(int d = 0; d < dims.length; d++) {
          part1[d] = obj.doubleValue(dims[d]);
        }
        for(int d = 0; d < odims.length; d++) {
          part2[d] = obj.doubleValue(odims[d]);
        }
        col1.add(obj.newInstance(part1));
        col2.add(obj.newInstance(part2));
      }
    }
    return bundle;
  }

  /**
   * The input type we use.
   * 
   * @return type information
   */
  private TypeInformation getInputTypeRestriction() {
    // Find maximum dimension requested
    int m = dims[0];
    for(int i = 1; i < dims.length; i++) {
      m = Math.max(dims[i], m);
    }
    return new VectorFieldTypeInformation<NumberVector<?, ?>>(NumberVector.class, m, Integer.MAX_VALUE);
  }

  /**
   * Parameterization class.
   * 
   * @author Erich Schubert
   * 
   * @apiviz.exclude
   */
  public static class Parameterizer<V extends NumberVector<V, ?>> extends AbstractParameterizer {
    /**
     * The parameter listing the split dimensions.
     */
    public static final OptionID SELECTED_ATTRIBUTES_ID = OptionID.getOrCreateOptionID("split.dims", "Dimensions to split into the first relation.");

    /**
     * Dimensions to use.
     */
    protected int[] dims;

    @Override
    protected void makeOptions(Parameterization config) {
      super.makeOptions(config);
      IntListParameter selectedAttributesP = new IntListParameter(SELECTED_ATTRIBUTES_ID, new ListGreaterEqualConstraint<Integer>(1));
      if(config.grab(selectedAttributesP)) {
        List<Integer> dimensionList = selectedAttributesP.getValue();
        dims = new int[dimensionList.size()];
        for(int i = 0; i < dimensionList.size(); i++) {
          dims[i] = dimensionList.get(i);
        }
      }
    }

    @Override
    protected SplitNumberVectorFilter<V> makeInstance() {
      return new SplitNumberVectorFilter<V>(dims);
    }
  }
}