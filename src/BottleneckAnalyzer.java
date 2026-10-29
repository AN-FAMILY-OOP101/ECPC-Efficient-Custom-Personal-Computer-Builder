public class BottleneckAnalyzer {

    private static final double CPU_MIN = 12523.0; // Intel Core i3-12100
    private static final double CPU_MAX = 68464.0; // Intel Core Ultra 7 270K Plus
    private static final double GPU_MIN = 9614.0;  // AMD Radeon RX 6500 XT
    private static final double GPU_MAX = 22619.0; // ASUS Dual GeForce RTX 5060 Ti OC 16GB

    private int cpuScore;
    private int gpuScore;
    private String workload;

    // Constructor to initialize scores and workload
    public BottleneckAnalyzer(int cpuScore, int gpuScore, String workload) {
        this.cpuScore = cpuScore;
        this.gpuScore = gpuScore;
        this.workload = workload;
    }

    // Normalizes CPU score to a relative 0-100 scale
    public double normalizeCpu() {
        if (CPU_MAX == CPU_MIN) return 0.0;
        return ((double) (cpuScore - CPU_MIN) / (CPU_MAX - CPU_MIN)) * 100.0;
    }

    // Normalizes GPU score to a relative 0-100 scale
    public double normalizeGpu() {
        if (GPU_MAX == GPU_MIN) return 0.0;
        return ((double) (gpuScore - GPU_MIN) / (GPU_MAX - GPU_MIN)) * 100.0;
    }

    public double calculateDifference() {
        double nCpu = normalizeCpu();
        double nGpu = normalizeGpu();
        return Math.abs(nCpu - nGpu);
    }

    // Determines the bottleneck category
    public String getBottleneckLevel() {
        double diff = calculateDifference();

        if (diff <= 5.0) {
            return "Excellent balance";
        } else if (diff <= 10.0) {
            return "Good balance";
        } else if (diff <= 20.0) {
            return "Moderate imbalance";
        } else if (diff <= 30.0) {
            return "High imbalance";
        } else {
            return "Severe imbalance";
        }
    }

    // Generates the final report
    public String getBottleneckReport() {
        if (cpuScore <= 0 || gpuScore <= 0) {
            return "Error: Performance scores must be loaded first.";
        }

        String level = getBottleneckLevel();
        double diff = calculateDifference();

        return "Workload: " + workload + " | Status: " + level 
                + " (Normalized Difference: " + String.format("%.2f", diff) + "%).";
    }
}