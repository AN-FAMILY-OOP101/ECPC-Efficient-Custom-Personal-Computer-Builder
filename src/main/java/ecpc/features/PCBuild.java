package ecpc.features;

import ecpc.components.*;
import ecpc.exceptions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the user's current PC build and manages its selected components.
 * A build can be assembled gradually; call validateCompleteBuild() before
 * final validation. A dedicated GPU is optional when the selected CPU has an iGPU.
 */

public class PCBuild {

    private String buildName;
    private CPU cpu;
    private Motherboard motherboard;
    private GPU gpu;
    private RAM ram;
    private Storage storage;
    private PowerSupply powerSupply;
    private PCCase pcCase;

    private static final int BASE_SYSTEM_WATTAGE = 75;

    public PCBuild() {
        this("Untitled Build");
    }

    public PCBuild(String buildName) {
        setBuildName(buildName);
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        if (buildName == null || buildName.trim().isEmpty()) {
            throw new InvalidInputException("Build name cannot be empty.");
        }
        this.buildName = buildName.trim();
    }

    public CPU getCpu() {
        return cpu;
    }

    public void setCpu(CPU cpu) {
        this.cpu = cpu;
    }

    public Motherboard getMotherboard() {
        return motherboard;
    }

    public void setMotherboard(Motherboard motherboard) {
        this.motherboard = motherboard;
    }

    public GPU getGpu() {
        return gpu;
    }

    public void setGpu(GPU gpu) {
        this.gpu = gpu;
    }

    public RAM getRam() {
        return ram;
    }

