public class GPU extends Component {
    private final int vramGB;
    private final int powerWatts;
    private final int performanceScore;
    private final int lengthMm;

    public GPU(String id, String name, String brand, double price, int vramGB, int performanceScore, int powerWatts, int lengthMm) {
        super(id, name, brand, price);
        this.vramGB = vramGB;
        this.performanceScore = performanceScore;
        this.powerWatts = powerWatts;
        this.lengthMm = lengthMm;
    }

    public int getVramGB() {
        return vramGB;
    }

    public int getPowerWatts() {
        return powerWatts;
    }

    public int getPerformanceScore() {
        return performanceScore;
    }

    public int getLengthMm() {
        return lengthMm;
    }

    @Override
    public String getSpecs() {
        return "VRAM: " + vramGB + "GB | Power: " + powerWatts + "W | Score: " + performanceScore + " | Length: " + lengthMm + "mm";
    }

    @Override
    public boolean checkCompatibility(Object build) {
        // Compatibility validation is handled by CompatibilityChecker.
        return true;
    }
}
