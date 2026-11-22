import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Starts ECPC, loads the catalog, and opens the Swing interface. */
public class Main {

    public static void main(String[] arguments) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // The default Swing appearance is a safe fallback.
        }

        SwingUtilities.invokeLater(() -> {
            PartCatalog catalog = new PartCatalog();
            Path databasePath = findDatabase();

            if (databasePath == null) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Locate the ECPC Components.json database");
                fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));
                if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                    JOptionPane.showMessageDialog(null,
                            "ECPC needs Components.json to load its hardware catalog.",
                            "Database Not Found", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                databasePath = fileChooser.getSelectedFile().toPath();
            }

            try {
                int componentCount = catalog.loadFromFile(databasePath.toString());
                if (componentCount == 0) {
                    throw new IOException("The component catalog contains no usable hardware records.");
                }
            } catch (IOException | RuntimeException exception) {
                JOptionPane.showMessageDialog(null,
                        "Could not load Components.json:\n" + exception.getMessage(),
                        "ECPC Startup Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MainFrame window = new MainFrame(catalog, databasePath.getFileName().toString());
            window.setVisible(true);

            if (!catalog.getLoadWarnings().isEmpty()) {
                JOptionPane.showMessageDialog(window,
                        catalog.getLoadWarnings().size()
                                + " component record(s) were skipped. Review the database if necessary.\n"
                                + catalog.getLoadWarnings().get(0),
                        "Catalog Warnings", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private static Path findDatabase() {
        Path workingDirectory = Paths.get("").toAbsolutePath();
        Path[] possiblePaths = {
                workingDirectory.resolve("Components.json"),
                workingDirectory.resolve("src").resolve("Components.json"),
                workingDirectory.resolve("ECPC_Final_GUI_Source").resolve("Components.json")
        };

        for (Path possiblePath : possiblePaths) {
            if (Files.isRegularFile(possiblePath)) {
                return possiblePath;
            }
        }

        try {
            Path applicationLocation = Paths.get(
                    Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path applicationDirectory = Files.isDirectory(applicationLocation)
                    ? applicationLocation : applicationLocation.getParent();
            if (applicationDirectory != null) {
                Path siblingDatabase = applicationDirectory.resolve("Components.json");
                if (Files.isRegularFile(siblingDatabase)) {
                    return siblingDatabase;
                }
            }
        } catch (URISyntaxException | SecurityException exception) {
            // The user can select the file in the dialog instead.
        }

        return null;
    }
}