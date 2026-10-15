public class CPU extends Component {
    // CPU details
    private String socket;
    private int tdp; // Power usage in watts
    private int performanceScore;
    private boolean iGPU;

    public CPU(String id, String name, String brand, double price, String socket, int tdp, int performanceScore, boolean iGPU) {
        super(id, name, brand, price); // Passes basic info to the parent Component
        this.socket = socket;
        this.tdp = tdp;
        this.performanceScore = performanceScore;
        this.iGPU = iGPU;
    }

    // for CPU specs
    public String getSocket() { return socket; }
    public int getTdp() { return tdp; }
    public int getPerformanceScore() { return performanceScore; }
    public boolean hasIGPU() {return iGPU}

    @Override
    public String getSpecs() {
        return "Socket: " + socket + " | TDP: " + tdp + "W | Score: " + performanceScore + " | Integrated Graphics: " + iGPU;
    }

    // checks if the CPU matches the motherboard socket
    @Override
    public boolean checkCompatibility(Object build) {
        return true;
    }
}