import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * ECPC desktop interface. The existing feature classes do all recommendation,
 * compatibility, analysis, and file-management work; this class handles UI events.
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame {

    /** Called from Main before any GUI widgets are constructed. */
    public static void installTheme() {
        Theme.install();
    }

    // Centralized GUI palette; business logic is unchanged.
    private static final Color BACKGROUND_COLOR = Theme.BACKGROUND;
    private static final Color PANEL_COLOR = Theme.PANEL;
    private static final Color PRIMARY_COLOR = Theme.ACCENT;
    private static final Color TEXT_COLOR = Theme.TEXT;
    private static final Color MUTED_COLOR = Theme.MUTED;
    private static final Color SUCCESS_COLOR = Theme.SUCCESS;
    private static final Color ALERT_COLOR = Theme.WARNING;
    private static final Font TITLE_FONT = Theme.PAGE_TITLE;
    private static final Font SECTION_FONT = Theme.SECTION;
    private static final Font REGULAR_FONT = Theme.BODY;

    private final Theme.NavigationButton[] navigationButtons =
            new Theme.NavigationButton[3];

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
    private final Theme.ReportPane summaryArea = new Theme.ReportPane();
    private final Theme.ReportPane analysisArea = new Theme.ReportPane();
    private final Theme.ReportPane recommendationArea = new Theme.ReportPane();
    private final Theme.ReportPane selectionDetailsArea = new Theme.ReportPane();
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

        // Theme input widgets before they are added to the layout.
        Theme.styleTextField(buildNameField);
        Theme.styleTextField(minimumBudgetField);
        Theme.styleTextField(maximumBudgetField);
        Theme.styleComboBox(workloadSelector);
        Theme.styleComboBox(brandSelector);
        Theme.styleProgress(budgetProgress);
        workloadSelector.setRenderer(new DarkComboRenderer());
        brandSelector.setRenderer(new DarkComboRenderer());

        setTitle("ECPC GX | Efficient Custom PC Configurator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 730));
        setSize(1320, 880);
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
        menuBar.setUI(new javax.swing.plaf.basic.BasicMenuBarUI());
        menuBar.setBackground(Theme.SIDEBAR);
        menuBar.setForeground(TEXT_COLOR);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(TEXT_COLOR);
        fileMenu.setBackground(Theme.SIDEBAR);
        fileMenu.setFont(Theme.LABEL);
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
        for (JMenuItem menuItem : new JMenuItem[]{newItem, openItem, saveItem, saveAsItem, exitItem}) {
            menuItem.setBackground(Theme.SIDEBAR);
            menuItem.setForeground(TEXT_COLOR);
            menuItem.setFont(REGULAR_FONT);
        }
        menuBar.add(fileMenu);
        return menuBar;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setBackground(Theme.SIDEBAR);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(21, 26, 19, 26)));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("ECPC  /  GX BUILD STUDIO");
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY_COLOR);
        JLabel subtitle = new JLabel("Select components, verify compatibility, and save your configuration.");
        subtitle.setFont(REGULAR_FONT);
        subtitle.setForeground(MUTED_COLOR);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(7));
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
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND_COLOR);

        JPanel dashboard = new JPanel(new BorderLayout(0, 15));
        dashboard.setBackground(BACKGROUND_COLOR);
        dashboard.setBorder(BorderFactory.createEmptyBorder(19, 22, 15, 22));
        dashboard.add(createOverview(), BorderLayout.NORTH);

        mainTabs.addTab("Manual Build", createManualBuildTab());
        mainTabs.addTab("Auto Builder", createAutoBuildTab());
        mainTabs.addTab("Analysis", createAnalysisTab());
        mainTabs.setBackground(BACKGROUND_COLOR);
        mainTabs.setForeground(TEXT_COLOR);
        mainTabs.setBorder(BorderFactory.createEmptyBorder());
        mainTabs.setFont(REGULAR_FONT);
        // The page buttons in the left navigation replace the default tab strip.
        // Programmatic setSelectedIndex() calls already used by the features still work.
        mainTabs.setUI(new BasicTabbedPaneUI() {
            @Override
            protected int calculateTabAreaHeight(int tabPlacement,
                    int horizontalRunCount, int maximumTabHeight) {
                return 0;
            }

            @Override
            protected void paintTabArea(Graphics graphics, int placement, int selectedIndex) {
                // Navigation is provided by the accessible Swing buttons in the sidebar.
            }

            @Override
            protected Insets getContentBorderInsets(int tabPlacement) {
                return new Insets(0, 0, 0, 0);
            }
        });
        mainTabs.addChangeListener(event -> updateSidebarSelection());
        dashboard.add(mainTabs, BorderLayout.CENTER);

        root.add(createSidebar(), BorderLayout.WEST);
        root.add(dashboard, BorderLayout.CENTER);
        updateSidebarSelection();
        return root;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(235, 500));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER),
                BorderFactory.createEmptyBorder(25, 16, 20, 16)));

        JPanel navigation = new JPanel();
        navigation.setOpaque(false);
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.Y_AXIS));
        JLabel navigationHeading = new JLabel("   WORKSPACE");
        navigationHeading.setFont(Theme.OVERLINE);
        navigationHeading.setForeground(Theme.PURPLE);
        navigationHeading.setAlignmentX(LEFT_ALIGNMENT);
        navigation.add(navigationHeading);
        navigation.add(Box.createVerticalStrut(18));
        String[] names = {"01   Manual Build", "02   Auto Builder", "03   Analysis"};
        for (int index = 0; index < names.length; index++) {
            final int pageIndex = index;
            Theme.NavigationButton navigationButton = new Theme.NavigationButton(names[index]);
            navigationButton.setAlignmentX(LEFT_ALIGNMENT);
            navigationButton.addActionListener(event -> mainTabs.setSelectedIndex(pageIndex));
            navigationButtons[index] = navigationButton;
            navigation.add(navigationButton);
            navigation.add(Box.createVerticalStrut(7));
        }
        sidebar.add(navigation, BorderLayout.NORTH);

        JPanel sidebarFooter = new JPanel();
        sidebarFooter.setLayout(new BoxLayout(sidebarFooter, BoxLayout.Y_AXIS));
        sidebarFooter.setBackground(Theme.SIDEBAR);
        sidebarFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));
        sidebarFooter.add(Box.createVerticalStrut(13));
        JLabel projectLabel = new JLabel("LOCAL BUILDER  /  v1.0");
        projectLabel.setForeground(MUTED_COLOR);
        projectLabel.setFont(Theme.OVERLINE);
        sidebarFooter.add(projectLabel);
        sidebarFooter.add(Box.createVerticalStrut(6));
        JLabel libraryLabel = new JLabel(catalog.size() + " hardware records");
        libraryLabel.setForeground(Theme.PURPLE);
        libraryLabel.setFont(Theme.CAPTION);
        sidebarFooter.add(libraryLabel);
        sidebar.add(sidebarFooter, BorderLayout.SOUTH);
        return sidebar;
    }

    private void updateSidebarSelection() {
        for (int index = 0; index < navigationButtons.length; index++) {
            if (navigationButtons[index] != null) {
                navigationButtons[index].setActive(index == mainTabs.getSelectedIndex());
            }
        }
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
        JPanel metric = new Theme.RoundedPanel(null);
        metric.setLayout(new BoxLayout(metric, BoxLayout.Y_AXIS));
        metric.setBackground(PANEL_COLOR);
        metric.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(Theme.OVERLINE);
        headingLabel.setForeground(MUTED_COLOR);
        value.setFont(Theme.METRIC);
        value.setForeground(TEXT_COLOR);
        metric.add(headingLabel);
        metric.add(Box.createVerticalStrut(9));
        metric.add(value);
        return metric;
    }

    private JPanel createManualBuildTab() {
        JPanel workspace = new JPanel(new BorderLayout(13, 0));
        workspace.setBackground(BACKGROUND_COLOR);
        workspace.setBorder(BorderFactory.createEmptyBorder(10, 0, 3, 0));

        JPanel selectionPanel = new Theme.RoundedPanel(new GridBagLayout());
        selectionPanel.setBackground(PANEL_COLOR);
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(22, 23, 22, 23));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.insets = new Insets(3, 2, 15, 2);

        JLabel panelTitle = sectionLabel("Choose your components");
        selectionPanel.add(panelTitle, constraints);
        constraints.gridy++;
        selectionPanel.add(labelAndField("Build name", buildNameField), constraints);

        addSelector(selectionPanel, constraints, "Processor (CPU)", CPU.class);
        addSelector(selectionPanel, constraints, "Motherboard", Motherboard.class);
        addSelector(selectionPanel, constraints, "Graphics Card (GPU) - Optional with an iGPU CPU", GPU.class);
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
        Theme.styleScrollPane(leftScroll);
        leftScroll.setPreferredSize(new Dimension(600, 500));

        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setOpaque(false);

        JPanel summaryPanel = cardPanel(new BorderLayout(0, 10));
        summaryPanel.add(sectionLabel("Current build summary"), BorderLayout.NORTH);
        configureTextArea(summaryArea);
        summaryPanel.add(styledScroll(summaryArea), BorderLayout.CENTER);
        summaryPanel.setPreferredSize(new Dimension(360, 310));

        JPanel detailsPanel = cardPanel(new BorderLayout(0, 10));
        JPanel detailHeading = new JPanel(new BorderLayout());
        detailHeading.setOpaque(false);
        detailHeading.add(sectionLabel("Selected component details"), BorderLayout.NORTH);
        selectionDetailsLabel.setFont(Theme.CAPTION);
        selectionDetailsLabel.setForeground(PRIMARY_COLOR);
        detailHeading.add(selectionDetailsLabel, BorderLayout.SOUTH);
        detailsPanel.add(detailHeading, BorderLayout.NORTH);
        configureTextArea(selectionDetailsArea);
        detailsPanel.add(styledScroll(selectionDetailsArea), BorderLayout.CENTER);
        detailsPanel.setPreferredSize(new Dimension(360, 208));

        rightPanel.add(summaryPanel, BorderLayout.CENTER);
        rightPanel.add(detailsPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightPanel);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setBackground(BACKGROUND_COLOR);
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
        Theme.styleComboBox(selector);
        selector.setRenderer(new ComponentRenderer());
        selector.setPreferredSize(new Dimension(470, 44));
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
        constraints.insets = new Insets(3, 2, 11, 2);
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
        constraints.insets = new Insets(5, 0, 19, 0);
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
        // Keep the larger text readable even on smaller displays: scroll the
        // preference card instead of allowing GridBagLayout to clip its heading.
        controls.setPreferredSize(new Dimension(425, 700));

        JPanel reportPanel = cardPanel(new BorderLayout(0, 12));
        reportPanel.add(sectionLabel("Recommendation result"), BorderLayout.NORTH);
        configureTextArea(recommendationArea);
        recommendationArea.setText("Choose a workload and budget, then select Generate PC Build.\n\n"
                + "The recommendation algorithm is implemented in BuildRecommender.java.\n"
                + "If no combination satisfies its limits, ECPC will explain the problem.");
        reportPanel.add(styledScroll(recommendationArea), BorderLayout.CENTER);

        JScrollPane preferencesScroll = new JScrollPane(controls);
        Theme.styleScrollPane(preferencesScroll);
        preferencesScroll.setPreferredSize(new Dimension(445, 500));
        preferencesScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        workspace.add(preferencesScroll, BorderLayout.WEST);
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
                + "Important: CPU Mark and G3D Mark use different benchmark scales. The\n"
                + "pairing rating is a broad ECPC heuristic, not a measured bottleneck percentage.");
        reportPanel.add(styledScroll(analysisArea), BorderLayout.CENTER);
        workspace.add(reportPanel, BorderLayout.CENTER);
        return workspace;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setBackground(Theme.SIDEBAR);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(11, 22, 11, 22)));
        footerLabel.setFont(Theme.CAPTION);
        footerLabel.setForeground(MUTED_COLOR);
        JLabel databaseLabel = new JLabel(catalog.size() + " parts  /  " + databaseName);
        databaseLabel.setFont(Theme.CAPTION);
        databaseLabel.setForeground(MUTED_COLOR);
        footer.add(footerLabel, BorderLayout.WEST);
        footer.add(databaseLabel, BorderLayout.EAST);
        return footer;
    }

    private JPanel labelAndField(String label, JComponent field) {
        JPanel layout = new JPanel(new BorderLayout(0, 7));
        layout.setOpaque(false);
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(Theme.LABEL);
        fieldLabel.setForeground(MUTED_COLOR);
        layout.add(fieldLabel, BorderLayout.NORTH);
        layout.add(field, BorderLayout.CENTER);
        return layout;
    }

    private JPanel cardPanel(java.awt.LayoutManager layout) {
        JPanel panel = new Theme.RoundedPanel(layout);
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(22, 23, 22, 23));
        return panel;
    }

    private JLabel sectionLabel(String heading) {
        JLabel label = new JLabel(heading);
        label.setFont(SECTION_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JButton styledButton(String label, boolean primary) {
        return new Theme.GXButton(label, primary);
    }

    private JScrollPane styledScroll(Theme.ReportPane reportPane) {
        JScrollPane scrollPane = new JScrollPane(reportPane);
        Theme.styleScrollPane(scrollPane);
        return scrollPane;
    }

    private void configureTextArea(Theme.ReportPane reportPane) {
        reportPane.setEditable(false);
        reportPane.setFont(Theme.REPORT);
        reportPane.setBackground(Theme.DEEP);
        reportPane.setForeground(TEXT_COLOR);
        reportPane.setCaretColor(Theme.PURPLE);
        // JTextPane uses its border insets for reliable internal padding.
        reportPane.setBorder(BorderFactory.createEmptyBorder(16, 17, 16, 17));
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
        int requiredParts = currentBuild.getCpu() != null && currentBuild.getCpu().hasIGPU()
                && currentBuild.getGpu() == null ? 6 : 7;
        completionLabel.setText(currentBuild.getSelectedParts().size() + " / " + requiredParts + " selected");
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
            String selectedName = selectedComponent.getName();
            selectionDetailsLabel.setText(selectedName.length() > 36
                    ? selectedName.substring(0, 33) + "..." : selectedName);
            selectionDetailsLabel.setToolTipText(selectedName);
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
        if (currentBuild.getCpu() == null) {
            analysisArea.setText("CPU-GPU BALANCE ANALYSIS\n\nSelect a CPU first.");
            return;
        }
        if (currentBuild.getGpu() == null) {
            if (currentBuild.getCpu().hasIGPU()) {
                analysisArea.setText("CPU-GPU BALANCE ANALYSIS\n\n"
                        + "No dedicated GPU is selected. The system will use the CPU's integrated graphics.\n"
                        + "A dedicated CPU-GPU balance estimate is not applicable.");
            } else {
                analysisArea.setText("CPU-GPU BALANCE ANALYSIS\n\n"
                        + "This CPU does not have integrated graphics. Select a dedicated GPU to analyze the pairing.");
            }
            return;
        }
        Workload workload =
                (Workload) workloadSelector.getSelectedItem();
        BottleneckAnalyzer analyzer = new BottleneckAnalyzer(
                currentBuild.getCpu().getPerformanceScore(),
                currentBuild.getGpu().getPerformanceScore(), workload);
        analysisArea.setText("CPU-GPU BALANCE ESTIMATE\n\n"
                + "CPU: " + currentBuild.getCpu().getName() + "\n"
                + "GPU: " + currentBuild.getGpu().getName() + "\n\n"
                + analyzer.getBottleneckReport() + "\n\n"
                + "IMPORTANT: This is a broad pairing estimate based on CPU Mark and G3D Mark scores. "
                + "These are different benchmark scales, so the result is not a measured real-world "
                + "bottleneck percentage. Actual performance varies by game, resolution, settings, "
                + "memory configuration, and other factors.");
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

    private static class DarkComboRenderer extends DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list,
                Object value, int index, boolean selected, boolean hasFocus) {
            super.getListCellRendererComponent(list, value, index, selected, hasFocus);
            setFont(Theme.BODY);
            setBackground(selected ? new Color(95, 38, 63) : Theme.SURFACE);
            setForeground(Theme.TEXT);
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            return this;
        }
    }

    /** The dropdown shows the component name, brand, and price rather than an object reference. */
    private class ComponentRenderer extends DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list,
                Object value, int index, boolean selected, boolean hasFocus) {
            super.getListCellRendererComponent(list, value, index, selected, hasFocus);
            setFont(Theme.BODY);
            setBackground(selected ? new Color(95, 38, 63) : Theme.SURFACE);
            setForeground(TEXT_COLOR);
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
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

    /** GX appearance is kept here so MainFrame is the only GUI source file. */
    private static final class Theme {
        static final Color BACKGROUND = new Color(16, 16, 21);
        static final Color SIDEBAR = new Color(20, 20, 29);
        static final Color PANEL = new Color(28, 28, 38);
        static final Color SURFACE = new Color(40, 40, 53);
        static final Color DEEP = new Color(22, 22, 31);
        static final Color BORDER = new Color(59, 59, 74);
        static final Color ACCENT = new Color(255, 25, 93);
        static final Color ACCENT_HOVER = new Color(255, 66, 121);
        static final Color PURPLE = new Color(178, 138, 255);
        static final Color TEXT = new Color(246, 246, 251);
        static final Color MUTED = new Color(203, 204, 219);
        static final Color SUCCESS = new Color(85, 220, 169);
        static final Color WARNING = new Color(255, 183, 113);

        static final Font BODY = font(Font.PLAIN, 15);
        static final Font LABEL = font(Font.BOLD, 14);
        static final Font CAPTION = font(Font.PLAIN, 13);
        static final Font OVERLINE = font(Font.BOLD, 13);
        static final Font SECTION = font(Font.BOLD, 19);
        static final Font PAGE_TITLE = font(Font.BOLD, 26);
        static final Font METRIC = font(Font.BOLD, 22);
        static final Font REPORT = BODY;
        static final Font REPORT_HEADING = font(Font.BOLD, 18);

        private static Font font(int style, int size) {
            return new Font("SansSerif", style, size);
        }

        /** Install appearance defaults before MainFrame creates Swing controls. */
        static void install() {
            setDefaults(PANEL, "control", "info", "nimbusBase", "OptionPane.background");
            setDefaults(SURFACE, "nimbusBlueGrey", "TextField.background", "ComboBox.background",
                    "List.background", "ProgressBar.background");
            setDefaults(DEEP, "nimbusLightBackground", "TextArea.background",
                    "ScrollPane.background", "Viewport.background");
            setDefaults(SIDEBAR, "MenuBar.background", "Menu.background",
                    "MenuItem.background", "PopupMenu.background");
            setDefaults(TEXT, "text", "textText", "controlText", "menuText",
                    "Label.foreground", "TextField.foreground", "TextArea.foreground",
                    "ComboBox.foreground", "List.foreground", "ComboBox.selectionForeground",
                    "List.selectionForeground", "MenuBar.foreground", "Menu.foreground",
                    "MenuItem.foreground", "OptionPane.messageForeground");
            setDefaults(BODY, "Label.font", "TextField.font", "TextArea.font", "TextPane.font",
                    "ComboBox.font", "List.font", "Menu.font", "MenuItem.font", "OptionPane.messageFont");
            setDefaults(LABEL, "Button.font", "OptionPane.buttonFont");
            UIManager.put("Panel.background", BACKGROUND);
            UIManager.put("ScrollBar.background", PANEL);
            UIManager.put("TextField.caretForeground", ACCENT);
            UIManager.put("textHighlight", ACCENT);
            UIManager.put("nimbusSelectionBackground", new Color(89, 37, 61));
            UIManager.put("ComboBox.selectionBackground", new Color(95, 38, 63));
            UIManager.put("List.selectionBackground", new Color(95, 38, 63));
            UIManager.put("ProgressBar.foreground", ACCENT);
        }

        private static void setDefaults(Object value, String... keys) {
            for (String key : keys) UIManager.put(key, value);
        }

        static void styleTextField(JTextField field) {
            field.setFont(BODY);
            field.setBackground(SURFACE);
            field.setForeground(TEXT);
            field.setCaretColor(ACCENT);
            field.setSelectionColor(new Color(101, 42, 70));
            field.setSelectedTextColor(TEXT);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1, true),
                    BorderFactory.createEmptyBorder(11, 14, 11, 14)));
            field.setPreferredSize(new Dimension(180, 44));
        }

        static void styleComboBox(JComboBox<?> comboBox) {
            comboBox.setFont(BODY);
            comboBox.setBackground(SURFACE);
            comboBox.setForeground(TEXT);
            comboBox.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
            comboBox.setPreferredSize(new Dimension(180, 44));
            comboBox.setMaximumRowCount(9);
            comboBox.setUI(new BasicComboBoxUI() {
                @Override protected JButton createArrowButton() {
                    JButton arrow = new JButton("▾");
                    arrow.setFont(font(Font.BOLD, 17));
                    arrow.setForeground(MUTED);
                    arrow.setBackground(SURFACE);
                    arrow.setFocusPainted(false);
                    arrow.setContentAreaFilled(false);
                    arrow.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER));
                    return arrow;
                }

                @Override public void paintCurrentValueBackground(Graphics graphics,
                        Rectangle bounds, boolean hasFocus) {
                    graphics.setColor(SURFACE);
                    graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                }
            });
        }

        static void styleScrollPane(JScrollPane pane) {
            pane.setOpaque(false);
            pane.setBackground(DEEP);
            pane.getViewport().setBackground(DEEP);
            pane.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
            pane.getVerticalScrollBar().setUnitIncrement(24);
            for (JScrollBar bar : new JScrollBar[]{pane.getVerticalScrollBar(), pane.getHorizontalScrollBar()}) {
                bar.setUI(new BasicScrollBarUI() {
                    @Override protected void configureScrollBarColors() {
                        thumbColor = BORDER;
                        trackColor = PANEL;
                    }

                    @Override protected JButton createDecreaseButton(int orientation) {
                        return zeroButton();
                    }

                    @Override protected JButton createIncreaseButton(int orientation) {
                        return zeroButton();
                    }

                    private JButton zeroButton() {
                        JButton button = new JButton();
                        button.setPreferredSize(new Dimension(0, 0));
                        return button;
                    }

                    @Override protected void paintThumb(Graphics graphics, JComponent component,
                            Rectangle bounds) {
                        if (bounds.isEmpty()) return;
                        Graphics2D canvas = (Graphics2D) graphics.create();
                        smooth(canvas);
                        canvas.setColor(BORDER);
                        canvas.fillRoundRect(bounds.x + 2, bounds.y + 2,
                                Math.max(2, bounds.width - 4), Math.max(2, bounds.height - 4), 9, 9);
                        canvas.dispose();
                    }
                });
            }
        }

        static void styleProgress(JProgressBar bar) {
            bar.setBackground(SURFACE);
            bar.setForeground(ACCENT);
            bar.setBorderPainted(false);
            bar.setFont(LABEL);
            bar.setPreferredSize(new Dimension(210, 30));
            bar.setUI(new BasicProgressBarUI() {
                @Override protected void paintDeterminate(Graphics graphics, JComponent component) {
                    Graphics2D canvas = (Graphics2D) graphics.create();
                    smooth(canvas);
                    int width = bar.getWidth(), height = bar.getHeight();
                    canvas.setColor(SURFACE);
                    canvas.fillRoundRect(0, 0, width, height, 11, 11);
                    int filled = (int) Math.round(width * bar.getPercentComplete());
                    if (filled > 0) {
                        canvas.setColor(ACCENT);
                        canvas.fillRoundRect(0, 0, Math.min(width, filled), height, 11, 11);
                    }
                    if (bar.isStringPainted()) {
                        canvas.setColor(TEXT);
                        canvas.setFont(LABEL);
                        String label = bar.getString();
                        FontMetrics metrics = canvas.getFontMetrics();
                        canvas.drawString(label, Math.max(4, (width - metrics.stringWidth(label)) / 2),
                                (height - metrics.getHeight()) / 2 + metrics.getAscent());
                    }
                    canvas.dispose();
                }
            });
        }

        private static void smooth(Graphics2D graphics) {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        /** Color headings and key/value labels while retaining plain-text report content. */
        static final class ReportPane extends JTextPane {
            @Override public void setText(String text) {
                super.setText(text == null ? "" : text);
                StyledDocument document = getStyledDocument();
                String contents = getText();
                if (document.getLength() == 0) return;

                SimpleAttributeSet bodyStyle = textStyle(REPORT.getSize(), TEXT, false);
                document.setCharacterAttributes(0, document.getLength(), bodyStyle, true);
                SimpleAttributeSet paragraph = new SimpleAttributeSet();
                StyleConstants.setLineSpacing(paragraph, .20f);
                StyleConstants.setSpaceBelow(paragraph, 3f);
                document.setParagraphAttributes(0, document.getLength(), paragraph, false);

                int lineStart = 0;
                for (String line : contents.split("\n", -1)) {
                    String trimmed = line.trim();
                    boolean heading = trimmed.length() >= 5 && trimmed.length() <= 60
                            && trimmed.equals(trimmed.toUpperCase(Locale.ROOT))
                            && trimmed.matches(".*[A-Z].*") && !trimmed.contains(":");
                    if (heading) {
                        document.setCharacterAttributes(lineStart, line.length(),
                                textStyle(REPORT_HEADING.getSize(), ACCENT_HOVER, true), false);
                        SimpleAttributeSet headingSpace = new SimpleAttributeSet();
                        StyleConstants.setSpaceAbove(headingSpace, 2f);
                        StyleConstants.setSpaceBelow(headingSpace, 8f);
                        document.setParagraphAttributes(lineStart, line.length(), headingSpace, false);
                    } else {
                        int colonPosition = line.indexOf(':');
                        if (colonPosition > 0 && colonPosition <= 34 && !trimmed.startsWith("http"))
                            document.setCharacterAttributes(lineStart, colonPosition + 1,
                                    textStyle(REPORT.getSize(), PURPLE, true), false);
                        if (trimmed.startsWith("IMPORTANT") || trimmed.startsWith("Note:"))
                            document.setCharacterAttributes(lineStart, line.length(),
                                    textStyle(REPORT.getSize(), WARNING, false), false);
                    }
                    lineStart += line.length() + 1;
                }
            }

            private SimpleAttributeSet textStyle(int size, Color color, boolean bold) {
                SimpleAttributeSet style = new SimpleAttributeSet();
                StyleConstants.setFontFamily(style, "SansSerif");
                StyleConstants.setFontSize(style, size);
                StyleConstants.setForeground(style, color);
                StyleConstants.setBold(style, bold);
                return style;
            }
        }

        static final class RoundedPanel extends JPanel {
            RoundedPanel(LayoutManager layout) {
                super(layout);
                setOpaque(false);
                setBackground(PANEL);
            }

            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D canvas = (Graphics2D) graphics.create();
                smooth(canvas);
                canvas.setColor(getBackground());
                canvas.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                canvas.setColor(BORDER);
                canvas.setStroke(new BasicStroke(1));
                canvas.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                canvas.dispose();
                super.paintComponent(graphics);
            }
        }

        static final class GXButton extends JButton {
            private final boolean primary;
            private boolean hovered;

            GXButton(String title, boolean primary) {
                super(title);
                this.primary = primary;
                setFont(LABEL);
                setForeground(primary ? Color.WHITE : TEXT);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setOpaque(false);
                setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent event) {
                        hovered = true;
                        repaint();
                    }
                    @Override public void mouseExited(MouseEvent event) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D canvas = (Graphics2D) graphics.create();
                smooth(canvas);
                canvas.setColor(!isEnabled() ? SURFACE : primary
                        ? (hovered ? ACCENT_HOVER : ACCENT)
                        : (hovered ? new Color(59, 51, 67) : SURFACE));
                canvas.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 13, 13);
                if (!primary) {
                    canvas.setColor(BORDER);
                    canvas.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 13, 13);
                }
                canvas.dispose();
                super.paintComponent(graphics);
            }
        }

        static final class NavigationButton extends JButton {
            private boolean active;
            private boolean hovered;

            NavigationButton(String text) {
                super(text);
                setFont(font(Font.BOLD, 15));
                setForeground(MUTED);
                setHorizontalAlignment(SwingConstants.LEFT);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setBorder(BorderFactory.createEmptyBorder(17, 16, 17, 10));
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setOpaque(false);
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent event) {
                        hovered = true;
                        repaint();
                    }
                    @Override public void mouseExited(MouseEvent event) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            void setActive(boolean selected) {
                active = selected;
                setForeground(active ? TEXT : MUTED);
                repaint();
            }

            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D canvas = (Graphics2D) graphics.create();
                smooth(canvas);
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

}
