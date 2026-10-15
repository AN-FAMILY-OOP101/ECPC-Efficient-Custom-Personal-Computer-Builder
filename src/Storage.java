public class Storage extends Component {

    private int capacityGB;
    private String storageType;
    private String interfaceType;

    public Storage(String id, String name, String brand, double price, int capacityGB, String storageType, String interfaceType) {
        super(id, name, brand, price);
        this.capacityGB = capacityGB;
        this.storageType = storageType;
        this.interfaceType = interfaceType;
    }

    public int getCapacityGB() {
        return capacityGB;
    }

    public String getStorageType() {
        return storageType;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    @Override
    public String getSpecs() {
        return capacityGB + "GB " + storageType + ", Interface: " + interfaceType;
    }

    @Override
    public boolean checkCompatibility(Object build) {
        return false;
    }
}