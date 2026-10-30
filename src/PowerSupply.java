public class PowerSupply extends Component {
    private int wattage;
    private String efficiencyRating;
    private String modularity;

    public PowerSupply(String id, String name, String brand, double price, int wattage, String efficiencyRating, String modularity) {
        super(id, name, brand, price);
        this.wattage = wattage;
        this.efficiencyRating = efficiencyRating;
        this.modularity = modularity;
    }

    public int getWattage() {
        return wattage;
    }

    public String getEfficiencyRating() {
        return efficiencyRating;
    }

    public String getModularity() {
        return modularity;
    }

    @Override
    public String getSpecs() {
        return wattage + "W" + " | Efficiency Rating: " + efficiencyRating + " | Modularity: " + modularity;
    }

    @Override
    public boolean checkCompatibility(Object build) {
        // Placeholder: actual compatibility checking is handled by CompatibilityChecker.
        return false;
    }
}
