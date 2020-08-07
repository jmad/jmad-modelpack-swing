package org.jmad.modelpack.gui.panels;

import static com.google.common.collect.Multimaps.synchronizedMultimap;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static org.jmad.modelpack.gui.util.MoreSwingUtilities.invokeOnSwingThread;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jmad.modelpack.domain.ModelPackage;
import org.jmad.modelpack.domain.ModelPackageVariant;
import org.jmad.modelpack.domain.ModelPackages;
import org.jmad.modelpack.domain.VariantType;
import org.jmad.modelpack.service.JMadModelPackageService;

class JMadModelPackSelectionPanel extends JPanel {
    private final JMadModelPackageService service;
    private final Multimap<ModelPackage, ModelPackageVariant> availableModelPacks = synchronizedMultimap(
            TreeMultimap.create(comparing(ModelPackage::name), ModelPackages.latestFirstPackageVariantComparator()));
    private Set<VariantType> enabledVariantTypes;
    private final DefaultTreeTableModel modelPackTableModel;
    private final LoadLayerUI loadUi;

    public JMadModelPackSelectionPanel(JMadModelPackageService service, Consumer<ModelPackageVariant> consumer) {
        this.service = service;
        this.modelPackTableModel = new DefaultTreeTableModel(null, asList("Model Pack", "Variant"));

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Model Packages"));
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(new JMadVariantTypeFilterPanel(this::variantFilterChanged));
        leftPanel.add(Box.createVerticalGlue());
        JButton reloadButton = new JButton("Refresh");
        reloadButton.addActionListener(e -> reloadAvailableModelPacks());
        leftPanel.add(reloadButton);
        JButton clearCacheButton = new JButton("Clear Cache");
        clearCacheButton.addActionListener(e -> service.clearCache());
        leftPanel.add(clearCacheButton);
        add(leftPanel, BorderLayout.WEST);

        JXTreeTable treeTable = new JXTreeTable(modelPackTableModel);
        treeTable.setRootVisible(false);
        treeTable.setLeafIcon(null);
        treeTable.setClosedIcon(null);
        treeTable.setOpenIcon(null);
        treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        treeTable.addTreeSelectionListener(e -> {
            TreePath newSelection = e.getNewLeadSelectionPath();
            if (newSelection == null) {
                return;
            }
            consumer.accept(((ModelPackTreeTableNode) newSelection.getLastPathComponent()).value());
        });
        loadUi = new LoadLayerUI();
        add(new JLayer<>(new JScrollPane(treeTable), loadUi), BorderLayout.CENTER);
        reloadAvailableModelPacks();
    }

    private void reloadAvailableModelPacks() {
        loadUi.setLoading(true);
        modelPackTableModel.setRoot(new DefaultMutableTreeTableNode());
        availableModelPacks.clear();
        service.availablePackages() //
                .doOnTerminate(() -> loadUi.setLoading(false)) //
                .subscribe(mpv -> {
                    availableModelPacks.put(mpv.modelPackage(), mpv);
                    invokeOnSwingThread(this::refreshTable);
                });
    }

    private void variantFilterChanged(Set<VariantType> variantTypes) {
        enabledVariantTypes = variantTypes;
        refreshTable();
    }

    private void refreshTable() {
        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode();
        Multimap<ModelPackage, ModelPackageVariant> filteredModelPackVariants = Multimaps
                .filterValues(availableModelPacks, mpv -> enabledVariantTypes.contains(mpv.variant().type()));
        for (ModelPackage modelPack : filteredModelPackVariants.keySet()) {
            List<ModelPackageVariant> variants = new ArrayList<>(filteredModelPackVariants.get(modelPack));
            ModelPackageVariant first = variants.get(0);
            ModelPackTreeTableNode treeNode = new ModelPackTreeTableNode(first);
            variants.stream().skip(1).map(ModelPackTreeTableNode::new).forEach(treeNode::add);
            root.add(treeNode);
        }
        modelPackTableModel.setRoot(root);
    }

    private static class ModelPackTreeTableNode extends AbstractMutableTreeTableNode {
        private static final int MODELPACK_COLUMN = 0;
        private static final int VARIANT_COLUMN = 1;
        private final ModelPackageVariant modelPackageVariant;

        private ModelPackTreeTableNode(ModelPackageVariant modelPackageVariant) {
            this.modelPackageVariant = modelPackageVariant;
        }

        public ModelPackageVariant value() {
            return modelPackageVariant;
        }

        @Override
        public Object getValueAt(int column) {
            if (column == MODELPACK_COLUMN) {
                return modelPackageVariant.modelPackage().name();
            } else if (column == VARIANT_COLUMN) {
                return modelPackageVariant.variant().name() + " [" + modelPackageVariant.variant().type() + "]";
            }
            throw new IllegalArgumentException("Invalid column: " + column);
        }

        @Override
        public int getColumnCount() {
            return 2;
        }
    }

}
