public class CPU extends Component {
    private String socket;
    private int tdp;
    private int performanceScore;
    private boolean iGPU;

    public CPU(String id, String name, String brand, double price, String socket, int tdp, int performanceScore, boolean iGPU) {
        super(id, name, brand, price);
        this.socket = socket;
        this.tdp = tdp;
        this.performanceScore = performanceScore;
        this.iGPU = iGPU;
    }

    public String getSocket() {
        return socket;
    }

    public int getTdp() {
        return tdp;
    }

    public int getPerformanceScore() {
        return performanceScore;
    }

    public boolean hasIGPU() {
        return iGPU;
    }

    @Override
    public String getSpecs() {
        return "Socket: " + socket + " | TDP: " + tdp + "W" + " | Score: " + performanceScore + " | Integrated Graphics: " + iGPU;
    }

    @Override
    public boolean checkCompatibility(Object build) {
        // Placeholder: actual compatibility checking is handled by CompatibilityChecker.
        return true;
    }
}
