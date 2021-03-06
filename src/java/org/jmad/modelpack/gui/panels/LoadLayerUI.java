package org.jmad.modelpack.gui.panels;

import static org.jmad.modelpack.gui.util.MoreSwingUtilities.invokeOnSwingThread;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;

public class LoadLayerUI extends LayerUI<JComponent> {
    private static final String REPAINT_EVENT = "LoadLayerUI_repaint";
    private boolean loading;

    @Override
    public void paint(Graphics graphics, JComponent component) {
        super.paint(graphics, component);
        if (loading) {
            Graphics2D overlay = (Graphics2D) graphics;
            Composite oldComposite = overlay.getComposite();
            overlay.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            overlay.setColor(Color.BLACK);
            overlay.fillRect(0, 0, component.getWidth(), component.getHeight());
            overlay.setComposite(oldComposite);
            overlay.setColor(Color.WHITE);
            String text = "Loading, please wait ...";
            Rectangle2D stringBounds = overlay.getFontMetrics().getStringBounds(text, overlay);
            overlay.drawString(text, (int) ((component.getWidth() - stringBounds.getWidth()) / 2),
                    (int) ((component.getHeight() - stringBounds.getHeight()) / 2));
        }
    }

    @Override
    public void applyPropertyChange(PropertyChangeEvent evt, JLayer<? extends JComponent> layer) {
        if (evt.getPropertyName().equals(REPAINT_EVENT)) {
            layer.repaint();
        }
    }

    @Override
    protected void processKeyEvent(KeyEvent e, JLayer<? extends JComponent> l) {
        if (loading) {
            e.consume();
        }
    }

    @Override
    protected void processMouseEvent(MouseEvent e, JLayer<? extends JComponent> l) {
        if (loading) {
            e.consume();
        }
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        ((JLayer) c).setLayerEventMask(AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        ((JLayer) c).setLayerEventMask(0);
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        invokeOnSwingThread(() -> firePropertyChange(REPAINT_EVENT, null, null));
    }
}
