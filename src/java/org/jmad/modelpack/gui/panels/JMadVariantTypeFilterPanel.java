package org.jmad.modelpack.gui.panels;

import static java.util.Collections.newSetFromMap;

import javax.swing.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.jmad.modelpack.domain.VariantType;

class JMadVariantTypeFilterPanel extends JPanel {

    private final Set<VariantType> enabledVariants = newSetFromMap(new ConcurrentHashMap<>());

    public JMadVariantTypeFilterPanel(Consumer<Set<VariantType>> selectionConsumer) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Filter"));
        enabledVariants.add(VariantType.RELEASE);
        for (VariantType variantType : VariantType.values()) {
            JCheckBox checkBox = new JCheckBox(variantType.toString().toLowerCase());
            checkBox.setSelected(enabledVariants.contains(variantType));
            checkBox.addActionListener(e -> {
                if (checkBox.isSelected()) {
                    enabledVariants.add(variantType);
                } else {
                    enabledVariants.remove(variantType);
                }
                selectionConsumer.accept(enabledVariants);
            });
            add(checkBox);
        }
        selectionConsumer.accept(enabledVariants);
    }

}
