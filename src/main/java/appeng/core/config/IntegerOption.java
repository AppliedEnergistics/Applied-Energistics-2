package appeng.core.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class IntegerOption extends BaseOption {

    private final int defaultValue;
    private final int minValue;
    private final int maxValue;
    private int currentValue;

    public IntegerOption(ConfigSection parent, String id, String comment, int defaultValue, int minValue,
            int maxValue) {
        super(parent, id, comment);
        this.defaultValue = defaultValue;
        this.currentValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public int get() {
        return currentValue;
    }

    public void set(int value) {
        if (value == currentValue) {
            return;
        }
        checkValue(value);
        currentValue = value;
        parent.markDirty();
    }

    private void checkValue(int value) {
        if (value < minValue || value > maxValue) {
            StringBuilder rangeDescription = new StringBuilder();
            if (minValue != Integer.MIN_VALUE) {
                rangeDescription.append("min: ").append(minValue);
            }
            if (maxValue != Integer.MAX_VALUE) {
                if (rangeDescription.length() > 0) {
                    rangeDescription.append(", ");
                }
                rangeDescription.append("max: ").append(maxValue);
            }

            throw new ConfigValidationException(this, "Value out of range: " + value + " (" + rangeDescription + ")");
        }
    }

    @Override
    protected JsonElement write() {
        return new JsonPrimitive(currentValue);
    }

    @Override
    protected void read(JsonElement element) {
        if (!element.isJsonPrimitive()) {
            throw new ConfigValidationException(this, "Expected a JSON primitive, but found: " + element);
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isNumber()) {
            throw new ConfigValidationException(this, "Expected a JSON number, but found: " + primitive);
        }
        int value;
        try {
            value = primitive.getAsInt();
        } catch (NumberFormatException ignored) {
            throw new ConfigValidationException(this, "Expected an integer value, but found: " + primitive);
        }
        checkValue(value);
        currentValue = value;
    }

    @Override
    public boolean isDifferentFromDefault() {
        return currentValue != defaultValue;
    }

    @Override
    public String getDefaultAsString() {
        return String.valueOf(defaultValue);
    }

    @Override
    public String getCurrentValueAsString() {
        return String.valueOf(currentValue);
    }

}
