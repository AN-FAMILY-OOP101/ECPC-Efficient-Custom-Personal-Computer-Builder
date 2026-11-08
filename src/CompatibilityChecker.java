public class CompatibilityChecker {

    // Main method: Runs each part's check and the build-level power check
    public boolean checkAllParts(PCBuild build) {
        if (build == null) return false;

        boolean socketPass = checkSocketCompatibility(build);
        boolean ramPass = checkRamCompatibility(build);
        boolean formFactorPass = checkFormFactorCompatibility(build);
        boolean gpuLengthPass = checkGpuClearance(build);
        boolean powerPass = checkPowerSufficiency(build);
        
        // Returns true only if all requirements pass
        return socketPass && ramPass && formFactorPass && gpuLengthPass && powerPass;
    }

    // Verifies CPU and Motherboard socket compatibility
    public boolean checkSocketCompatibility(PCBuild build) {
        if (build.getCpu() == null || build.getMotherboard() == null) {
            return false;
        }
        String cpuSocket = build.getCpu().getSocket();
        String mbSocket = build.getMotherboard().getSocket();
        
        if (cpuSocket == null || mbSocket == null) return false;
        return cpuSocket.equalsIgnoreCase(mbSocket);
    }

    // Verifies RAM type and capacity limits against the Motherboard
    public boolean checkRamCompatibility(PCBuild build) {
        if (build.getMotherboard() == null || build.getRam() == null) {
            return false;
        }
        
        String mbRamType = build.getMotherboard().getRamType();
        int maxRamGb = build.getMotherboard().getMaxRamGb();
        
        String ramType = build.getRam().getMemoryType();
        int ramCapacity = build.getRam().getCapacityGB();
        
        if (mbRamType == null || ramType == null) return false;
        
        // Check if memory type matches (DDR4 vs DDR5) and total capacity doesn't exceed board limit
        boolean typeMatches = mbRamType.equalsIgnoreCase(ramType);
        boolean capacityWithinLimit = ramCapacity <= maxRamGb;
        
        return typeMatches && capacityWithinLimit;
    }

    // Verifies Motherboard form factor against the PC Case supported form factors
    public boolean checkFormFactorCompatibility(PCBuild build) {
        if (build.getMotherboard() == null || build.getPcCase() == null) {
            return false;
        }
        
        String mbFormFactor = build.getMotherboard().getFormFactor();
        String[] supportedFactors = build.getPcCase().getSupportedFormFactors();
        
        if (mbFormFactor == null || supportedFactors == null) return false;
        
        for (String factor : supportedFactors) {
            if (factor.equalsIgnoreCase(mbFormFactor)) {
                return true;
            }
        }
        return false;
    }

    // Verifies GPU length against the PC Case clearance limit
    public boolean checkGpuClearance(PCBuild build) {
        // If the build doesn't include a dedicated GPU (using integrated graphics), pass clearance
        if (build.getGpu() == null) {
            return true;
        }
        if (build.getPcCase() == null) {
            return false;
        }
        
        int gpuLength = build.getGpu().getLengthMm();
        int maxCaseGpuLength = build.getPcCase().getMaxGpuLengthMm();
        
        return gpuLength <= maxCaseGpuLength;
    }

    // Validates that power supply wattage covers estimated power draw with a safety margin
    public boolean checkPowerSufficiency(PCBuild build) {
        if (build.getPsu() == null) {
            return false;
        }
        
        int totalEstimatedDraw = 0;
        
        // CPU power draw (using TDP)
        if (build.getCpu() != null) {
            totalEstimatedDraw += build.getCpu().getTdp();
        }
        
        // GPU power draw (using board power watts)
        if (build.getGpu() != null) {
            totalEstimatedDraw += build.getGpu().getPowerWatts();
        }
        
        // Base system overhead (Motherboard, RAM, SSD, fans ~ 75W)
        totalEstimatedDraw += 75;
        
        int psuWattage = build.getPsu().getWattage();
        
        // Apply a recommended PSU safety headroom factor (total load should not exceed 80% of PSU rated wattage, 
        // or PSU must comfortably exceed estimated power draw by at least a safety margin)
        double safetyMarginMultiplier = 1.25; 
        
        return (totalEstimatedDraw * safetyMarginMultiplier) <= psuWattage;
    }

    // Generates the final compatibility report with detailed status checks
    public String getCompatibilityReport(PCBuild build) {
        if (build == null) {
            return "Error: PCBuild instance is null.";
        }

        boolean socket = checkSocketCompatibility(build);
        boolean ram = checkRamCompatibility(build);
        boolean formFactor = checkFormFactorCompatibility(build);
        boolean gpuLength = checkGpuClearance(build);
        boolean power = checkPowerSufficiency(build);

        if (socket && ram && formFactor && gpuLength && power) {
            return "Compatibility Status: PASS. All components are fully compatible and power is sufficient.";
        } else {
            StringBuilder report = new StringBuilder("Compatibility Status: FAIL. Issues detected:\n");
            if (!socket) report.append("- CPU and Motherboard sockets do not match.\n");
            if (!ram) report.append("- RAM type mismatch or capacity exceeds motherboard limit.\n");
            if (!formFactor) report.append("- Motherboard form factor does not fit the selected PC case.\n");
            if (!gpuLength) report.append("- GPU length exceeds the case's maximum clearance limit.\n");
            if (!power) report.append("- Power Supply wattage is insufficient for estimated system power draw.\n");
            return report.toString().trim();
        }
    }
}