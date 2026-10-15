public class PowerSupply extends Component {

    private int wattage;
    private String efficiencyRating;

    public PowerSupply(String id, String name, String brand, double price, int wattage, String efficiencyRating) {
        super(id, name, brand, price);
        this.wattage = wattage;
        this.efficiencyRating = efficiencyRating;
    }

    public int getWattage() {
        return wattage;
    }

    public String getEfficiencyRating() {
        return efficiencyRating;
    }

    @Override
    public String getSpecs() {
        return wattage + "W, Efficiency Rating: " + efficiencyRating;
    }

    @Override
    public boolean checkCompatibility(Object build) {
        return false;
    }
}