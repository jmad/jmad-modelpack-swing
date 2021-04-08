/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.jmad.modelpack.gui.dialogs;

import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.Box.createHorizontalGlue;
import static javax.swing.Box.createHorizontalStrut;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

import org.jmad.modelpack.gui.domain.JMadModelSelection;
import org.jmad.modelpack.gui.domain.JMadModelSelectionType;
import org.jmad.modelpack.gui.panels.JMadModelSelectionPanel;
import org.jmad.modelpack.gui.panels.JMadRepositorySelectionPanel;
import org.jmad.modelpack.service.JMadModelPackageRepositoryManager;
import org.jmad.modelpack.service.JMadModelPackageService;

public class JMadModelSelectionDialog extends JDialog {
    private JMadModelSelection modelSelection = null;

    public JMadModelSelectionDialog(JMadModelPackageService service, JMadModelSelectionType selectionType,
            JMadModelPackageRepositoryManager repositoryManager) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1000, 800));

        JMadModelSelectionPanel modelSelectionPanel = new JMadModelSelectionPanel(service, selectionType);
        JMadRepositorySelectionPanel repositorySelectionPanel = new JMadRepositorySelectionPanel(repositoryManager);

        JTabbedPane tabPane = new JTabbedPane();
        tabPane.addTab("Available models", modelSelectionPanel);
        tabPane.addTab("Models repositories", repositorySelectionPanel);
        add(tabPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(createEmptyBorder(5, 5, 5, 5));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            modelSelection = modelSelectionPanel.getModelSelection();
            if (modelSelection == null) {
                JOptionPane.showMessageDialog(this, "Please select a model!");
            } else {
                setVisible(false);
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            modelSelection = null;
            setVisible(false);
        });
        buttonPanel.add(createHorizontalGlue());
        buttonPanel.add(createHorizontalStrut(5));
        buttonPanel.add(cancelButton);
        buttonPanel.add(createHorizontalStrut(5));
        buttonPanel.add(okButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
    }

    public synchronized Optional<JMadModelSelection> getModelSelection() {
        return Optional.ofNullable(modelSelection);
    }
}
