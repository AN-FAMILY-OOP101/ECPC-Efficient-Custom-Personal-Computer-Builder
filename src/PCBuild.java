import java.util.ArrayList;
import java.util.List;

public class PCBuild {

    private CPU cpu;
    private Motherboard motherboard;
    private GPU gpu;
    private RAM ram;
    private Storage storage;
    private PowerSupply powerSupply;
    private PCCase pcCase;

    public PCBuild() {
    }

    public CPU getCpu() {
        return cpu;
    }

    public void setCpu(CPU cpu) {
        this.cpu = cpu;
    }

    public Motherboard getMotherboard() {
        return motherboard;
    }

    public void setMotherboard(Motherboard motherboard) {
        this.motherboard = motherboard;
    }

    public GPU getGpu() {
        return gpu;
    }

    public void setGpu(GPU gpu) {
        this.gpu = gpu;
    }

    public RAM getRam() {
        return ram;
    }

    public void setRam(RAM ram) {
        this.ram = ram;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public PowerSupply getPowerSupply() {
        return powerSupply;
    }

    public void setPowerSupply(PowerSupply powerSupply) {
        this.powerSupply = powerSupply;
    }

    public PCCase getPcCase() {
        return pcCase;
    }

    public void setPcCase(PCCase pcCase) {
        this.pcCase = pcCase;
    }

    public double getTotalPrice() {
        double total = 0;

        if (cpu != null) total += cpu.getPrice();
        if (motherboard != null) total += motherboard.getPrice();
        if (gpu != null) total += gpu.getPrice();
        if (ram != null) total += ram.getPrice();
        if (storage != null) total += storage.getPrice();
        if (powerSupply != null) total += powerSupply.getPrice();
        if (pcCase != null) total += pcCase.getPrice();

        return total;
    }

    public List<Component> getSelectedParts() {
        List<Component> parts = new ArrayList<>();

        if (cpu != null) parts.add(cpu);
        if (motherboard != null) parts.add(motherboard);
        if (gpu != null) parts.add(gpu);
        if (ram != null) parts.add(ram);
        if (storage != null) parts.add(storage);
        if (powerSupply != null) parts.add(powerSupply);
        if (pcCase != null) parts.add(pcCase);

        return parts;
    }
}