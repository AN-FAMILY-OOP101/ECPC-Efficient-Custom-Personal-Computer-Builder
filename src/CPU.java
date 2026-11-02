public class CPU extends Component {
    // CPU details
    private final String socket;
    private final int tdp; // Power usage in watts
    private final int performanceScore;
    private final boolean hasIGPU;

    public CPU(String id, String name, String brand, double price, String socket, int tdp, int performanceScore, boolean hasIGPU) {
        super(id, name, brand, price); // Passes basic info to the parent Component
        this.socket = socket;
        this.tdp = tdp;
        this.performanceScore = performanceScore;
        this.hasIGPU = hasIGPU;
    }

    // for CPU specs
    public String getSocket() { return socket; }
    public int getTdp() { return tdp; }
    public int getPerformanceScore() { return performanceScore; }
    public boolean hasIGPU() {return hasIGPU;}

    @Override
    public String getSpecs() {
        return "Socket: " + socket + " | TDP: " + tdp + "W | Score: " + performanceScore + " | Integrated Graphics: " + hasIGPU;
    }

    // checks if the CPU matches the motherboard socket
    @Override
    public boolean checkCompatibility(Object build) {
        return true;
    }
}
