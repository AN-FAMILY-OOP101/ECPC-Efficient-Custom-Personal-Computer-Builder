package ecpc.features;

import ecpc.components.*;
import ecpc.exceptions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * The ECPC hardware catalog. All parts are kept in one ArrayList<Component>.
 * It can find, search and filter parts, and load them from Components.json
 * (a small JSON reader is included at the bottom, so no library is needed).
 *
 * Invalid operations throw InvalidInputException. An unreadable or malformed
 * file throws IOException.
 */
public class PartCatalog {

    private final List<Component> components = new ArrayList<>();
    private final List<String> loadWarnings = new ArrayList<>();

    // ------------------------------------------------------------------
    // Adding and finding parts
    // ------------------------------------------------------------------

    /** Adds a part after checking its ID, name, brand and price, and that its ID is unique. */
    public void addPart(Component part) {
        if (part == null) {
            throw new InvalidInputException("Cannot add a null component.");
        }
        if (isBlank(part.getId()) || isBlank(part.getName()) || isBlank(part.getBrand())) {
            throw new InvalidInputException("Component ID, name and brand cannot be empty.");
        }
        if (!Double.isFinite(part.getPrice()) || part.getPrice() < 0) {
            throw new InvalidInputException("Invalid price for component " + part.getId());
        }
        if (findById(part.getId()) != null) {
            throw new InvalidInputException("Duplicate component ID: " + part.getId());
        }
        components.add(part);
    }

    /** Returns a copy, so callers cannot change the catalog by accident. */
    public List<Component> getAllParts() {
        return new ArrayList<>(components);
    }

    public int size() {
        return components.size();
    }

