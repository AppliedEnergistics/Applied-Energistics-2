package appeng.core.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class StringOption extends BaseOption {

    private final String defaultValue;
    private String currentValue;

    public StringOption(ConfigSection parent, String id, String comment, String defaultValue) {
        super(parent, id, comment);
        this.defaultValue = defaultValue;
        this.currentValue = this.defaultValue;
    }

    public String get() {
        return currentValue;
    }

    public void set(String value) {
        Preconditions.checkNotNull(value);
        if (value.equals(currentValue)) {
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
            throw new ConfigValidationException(this, "Expected a JSON primitive: " + element);
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isString()) {
            throw new ConfigValidationException(this, "Expected a JSON string, but found: " + primitive);
        }

        this.currentValue = primitive.getAsString();
    }

    @Override
    public boolean isDifferentFromDefault() {
        return !currentValue.equals(defaultValue);
    }

    @Override
    public String getDefaultAsString() {
        return defaultValue;
    }

    @Override
    public String getCurrentValueAsString() {
        return currentValue;
    }
}
