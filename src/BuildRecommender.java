import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class BuildRecommender {

    public enum Workload {
        SCHOOLWORK,
        GAMING,
        VIDEO_EDITING
    }

    private final PartCatalog catalog;
    private PCBuild recommendedBuild;

    public BuildRecommender(PartCatalog catalog) {
        if (catalog == null) {
            throw new IllegalArgumentException("Catalog cannot be null.");
        }

        this.catalog = catalog;
    }

    public PCBuild generateBuild(double minBudget, double maxBudget,
                                 Workload workload, String preferredBrand) {

        if (minBudget < 0 || maxBudget <= 0 || minBudget > maxBudget) {
            throw new IllegalArgumentException("Invalid budget range.");
        }

        if (workload == null) {
            throw new IllegalArgumentException("Workload cannot be null.");
        }

        double cpuShare;
        double gpuShare;
        double motherboardShare;
        double ramShare;
        double storageShare;
        double psuShare;
        double caseShare;

        switch (workload) {
            case GAMING:
                cpuShare = 0.18;
                gpuShare = 0.30;
                motherboardShare = 0.14;
                ramShare = 0.12;
                storageShare = 0.08;
                psuShare = 0.10;
                caseShare = 0.08;
                break;

            case VIDEO_EDITING:
                cpuShare = 0.24;
                gpuShare = 0.22;
                motherboardShare = 0.14;
                ramShare = 0.16;
                storageShare = 0.10;
                psuShare = 0.08;
                caseShare = 0.06;
                break;

            default:
                cpuShare = 0.20;
                gpuShare = 0.08;
                motherboardShare = 0.16;
                ramShare = 0.14;
                storageShare = 0.12;
                psuShare = 0.12;
                caseShare = 0.12;
                break;
        }

        PCBuild build = new PCBuild();

        CPU cpu = selectBestValuePart(
                CPU.class, maxBudget * cpuShare,
                part -> part.getPerformanceScore(),
                part -> true, preferredBrand
        );

        GPU gpu = selectBestValuePart(
                GPU.class, maxBudget * gpuShare,
                part -> part.getPerformanceScore(),
                part -> true, preferredBrand
        );

        if (cpu == null || gpu == null) {
            throw new IllegalStateException(
                    "No suitable CPU or GPU found within the allocated budget."
            );
        }

        Motherboard motherboard = selectBestValuePart(
                Motherboard.class, maxBudget * motherboardShare,
                part -> 1,
                part -> part.getSocket().equalsIgnoreCase(cpu.getSocket()),
                preferredBrand
        );

        if (motherboard == null) {
            throw new IllegalStateException(
                    "No compatible motherboard found for the selected CPU."
            );
        }

        RAM ram = selectBestValuePart(
                RAM.class, maxBudget * ramShare,
                part -> 1,
                part -> part.getMemoryType().equalsIgnoreCase(motherboard.getRamType())
                        && part.getCapacityGB() <= motherboard.getMaxRamGb(),
                preferredBrand
        );

        if (ram == null) {
            throw new IllegalStateException(
                    "No compatible RAM found for the selected motherboard."
            );
        }

        PCCase pcCase = selectBestValuePart(
                PCCase.class, maxBudget * caseShare,
                part -> 1,
                part -> part.getSupportedFormFactors().stream()
                        .anyMatch(form -> form.equalsIgnoreCase(motherboard.getFormFactor()))
                        && part.getMaxGpuLengthMm() >= gpu.getLengthMm(),
                preferredBrand
        );

        if (pcCase == null) {
            throw new IllegalStateException(
                    "No case supports the selected motherboard and GPU."
            );
        }

        int requiredWattage = cpu.getTdp() + gpu.getPowerWatts() + 150;

        PowerSupply powerSupply = selectBestValuePart(
                PowerSupply.class, maxBudget * psuShare,
                part -> 1,
                part -> part.getWattage() >= requiredWattage,
                preferredBrand
        );

        if (powerSupply == null) {
            throw new IllegalStateException(
                    "No power supply with sufficient wattage was found."
            );
        }

        Storage storage = selectBestValuePart(
                Storage.class, maxBudget * storageShare,
                part -> 1,
                part -> true, preferredBrand
        );

        if (storage == null) {
            throw new IllegalStateException("No storage device was found.");
        }

        build.setCpu(cpu);
        build.setGpu(gpu);
        build.setMotherboard(motherboard);
        build.setRam(ram);
        build.setStorage(storage);
        build.setPowerSupply(powerSupply);
        build.setPcCase(pcCase);

        if (build.getTotalPrice() > maxBudget) {
            throw new IllegalStateException(
                    "The recommended build exceeds the maximum budget."
            );
        }

        if (build.getTotalPrice() < minBudget) {
            throw new IllegalStateException(
                    "The recommended build is below the minimum budget."
            );
        }

        recommendedBuild = build;
        return recommendedBuild;
    }

    public <T extends Component> T selectBestValuePart(
            Class<T> type,
            double priceLimit,
            ToDoubleFunction<T> valueFunction,
            Predicate<T> condition,
            String preferredBrand) {

        List<T> parts = catalog.getPartsByType(type);
        T bestPart = null;
        double bestValue = -1;

        for (T part : parts) {
            if (part.getPrice() > priceLimit || !condition.test(part)) {
                continue;
            }

            double value = valueFunction.applyAsDouble(part)
                    / Math.max(part.getPrice(), 1);

            if (preferredBrand != null
                    && !preferredBrand.trim().isEmpty()
                    && part.getBrand().equalsIgnoreCase(preferredBrand.trim())) {
                value *= 1.10;
            }

            if (value > bestValue) {
                bestValue = value;
                bestPart = part;
            }
        }

        return bestPart;
    }

    public PCBuild getRecommendedBuild() {
        return recommendedBuild;
    }
}