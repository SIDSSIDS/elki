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
package elki.gui.configurator;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Paths;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import elki.gui.icons.StockIcon;
import elki.logging.LoggingUtil;
import elki.utilities.optionhandling.parameters.FileParameter;

/**
 * Provide a configuration panel to choose a file with a file selector button.
 * 
 * @author Erich Schubert
 * @since 0.4.0
 * 
 * @assoc - - - FileParameter
 */
public class FileParameterConfigurator extends AbstractSingleParameterConfigurator<FileParameter> implements ActionListener {
  /**
   * The panel to store the components
   */
  final JPanel panel;

  /**
   * Text field to store the name
   */
  final JTextField textfield;

  /**
   * The button to open the file selector
   */
  final JButton button;

  /**
   * Constructor.
   * 
   * @param fp File parameter
   * @param parent Component to attach to.
   */
  public FileParameterConfigurator(FileParameter fp, JComponent parent) {
    super(fp, parent);
    // create components
    textfield = new JTextField();
    textfield.setBorder(new EmptyBorder(3, 3, 3, 3));
    textfield.setToolTipText(param.getOptionID().getDescription());
    textfield.addActionListener(this);
    button = new JButton(StockIcon.getStockIcon(StockIcon.DOCUMENT_OPEN));
    button.setToolTipText(param.getOptionID().getDescription());
    button.addActionListener(this);
    if(fp.isDefined()) {
      textfield.setText(fp.getValueAsString());
    }

    // make a panel
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weightx = 1.0;
    panel = new JPanel(new BorderLayout());
    panel.add(textfield, BorderLayout.CENTER);
    panel.add(button, BorderLayout.EAST);

    parent.add(panel, constraints);
    finishGridRow();
  }

  /**
   * Button callback to show the file selector
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    // Use a new JFileChooser. Inconsistent behaviour otherwise!
    final JFileChooser fc = new JFileChooser(new File("."));
    if(param.isDefined()) {
      fc.setSelectedFile(Paths.get(param.getValue()).toFile());
    }

    if(e.getSource() == button) {
      int returnVal = fc.showOpenDialog(button);
      if(returnVal == JFileChooser.APPROVE_OPTION) {
        textfield.setText(fc.getSelectedFile().getPath());
        fireValueChanged();
      }
      // else: do nothing on cancel.
    }
    else if(e.getSource() == textfield) {
      fireValueChanged();
    }
    else {
      LoggingUtil.warning("actionPerformed triggered by unknown source: " + e.getSource());
    }
  }

  @Override
  public String getUserInput() {
    return textfield.getText();
  }
}
