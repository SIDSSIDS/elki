/*
 * This file is part of ELKI:
 * Environment for Developing KDD-Applications Supported by Index-Structures
 *
 * Copyright (C) 2019
 * ELKI Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package elki.datasource.filter.normalization.instancewise;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;

import elki.data.DoubleVector;
import elki.data.SparseFloatVector;
import elki.data.type.TypeUtil;
import elki.datasource.AbstractDataSourceTest;
import elki.datasource.InputStreamDatabaseConnection;
import elki.datasource.bundle.MultipleObjectsBundle;
import elki.datasource.parser.Parser;
import elki.datasource.parser.TermFrequencyParser;
import elki.utilities.ELKIBuilder;

/**
 * Test the length normalization filter.
 *
 * @author Matthew Arcifa
 * @since 0.7.5
 */
public class LengthNormalizationTest extends AbstractDataSourceTest {
  @Test
  public void defaultParameters() {
    String filename = UNITTEST + "normalization-test-1.csv";
    LengthNormalization<DoubleVector> filter = new ELKIBuilder<>(LengthNormalization.class).build();
    MultipleObjectsBundle bundle = readBundle(filename, filter);
    int dim = getFieldDimensionality(bundle, 0, TypeUtil.NUMBER_VECTOR_FIELD);

    // Verify that the length of each row vector is 1.
    for(int row = 0; row < bundle.dataLength(); row++) {
      DoubleVector d = get(bundle, row, 0, DoubleVector.class);
      double len = 0.0;
      for(int col = 0; col < dim; col++) {
        final double v = d.doubleValue(col);
        len += v * v;
      }
      assertEquals("Vector length is not 1", 1., len, 1e-15);
    }
  }

  @Test
  public void sparse() throws IOException {
    String filename = UNITTEST + "parsertest.tf";
    LengthNormalization<DoubleVector> filter = new ELKIBuilder<>(LengthNormalization.class).build();
    Parser parser = new ELKIBuilder<>(TermFrequencyParser.class)//
        .build();
    MultipleObjectsBundle bundle;
    try (InputStream is = open(filename);
        InputStreamDatabaseConnection dbc = new InputStreamDatabaseConnection(is, Arrays.asList(filter), parser)) {
      bundle = dbc.loadData();
    }
    // Verify that the length of each row vector is 1.
    for(int row = 0; row < bundle.dataLength(); row++) {
      SparseFloatVector d = get(bundle, row, 0, SparseFloatVector.class);
      double len = 0.0;
      for(int j = d.iter(); d.iterValid(j); j = d.iterAdvance(j)) {
        final double v = d.iterDoubleValue(j);
        len += v * v;
      }
      assertEquals("Vector length is not 1", 1., len, 1e-7);
    }
  }
}
