public class RAM extends Component {

    private final int capacityGB;
    private final String memoryType;
    private final int speedMHz;

    public RAM(String id, String name, String brand, double price, int capacityGB, String memoryType, int speedMHz) {
        super(id, name, brand, price);
        this.capacityGB = capacityGB;
        this.memoryType = memoryType;
        this.speedMHz = speedMHz;
    }

    public int getCapacityGB() {
        return capacityGB;
    }

    public String getMemoryType() {
        return memoryType;
    }

    public int getSpeedMHz() {
        return speedMHz;
    }

    @Override
    public String getSpecs() {
        return capacityGB + "GB " + memoryType + ", " + speedMHz + "MHz";
    }

    @Override
    public boolean checkCompatibility(Object build) {
        return false;
    }
}