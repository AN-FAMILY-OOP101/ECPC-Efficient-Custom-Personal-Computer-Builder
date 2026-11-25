package ecpc.features;

import ecpc.components.*;
import ecpc.exceptions.*;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Saves a PCBuild to a small text file and loads it back.
 *
 * Only the part IDs are saved, so a loaded build always gets its current price
 * and specs from the PartCatalog. A saved file looks like this:
 *
 *     ECPC-BUILD
 *     Name=My Gaming PC
 *     CPU=CPU-007
 *     Motherboard=MB-005
 *     GPU=none
 *     RAM=RAM-010
 *     Storage=SSD-005
 *     PowerSupply=PSU-003
 *     Case=CASE-001
 *
 * "none" marks an empty slot, so a build that is not finished can be saved too.
 *
 * Misuse (a null build, an empty or invalid path) throws InvalidInputException.
 * A file that cannot be read or written, or has a problem inside, throws IOException
 * with a message that is ready to show to the user.
 */
public class BuildFileManager {

    private static final String HEADER = "ECPC-BUILD";

    // The seven slots in the order they are written. TYPES[i] is the kind of part allowed in SLOTS[i].
    private static final String[] SLOTS =
            {"CPU", "Motherboard", "GPU", "RAM", "Storage", "PowerSupply", "Case"};
    private static final Class<?>[] TYPES =
            {CPU.class, Motherboard.class, GPU.class, RAM.class, Storage.class, PowerSupply.class, PCCase.class};

    /** Saves the build (finished or not) to the file. An existing file is replaced. */
    public void saveBuild(PCBuild build, String filePath) throws IOException {
        if (build == null) {
            throw new InvalidInputException("Build cannot be null.");
        }
        Path path = toPath(filePath);

        // The part in each slot, in the same order as SLOTS
        Component[] parts = {build.getCpu(), build.getMotherboard(), build.getGpu(), build.getRam(),
            build.getStorage(), build.getPowerSupply(), build.getPcCase()};

        StringBuilder text = new StringBuilder(HEADER).append("\n");
        // A line break inside the name would break the file format, so it becomes a space
        text.append("Name=").append(build.getBuildName().replaceAll("[\\r\\n]+", " ")).append("\n");
        for (int i = 0; i < SLOTS.length; i++) {
            String id = (parts[i] == null) ? "none" : parts[i].getId();
            text.append(SLOTS[i]).append("=").append(id).append("\n");
        }
        try {
            Files.write(path, text.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IOException("Could not save the build to " + path + ": " + describe(e), e);
        }
    }

    /**
     * Loads a build from a file, looking every part up in the catalog.
     * The build is only returned if the whole file is valid. The parts are not
     * checked for compatibility here: use CompatibilityChecker for that.
     */
    public PCBuild loadBuild(String filePath, PartCatalog catalog) throws IOException {
        Path path = toPath(filePath);
        if (catalog == null) {
            throw new InvalidInputException("Catalog cannot be null.");
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IOException("Could not read " + path + ": " + describe(e), e);
        }
        // (the replace removes the invisible marker that some editors add at the start of a file)
        if (lines.isEmpty() || !lines.get(0).replace("\uFEFF", "").trim().equals(HEADER)) {
            throw new IOException("This is not an ECPC build file.");
        }

        PCBuild build = new PCBuild();
        Set<String> seenKeys = new HashSet<>(); // so no line can appear twice

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue; // blank lines are ignored
            }

            int equalsSign = line.indexOf('=');
            if (equalsSign < 1) {
                throw lineError(i, "Cannot understand \"" + line + "\".");
            }
            String key = line.substring(0, equalsSign).trim();
            String value = line.substring(equalsSign + 1).trim();
            int slot = slotNumber(key); // -1 if the key is not a slot name

            if (slot < 0 && !key.equalsIgnoreCase("Name")) {
                throw lineError(i, "Unknown entry \"" + key + "\".");
            }
            if (!seenKeys.add(key.toLowerCase())) {
                throw lineError(i, "\"" + key + "\" appears more than once.");
            }
            if (value.isEmpty()) {
                throw lineError(i, "\"" + key + "\" has no value.");
            }

            if (slot < 0) {
                build.setBuildName(value);
            } else if (!value.equalsIgnoreCase("none")) {
                Component part = catalog.findById(value);
                if (part == null) {
                    throw lineError(i, "Part " + value + " is not in the catalog.");
                }
                if (!TYPES[slot].isInstance(part)) {
                    throw lineError(i, value + " is not a " + SLOTS[slot] + " part.");
                }
                build.addComponent(part); // puts the part in its own slot
            }
        }
        return build;
    }

    // Checks the path text and turns it into a Path
    private Path toPath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new InvalidInputException("File path cannot be empty.");
        }
        try {
            return Paths.get(filePath.trim());
        } catch (InvalidPathException e) {
            throw new InvalidInputException("Invalid file path: " + filePath.trim());
        }
    }

    // Java's file errors are short and technical. This says the common ones in plain words.
    private String describe(IOException e) {
        if (e instanceof NoSuchFileException) {
            return "the file or folder does not exist.";
        }
        if (e instanceof AccessDeniedException) {
            return "access was denied.";
        }
        if (e instanceof MalformedInputException) {
            return "it is not a text file.";
        }
        return e.getMessage();
    }

    // The position of a slot name in SLOTS (ignoring upper/lower case), or -1
    private int slotNumber(String key) {
        for (int i = 0; i < SLOTS.length; i++) {
            if (SLOTS[i].equalsIgnoreCase(key)) {
                return i;
            }
        }
        return -1;
    }

    // i is the position in the list of lines, which starts at 0, so the line number is i + 1
    private IOException lineError(int i, String message) {
        return new IOException("Line " + (i + 1) + ": " + message);
    }
}