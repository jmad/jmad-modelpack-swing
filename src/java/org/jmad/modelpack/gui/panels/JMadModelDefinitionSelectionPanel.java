package org.jmad.modelpack.gui.panels;

import static java.util.Arrays.stream;
import static org.jmad.modelpack.gui.util.MoreSwingUtilities.invokeOnSwingThread;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

import cern.accsoft.steering.jmad.domain.machine.RangeDefinition;
import cern.accsoft.steering.jmad.domain.machine.SequenceDefinition;
import cern.accsoft.steering.jmad.model.JMadModelStartupConfiguration;
import cern.accsoft.steering.jmad.modeldefs.domain.JMadModelDefinition;
import cern.accsoft.steering.jmad.modeldefs.domain.OpticsDefinition;
import org.jmad.modelpack.domain.ModelPackageVariant;
import org.jmad.modelpack.gui.domain.JMadModelSelection;
import org.jmad.modelpack.gui.domain.JMadModelSelectionType;
import org.jmad.modelpack.service.JMadModelPackageService;

class JMadModelDefinitionSelectionPanel extends JPanel {
    private final JMadModelPackageService service;
    private final JList<JMadModelDefinition> modelDefinitionSelector;
    private final JComboBox<SequenceDefinition> sequenceDefinitionSelector;
    private final JComboBox<RangeDefinition> rangeDefinitionSelector;
    private final JList<OpticsDefinition> opticsDefinitionSelector;
    private final LoadLayerUI loadUi;

    public JMadModelDefinitionSelectionPanel(JMadModelPackageService service, JMadModelSelectionType selectionType) {
        this.service = service;
        loadUi = new LoadLayerUI();

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 500));

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());

        JPanel modelDefinitionPanel = new JPanel();
        modelDefinitionPanel.setLayout(new BorderLayout());
        modelDefinitionPanel.setBorder(BorderFactory.createTitledBorder("Model Definitions"));
        modelDefinitionSelector = new JList<>();
        modelDefinitionSelector.addListSelectionListener(e -> modelDefinitionSelectionChanged());
        modelDefinitionPanel.add(new JScrollPane(modelDefinitionSelector), BorderLayout.CENTER);
        content.add(modelDefinitionPanel, BorderLayout.CENTER);

        if (selectionType == JMadModelSelectionType.ALL) {
            JPanel modelConfigPanel = new JPanel();
            sequenceDefinitionSelector = new JComboBox<>();
            sequenceDefinitionSelector.addActionListener(e -> sequenceSelectionChanged());
            rangeDefinitionSelector = new JComboBox<>();
            opticsDefinitionSelector = new JList<>();
            opticsDefinitionSelector.setMinimumSize(new Dimension(50, 100));
            modelConfigPanel.setBorder(BorderFactory.createTitledBorder("Model Configuration"));
            modelConfigPanel.setLayout(new BoxLayout(modelConfigPanel, BoxLayout.Y_AXIS));
            modelConfigPanel.add(new JLabel("Sequence:"));
            modelConfigPanel.add(sequenceDefinitionSelector);
            modelConfigPanel.add(Box.createVerticalStrut(5));
            modelConfigPanel.add(new JLabel("Range:"));
            modelConfigPanel.add(rangeDefinitionSelector);
            modelConfigPanel.add(Box.createVerticalStrut(5));
            modelConfigPanel.add(new JLabel("Optic:"));
            modelConfigPanel.add(new JScrollPane(opticsDefinitionSelector));
            stream(modelConfigPanel.getComponents()).forEach(c -> ((JComponent) c).setAlignmentX(0.0f));
            content.add(modelConfigPanel, BorderLayout.SOUTH);
        } else {
            sequenceDefinitionSelector = null;
            rangeDefinitionSelector = null;
            opticsDefinitionSelector = null;
        }
        add(new JLayer<>(content, loadUi), BorderLayout.CENTER);
    }

    private void modelDefinitionSelectionChanged() {
        JMadModelDefinition modelDef = modelDefinitionSelector.getSelectedValue();
        if (modelDef == null) {
            Optional.ofNullable(sequenceDefinitionSelector).ifPresent(s -> s.setModel(new DefaultComboBoxModel<>()));
            Optional.ofNullable(rangeDefinitionSelector).ifPresent(s -> s.setModel(new DefaultComboBoxModel<>()));
            Optional.ofNullable(opticsDefinitionSelector).ifPresent(s -> s.setModel(new DefaultListModel<>()));
            return;
        }
        if (sequenceDefinitionSelector != null) {
            SequenceDefinition[] sequenceDefinitions = modelDef.getSequenceDefinitions()
                    .toArray(new SequenceDefinition[0]);
            sequenceDefinitionSelector.setModel(new DefaultComboBoxModel<>(sequenceDefinitions));
            sequenceDefinitionSelector.setSelectedItem(modelDef.getDefaultSequenceDefinition());
            sequenceSelectionChanged();
        }
        if (opticsDefinitionSelector != null) {
            DefaultListModel<OpticsDefinition> newModel = new DefaultListModel<>();
            modelDef.getOpticsDefinitions().forEach(newModel::addElement);
            opticsDefinitionSelector.setModel(newModel);
            opticsDefinitionSelector.setSelectedValue(modelDef.getDefaultOpticsDefinition(), true);
        }
    }

    private void sequenceSelectionChanged() {
        if (sequenceDefinitionSelector == null || rangeDefinitionSelector == null) {
            return;
        }
        SequenceDefinition sequenceDefinition = (SequenceDefinition) sequenceDefinitionSelector.getSelectedItem();
        if (sequenceDefinition == null) {
            return;
        }
        RangeDefinition[] rangeDefinitions = sequenceDefinition.getRangeDefinitions().toArray(new RangeDefinition[0]);
        rangeDefinitionSelector.setModel(new DefaultComboBoxModel<>(rangeDefinitions));
        rangeDefinitionSelector.setSelectedItem(sequenceDefinition.getDefaultRangeDefinition());
    }

    public void setModelPack(ModelPackageVariant modelPack) {
        DefaultListModel<JMadModelDefinition> newModel = new DefaultListModel<>();
        loadUi.setLoading(true);
        service.modelDefinitionsFrom(modelPack) //
                .doOnTerminate(() -> loadUi.setLoading(false)) //
                .subscribe(newModel::addElement);
        modelDefinitionSelector.setModel(newModel);
        invokeOnSwingThread(this::modelDefinitionSelectionChanged);
    }

    public JMadModelSelection getModelSelection() {
        JMadModelDefinition selectedModel = modelDefinitionSelector.getSelectedValue();
        if (selectedModel == null) {
            return null;
        }
        if (rangeDefinitionSelector != null && opticsDefinitionSelector != null) {
            JMadModelStartupConfiguration startupConfiguration = new JMadModelStartupConfiguration();
            startupConfiguration.setInitialRangeDefinition((RangeDefinition) rangeDefinitionSelector.getSelectedItem());
            startupConfiguration.setInitialOpticsDefinition(opticsDefinitionSelector.getSelectedValue());
            return new JMadModelSelection(selectedModel, startupConfiguration);
        } else {
            return new JMadModelSelection(selectedModel);
        }
    }
}
