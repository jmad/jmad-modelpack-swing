/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.jmad.modelpack.gui.conf;

import static org.jmad.modelpack.gui.util.MoreSwingUtilities.invokeOnSwingThread;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.jmad.modelpack.gui.dialogs.JMadModelSelectionDialog;
import org.jmad.modelpack.gui.domain.JMadModelSelection;
import org.jmad.modelpack.gui.domain.JMadModelSelectionType;
import org.jmad.modelpack.service.JMadModelPackageRepositoryManager;
import org.jmad.modelpack.service.JMadModelPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("jmadModelSelectionDialogFactory")
@Lazy
public class JMadModelSelectionDialogFactory {

    @Autowired
    private JMadModelPackageService packageService;

    @Autowired
    private JMadModelPackageRepositoryManager repositoryManager;

    public Optional<JMadModelSelection> showAndWaitModelSelection() {
        return showAndWaitModelSelection(JMadModelSelectionType.ALL);
    }

    public Optional<JMadModelSelection> showAndWaitModelSelection(JMadModelSelectionType selectionType) {
        AtomicReference<Optional<JMadModelSelection>> userSelection = new AtomicReference<>();
        invokeOnSwingThread(() -> {
            JMadModelSelectionDialog modelSelectionDialog = new JMadModelSelectionDialog(packageService,
                    selectionType, repositoryManager);
            modelSelectionDialog.setModal(true);
            modelSelectionDialog.setVisible(true);
            userSelection.set(modelSelectionDialog.getModelSelection());
        });

        return userSelection.get();
    }
}
