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
package elki.gui.multistep;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;

import javax.swing.*;

import elki.KDDTask;
import elki.application.AbstractApplication;
import elki.gui.GUIUtil;
import elki.gui.minigui.MiniGUI;
import elki.gui.multistep.panels.*;
import elki.gui.util.LogPanel;
import elki.gui.util.SavedSettingsFile;
import elki.logging.CLISmartHandler;
import elki.logging.Logging;
import elki.logging.LoggingConfiguration;
import elki.utilities.Alias;
import elki.utilities.optionhandling.parameterization.ListParameterization;
import elki.utilities.optionhandling.parameterization.Parameterization;
import elki.utilities.optionhandling.parameterization.SerializedParameterization;
import elki.workflow.OutputStep;

/**
 * Experimenter-style multi step GUI.
 *
 * @author Erich Schubert
 * @since 0.4.0
 *
 * @opt nodefillcolor LemonChiffon
 * @composed - - - AlgorithmTabPanel
 * @composed - - - EvaluationTabPanel
 * @composed - - - InputTabPanel
 * @composed - - - LoggingTabPanel
 * @composed - - - OutputTabPanel
 * @composed - - - SavedSettingsTabPanel
 */
@Alias({ "multi", "multigui", "multistepgui" })
public class MultiStepGUI extends AbstractApplication {
  /**
   * ELKI logger for the GUI
   */
  private static final Logging LOG = Logging.getLogger(MultiStepGUI.class);

  /**
   * Quit action, for mnemonics.
   */
  private static final String ACTION_QUIT = "quit";

  /**
   * The frame
   */
  JFrame frame;

  /**
   * Logging output area.
   */
  protected LogPanel outputArea;

  /**
   * Input panel.
   */
  private InputTabPanel inputTab;

  /**
   * Algorithm panel.
   */
  private AlgorithmTabPanel algTab;

  /**
   * Evaluation panel.
   */
  private EvaluationTabPanel evalTab;

  /**
   * Output panel.
   */
  private OutputTabPanel outTab;

  /**
   * Logging panel.
   */
  private LoggingTabPanel logTab;

  /**
   * Saved settingspanel.
   */
  private SavedSettingsTabPanel setTab;

  /**
   * Constructor
   */
  public MultiStepGUI() {
    super();
    frame = new JFrame("ELKI Multi-Step GUI");
    Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    int ppi = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
    frame.setPreferredSize(new Dimension(Math.min(10 * ppi, screen.width), Math.min(10 * ppi, screen.height - 32)));
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    try {
      frame.setIconImage(new ImageIcon(KDDTask.class.getResource("elki-icon.png")).getImage());
    }
    catch(Exception e) {
      // Ignore - icon not found is not fatal.
    }
    frame.setLayout(new GridBagLayout());

    // setup text output area
    outputArea = new LogPanel();

    // Create the scroll pane and add the table to it.
    JScrollPane outputPane = new JScrollPane(outputArea);
    outputPane.setPreferredSize(new Dimension(800, 400));

    // Add the output pane to the bottom
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.weightx = constraints.weighty = 1;
    frame.add(outputPane, constraints);

    // reconfigure logging
    outputArea.becomeDefaultLogger();

    // setup tabbed panels
    JTabbedPane panels = new JTabbedPane();

    // Add the output pane to the bottom
    constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridx = constraints.gridy = 0;
    constraints.weightx = constraints.weighty = 1;
    frame.add(panels, constraints);

    addPanels(panels);
    frame.pack();

    // Keyboard bindings to quit.
    JRootPane rootpane = frame.getRootPane();
    rootpane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK), ACTION_QUIT);
    rootpane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK), ACTION_QUIT);
    rootpane.getActionMap().put(ACTION_QUIT, new AbstractAction() {
      /**
       * Serial version
       */
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        frame.dispose();
      }
    });
  }

  private void addPanels(JTabbedPane panels) {
    SavedSettingsFile settings = new SavedSettingsFile(MiniGUI.SAVED_SETTINGS_FILENAME);
    try {
      settings.load();
    }
    catch(NoSuchFileException e) {
      // ok.
    }
    catch(IOException e) {
      LOG.exception("Error loading saved settings.", e);
    }

    inputTab = new InputTabPanel();
    algTab = new AlgorithmTabPanel(inputTab);
    evalTab = new EvaluationTabPanel(inputTab, algTab);
    outTab = new OutputTabPanel(inputTab, evalTab);
    logTab = new LoggingTabPanel();
    setTab = new SavedSettingsTabPanel(settings, this);
    panels.addTab("Input", inputTab);
    panels.addTab("Algorithm", algTab);
    panels.addTab("Evaluation", evalTab);
    panels.addTab("Output", outTab);
    panels.addTab("Logging", logTab);
    panels.addTab("Saved Settings", setTab);

    setParameters(new ListParameterization());
  }

  /**
   * Set the parameters.
   *
   * @param config Parameterization
   */
  public void setParameters(Parameterization config) {
    // Clear errors after each step, so they don't consider themselves failed
    // because of earlier errors.
    logTab.setParameters(config);
    // config.clearErrors();
    inputTab.setParameters(config);
    // config.clearErrors();
    algTab.setParameters(config);
    // config.clearErrors();
    evalTab.setParameters(config);
    // config.clearErrors();
    outTab.setParameters(config);
    // config.clearErrors();
  }

  /**
   * Get the serialized parameters
   *
   * @return Serialized parameters
   */
  public ArrayList<String> serializeParameters() {
    ListParameterization params = new ListParameterization();
    logTab.appendParameters(params);
    inputTab.appendParameters(params);
    algTab.appendParameters(params);
    evalTab.appendParameters(params);
    outTab.appendParameters(params);
    return params.serialize();
  }

  @Override
  public void run() {
    frame.setVisible(true);
    outputArea.becomeDefaultLogger();
  }

  /**
   * Main method that just spawns the UI.
   *
   * @param args command line parameters
   */
  public static void main(final String[] args) {
    GUIUtil.logUncaughtExceptions(LOG);
    GUIUtil.setLookAndFeel();
    OutputStep.setDefaultHandlerVisualizer();

    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          final MultiStepGUI gui = new MultiStepGUI();
          gui.run();
          gui.setParameters(args != null && args.length > 0 ? new SerializedParameterization(args) : new SerializedParameterization());
        }
        catch(Exception | Error e) {
          // Restore error handler, as the GUI is likely broken.
          LoggingConfiguration.replaceDefaultHandler(new CLISmartHandler());
          LOG.exception(e);
        }
      }
    });
  }
}
