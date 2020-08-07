package org.jmad.modelpack.gui.util;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public final class MoreSwingUtilities {
    private MoreSwingUtilities() {
        throw new UnsupportedOperationException("static only");
    }

    public static void invokeOnSwingThread(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InvocationTargetException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
