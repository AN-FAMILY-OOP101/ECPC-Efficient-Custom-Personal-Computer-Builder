import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Generates a compatible PC build based on the user's workload and budget. */
public class BuildRecommender {

    public enum Workload {
        SCHOOLWORK,
        GAMING,
        VIDEO_EDITING
    }

    private static final int CANDIDATES_PER_CATEGORY = 8;
    private final PartCatalog catalog;
    private PCBuild recommendedBuild;

    public BuildRecommender(PartCatalog catalog) {
        if (catalog == null) {
            throw new InvalidInputException("Catalog cannot be null.");
        }
        this.catalog = catalog;
    }

    public PCBuild generateBuild(double minBudget, double maxBudget,
                                 Workload workload, String preferredBrand) {
        if (Double.isNaN(minBudget) || Double.isNaN(maxBudget)
                || Double.isInfinite(minBudget) || Double.isInfinite(maxBudget)
                || minBudget < 0 || maxBudget <= 0 || minBudget > maxBudget) {
            throw new InvalidInputException("Please enter a valid budget range.");
        }
        if (workload == null) {
            throw new InvalidInputException("Workload cannot be null.");
        }

        List<CPU> cpus = topParts(CPU.class, p -> cpuScore(p, workload, preferredBrand));
        List<GPU> gpus = topParts(GPU.class, p -> gpuScore(p, workload, preferredBrand));
        List<Motherboard> boards = topParts(Motherboard.class, p -> valueScore(p, preferredBrand));
        List<RAM> memory = topParts(RAM.class, p -> ramScore(p, workload, preferredBrand));
        List<Storage> drives = topParts(Storage.class, p -> storageScore(p, preferredBrand));
        List<PowerSupply> psus = topParts(PowerSupply.class, p -> valueScore(p, preferredBrand));
        List<PCCase> cases = topParts(PCCase.class, p -> valueScore(p, preferredBrand));

        if (cpus.isEmpty() || gpus.isEmpty() || boards.isEmpty() || memory.isEmpty()
                || drives.isEmpty() || psus.isEmpty() || cases.isEmpty()) {
            throw new IncompleteBuildException("The catalog does not contain every required component category.");
        }

        CompatibilityChecker checker = new CompatibilityChecker();
        PCBuild bestBuild = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        boolean foundCompleteCombination = false;

        for (CPU cpu : cpus) {
            for (GPU gpu : gpus) {
                for (Motherboard board : boards) {
                    if (!same(cpu.getSocket(), board.getSocket())) continue;
                    for (RAM ram : memory) {
                        if (!same(ram.getMemoryType(), board.getRamType())
                                || ram.getCapacityGB() > board.getMaxRamGb()) continue;
                        for (Storage drive : drives) {
                            for (PowerSupply psu : psus) {
                                for (PCCase pcCase : cases) {
                                    if (!supports(pcCase, board.getFormFactor())
                                            || gpu.getLengthMm() > pcCase.getMaxGpuLengthMm()) continue;

                                    PCBuild candidate = new PCBuild("Recommended Build");
                                    candidate.setCpu(cpu);
                                    candidate.setGpu(gpu);
                                    candidate.setMotherboard(board);
                                    candidate.setRam(ram);
                                    candidate.setStorage(drive);
                                    candidate.setPowerSupply(psu);
                                    candidate.setPcCase(pcCase);

                                    double total = candidate.getTotalPrice();
                                    if (total < minBudget || total > maxBudget) continue;
                                    foundCompleteCombination = true;

                                    if (!checker.checkAllParts(candidate)) continue;

                                    double score = cpuScore(cpu, workload, preferredBrand)
                                            + gpuScore(gpu, workload, preferredBrand)
                                            + valueScore(board, preferredBrand)
                                            + ramScore(ram, workload, preferredBrand)
                                            + storageScore(drive, preferredBrand)
                                            + valueScore(psu, preferredBrand)
                                            + valueScore(pcCase, preferredBrand);
                                    if (score > bestScore) {
                                        bestScore = score;
                                        bestBuild = candidate;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (bestBuild == null) {
            if (!foundCompleteCombination) {
                throw new BudgetExceededException(
                        "No complete compatible build was found within PHP "
                                + String.format("%.2f - %.2f", minBudget, maxBudget) + "."
                );
            }
            throw new IncompatibleComponentException(
                    "The available components could not produce a compatible build."
            );
        }

        recommendedBuild = bestBuild;
        return recommendedBuild;
    }

    private <T extends Component> List<T> topParts(Class<T> type,
                                                    java.util.function.ToDoubleFunction<T> scorer) {
        List<T> parts = catalog.getPartsByType(type);
        parts.sort(Comparator.comparingDouble(scorer).reversed());
        if (parts.size() > CANDIDATES_PER_CATEGORY) {
            return new ArrayList<>(parts.subList(0, CANDIDATES_PER_CATEGORY));
        }
        return parts;
    }

    private double cpuScore(CPU cpu, Workload workload, String brand) {
        double weight = workload == Workload.VIDEO_EDITING ? 1.35
                : workload == Workload.GAMING ? 1.0 : 0.85;
        return (cpu.getPerformanceScore() * weight / Math.max(cpu.getPrice(), 1))
                * brandBonus(cpu, brand);
    }

    private double gpuScore(GPU gpu, Workload workload, String brand) {
        double weight = workload == Workload.GAMING ? 1.5
                : workload == Workload.VIDEO_EDITING ? 1.15 : 0.45;
        return (gpu.getPerformanceScore() * weight / Math.max(gpu.getPrice(), 1))
                * brandBonus(gpu, brand);
    }

    private double ramScore(RAM ram, Workload workload, String brand) {
        double capacityWeight = workload == Workload.VIDEO_EDITING ? 1.4 : 1.0;
        return ((ram.getCapacityGB() * capacityWeight + ram.getSpeedMHz() / 1000.0)
                / Math.max(ram.getPrice(), 1)) * brandBonus(ram, brand);
    }

    private double storageScore(Storage storage, String brand) {
        return (storage.getCapacityGB() / Math.max(storage.getPrice(), 1))
                * brandBonus(storage, brand);
    }

    private double valueScore(Component part, String brand) {
        return (1.0 / Math.max(part.getPrice(), 1)) * brandBonus(part, brand);
    }

    private double brandBonus(Component part, String preferredBrand) {
        if (preferredBrand != null && !preferredBrand.trim().isEmpty()
                && part.getBrand() != null
                && part.getBrand().equalsIgnoreCase(preferredBrand.trim())) {
            return 1.10;
        }
        return 1.0;
    }

    private boolean same(String first, String second) {
        return first != null && second != null && first.equalsIgnoreCase(second);
    }

    private boolean supports(PCCase pcCase, String formFactor) {
        if (pcCase.getSupportedFormFactors() == null || formFactor == null) return false;
        for (String supported : pcCase.getSupportedFormFactors()) {
            if (same(supported, formFactor)) return true;
        }
        return false;
    }

    public PCBuild getRecommendedBuild() {
        return recommendedBuild;
    }
        }
