import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * ECPC desktop interface. The existing feature classes do all recommendation,
 * compatibility, analysis, and file-management work; this class handles UI events.
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame {

    private static final Color BACKGROUND_COLOR = new Color(245, 247, 251);
    private static final Color PANEL_COLOR = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(36, 79, 143);
    private static final Color TEXT_COLOR = new Color(33, 45, 63);
    private static final Color MUTED_COLOR = new Color(98, 112, 129);
    private static final Color SUCCESS_COLOR = new Color(25, 126, 86);
    private static final Color ALERT_COLOR = new Color(181, 79, 46);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 23);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font REGULAR_FONT = new Font("SansSerif", Font.PLAIN, 13);

    private final PartCatalog catalog;
    private final CompatibilityChecker compatibilityChecker;
    private final BuildRecommender buildRecommender;
    private final BuildFileManager buildFileManager;
    private final Map<Class<? extends Component>, JComboBox<Component>> componentSelectors;
    private final String databaseName;

    private PCBuild currentBuild;
    private File currentSaveFile;
    private boolean updatingSelectors;

    private final JTextField buildNameField = new JTextField("Untitled Build", 23);
    private final JTextField minimumBudgetField = new JTextField("0", 10);
    private final JTextField maximumBudgetField = new JTextField("120000", 10);
    private final JComboBox<Workload> workloadSelector =
            new JComboBox<>(Workload.values());
    private final JComboBox<String> brandSelector =
            new JComboBox<>(new String[]{"Any brand", "AMD", "Intel"});

    private final JLabel totalPriceLabel = new JLabel();
    private final JLabel completionLabel = new JLabel();
    private final JLabel powerLabel = new JLabel();
    private final JLabel compatibilityLabel = new JLabel();
    private final JLabel budgetLabel = new JLabel();
    private final JLabel selectionDetailsLabel = new JLabel("Select a component to inspect its specifications.");
    private final JLabel footerLabel = new JLabel();
    private final JProgressBar budgetProgress = new JProgressBar(0, 100);
    private final JTextArea summaryArea = new JTextArea();
    private final JTextArea analysisArea = new JTextArea();
    private final JTextArea recommendationArea = new JTextArea();
    private final JTextArea selectionDetailsArea = new JTextArea();
    private final JTabbedPane mainTabs = new JTabbedPane();

    public MainFrame(PartCatalog catalog, String databaseName) {
        if (catalog == null) {
            throw new InvalidInputException("Part catalog must not be null.");
        }
        this.catalog = catalog;
        this.databaseName = databaseName;
        this.compatibilityChecker = new CompatibilityChecker();
        this.buildRecommender = new BuildRecommender(catalog);
        this.buildFileManager = new BuildFileManager();
        this.componentSelectors = new LinkedHashMap<>();
        this.currentBuild = new PCBuild();

        setTitle("ECPC | Efficient Custom PC Configurator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(950, 690));
        setSize(1220, 820);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);
        getContentPane().setLayout(new BorderLayout(0, 0));

        setJMenuBar(createMenuBar());
        getContentPane().add(createHeader(), BorderLayout.NORTH);
        getContentPane().add(createMainContent(), BorderLayout.CENTER);
        getContentPane().add(createFooter(), BorderLayout.SOUTH);

        buildNameField.addActionListener(event -> applyBuildName());
        buildNameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent event) {
                applyBuildName();
            }
        });
        maximumBudgetField.addActionListener(event -> refreshOverview());
        updateSelectorsFromBuild();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New Build");
        JMenuItem openItem = new JMenuItem("Open Build...");
        JMenuItem saveItem = new JMenuItem("Save Build");
        JMenuItem saveAsItem = new JMenuItem("Save Build As...");
        JMenuItem exitItem = new JMenuItem("Exit");

        newItem.addActionListener(event -> newBuild());
        openItem.addActionListener(event -> openBuild());
        saveItem.addActionListener(event -> saveBuild(false));
        saveAsItem.addActionListener(event -> saveBuild(true));
        exitItem.addActionListener(event -> dispose());

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        return menuBar;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setBackground(PANEL_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(20, 25, 18, 25));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("ECPC  /  PC BUILD STUDIO");
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY_COLOR);
        JLabel subtitle = new JLabel("Select components, verify compatibility, and save your configuration.");
        subtitle.setFont(REGULAR_FONT);
        subtitle.setForeground(MUTED_COLOR);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.CENTER);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        headerActions.setOpaque(false);
        JButton newButton = styledButton("New Build", false);
        JButton openButton = styledButton("Open", false);
        JButton saveButton = styledButton("Save", true);
        newButton.addActionListener(event -> newBuild());
        openButton.addActionListener(event -> openBuild());
        saveButton.addActionListener(event -> saveBuild(false));
        headerActions.add(newButton);
        headerActions.add(openButton);
        headerActions.add(saveButton);
        header.add(headerActions, BorderLayout.EAST);
        return header;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(BorderFactory.createEmptyBorder(14, 22, 10, 22));
        content.setBackground(BACKGROUND_COLOR);
        content.add(createOverview(), BorderLayout.NORTH);
        mainTabs.addTab("Manual Build", createManualBuildTab());
        mainTabs.addTab("Auto Builder", createAutoBuildTab());
        mainTabs.addTab("Analysis", createAnalysisTab());
        mainTabs.setFont(REGULAR_FONT);
        content.add(mainTabs, BorderLayout.CENTER);
        return content;
    }

    private JPanel createOverview() {
        JPanel overview = new JPanel(new java.awt.GridLayout(1, 4, 12, 0));
        overview.setOpaque(false);
        overview.add(metricCard("TOTAL BUILD PRICE", totalPriceLabel));
        overview.add(metricCard("PARTS SELECTED", completionLabel));
        overview.add(metricCard("ESTIMATED POWER", powerLabel));
        overview.add(metricCard("COMPATIBILITY", compatibilityLabel));
        return overview;
    }

    private JPanel metricCard(String heading, JLabel value) {
        JPanel metric = new JPanel();
        metric.setLayout(new BoxLayout(metric, BoxLayout.Y_AXIS));
        metric.setBackground(PANEL_COLOR);
        metric.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 231, 240)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        headingLabel.setForeground(MUTED_COLOR);
        value.setFont(new Font("SansSerif", Font.BOLD, 19));
        value.setForeground(TEXT_COLOR);
        metric.add(headingLabel);
        metric.add(Box.createVerticalStrut(6));
        metric.add(value);
        return metric;
    }

    private JPanel createManualBuildTab() {
        JPanel workspace = new JPanel(new BorderLayout(13, 0));
        workspace.setBackground(BACKGROUND_COLOR);
        workspace.setBorder(BorderFactory.createEmptyBorder(10, 0, 3, 0));

        JPanel selectionPanel = new JPanel(new GridBagLayout());
        selectionPanel.setBackground(PANEL_COLOR);
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.insets = new Insets(3, 2, 11, 2);

        JLabel panelTitle = sectionLabel("Choose your components");
        selectionPanel.add(panelTitle, constraints);
        constraints.gridy++;
        selectionPanel.add(labelAndField("Build name", buildNameField), constraints);

        addSelector(selectionPanel, constraints, "Processor (CPU)", CPU.class);
        addSelector(selectionPanel, constraints, "Motherboard", Motherboard.class);
        addSelector(selectionPanel, constraints, "Graphics Card (GPU)", GPU.class);
        addSelector(selectionPanel, constraints, "Memory (RAM)", RAM.class);
        addSelector(selectionPanel, constraints, "Storage", Storage.class);
        addSelector(selectionPanel, constraints, "Power Supply (PSU)", PowerSupply.class);
        addSelector(selectionPanel, constraints, "PC Case", PCCase.class);

        JPanel selectionButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        selectionButtons.setOpaque(false);
        JButton checkButton = styledButton("Check Compatibility", true);
        JButton clearButton = styledButton("Clear Selections", false);
        checkButton.addActionListener(event -> runCompatibilityCheck());
        clearButton.addActionListener(event -> clearSelections());
        selectionButtons.add(checkButton);
        selectionButtons.add(clearButton);
        constraints.gridy++;
        constraints.insets = new Insets(14, 0, 4, 0);
        selectionPanel.add(selectionButtons, constraints);
        constraints.gridy++;
        constraints.weighty = 1;
        selectionPanel.add(Box.createVerticalGlue(), constraints);

        JScrollPane leftScroll = new JScrollPane(selectionPanel);
        leftScroll.setBorder(BorderFactory.createLineBorder(new Color(224, 231, 240)));
        leftScroll.getVerticalScrollBar().setUnitIncrement(16);
        leftScroll.setPreferredSize(new Dimension(600, 500));

        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setOpaque(false);

        JPanel summaryPanel = cardPanel(new BorderLayout(0, 10));
        summaryPanel.add(sectionLabel("Current build summary"), BorderLayout.NORTH);
        configureTextArea(summaryArea);
        summaryPanel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);
        summaryPanel.setPreferredSize(new Dimension(360, 310));

        JPanel detailsPanel = cardPanel(new BorderLayout(0, 10));
        JPanel detailHeading = new JPanel(new BorderLayout());
        detailHeading.setOpaque(false);
        detailHeading.add(sectionLabel("Selected component details"), BorderLayout.NORTH);
        selectionDetailsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        selectionDetailsLabel.setForeground(PRIMARY_COLOR);
        detailHeading.add(selectionDetailsLabel, BorderLayout.SOUTH);
        detailsPanel.add(detailHeading, BorderLayout.NORTH);
        configureTextArea(selectionDetailsArea);
        detailsPanel.add(new JScrollPane(selectionDetailsArea), BorderLayout.CENTER);
        detailsPanel.setPreferredSize(new Dimension(360, 175));

        rightPanel.add(summaryPanel, BorderLayout.CENTER);
        rightPanel.add(detailsPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightPanel);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        workspace.add(splitPane, BorderLayout.CENTER);
        return workspace;
    }

    private <T extends Component> void addSelector(JPanel parent,
            GridBagConstraints constraints, String label, Class<T> componentType) {
        DefaultComboBoxModel<Component> selectionModel = new DefaultComboBoxModel<>();
        selectionModel.addElement(null); // Empty component slot
        for (T component : catalog.getPartsByType(componentType)) {
            selectionModel.addElement(component);
        }

        JComboBox<Component> selector = new JComboBox<>(selectionModel);
        selector.setFont(REGULAR_FONT);
        selector.setRenderer(new ComponentRenderer());
        selector.setPreferredSize(new Dimension(470, 33));
        selector.addActionListener(event -> {
            if (updatingSelectors) {
                return;
            }
            Component selectedComponent = (Component) selector.getSelectedItem();
            if (selectedComponent == null) {
                currentBuild.removeComponent(componentType);
            } else {
                currentBuild.addComponent(selectedComponent);
            }
            showComponentDetails(selectedComponent);
            invalidateReports();
            refreshOverview();
        });
        componentSelectors.put(componentType, selector);
        constraints.gridy++;
        constraints.insets = new Insets(2, 2, 7, 2);
        parent.add(labelAndField(label, selector), constraints);
    }

    private JPanel createAutoBuildTab() {
        JPanel workspace = new JPanel(new BorderLayout(12, 0));
        workspace.setBorder(BorderFactory.createEmptyBorder(10, 0, 3, 0));
        workspace.setBackground(BACKGROUND_COLOR);

        JPanel controls = cardPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(4, 0, 15, 0);
        controls.add(sectionLabel("Build preferences"), constraints);
        constraints.gridy++;
        controls.add(labelAndField("Minimum budget (PHP)", minimumBudgetField), constraints);
        constraints.gridy++;
        controls.add(labelAndField("Maximum budget (PHP)", maximumBudgetField), constraints);
        constraints.gridy++;
        controls.add(labelAndField("Primary workload", workloadSelector), constraints);
        constraints.gridy++;
        controls.add(labelAndField("Preferred brand (soft preference)", brandSelector), constraints);

        JButton generateButton = styledButton("Generate PC Build", true);
        generateButton.addActionListener(event -> generateAutomaticBuild());
        constraints.gridy++;
        constraints.insets = new Insets(10, 0, 12, 0);
        controls.add(generateButton, constraints);
        constraints.gridy++;
        budgetProgress.setStringPainted(true);
        controls.add(budgetProgress, constraints);
        constraints.gridy++;
        budgetLabel.setFont(REGULAR_FONT);
        controls.add(budgetLabel, constraints);
        constraints.gridy++;
        constraints.weighty = 1;
        controls.add(Box.createVerticalGlue(), constraints);
        controls.setPreferredSize(new Dimension(390, 300));

        JPanel reportPanel = cardPanel(new BorderLayout(0, 12));
        reportPanel.add(sectionLabel("Recommendation result"), BorderLayout.NORTH);
        configureTextArea(recommendationArea);
        recommendationArea.setText("Choose a workload and budget, then select Generate PC Build.\n\n"
                + "The recommendation algorithm is implemented in BuildRecommender.java.\n"
                + "If no combination satisfies its limits, ECPC will explain the problem.");
        reportPanel.add(new JScrollPane(recommendationArea), BorderLayout.CENTER);

        workspace.add(controls, BorderLayout.WEST);
        workspace.add(reportPanel, BorderLayout.CENTER);
        return workspace;
    }

    private JPanel createAnalysisTab() {
        JPanel workspace = new JPanel(new BorderLayout(0, 12));
        workspace.setBackground(BACKGROUND_COLOR);
        workspace.setBorder(BorderFactory.createEmptyBorder(10, 0, 3, 0));

        JPanel actions = cardPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        JButton compatibilityButton = styledButton("Run Compatibility Check", true);
        JButton balanceButton = styledButton("Estimate CPU-GPU Balance", false);
        JButton copyButton = styledButton("Copy Report", false);
        compatibilityButton.addActionListener(event -> runCompatibilityCheck());
        balanceButton.addActionListener(event -> analyzeBottleneck());
        copyButton.addActionListener(event -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(analysisArea.getText()), null);
            setFooterMessage("Report copied to clipboard.");
        });
        actions.add(compatibilityButton);
        actions.add(balanceButton);
        actions.add(copyButton);
        workspace.add(actions, BorderLayout.NORTH);

        JPanel reportPanel = cardPanel(new BorderLayout(0, 12));
        reportPanel.add(sectionLabel("Build analysis"), BorderLayout.NORTH);
        configureTextArea(analysisArea);
        analysisArea.setText("Results will appear here.\n\n"
                + "Compatibility checks use CompatibilityChecker.java.\n"
                + "CPU-GPU balance uses BottleneckAnalyzer.java.\n\n"
                + "Important: independently normalized CPU/GPU benchmark scores are a\n"
                + "relative ECPC heuristic, not a measured gaming bottleneck percentage.");
        reportPanel.add(new JScrollPane(analysisArea), BorderLayout.CENTER);
        workspace.add(reportPanel, BorderLayout.CENTER);
        return workspace;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setBackground(PANEL_COLOR);
        footer.setBorder(BorderFactory.createEmptyBorder(9, 22, 9, 22));
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footerLabel.setForeground(MUTED_COLOR);
        JLabel databaseLabel = new JLabel(catalog.size() + " parts  /  " + databaseName);
        databaseLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        databaseLabel.setForeground(MUTED_COLOR);
        footer.add(footerLabel, BorderLayout.WEST);
        footer.add(databaseLabel, BorderLayout.EAST);
        return footer;
    }

    private JPanel labelAndField(String label, JComponent field) {
        JPanel layout = new JPanel(new BorderLayout(0, 4));
        layout.setOpaque(false);
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        fieldLabel.setForeground(MUTED_COLOR);
        layout.add(fieldLabel, BorderLayout.NORTH);
        layout.add(field, BorderLayout.CENTER);
        return layout;
    }

    private JPanel cardPanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 231, 240)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        return panel;
    }

    private JLabel sectionLabel(String heading) {
        JLabel label = new JLabel(heading);
        label.setFont(SECTION_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JButton styledButton(String label, boolean primary) {
        JButton button = new JButton(label);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? PRIMARY_COLOR : new Color(210, 219, 230)),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        button.setForeground(primary ? Color.WHITE : PRIMARY_COLOR);
        button.setBackground(primary ? PRIMARY_COLOR : Color.WHITE);
        button.setOpaque(true);
        return button;
    }

    private void configureTextArea(JTextArea textArea) {
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 12, 10, 12));
        textArea.setBackground(new Color(250, 251, 253));
        textArea.setForeground(TEXT_COLOR);
        textArea.setBorder(BorderFactory.createEmptyBorder());
    }

    private void applyBuildName() {
        try {
            currentBuild.setBuildName(buildNameField.getText());
            buildNameField.setText(currentBuild.getBuildName());
            refreshOverview();
        } catch (InvalidInputException exception) {
            buildNameField.setText(currentBuild.getBuildName());
            showError("Invalid Build Name", exception.getMessage());
        }
    }

    private void updateSelectorsFromBuild() {
        updatingSelectors = true;
        try {
            buildNameField.setText(currentBuild.getBuildName());
            selectComponent(CPU.class, currentBuild.getCpu());
            selectComponent(Motherboard.class, currentBuild.getMotherboard());
            selectComponent(GPU.class, currentBuild.getGpu());
            selectComponent(RAM.class, currentBuild.getRam());
            selectComponent(Storage.class, currentBuild.getStorage());
            selectComponent(PowerSupply.class, currentBuild.getPowerSupply());
            selectComponent(PCCase.class, currentBuild.getPcCase());
        } finally {
            updatingSelectors = false;
        }
        showComponentDetails(null);
        invalidateReports();
        refreshOverview();
    }

    private void selectComponent(Class<? extends Component> componentType, Component selectedComponent) {
        JComboBox<Component> selector = componentSelectors.get(componentType);
        if (selector != null) {
            selector.setSelectedItem(selectedComponent);
        }
    }

    private void refreshOverview() {
        totalPriceLabel.setText(money(currentBuild.getTotalPrice()));
        completionLabel.setText(currentBuild.getSelectedParts().size() + " / 7 selected");
        powerLabel.setText(currentBuild.getEstimatedWattage() + " W est.");

        List<String> missingComponents = currentBuild.getMissingComponents();
        if (!missingComponents.isEmpty()) {
            compatibilityLabel.setText("Incomplete");
            compatibilityLabel.setForeground(ALERT_COLOR);
        } else if (compatibilityChecker.checkAllParts(currentBuild)) {
            compatibilityLabel.setText("Checks passed");
            compatibilityLabel.setForeground(SUCCESS_COLOR);
        } else {
            compatibilityLabel.setText("Review needed");
            compatibilityLabel.setForeground(ALERT_COLOR);
        }

        summaryArea.setText(currentBuild.displaySummary());
        summaryArea.setCaretPosition(0);

        try {
            double maximumBudget = parseBudget(maximumBudgetField.getText(), "Maximum budget");
            if (maximumBudget <= 0) {
                throw new InvalidInputException("Maximum budget must be greater than zero.");
            }
            double ratio = currentBuild.getTotalPrice() / maximumBudget;
            budgetProgress.setValue((int) Math.min(100, Math.round(ratio * 100)));
            budgetProgress.setString(String.format(Locale.US, "%.1f%% of maximum budget", ratio * 100.0));
            budgetLabel.setText("Remaining: " + money(maximumBudget - currentBuild.getTotalPrice()));
            budgetLabel.setForeground(ratio > 1.0 ? ALERT_COLOR : MUTED_COLOR);
        } catch (InvalidInputException exception) {
            budgetProgress.setValue(0);
            budgetProgress.setString("Enter a valid maximum budget");
            budgetLabel.setText("Budget unavailable");
            budgetLabel.setForeground(ALERT_COLOR);
        }
        setFooterMessage("Edit your build or select an analysis action.");
    }

    private void showComponentDetails(Component selectedComponent) {
        if (selectedComponent == null) {
            selectionDetailsLabel.setText("Component details");
            selectionDetailsArea.setText("Select a component to see its ID, brand, price, and specifications.");
        } else {
            selectionDetailsLabel.setText(selectedComponent.getName());
            selectionDetailsArea.setText("ID: " + selectedComponent.getId()
                    + "\nBrand: " + selectedComponent.getBrand()
                    + "\nPrice: " + money(selectedComponent.getPrice())
                    + "\n\n" + selectedComponent.getSpecs());
        }
        selectionDetailsArea.setCaretPosition(0);
    }

    private void invalidateReports() {
        analysisArea.setText("Build changed. Run a new compatibility or balance check.\n\n"
                + "Results from previous selections are no longer displayed.");
        recommendationArea.setText("The current build has been updated.\n\n"
                + "Use Generate PC Build to produce a new automatic recommendation.");
    }

    private void clearSelections() {
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Clear all selected components from this build?",
                "Clear Selections", JOptionPane.YES_NO_OPTION);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        currentBuild.clearBuild();
        currentSaveFile = null;
        updateSelectorsFromBuild();
        setFooterMessage("Component selections cleared.");
    }

    private void newBuild() {
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Start a new build? Unsaved changes will be lost.",
                "New Build", JOptionPane.YES_NO_OPTION);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        currentBuild = new PCBuild();
        currentSaveFile = null;
        updateSelectorsFromBuild();
        mainTabs.setSelectedIndex(0);
        setFooterMessage("Started a new build.");
    }

    private void generateAutomaticBuild() {
        try {
            double minimumBudget = parseBudget(minimumBudgetField.getText(), "Minimum budget");
            double maximumBudget = parseBudget(maximumBudgetField.getText(), "Maximum budget");
            if (minimumBudget < 0 || maximumBudget <= 0 || minimumBudget > maximumBudget) {
                throw new InvalidInputException("Enter a valid budget range (minimum <= maximum, maximum > 0).");
            }
            Workload workload =
                    (Workload) workloadSelector.getSelectedItem();
            String selectedBrand = (String) brandSelector.getSelectedItem();
            String preferredBrand = "Any brand".equals(selectedBrand) ? "" : selectedBrand;
            PCBuild recommendation = buildRecommender.generateBuild(
                    minimumBudget, maximumBudget, workload, preferredBrand);
            recommendation.setBuildName("ECPC " + readableWorkload(workload) + " Build");
            currentBuild = recommendation;
            currentSaveFile = null;
            updateSelectorsFromBuild();

            recommendationArea.setText("AUTOMATIC BUILD GENERATED\n"
                    + "Workload: " + readableWorkload(workload) + "\n"
                    + "Preferred brand: " + (preferredBrand.isEmpty() ? "Any" : preferredBrand) + "\n"
                    + "Budget range: " + money(minimumBudget) + " to " + money(maximumBudget)
                    + "\n\n" + currentBuild.displaySummary() + "\n\n"
                    + compatibilityChecker.getCompatibilityReport(currentBuild));
            recommendationArea.setCaretPosition(0);
            setFooterMessage("Automatic build generated. Review its selections in Manual Build.");
        } catch (InvalidInputException | IllegalArgumentException | IllegalStateException exception) {
            recommendationArea.setText("NO BUILD GENERATED\n\n" + exception.getMessage()
                    + "\n\nTry increasing the maximum budget or adjusting the workload. "
                    + "The existing BuildRecommender controls component allocation.");
            recommendationArea.setCaretPosition(0);
            showError("Auto Builder", exception.getMessage());
        }
    }

    private String readableWorkload(Workload workload) {
        if (workload == null) {
            return "General";
        }
        switch (workload) {
            case GAMING:
                return "Gaming";
            case VIDEO_EDITING:
                return "Video Editing";
            case SCHOOLWORK:
            default:
                return "Schoolwork";
        }
    }

    private void runCompatibilityCheck() {
        mainTabs.setSelectedIndex(2);
        try {
            currentBuild.validateCompleteBuild();
            boolean passed = compatibilityChecker.checkAllParts(currentBuild);
            analysisArea.setText("COMPATIBILITY REPORT\n\n"
                    + compatibilityChecker.getCompatibilityReport(currentBuild)
                    + "\n\n" + (passed ? "No problems detected by the implemented rules."
                    : "Review the selected parts in Manual Build.")
                    + "\n\nNote: the checks do not cover every possible BIOS, connector, "
                    + "or physical clearance requirement.");
            setFooterMessage(passed ? "Compatibility checks passed." : "Compatibility needs review.");
        } catch (IncompleteBuildException exception) {
            analysisArea.setText("BUILD INCOMPLETE\n\n" + exception.getMessage()
                    + "\n\nSelect the missing components before running a full check.");
            setFooterMessage("Add missing components to finish compatibility checking.");
        }
        analysisArea.setCaretPosition(0);
    }

    private void analyzeBottleneck() {
        mainTabs.setSelectedIndex(2);
        if (currentBuild.getCpu() == null || currentBuild.getGpu() == null) {
            analysisArea.setText("CPU-GPU BALANCE ANALYSIS\n\n"
                    + "Select both a CPU and a GPU before running this analysis.");
            return;
        }
        Workload workload =
                (Workload) workloadSelector.getSelectedItem();
        BottleneckAnalyzer analyzer = new BottleneckAnalyzer(
                currentBuild.getCpu().getPerformanceScore(),
                currentBuild.getGpu().getPerformanceScore(), readableWorkload(workload));
        analysisArea.setText("CPU-GPU BALANCE ESTIMATE\n\n"
                + "CPU: " + currentBuild.getCpu().getName() + "\n"
                + "GPU: " + currentBuild.getGpu().getName() + "\n\n"
                + analyzer.getBottleneckReport() + "\n\n"
                + "IMPORTANT: These results compare independently normalized benchmark scores. "
                + "They are an ECPC heuristic, not a measured real-world bottleneck percentage. "
                + "Performance can vary by application, resolution, and settings.");
        analysisArea.setCaretPosition(0);
        setFooterMessage("CPU-GPU balance estimate generated.");
    }

    private void saveBuild(boolean saveAs) {
        try {
            currentBuild.setBuildName(buildNameField.getText());
            File selectedFile = currentSaveFile;
            if (saveAs || selectedFile == null) {
                JFileChooser fileChooser = buildFileChooser();
                fileChooser.setDialogTitle("Save ECPC build");
                if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                selectedFile = ensureEcpcExtension(fileChooser.getSelectedFile());
                if (selectedFile.exists()) {
                    int confirmation = JOptionPane.showConfirmDialog(this,
                            "Replace existing file " + selectedFile.getName() + "?",
                            "Confirm Overwrite", JOptionPane.YES_NO_OPTION);
                    if (confirmation != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
            }
            buildFileManager.saveBuild(currentBuild, selectedFile.getAbsolutePath());
            currentSaveFile = selectedFile;
            setFooterMessage("Build saved to " + selectedFile.getName());
        } catch (IOException | InvalidInputException exception) {
            showError("Save Build Failed", exception.getMessage());
        }
    }

    private void openBuild() {
        JFileChooser fileChooser = buildFileChooser();
        fileChooser.setDialogTitle("Open saved ECPC build");
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = fileChooser.getSelectedFile();
        try {
            PCBuild restoredBuild = buildFileManager.loadBuild(selectedFile.getAbsolutePath(), catalog);
            currentBuild = restoredBuild;
            currentSaveFile = selectedFile;
            updateSelectorsFromBuild();
            mainTabs.setSelectedIndex(0);
            setFooterMessage("Opened " + selectedFile.getName());
        } catch (IOException | InvalidInputException exception) {
            showError("Open Build Failed", exception.getMessage());
        }
    }

    private JFileChooser buildFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("ECPC saved builds (*.ecpc)", "ecpc"));
        return fileChooser;
    }

    private File ensureEcpcExtension(File file) {
        if (file.getName().toLowerCase(Locale.ROOT).endsWith(".ecpc")) {
            return file;
        }
        return new File(file.getParentFile(), file.getName() + ".ecpc");
    }

    private double parseBudget(String input, String label) {
        try {
            String cleanedInput = input.replace(",", "").trim();
            double value = Double.parseDouble(cleanedInput);
            if (!Double.isFinite(value)) {
                throw new NumberFormatException("non-finite budget");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new InvalidInputException(label + " must be a valid number.");
        }
    }

    private String money(double amount) {
        return String.format(Locale.US, "PHP %,.2f", amount);
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        setFooterMessage(title + ": " + message);
    }

    private void setFooterMessage(String message) {
        footerLabel.setText(message);
    }

    /** The dropdown shows the component name, brand, and price rather than an object reference. */
    private class ComponentRenderer extends DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list,
                Object value, int index, boolean selected, boolean hasFocus) {
            super.getListCellRendererComponent(list, value, index, selected, hasFocus);
            if (value instanceof Component) {
                Component component = (Component) value;
                setText(component.getName() + "  |  " + money(component.getPrice()));
                setToolTipText(component.getSpecs());
            } else {
                setText("-- Not selected --");
                setToolTipText(null);
            }
            return this;
        }
    }
}