    /** Returns the part with this ID (ignoring upper/lower case), or null if there is none. */
    public Component findById(String id) {
        if (isBlank(id)) {
            return null;
        }
        for (Component part : components) {
            if (part.getId().equalsIgnoreCase(id.trim())) {
                return part;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Searching and filtering. Every list is sorted from cheapest to most expensive.
    // ------------------------------------------------------------------

    /** Parts of one class inside an inclusive price range, e.g. (GPU.class, 20000, 35000). */
    public <T extends Component> List<T> filterByTypeAndPrice(Class<T> type, double min, double max) {
        if (type == null) {
            throw new InvalidInputException("Component type cannot be null.");
        }
        if (!Double.isFinite(min) || !Double.isFinite(max) || min < 0 || max < 0) {
            throw new InvalidInputException("Price limits must be non-negative numbers.");
        }
        if (min > max) {
            throw new InvalidInputException("Minimum price cannot be greater than maximum price.");
        }

        List<T> result = new ArrayList<>();
        for (Component part : components) {
            if (type.isInstance(part) && part.getPrice() >= min && part.getPrice() <= max) {
                result.add(type.cast(part));
            }
        }
        result.sort(Comparator.comparingDouble(Component::getPrice));
        return result;
    }

    /** All parts of one class, e.g. getPartsByType(CPU.class). */
    public <T extends Component> List<T> getPartsByType(Class<T> type) {
        return filterByTypeAndPrice(type, 0, Double.MAX_VALUE);
    }

    /** Parts of any class inside an inclusive price range. */
    public List<Component> filterByPrice(double min, double max) {
        return filterByTypeAndPrice(Component.class, min, max);
    }

    /** Parts of one class made by one brand (ignoring upper/lower case). */
    public <T extends Component> List<T> filterByTypeAndBrand(Class<T> type, String brand) {
        List<T> ofType = getPartsByType(type);
        if (isBlank(brand)) {
            throw new InvalidInputException("Brand cannot be empty.");
        }
        List<T> result = new ArrayList<>();
        for (T part : ofType) {
            if (part.getBrand().equalsIgnoreCase(brand.trim())) {
                result.add(part);
            }
        }
        return result;
    }

    /** The cheapest part of one class, or null if there is none. */
    public <T extends Component> T getCheapest(Class<T> type) {
        List<T> ofType = getPartsByType(type);
        return ofType.isEmpty() ? null : ofType.get(0);
    }

    private static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    // ------------------------------------------------------------------
    // Loading from Components.json
    // ------------------------------------------------------------------

    /**
     * Replaces the catalog with the parts in the JSON file.
     * If the file cannot be read or is not valid JSON, an IOException is thrown and the
     * catalog stays as it was. A single bad part is skipped and explained in getLoadWarnings().
     *
     * @return the number of parts loaded
     */
    public int loadFromFile(String filePath) throws IOException {
        if (isBlank(filePath)) {
            throw new InvalidInputException("Catalog file path cannot be empty.");
        }
        String json = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        Object root = new JsonReader(json).parse(); // a problem here throws before the catalog is touched

        if (!(root instanceof Map) || !(((Map<?, ?>) root).get("components") instanceof List)) {
            throw new IOException("Catalog JSON must be an object with a 'components' array.");
        }
        List<?> entries = (List<?>) ((Map<?, ?>) root).get("components");

        components.clear();
        loadWarnings.clear();
        for (int i = 0; i < entries.size(); i++) {
            try {
                if (!(entries.get(i) instanceof Map)) {
                    throw new InvalidInputException("Component entry must be a JSON object.");
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) entries.get(i);
                addPart(createComponent(data)); // addPart also rejects duplicate IDs
            } catch (InvalidInputException | IllegalArgumentException e) {
                loadWarnings.add("Entry #" + (i + 1) + " skipped: " + e.getMessage());
            }
        }
        return components.size();
    }

    /** Parts that were skipped by the last loadFromFile(), and why (empty if all loaded). */
    public List<String> getLoadWarnings() {
        return new ArrayList<>(loadWarnings);
    }

    /** Builds the right Component subclass from one JSON entry. */
    private static Component createComponent(Map<String, Object> data) {
        String type = text(data, "type");
        String id = text(data, "id");
        String name = text(data, "name");
        String brand = text(data, "brand");
        double price = number(data, "price");

        switch (type) {
            case "CPU":
                return new CPU(id, name, brand, price, text(data, "socket"),
                        positiveInt(data, "tdp"), positiveInt(data, "performanceScore"),
                        bool(data, "integratedGraphics"));
            case "Motherboard":
                return new Motherboard(id, name, brand, price, text(data, "socket"),
                        text(data, "ramType"), positiveInt(data, "maxRamGb"), text(data, "formFactor"));
            case "GPU":
                return new GPU(id, name, brand, price, positiveInt(data, "vramGB"),
                        positiveInt(data, "performanceScore"), positiveInt(data, "powerWatts"),
                        positiveInt(data, "lengthMm"));
            case "RAM":
                return new RAM(id, name, brand, price, positiveInt(data, "capacityGB"),
                        text(data, "memoryType"), positiveInt(data, "speedMHz"));
            case "Storage":
                return new Storage(id, name, brand, price, positiveInt(data, "capacityGB"),
                        text(data, "storageType"), text(data, "interfaceType"));
            case "PowerSupply":
                return new PowerSupply(id, name, brand, price, positiveInt(data, "wattage"),
                        text(data, "efficiencyRating"), text(data, "modularity"));
            case "PCCase":
                return new PCCase(id, name, brand, price, textList(data, "supportedFormFactors"),
                        positiveInt(data, "maxGpuLengthMm"));
            default:
                throw new InvalidInputException("Unsupported component type: " + type + " (ID: " + id + ")");
        }
    }

    // ---- Each helper reads one field and explains clearly if it is missing or wrong ----

    private static String text(Map<String, Object> data, String field) {
        Object value = data.get(field);
        if (!(value instanceof String) || ((String) value).trim().isEmpty()) {
            throw new InvalidInputException("Missing or invalid '" + field + "'" + where(data));
        }
        return ((String) value).trim();
    }

    private static double number(Map<String, Object> data, String field) {
        Object value = data.get(field);
        if (!(value instanceof Number) || !Double.isFinite(((Number) value).doubleValue())) {
            throw new InvalidInputException("Missing or invalid '" + field + "'" + where(data));
        }
        return ((Number) value).doubleValue();
    }

    private static int positiveInt(Map<String, Object> data, String field) {
        double value = number(data, field);
        if (value <= 0 || value != Math.rint(value) || value > Integer.MAX_VALUE) {
            throw new InvalidInputException("'" + field + "' must be a positive whole number" + where(data));
        }
        return (int) value;
    }

    private static boolean bool(Map<String, Object> data, String field) {
        if (!(data.get(field) instanceof Boolean)) {
            throw new InvalidInputException("Missing or invalid true/false field '" + field + "'" + where(data));
        }
        return (Boolean) data.get(field);
    }

    private static List<String> textList(Map<String, Object> data, String field) {
        Object value = data.get(field);
        if (!(value instanceof List) || ((List<?>) value).isEmpty()) {
            throw new InvalidInputException("Missing or empty list '" + field + "'" + where(data));
        }
        List<String> result = new ArrayList<>();
        for (Object item : (List<?>) value) {
            if (!(item instanceof String) || ((String) item).trim().isEmpty()) {
                throw new InvalidInputException("Invalid text in '" + field + "'" + where(data));
            }
            result.add(((String) item).trim());
        }
        return result;
    }

    // Adds " (ID: CPU-001)" to an error message so the bad entry is easy to find
    private static String where(Map<String, Object> data) {
        return data.get("id") instanceof String ? " (ID: " + data.get("id") + ")" : "";
    }

    // ------------------------------------------------------------------
    // A small JSON reader (no library needed).
    // JSON objects become Map, arrays become List, numbers become Double,
    // true/false become Boolean, and text becomes String.
    // ------------------------------------------------------------------

    private static final class JsonReader {
        private final String text;
        private int pos = 0; // the position we are reading at

        private JsonReader(String text) {
            this.text = text;
        }

        private Object parse() throws IOException {
            if (text.startsWith("\uFEFF")) {
                pos++; // some editors put an invisible marker at the start of the file
            }
            Object value = readValue();
            skipSpaces();
            if (pos != text.length()) {
                throw error("Unexpected text after the JSON value");
            }
            return value;
        }

        private Object readValue() throws IOException {
            skipSpaces();
            if (pos >= text.length()) {
                throw error("Expected a JSON value");
            }
            char c = text.charAt(pos);
            if (c == '{') return readObject();
            if (c == '[') return readArray();
            if (c == '"') return readString();
            if (c == 't') { readWord("true"); return Boolean.TRUE; }
            if (c == 'f') { readWord("false"); return Boolean.FALSE; }
            if (c == 'n') { readWord("null"); return null; }
            if (c == '-' || (c >= '0' && c <= '9')) return readNumber();
            throw error("Unexpected character '" + c + "'");
        }

        private Map<String, Object> readObject() throws IOException {
            Map<String, Object> fields = new LinkedHashMap<>();
            pos++; // the opening {
            skipSpaces();
            if (consume('}')) return fields;

            while (true) {
                skipSpaces();
                if (pos >= text.length() || text.charAt(pos) != '"') {
                    throw error("Field names must be in quotes");
                }
                String name = readString();
                skipSpaces();
                expect(':');
                if (fields.containsKey(name)) {
                    throw error("Duplicate field '" + name + "'");
                }
                fields.put(name, readValue());
                skipSpaces();
                if (consume('}')) return fields;
                expect(',');
            }
        }

        private List<Object> readArray() throws IOException {
            List<Object> items = new ArrayList<>();
            pos++; // the opening [
            skipSpaces();
            if (consume(']')) return items;

            while (true) {
                items.add(readValue());
                skipSpaces();
                if (consume(']')) return items;
                expect(',');
            }
        }

        private String readString() throws IOException {
            expect('"');
            StringBuilder result = new StringBuilder();
            while (pos < text.length()) {
                char c = text.charAt(pos++);
                if (c == '"') return result.toString();
                if (c != '\\') {
                    result.append(c);
                } else if (pos < text.length()) {
                    char escape = text.charAt(pos++);
                    int k = "\"\\/bfnrt".indexOf(escape); // \" \\ \/ \b \f \n \r \t
                    if (k >= 0) {
                        result.append("\"\\/\b\f\n\r\t".charAt(k));
                    } else if (escape == 'u' && pos + 4 <= text.length()) {
                        try {
                            result.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                        } catch (NumberFormatException e) {
                            throw error("Invalid \\u escape");
                        }
                        pos += 4;
                    } else {
                        throw error("Invalid escape sequence");
                    }
                }
            }
            throw error("Unterminated text");
        }

        private Double readNumber() throws IOException {
            int start = pos;
            while (pos < text.length() && "+-0123456789.eE".indexOf(text.charAt(pos)) >= 0) {
                pos++;
            }
            try {
                return Double.valueOf(text.substring(start, pos));
            } catch (NumberFormatException e) {
                throw error("Invalid number");
            }
        }

        private void readWord(String word) throws IOException {
            if (!text.startsWith(word, pos)) {
                throw error("Expected '" + word + "'");
            }
            pos += word.length();
        }

        private void skipSpaces() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }

        // If the next character is "c", moves past it and returns true
        private boolean consume(char c) {
            if (pos < text.length() && text.charAt(pos) == c) {
                pos++;
                return true;
            }
            return false;
        }

        private void expect(char c) throws IOException {
            if (!consume(c)) {
                throw error("Expected '" + c + "'");
            }
        }

        private IOException error(String message) {
            return new IOException(message + " at character " + pos);
        }
    }
}