package ecpc.features;

import ecpc.exceptions.*;

import java.util.Locale;

/**
 * Estimates CPU/GPU balance using a ratio of the catalog's published benchmark scores.
 * CPU Mark and G3D Mark are different benchmark scales, so this is a broad pairing heuristic, not a measured gaming bottleneck percentage.
 */
public class BottleneckAnalyzer {

    // CPU Mark and G3D Mark are different scales.
    // This ECPC reference ratio avoids treating equal raw numbers as an inherently balanced pairing.
    private static final double BALANCED_CPU_GPU_RATIO = 1.30;

    private final int cpuScore;
    private final int gpuScore;
    private final Workload workload;

    public BottleneckAnalyzer(int cpuScore, int gpuScore, Workload workload) {
        if (workload == null) {
            throw new InvalidInputException("Workload cannot be null.");
        }
        if (cpuScore <= 0 || gpuScore <= 0) {
            throw new InvalidInputException("CPU and GPU performance scores must be positive.");
        }
        this.cpuScore = cpuScore;
        this.gpuScore = gpuScore;
        this.workload = workload;
    }

    /** Compatibility constructor for older code that supplies a String. */
    @Deprecated
    public BottleneckAnalyzer(int cpuScore, int gpuScore, String workload) {
        this(cpuScore, gpuScore, parseWorkload(workload));
    }

    private static Workload parseWorkload(String workload) {
        if (workload == null || workload.trim().isEmpty()) {
            throw new InvalidInputException("Workload cannot be null or blank.");
        }
        String normalized = workload.trim().toUpperCase(Locale.ROOT)
                .replace(' ', '_').replace('-', '_');
        try {
            return Workload.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new InvalidInputException("Unsupported workload: " + workload
                    + ". Use SCHOOLWORK, GAMING, or VIDEO_EDITING.");
        }
    }

    /** CPU-to-GPU benchmark ratio. Values near 1 indicate closer scores. */
    public double calculateRatio() {
        return (double) cpuScore / gpuScore;
    }

    /**
     * Symmetric distance from ECPC's reference ratio.
     * A value of 1.0 is the reference pairing; larger values indicate a wider mismatch.
     */
    public double calculateDifference() {
        double relativeRatio = calculateRatio() / BALANCED_CPU_GPU_RATIO;
        return Math.max(relativeRatio, 1.0 / relativeRatio);
    }

    public String getBottleneckLevel() {
        double imbalance = calculateDifference();
        if (imbalance <= 1.15) return "Excellent balance";
        if (imbalance <= 1.40) return "Good balance";
        if (imbalance <= 1.90) return "Slight imbalance";
        if (imbalance <= 2.60) return "Moderate imbalance";
        if (imbalance <= 3.50) return "High imbalance";
        return "Severe imbalance";
    }

  public String getBottleneckReport() {
        double ratio = calculateRatio();
        String strongerPart = ratio < 1.0 ? "GPU score is higher" : "CPU score is higher";
        return "Workload: " + workload
                + " | Status: " + getBottleneckLevel()
                + " | CPU/GPU score ratio: "
                + String.format(Locale.ROOT, "%.2f", ratio)
                + " | Relative mismatch: "
                + String.format(Locale.ROOT, "%.2f", calculateDifference())
                + " | " + strongerPart + ".";
    }

    public Workload getWorkload() { return workload; }
    public int getCpuScore() { return cpuScore; }
    public int getGpuScore() { return gpuScore; }
}