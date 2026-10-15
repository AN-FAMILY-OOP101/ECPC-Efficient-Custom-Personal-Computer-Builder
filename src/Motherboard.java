public class Motherboard extends Component {
    // Motherboard details
    private String socket;
    private String ramType; // DDR4 or DDR5
    private int maxRamGb;
    private String formFactor; // ATX, mATX, or sizes of motherboard

    public Motherboard(String id, String name, String brand, double price, String socket, String ramType, int maxRamGb, String formFactor) {
        super(id, name, brand, price);
        this.socket = socket;
        this.ramType = ramType;
        this.maxRamGb = maxRamGb;
        this.formFactor = formFactor;
    }

    // Motherboard specs
    public String getSocket() { return socket; }
    public String getRamType() { return ramType; }
    public int getMaxRamGb() { return maxRamGb; }
    public String getFormFactor() { return formFactor; }

    @Override
    public String getSpecs() {
        return "Socket: " + socket + " | RAM: " + ramType + " (Max " + maxRamGb + "GB) | Form Factor: " + formFactor;
    }

    @Override
    public boolean checkCompatibility(Object build) {
        
        return true;
    }
}