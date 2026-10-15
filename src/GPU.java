public class GPU extends Component {

    private int vramGB;
    private int performanceScore;
    private int powerWatts;

    public GPU(String id, String name, String brand, double price, int vramGB, int performanceScore, int powerWatts) {
        super(id, name, brand, price);
        this.vramGB = vramGB;
        this.performanceScore = performanceScore;
        this.powerWatts = powerWatts;
    }

    public int getVramGB() {
        return vramGB;
    }

    public int getPerformanceScore() {
        return performanceScore;
    }

    public int getPowerWatts() {
        return powerWatts;
    }

    @Override
    public String getSpecs() {
        return vramGB + "GB VRAM, Performance Score: " + performanceScore + ", Power: " + powerWatts + "W";
    }

    @Override
    public boolean checkCompatibility(Object build) {
        return false;
    }
}