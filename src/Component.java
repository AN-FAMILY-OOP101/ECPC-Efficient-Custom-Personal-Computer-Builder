public abstract class Component {
    // Basic details every PC part needs
    private final String id;
    private final String name;
    private final String brand;
    private final double price;

    // Constructor to set up the parts when created
    public Component(String id, String name, String brand, double price) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.price = price;
    }

    // Encapsulation
    public String getId() { return id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public double getPrice() { return price; }

    // Polymorphism
    public abstract String getSpecs();
    public abstract boolean checkCompatibility(Object build);
}