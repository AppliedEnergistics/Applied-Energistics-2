package appeng.core.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanOption extends BaseOption {

    private final boolean defaultValue;
    private boolean currentValue;

    public BooleanOption(ConfigSection parent, String id, String comment, boolean defaultValue) {
        super(parent, id, comment);
        this.defaultValue = defaultValue;
        this.currentValue = defaultValue;
    }

    public boolean get() {
        return currentValue;
    }

    public void set(boolean value) {
        if (value == currentValue) {
            return;
        }
        currentValue = value;
        parent.markDirty();
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
        if (!primitive.isBoolean()) {
            throw new ConfigValidationException(this, "Expected a JSON boolean, but found: " + primitive);
        }
        currentValue = primitive.getAsBoolean();
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
