import java.util.Locale;

/**
 * Analyzes the balance between a PC's CPU and GPU for a selected workload.
 */
public class BottleneckAnalyzer {

    private static final double CPU_MIN = 12523.0;
    private static final double CPU_MAX = 68464.0;
    private static final double GPU_MIN = 9614.0;
    private static final double GPU_MAX = 22619.0;

    private final int cpuScore;
    private final int gpuScore;
    private final Workload workload;

    /** Creates an analyzer using the project's Workload enum. */
    public BottleneckAnalyzer(int cpuScore, int gpuScore, Workload workload) {
        if (workload == null) {
            throw new InvalidInputException("Workload cannot be null.");
        }

        this.cpuScore = cpuScore;
        this.gpuScore = gpuScore;
        this.workload = workload;
    }

    /**
     * Compatibility constructor for older code that still supplies a String.
     * New code should pass a Workload enum instead.
     */
    @Deprecated
    public BottleneckAnalyzer(int cpuScore, int gpuScore, String workload) {
        this(cpuScore, gpuScore, parseWorkload(workload));
    }

    private static Workload parseWorkload(String workload) {
        if (workload == null || workload.trim().isEmpty()) {
            throw new InvalidInputException("Workload cannot be null or blank.");
        }

        String normalized = workload.trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_');

        try {
            return Workload.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new InvalidInputException(
                    "Unsupported workload: " + workload
                    + ". Use SCHOOLWORK, GAMING, or VIDEO_EDITING."
            );
        }
    }

    /** Normalizes the CPU score to a relative 0-100 scale. */
    public double normalizeCpu() {
        if (CPU_MAX == CPU_MIN) {
            return 0.0;
        }

        return ((cpuScore - CPU_MIN) / (CPU_MAX - CPU_MIN)) * 100.0;
    }

    /** Normalizes the GPU score to a relative 0-100 scale. */
    public double normalizeGpu() {
        if (GPU_MAX == GPU_MIN) {
            return 0.0;
        }

        return ((gpuScore - GPU_MIN) / (GPU_MAX - GPU_MIN)) * 100.0;
    }

    /** Returns the absolute difference between normalized CPU and GPU scores. */
    public double calculateDifference() {
        return Math.abs(normalizeCpu() - normalizeGpu());
    }

    /** Determines the balance category based on the normalized score difference. */
    public String getBottleneckLevel() {
        double difference = calculateDifference();

        if (difference <= 5.0) {
            return "Excellent balance";
        } else if (difference <= 10.0) {
            return "Good balance";
        } else if (difference <= 20.0) {
            return "Moderate imbalance";
        } else if (difference <= 30.0) {
            return "High imbalance";
        } else {
            return "Severe imbalance";
        }
    }

    /** Generates a readable bottleneck analysis report. */
    public String getBottleneckReport() {
        if (cpuScore <= 0 || gpuScore <= 0) {
            return "Error: Performance scores must be loaded first.";
        }

        String level = getBottleneckLevel();
        double difference = calculateDifference();

        return "Workload: " + workload
                + " | Status: " + level
                + " (Normalized Difference: "
                + String.format(Locale.ROOT, "%.2f", difference) + "%).";
    }

    public Workload getWorkload() {
        return workload;
    }

    public int getCpuScore() {
        return cpuScore;
    }

    public int getGpuScore() {
        return gpuScore;
    }
}
