import java.util.List;

public class PCCase extends Component {
    private List<String> supportedFormFactors; 
    private int maxGpuLengthMm;

    // Constructor
    public PCCase(String id, String name, String brand, double price, List<String> supportedFormFactors, int maxGpuLengthMm) {
        super(id, name, brand, price); 
        this.supportedFormFactors = supportedFormFactors;
        this.maxGpuLengthMm = maxGpuLengthMm;
    }

    // retrieves the case specs
    public List<String> getSupportedFormFactors() {
        return supportedFormFactors;
    }

    public int getMaxGpuLengthMm() {
        return maxGpuLengthMm;
    }

    @Override
    public String getSpecs() {
        return "Supported Form Factors: " + supportedFormFactors + " | Max GPU Length: " + maxGpuLengthMm + "mm";
    }

    @Override
    public boolean checkCompatibility(Object build) {

        return true;
    }
}