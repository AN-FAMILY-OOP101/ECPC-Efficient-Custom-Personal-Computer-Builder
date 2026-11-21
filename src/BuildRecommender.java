import java.util.List;

/**
 * Generates a complete, compatible PC build based on workload priorities
 * and the user's requested budget range.
 */
public class BuildRecommender {

    private static final double PSU_SAFETY_MULTIPLIER = 1.25;
    private static final int BASE_SYSTEM_WATTAGE = 75;
    private static final double SCORE_TIE_TOLERANCE = 0.0000001;

    private final PartCatalog catalog;
    private PCBuild recommendedBuild;

    public BuildRecommender(PartCatalog catalog) {
        if (catalog == null) {
            throw new InvalidInputException("Catalog cannot be null.");
        }
        this.catalog = catalog;
    }

    /**
     * Finds the highest-scoring compatible build whose total price is within
     * the inclusive minimum and maximum budget.
     */
    public PCBuild generateBuild(double minBudget, double maxBudget,
                                 Workload workload, String preferredBrand) {

        // Clear the previous result first, so a failed request cannot expose a stale build.
        recommendedBuild = null;

        validateBudgetRange(minBudget, maxBudget);

        if (workload == null) {
            throw new InvalidInputException("Workload cannot be null.");
        }

        List<CPU> cpus = catalog.getPartsByType(CPU.class);
        List<GPU> gpus = catalog.getPartsByType(GPU.class);
        List<Motherboard> motherboards = catalog.getPartsByType(Motherboard.class);
        List<RAM> memoryOptions = catalog.getPartsByType(RAM.class);
        List<Storage> storageOptions = catalog.getPartsByType(Storage.class);
        List<PowerSupply> powerSupplies = catalog.getPartsByType(PowerSupply.class);
        List<PCCase> cases = catalog.getPartsByType(PCCase.class);

        if (cpus.isEmpty() || gpus.isEmpty() || motherboards.isEmpty()
                || memoryOptions.isEmpty() || storageOptions.isEmpty()
                || powerSupplies.isEmpty() || cases.isEmpty()) {
            throw new IncompleteBuildException(
                    "The catalog must contain a CPU, GPU, motherboard, RAM, storage, power supply, and PC case.");
        }

        ScoreRanges ranges = new ScoreRanges(
                minimumCpuScore(cpus), maximumCpuScore(cpus),
                minimumGpuScore(gpus), maximumGpuScore(gpus),
                minimumRamCapacity(memoryOptions), maximumRamCapacity(memoryOptions),
                minimumRamSpeed(memoryOptions), maximumRamSpeed(memoryOptions),
                minimumStorageCapacity(storageOptions), maximumStorageCapacity(storageOptions));

        CompatibilityChecker checker = new CompatibilityChecker();

        CPU bestCpu = null;
        GPU bestGpu = null;
        Motherboard bestMotherboard = null;
        RAM bestRam = null;
        Storage bestStorage = null;
        PowerSupply bestPowerSupply = null;
        PCCase bestCase = null;

        double bestScore = Double.NEGATIVE_INFINITY;
        double bestTotal = Double.POSITIVE_INFINITY;
        boolean foundCompatibleCombination = false;

        double cpuWeight = getCpuWeight(workload);
        double gpuWeight = getGpuWeight(workload);
        double ramWeight = getRamWeight(workload);
        double storageWeight = getStorageWeight(workload);
        String brandPreference = normalizeBrand(preferredBrand);

        /*
         * Search all catalog candidates. Compatibility checks are applied before
         * scoring, and no fixed top-eight candidate limit is used.
         */
        for (CPU cpu : cpus) {
            for (GPU gpu : gpus) {
                int requiredPsuWattage = (int) Math.ceil(
                        (BASE_SYSTEM_WATTAGE + cpu.getTdp() + gpu.getPowerWatts())
                                * PSU_SAFETY_MULTIPLIER);

                for (Motherboard motherboard : motherboards) {
                    if (!same(cpu.getSocket(), motherboard.getSocket())) {
                        continue;
                    }

                    for (RAM ram : memoryOptions) {
                        if (!same(ram.getMemoryType(), motherboard.getRamType())
                                || ram.getCapacityGB() > motherboard.getMaxRamGb()) {
                            continue;
                        }

                        double corePrice = cpu.getPrice() + gpu.getPrice()
                                + motherboard.getPrice() + ram.getPrice();

                        double coreScore = getCpuScore(cpu, ranges) * cpuWeight
                                + getGpuScore(gpu, ranges) * gpuWeight
                                + getRamScore(ram, ranges) * ramWeight;

                        double brandScore = getBrandBonus(cpu, brandPreference, cpuWeight)
                                + getBrandBonus(gpu, brandPreference, gpuWeight)
                                + getBrandBonus(ram, brandPreference, ramWeight)
                                + getBrandBonus(motherboard, brandPreference, 0.01);

                        for (Storage storage : storageOptions) {
                            double partialPrice = corePrice + storage.getPrice();

                            double score = coreScore
                                    + getStorageScore(storage, ranges) * storageWeight
                                    + brandScore
                                    + getBrandBonus(storage, brandPreference, storageWeight);

                            for (PowerSupply powerSupply : powerSupplies) {
                                if (powerSupply.getWattage() < requiredPsuWattage) {
                                    continue;
                                }

                                double priceWithPsu = partialPrice + powerSupply.getPrice();

                                for (PCCase pcCase : cases) {
                                    if (!supports(pcCase, motherboard.getFormFactor())
                                            || gpu.getLengthMm() > pcCase.getMaxGpuLengthMm()) {
                                        continue;
                                    }

                                    foundCompatibleCombination = true;
                                    double total = priceWithPsu + pcCase.getPrice();
                                    if (total < minBudget || total > maxBudget) {
                                        continue;
                                    }

                                    double candidateScore = score
                                            + getBrandBonus(powerSupply, brandPreference, 0.01)
                                            + getBrandBonus(pcCase, brandPreference, 0.01);

                                    boolean betterScore = candidateScore > bestScore + SCORE_TIE_TOLERANCE;
                                    boolean equalScoreButCheaper =
                                            Math.abs(candidateScore - bestScore) <= SCORE_TIE_TOLERANCE
                                                    && total < bestTotal;

                                    if (betterScore || equalScoreButCheaper) {
                                        bestScore = candidateScore;
                                        bestTotal = total;
                                        bestCpu = cpu;
                                        bestGpu = gpu;
                                        bestMotherboard = motherboard;
                                        bestRam = ram;
                                        bestStorage = storage;
                                        bestPowerSupply = powerSupply;
                                        bestCase = pcCase;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (bestCpu == null) {
            if (!foundCompatibleCombination) {
                throw new IncompatibleComponentException(
                        "The catalog does not contain a fully compatible combination of all required components.");
            }
            throw new BudgetExceededException(String.format(
                    "No complete compatible build was found within PHP %.2f to PHP %.2f.",
                    minBudget, maxBudget));
        }

        PCBuild result = new PCBuild("Recommended Build");
        result.setCpu(bestCpu);
        result.setGpu(bestGpu);
        result.setMotherboard(bestMotherboard);
        result.setRam(bestRam);
        result.setStorage(bestStorage);
        result.setPowerSupply(bestPowerSupply);
        result.setPcCase(bestCase);

        result.validateCompleteBuild();
        result.validateBudget(minBudget, maxBudget);
        result.validateCompatibility(checker);

        recommendedBuild = result;
        return recommendedBuild;
    }

    private void validateBudgetRange(double minBudget, double maxBudget) {
        if (!Double.isFinite(minBudget) || !Double.isFinite(maxBudget)
                || minBudget < 0 || maxBudget <= 0 || minBudget > maxBudget) {
            throw new InvalidInputException("Please enter a valid budget range.");
        }
    }

    private double getCpuWeight(Workload workload) {
        switch (workload) {
            case GAMING: return 0.25;
            case VIDEO_EDITING: return 0.40;
            case SCHOOLWORK:
            default: return 0.30;
        }
    }

    private double getGpuWeight(Workload workload) {
        switch (workload) {
            case GAMING: return 0.50;
            case VIDEO_EDITING: return 0.20;
            case SCHOOLWORK:
            default: return 0.10;
        }
    }

    private double getRamWeight(Workload workload) {
        switch (workload) {
            case GAMING: return 0.15;
            case VIDEO_EDITING: return 0.25;
            case SCHOOLWORK:
            default: return 0.30;
        }
    }

    private double getStorageWeight(Workload workload) {
        switch (workload) {
            case GAMING: return 0.10;
            case VIDEO_EDITING: return 0.15;
            case SCHOOLWORK:
            default: return 0.30;
        }
    }

    private double getCpuScore(CPU cpu, ScoreRanges ranges) {
        return normalize(cpu.getPerformanceScore(), ranges.minCpu, ranges.maxCpu);
    }

    private double getGpuScore(GPU gpu, ScoreRanges ranges) {
        return normalize(gpu.getPerformanceScore(), ranges.minGpu, ranges.maxGpu);
    }

    private double getRamScore(RAM ram, ScoreRanges ranges) {
        double capacityScore = normalize(
                ram.getCapacityGB(), ranges.minRamCapacity, ranges.maxRamCapacity);
        double speedScore = normalize(
                ram.getSpeedMHz(), ranges.minRamSpeed, ranges.maxRamSpeed);
        return capacityScore * 0.70 + speedScore * 0.30;
    }

    private double getStorageScore(Storage storage, ScoreRanges ranges) {
        double capacityScore = normalize(
                storage.getCapacityGB(), ranges.minStorageCapacity, ranges.maxStorageCapacity);
        double typeScore = storage.getStorageType() != null
                && storage.getStorageType().toUpperCase().contains("SSD") ? 1.0 : 0.0;
        double interfaceScore = storage.getInterfaceType() != null
                && storage.getInterfaceType().toUpperCase().contains("NVME") ? 1.0 : 0.0;
        return capacityScore * 0.70 + typeScore * 0.20 + interfaceScore * 0.10;
    }

    private double getBrandBonus(Component part, String preferredBrand, double weight) {
        if (preferredBrand == null || preferredBrand.isEmpty()
                || part.getBrand() == null
                || !part.getBrand().equalsIgnoreCase(preferredBrand)) {
            return 0.0;
        }
        // A small preference bonus; performance remains the primary factor.
        return weight * 0.02;
    }

    private double normalize(double value, double minimum, double maximum) {
        if (maximum <= minimum) {
            return 1.0;
        }
        return (value - minimum) / (maximum - minimum);
    }

    private boolean same(String first, String second) {
        return first != null && second != null && first.equalsIgnoreCase(second);
    }

    private boolean supports(PCCase pcCase, String formFactor) {
        if (pcCase.getSupportedFormFactors() == null || formFactor == null) {
            return false;
        }
        for (String supported : pcCase.getSupportedFormFactors()) {
            if (same(supported, formFactor)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeBrand(String brand) {
        return brand == null ? null : brand.trim();
    }

    private int minimumCpuScore(List<CPU> parts) {
        int value = Integer.MAX_VALUE;
        for (CPU part : parts) value = Math.min(value, part.getPerformanceScore());
        return value;
    }

    private int maximumCpuScore(List<CPU> parts) {
        int value = Integer.MIN_VALUE;
        for (CPU part : parts) value = Math.max(value, part.getPerformanceScore());
        return value;
    }

    private int minimumGpuScore(List<GPU> parts) {
        int value = Integer.MAX_VALUE;
        for (GPU part : parts) value = Math.min(value, part.getPerformanceScore());
        return value;
    }

    private int maximumGpuScore(List<GPU> parts) {
        int value = Integer.MIN_VALUE;
        for (GPU part : parts) value = Math.max(value, part.getPerformanceScore());
        return value;
    }

    private int minimumRamCapacity(List<RAM> parts) {
        int value = Integer.MAX_VALUE;
        for (RAM part : parts) value = Math.min(value, part.getCapacityGB());
        return value;
    }

    private int maximumRamCapacity(List<RAM> parts) {
        int value = Integer.MIN_VALUE;
        for (RAM part : parts) value = Math.max(value, part.getCapacityGB());
        return value;
    }

    private int minimumRamSpeed(List<RAM> parts) {
        int value = Integer.MAX_VALUE;
        for (RAM part : parts) value = Math.min(value, part.getSpeedMHz());
        return value;
    }

    private int maximumRamSpeed(List<RAM> parts) {
        int value = Integer.MIN_VALUE;
        for (RAM part : parts) value = Math.max(value, part.getSpeedMHz());
        return value;
    }

    private int minimumStorageCapacity(List<Storage> parts) {
        int value = Integer.MAX_VALUE;
        for (Storage part : parts) value = Math.min(value, part.getCapacityGB());
        return value;
    }

    private int maximumStorageCapacity(List<Storage> parts) {
        int value = Integer.MIN_VALUE;
        for (Storage part : parts) value = Math.max(value, part.getCapacityGB());
        return value;
    }

    /** Returns the most recently generated build, or null if the last attempt failed. */
    public PCBuild getRecommendedBuild() {
        return recommendedBuild;
    }

    private static class ScoreRanges {
        private final double minCpu;
        private final double maxCpu;
        private final double minGpu;
        private final double maxGpu;
        private final double minRamCapacity;
        private final double maxRamCapacity;
        private final double minRamSpeed;
        private final double maxRamSpeed;
        private final double minStorageCapacity;
        private final double maxStorageCapacity;

        private ScoreRanges(double minCpu, double maxCpu,
                            double minGpu, double maxGpu,
                            double minRamCapacity, double maxRamCapacity,
                            double minRamSpeed, double maxRamSpeed,
                            double minStorageCapacity, double maxStorageCapacity) {
            this.minCpu = minCpu;
            this.maxCpu = maxCpu;
            this.minGpu = minGpu;
            this.maxGpu = maxGpu;
            this.minRamCapacity = minRamCapacity;
            this.maxRamCapacity = maxRamCapacity;
            this.minRamSpeed = minRamSpeed;
            this.maxRamSpeed = maxRamSpeed;
            this.minStorageCapacity = minStorageCapacity;
            this.maxStorageCapacity = maxStorageCapacity;
        }
    }
}
