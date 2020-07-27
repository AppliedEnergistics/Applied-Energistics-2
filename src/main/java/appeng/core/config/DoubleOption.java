package appeng.core.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class DoubleOption extends BaseOption {

    private final double defaultValue;
    private final double minValue;
    private final double maxValue;
    private double currentValue;

    public DoubleOption(ConfigSection parent, String id, String comment, double defaultValue, double minValue,
            double maxValue) {
        super(parent, id, comment);
        this.defaultValue = defaultValue;
        this.currentValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public double get() {
        return currentValue;
    }

    public void set(double value) {
        if (value == currentValue) {
            return;
        }
        checkValue(value);
        currentValue = value;
        parent.markDirty();
    }

    private void checkValue(double value) {
        if (value < minValue || value > maxValue) {
            StringBuilder rangeDescription = new StringBuilder();
            if (minValue != Double.MIN_VALUE) {
                rangeDescription.append("min: ").append(minValue);
            }
            if (maxValue != Double.MAX_VALUE) {
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
        double value = primitive.getAsDouble();
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
