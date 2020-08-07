package org.jmad.modelpack.gui.panels;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static javax.swing.SwingUtilities.invokeLater;
import static org.jmad.modelpack.service.JMadModelPackageRepositoryManager.EnableState.DISABLED;
import static org.jmad.modelpack.service.JMadModelPackageRepositoryManager.EnableState.ENABLED;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;
import org.jmad.modelpack.domain.JMadModelPackageRepository;
import org.jmad.modelpack.service.JMadModelPackageRepositoryManager;
import org.jmad.modelpack.service.JMadModelPackageRepositoryManager.EnableState;

public class JMadRepositorySelectionPanel extends JPanel {

    public JMadRepositorySelectionPanel(JMadModelPackageRepositoryManager repositoryManager) {
        setLayout(new BorderLayout());
        RepositoryTableModel tableModel = new RepositoryTableModel((repository, state) -> {
            if (state == ENABLED) {
                repositoryManager.enable(repository);
            } else if (state == DISABLED) {
                repositoryManager.disable(repository);
            }
        });
        repositoryManager.state().subscribe(tableModel::setState);
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
        table.getColumnModel().getColumn(0).setMaxWidth(100);
    }

    private static class RepositoryTableModel extends AbstractTableModel {
        private static final int ENABLED_COLUMN = 0;
        private static final int REPOSITORY_COLUMN = 1;

        private Map<JMadModelPackageRepository, EnableState> enabledState = emptyMap();
        private List<JMadModelPackageRepository> orderedModelList = emptyList();
        private final BiConsumer<JMadModelPackageRepository, EnableState> enableConsumer;

        private RepositoryTableModel(BiConsumer<JMadModelPackageRepository, EnableState> enableConsumer) {
            this.enableConsumer = enableConsumer;
        }

        public synchronized void setState(Map<JMadModelPackageRepository, EnableState> state) {
            this.enabledState = ImmutableMap.copyOf(state);
            this.orderedModelList = state.keySet().stream() //
                    .sorted(comparing(JMadModelPackageRepository::uri)) //
                    .collect(toList());
            invokeLater(this::fireTableDataChanged);
        }

        @Override
        public synchronized int getRowCount() {
            return this.enabledState.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public synchronized Object getValueAt(int row, int column) {
            JMadModelPackageRepository repository = orderedModelList.get(row);
            if (column == ENABLED_COLUMN) {
                return enabledState.get(repository).asBoolEnabled();
            } else if (column == REPOSITORY_COLUMN) {
                return repository;
            }
            throw new IllegalArgumentException("Illegal column: " + column);
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == ENABLED_COLUMN) {
                return Boolean.class;
            } else if (column == REPOSITORY_COLUMN) {
                return JMadModelPackageRepository.class;
            }
            throw new IllegalArgumentException("Illegal column: " + column);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return (column == ENABLED_COLUMN);
        }

        @Override
        public synchronized void setValueAt(Object aValue, int row, int column) {
            checkState(column == ENABLED_COLUMN, "Only enable state is editable");
            checkState(aValue instanceof Boolean, "value must be boolean");
            boolean enabled = (Boolean) aValue;
            enableConsumer.accept(orderedModelList.get(row), enabled ? ENABLED : DISABLED);
        }

        @Override
        public String getColumnName(int column) {
            if (column == ENABLED_COLUMN) {
                return "Enabled";
            } else if (column == REPOSITORY_COLUMN) {
                return "Repository URI";
            }
            throw new IllegalArgumentException("Illegal column: " + column);
        }
    }

}
