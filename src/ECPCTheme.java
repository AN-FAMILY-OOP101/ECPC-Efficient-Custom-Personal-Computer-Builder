import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * GX-inspired visual styling for ECPC. All values here affect presentation only.
 * No recommendation, compatibility or catalog logic belongs in this class.
 */
public final class ECPCTheme {
    public static final Color BACKGROUND = new Color(16, 16, 21);
    public static final Color SIDEBAR = new Color(20, 20, 29);
    public static final Color PANEL = new Color(28, 28, 38);
    public static final Color SURFACE = new Color(40, 40, 53);
    public static final Color DEEP = new Color(22, 22, 31);
    public static final Color BORDER = new Color(59, 59, 74);
    public static final Color ACCENT = new Color(255, 25, 93);
    public static final Color ACCENT_HOVER = new Color(255, 66, 121);
    public static final Color PURPLE = new Color(178, 138, 255);
    public static final Color TEXT = new Color(246, 246, 251);
    public static final Color MUTED = new Color(203, 204, 219);
    public static final Color SUCCESS = new Color(85, 220, 169);
    public static final Color WARNING = new Color(255, 183, 113);

    // Unified typography scale. Java's logical SansSerif maps to a readable local font
    // on Windows, Linux, and macOS without bundling third-party font files.
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 15);
    public static final Font LABEL = new Font("SansSerif", Font.BOLD, 14);
    public static final Font CAPTION = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font OVERLINE = new Font("SansSerif", Font.BOLD, 13);
    public static final Font SECTION = new Font("SansSerif", Font.BOLD, 19);
    public static final Font PAGE_TITLE = new Font("SansSerif", Font.BOLD, 26);
    public static final Font METRIC = new Font("SansSerif", Font.BOLD, 22);
    public static final Font REPORT = new Font("SansSerif", Font.PLAIN, 15);
    public static final Font REPORT_HEADING = new Font("SansSerif", Font.BOLD, 18);

    private ECPCTheme() {
    }

    /** Apply global defaults after installing the Swing Look and Feel. */
    public static void install() {
        UIManager.put("control", PANEL);
        UIManager.put("info", SURFACE);
        UIManager.put("nimbusBase", PANEL);
        UIManager.put("nimbusBlueGrey", SURFACE);
        UIManager.put("nimbusLightBackground", DEEP);
        UIManager.put("nimbusSelectionBackground", new Color(89, 37, 61));
        UIManager.put("text", TEXT);
        UIManager.put("textText", TEXT);
        UIManager.put("textHighlight", ACCENT);
        UIManager.put("controlText", TEXT);
        UIManager.put("menuText", TEXT);
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.font", BODY);
        UIManager.put("Button.font", LABEL);
        UIManager.put("TextField.font", BODY);
        UIManager.put("TextArea.font", REPORT);
        UIManager.put("TextPane.font", REPORT);
        UIManager.put("ComboBox.font", BODY);
        UIManager.put("List.font", BODY);
        UIManager.put("Menu.font", BODY);
        UIManager.put("MenuItem.font", BODY);
        UIManager.put("OptionPane.messageFont", BODY);
        UIManager.put("OptionPane.buttonFont", LABEL);
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", ACCENT);
        UIManager.put("TextArea.background", DEEP);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("ComboBox.background", SURFACE);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.selectionBackground", new Color(95, 38, 63));
        UIManager.put("ComboBox.selectionForeground", TEXT);
        UIManager.put("List.background", SURFACE);
        UIManager.put("List.foreground", TEXT);
        UIManager.put("List.selectionBackground", new Color(95, 38, 63));
        UIManager.put("List.selectionForeground", TEXT);
        UIManager.put("MenuBar.background", SIDEBAR);
        UIManager.put("MenuBar.foreground", TEXT);
        UIManager.put("Menu.background", SIDEBAR);
        UIManager.put("Menu.foreground", TEXT);
        UIManager.put("MenuItem.background", SIDEBAR);
        UIManager.put("MenuItem.foreground", TEXT);
        UIManager.put("PopupMenu.background", SIDEBAR);
        UIManager.put("OptionPane.background", PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("ScrollPane.background", DEEP);
        UIManager.put("Viewport.background", DEEP);
        UIManager.put("ScrollBar.background", PANEL);
        UIManager.put("ProgressBar.background", SURFACE);
        UIManager.put("ProgressBar.foreground", ACCENT);
    }

    public static void styleTextField(JTextField field) {
        field.setFont(BODY);
        field.setBackground(SURFACE);
        field.setForeground(TEXT);
        field.setCaretColor(ACCENT);
        field.setSelectionColor(new Color(101, 42, 70));
        field.setSelectedTextColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(11, 14, 11, 14)));
        field.setPreferredSize(new java.awt.Dimension(180, 44));
    }

    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(BODY);
        comboBox.setBackground(SURFACE);
        comboBox.setForeground(TEXT);
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        comboBox.setFocusable(true);
        comboBox.setPreferredSize(new java.awt.Dimension(180, 44));
        comboBox.setMaximumRowCount(9);
        // Avoid the OS/Nimbus silver gradient on closed dropdowns.
        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new JButton("▾");
                arrow.setFont(new Font("SansSerif", Font.BOLD, 17));
                arrow.setForeground(MUTED);
                arrow.setBackground(SURFACE);
                arrow.setFocusPainted(false);
                arrow.setContentAreaFilled(false);
                arrow.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER));
                return arrow;
            }

            @Override
            public void paintCurrentValueBackground(Graphics graphics,
                    Rectangle bounds, boolean focused) {
                graphics.setColor(SURFACE);
                graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });
    }

    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setOpaque(false);
        scrollPane.setBackground(DEEP);
        scrollPane.getViewport().setBackground(DEEP);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);
        for (JScrollBar scrollBar : new JScrollBar[]{
                scrollPane.getVerticalScrollBar(), scrollPane.getHorizontalScrollBar()}) {
            scrollBar.setUI(new BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    thumbColor = BORDER;
                    trackColor = PANEL;
                }

                @Override
                protected JButton createDecreaseButton(int orientation) {
                    return invisibleScrollButton();
                }

                @Override
                protected JButton createIncreaseButton(int orientation) {
                    return invisibleScrollButton();
                }

                private JButton invisibleScrollButton() {
                    JButton button = new JButton();
                    button.setPreferredSize(new java.awt.Dimension(0, 0));
                    button.setMinimumSize(new java.awt.Dimension(0, 0));
                    button.setMaximumSize(new java.awt.Dimension(0, 0));
                    return button;
                }

                @Override
                protected void paintThumb(Graphics graphics, JComponent component,
                        Rectangle thumbBounds) {
                    if (thumbBounds.isEmpty()) {
                        return;
                    }
                    Graphics2D canvas = (Graphics2D) graphics.create();
                    canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    canvas.setColor(BORDER);
                    canvas.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2,
                            Math.max(2, thumbBounds.width - 4),
                            Math.max(2, thumbBounds.height - 4), 9, 9);
                    canvas.dispose();
                }
            });
        }
    }

    public static void styleProgress(JProgressBar progressBar) {
        progressBar.setBackground(SURFACE);
        progressBar.setForeground(ACCENT);
        progressBar.setBorderPainted(false);
        progressBar.setFont(LABEL);
        progressBar.setPreferredSize(new java.awt.Dimension(210, 30));
        progressBar.setUI(new BasicProgressBarUI() {
            @Override
            protected void paintDeterminate(Graphics graphics, JComponent component) {
                Graphics2D canvas = (Graphics2D) graphics.create();
                canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int width = progressBar.getWidth();
                int height = progressBar.getHeight();
                canvas.setColor(SURFACE);
                canvas.fillRoundRect(0, 0, width, height, 11, 11);
                int filledWidth = (int) Math.round(width * progressBar.getPercentComplete());
                if (filledWidth > 0) {
                    canvas.setColor(ACCENT);
                    canvas.fillRoundRect(0, 0, Math.min(width, filledWidth), height, 11, 11);
                }
                if (progressBar.isStringPainted()) {
                    canvas.setFont(LABEL);
                    canvas.setColor(TEXT);
                    String label = progressBar.getString();
                    FontMetrics metrics = canvas.getFontMetrics();
                    int textX = Math.max(4, (width - metrics.stringWidth(label)) / 2);
                    int textY = (height - metrics.getHeight()) / 2 + metrics.getAscent();
                    canvas.drawString(label, textX, textY);
                }
                canvas.dispose();
            }
        });
    }

    /**
     * Readable report surface for the Manual, Auto Builder, and Analysis pages.
     * Keeps the original plain text so copy/paste and feature logic stay unchanged,
     * but highlights report headings and key/value labels inside each panel.
     */
    public static class ReportPane extends javax.swing.JTextPane {
        @Override
        public void setText(String text) {
            super.setText(text == null ? "" : text);
            javax.swing.text.StyledDocument document = getStyledDocument();
            String contents = getText();

            javax.swing.text.SimpleAttributeSet bodyStyle = new javax.swing.text.SimpleAttributeSet();
            javax.swing.text.StyleConstants.setFontFamily(bodyStyle, "SansSerif");
            javax.swing.text.StyleConstants.setFontSize(bodyStyle, REPORT.getSize());
            javax.swing.text.StyleConstants.setForeground(bodyStyle, TEXT);
            document.setCharacterAttributes(0, document.getLength(), bodyStyle, true);

            javax.swing.text.SimpleAttributeSet spacing = new javax.swing.text.SimpleAttributeSet();
            javax.swing.text.StyleConstants.setLineSpacing(spacing, 0.20f);
            javax.swing.text.StyleConstants.setSpaceBelow(spacing, 3f);
            document.setParagraphAttributes(0, document.getLength(), spacing, false);

            int lineStart = 0;
            String[] lines = contents.split("\n", -1);
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    boolean isHeading = trimmedLine.length() >= 5
                            && trimmedLine.length() <= 60
                            && trimmedLine.equals(trimmedLine.toUpperCase(java.util.Locale.ROOT))
                            && trimmedLine.matches(".*[A-Z].*")
                            && !trimmedLine.contains(":");

                    if (isHeading) {
                        javax.swing.text.SimpleAttributeSet heading = new javax.swing.text.SimpleAttributeSet();
                        javax.swing.text.StyleConstants.setFontFamily(heading, "SansSerif");
                        javax.swing.text.StyleConstants.setFontSize(heading, REPORT_HEADING.getSize());
                        javax.swing.text.StyleConstants.setBold(heading, true);
                        javax.swing.text.StyleConstants.setForeground(heading, ACCENT_HOVER);
                        document.setCharacterAttributes(lineStart, line.length(), heading, false);

                        javax.swing.text.SimpleAttributeSet headingSpacing = new javax.swing.text.SimpleAttributeSet();
                        javax.swing.text.StyleConstants.setSpaceAbove(headingSpacing, 2f);
                        javax.swing.text.StyleConstants.setSpaceBelow(headingSpacing, 8f);
                        document.setParagraphAttributes(lineStart, line.length(), headingSpacing, false);
                    } else {
                        int colon = line.indexOf(':');
                        // Emphasize field labels without changing the stored report text.
                        if (colon > 0 && colon <= 34 && !trimmedLine.startsWith("http")) {
                            javax.swing.text.SimpleAttributeSet keyStyle = new javax.swing.text.SimpleAttributeSet();
                            javax.swing.text.StyleConstants.setBold(keyStyle, true);
                            javax.swing.text.StyleConstants.setForeground(keyStyle, PURPLE);
                            document.setCharacterAttributes(lineStart, colon + 1, keyStyle, false);
                        }
                        if (trimmedLine.startsWith("IMPORTANT") || trimmedLine.startsWith("Note:")) {
                            javax.swing.text.SimpleAttributeSet caution = new javax.swing.text.SimpleAttributeSet();
                            javax.swing.text.StyleConstants.setForeground(caution, WARNING);
                            document.setCharacterAttributes(lineStart, line.length(), caution, false);
                        }
                    }
                }
                lineStart += line.length() + 1;
            }
        }
    }

    /** A panel with a rounded painted surface and subtle border. */
    public static class RoundedPanel extends JPanel {
        public RoundedPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBackground(PANEL);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D canvas = (Graphics2D) graphics.create();
            canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            canvas.setColor(getBackground());
            canvas.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            canvas.setColor(BORDER);
            canvas.setStroke(new BasicStroke(1.0f));
            canvas.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            canvas.dispose();
            super.paintComponent(graphics);
        }
    }

    /** Rounded, hoverable button using the standard Swing action mechanisms. */
    public static class GXButton extends JButton {
        private final boolean primary;
        private boolean hovered;

        public GXButton(String caption, boolean primary) {
            super(caption);
            this.primary = primary;
            setFont(LABEL);
            setForeground(primary ? Color.WHITE : TEXT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(12, 18, 12, 18));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D canvas = (Graphics2D) graphics.create();
            canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = primary ? (hovered ? ACCENT_HOVER : ACCENT)
                    : (hovered ? new Color(59, 51, 67) : SURFACE);
            if (!isEnabled()) {
                fill = SURFACE;
            }
            canvas.setColor(fill);
            canvas.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 13, 13);
            if (!primary) {
                canvas.setColor(BORDER);
                canvas.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 13, 13);
            }
            canvas.dispose();
            super.paintComponent(graphics);
        }
    }

    /** Dedicated sidebar item; shows the selected page without altering application state. */
    public static class NavigationButton extends JButton {
        private boolean active;
        private boolean hovered;

        public NavigationButton(String text) {
            super(text);
            setFont(new Font("SansSerif", Font.BOLD, 15));
            setForeground(MUTED);
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(17, 16, 17, 10));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 56));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    hovered = false;
                    repaint();
                }
            });
        }

        public void setActive(boolean selected) {
            active = selected;
            setForeground(active ? TEXT : MUTED);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D canvas = (Graphics2D) graphics.create();
            canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active || hovered) {
                canvas.setColor(active ? new Color(61, 32, 50) : SURFACE);
                canvas.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 13, 13);
            }
            if (active) {
                canvas.setColor(ACCENT);
                canvas.fillRoundRect(2, 10, 4, getHeight() - 20, 5, 5);
            }
            canvas.dispose();
            super.paintComponent(graphics);
        }
    }
}
