package org.jmad.modelpack.gui.panels;

import javax.swing.*;
import java.awt.*;

import org.jmad.modelpack.gui.domain.JMadModelSelection;
import org.jmad.modelpack.gui.domain.JMadModelSelectionType;
import org.jmad.modelpack.service.JMadModelPackageService;

public class JMadModelSelectionPanel extends JPanel {
    private final JMadModelDefinitionSelectionPanel modelDefSelector;

    public JMadModelSelectionPanel(JMadModelPackageService service, JMadModelSelectionType selectionType) {
        setLayout(new BorderLayout());
        modelDefSelector = new JMadModelDefinitionSelectionPanel(service, selectionType);
        JMadModelPackSelectionPanel modelPackSelector = new JMadModelPackSelectionPanel(service,
                modelDefSelector::setModelPack);

        add(modelPackSelector, BorderLayout.CENTER);
        add(modelDefSelector, BorderLayout.EAST);
    }

    public JMadModelSelection getModelSelection() {
        return modelDefSelector.getModelSelection();
    }
}
