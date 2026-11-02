public class GPU extends Component {
    private final String socket;
    private final int tdp;
    private final int performanceScore;

    public GPU(String id, String name, String brand, double price, String socket, int tdp, int performanceScore) {
        super(id, name, brand, price);
        this.socket = socket;
        this.tdp = tdp;
        this.performanceScore = performanceScore;
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


    @Override
    public String getSpecs() {
        return "Socket: " + socket + " | TDP: " + tdp + "W" + " | Score: " + performanceScore;
    }

    @Override
    public boolean checkCompatibility(Object build) {
        // Placeholder: actual compatibility checking is handled by CompatibilityChecker.
        return true;
    }
}