    public void setRam(RAM ram) {
        this.ram = ram;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public PowerSupply getPowerSupply() {
        return powerSupply;
    }

    /** Alias retained for compatibility with CompatibilityChecker. */
    public PowerSupply getPsu() {
        return powerSupply;
    }

    public void setPowerSupply(PowerSupply powerSupply) {
        this.powerSupply = powerSupply;
    }

    /** Alias retained for classes that use the shorter PSU naming. */
    public void setPsu(PowerSupply powerSupply) {
        this.powerSupply = powerSupply;
    }

    public PCCase getPcCase() {
        return pcCase;
    }

    public void setPcCase(PCCase pcCase) {
        this.pcCase = pcCase;
    }

    /** Adds or replaces the selected component in its matching category. */
    public void addComponent(Component component) {
        if (component == null) {
            throw new InvalidInputException("Cannot add a null component to the build.");
        }

        if (component instanceof CPU) {
            setCpu((CPU) component);
        } else if (component instanceof Motherboard) {
            setMotherboard((Motherboard) component);
        } else if (component instanceof GPU) {
            setGpu((GPU) component);
        } else if (component instanceof RAM) {
            setRam((RAM) component);
        } else if (component instanceof Storage) {
            setStorage((Storage) component);
        } else if (component instanceof PowerSupply) {
            setPowerSupply((PowerSupply) component);
        } else if (component instanceof PCCase) {
            setPcCase((PCCase) component);
        } else {
            throw new InvalidInputException(
                    "Unsupported component type: " + component.getClass().getSimpleName());
        }
    }

    /** Removes the selected component belonging to the supplied component class. */
    public void removeComponent(Class<? extends Component> componentType) {
        if (componentType == null) {
            throw new InvalidInputException("Component type cannot be null.");
        }

        if (componentType == CPU.class) {
            cpu = null;
        } else if (componentType == Motherboard.class) {
            motherboard = null;
        } else if (componentType == GPU.class) {
            gpu = null;
        } else if (componentType == RAM.class) {
            ram = null;
        } else if (componentType == Storage.class) {
            storage = null;
        } else if (componentType == PowerSupply.class) {
            powerSupply = null;
        } else if (componentType == PCCase.class) {
            pcCase = null;
        } else {
            throw new InvalidInputException(
                    "Unsupported component type: " + componentType.getSimpleName());
        }
    }

    /** Convenience overload for removing a component by its concrete object type. */
    public void removeComponent(Component component) {
        if (component == null) {
            throw new InvalidInputException("Component cannot be null.");
        }
        removeComponent(component.getClass().asSubclass(Component.class));
    }

    /** Returns the sum of the prices of all currently selected components. */
    public double getTotalPrice() {
        double total = 0.0;
        for (Component part : getSelectedParts()) {
            total += part.getPrice();
        }
        return total;
    }

    /** Estimates system draw using CPU TDP, GPU power, and 75W system overhead. */
    public int getEstimatedWattage() {
        int estimatedWattage = BASE_SYSTEM_WATTAGE;

        if (cpu != null) {
            estimatedWattage += cpu.getTdp();
        }
        if (gpu != null) {
            estimatedWattage += gpu.getPowerWatts();
        }
        return estimatedWattage;
    }

    /** Returns a new list containing every currently selected component. */
    public List<Component> getSelectedParts() {
        List<Component> parts = new ArrayList<>();

        if (cpu != null) parts.add(cpu);
        if (motherboard != null) parts.add(motherboard);
        if (ram != null) parts.add(ram);
        if (gpu != null) parts.add(gpu);
        if (storage != null) parts.add(storage);
        if (powerSupply != null) parts.add(powerSupply);
        if (pcCase != null) parts.add(pcCase);

        return parts;
    }

    /** Returns the names of all component categories that have not been selected. */
    public List<String> getMissingComponents() {
        List<String> missing = new ArrayList<>();

        if (cpu == null) missing.add("CPU");
        if (motherboard == null) missing.add("Motherboard");
        if (ram == null) missing.add("RAM");
        // A dedicated GPU is optional only when the selected CPU has integrated graphics.
        if (gpu == null && (cpu == null || !cpu.hasIGPU())) missing.add("GPU");
        if (storage == null) missing.add("Storage");
        if (powerSupply == null) missing.add("Power Supply");
        if (pcCase == null) missing.add("PC Case");

        return Collections.unmodifiableList(missing);
    }

    /** Throws IncompleteBuildException when a required component is missing. */
    public void validateCompleteBuild() {
        List<String> missing = getMissingComponents();
        if (!missing.isEmpty()) {
            throw new IncompleteBuildException(
                    "Cannot complete the build. Missing components: " + String.join(", ", missing));
        }
    }

    /** Validates a maximum budget and throws BudgetExceededException when exceeded. */
    public void validateBudget(double maxBudget) {
        if (!Double.isFinite(maxBudget) || maxBudget <= 0) {
            throw new InvalidInputException("Maximum budget must be a positive number.");
        }
        if (getTotalPrice() > maxBudget) {
            throw new BudgetExceededException(String.format(
                    "Build total of PHP %.2f exceeds the maximum budget of PHP %.2f.",
                    getTotalPrice(), maxBudget));
        }
    }

    /** Validates a budget range and checks that the current build falls within it. */
    public void validateBudget(double minBudget, double maxBudget) {
        if (!Double.isFinite(minBudget) || !Double.isFinite(maxBudget)
                || minBudget < 0 || maxBudget <= 0 || minBudget > maxBudget) {
            throw new InvalidInputException("Invalid budget range.");
        }
        validateBudget(maxBudget);
        if (getTotalPrice() < minBudget) {
            throw new InvalidInputException(String.format(
                    "Build total of PHP %.2f is below the minimum budget of PHP %.2f.",
                    getTotalPrice(), minBudget));
        }
    }

    /** Validates that all required components are present and compatible. */
    public void validateCompatibility(CompatibilityChecker checker) {
        if (checker == null) {
            throw new InvalidInputException("Compatibility checker cannot be null.");
        }
        validateCompleteBuild();
        if (!checker.checkAllParts(this)) {
            throw new IncompatibleComponentException(checker.getCompatibilityReport(this));
        }
    }

    /** Builds a readable summary; partial builds can also be displayed. */
    public String displaySummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("PC BUILD SUMMARY\n");
        summary.append("Build Name: ").append(buildName).append("\n\n");

        appendPart(summary, "CPU", cpu);
        appendPart(summary, "Motherboard", motherboard);
        appendPart(summary, "RAM", ram);
        appendPart(summary, "GPU", gpu);
        appendPart(summary, "Storage", storage);
        appendPart(summary, "Power Supply", powerSupply);
        appendPart(summary, "PC Case", pcCase);

        summary.append("\nEstimated Power Consumption: ")
                .append(getEstimatedWattage()).append("W\n");
        summary.append(String.format("Total Price: PHP %.2f", getTotalPrice()));
        return summary.toString();
    }

    private void appendPart(StringBuilder summary, String label, Component part) {
        summary.append(label).append(": ");
        if (part == null) {
            summary.append("Not selected\n");
        } else {
            summary.append(part.getName())
                    .append(String.format(" (PHP %.2f)", part.getPrice()))
                    .append("\n");
        }
    }

    /** Clears all selected components while keeping the build name. */
    public void clearBuild() {
        cpu = null;
        motherboard = null;
        gpu = null;
        ram = null;
        storage = null;
        powerSupply = null;
        pcCase = null;
    }
}