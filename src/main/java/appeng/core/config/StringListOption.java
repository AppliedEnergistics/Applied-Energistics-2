package appeng.core.config;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class StringListOption extends BaseOption {

    private final List<String> defaultValue;
    private List<String> currentValue;

    public StringListOption(ConfigSection parent, String id, String comment, List<String> defaultValue) {
        super(parent, id, comment);
        this.defaultValue = ImmutableList.copyOf(defaultValue);
        this.currentValue = this.defaultValue;
    }

    public List<String> get() {
        return currentValue;
    }

    public void set(List<String> value) {
        Preconditions.checkNotNull(value);
        if (value.equals(currentValue)) {
            return;
        }
        currentValue = ImmutableList.copyOf(value);
        parent.markDirty();
    }

    @Override
    protected JsonElement write() {
        JsonArray arr = new JsonArray();
        for (String s : currentValue) {
            arr.add(s);
        }
        return arr;
    }

    @Override
    protected void read(JsonElement element) {
        if (!element.isJsonArray()) {
            throw new ConfigValidationException(this, "Expected a JSON array, but found: " + element);
        }
        JsonArray array = element.getAsJsonArray();
        List<String> values = new ArrayList<>(array.size());
        for (JsonElement arrEl : array) {
            if (!arrEl.isJsonPrimitive()) {
                throw new ConfigValidationException(this, "Expected a JSON array of strings, but found: " + arrEl);
            }
            JsonPrimitive primitive = arrEl.getAsJsonPrimitive();
            if (!primitive.isString()) {
                throw new ConfigValidationException(this, "Expected a JSON array of strings, but found: " + arrEl);
            }
            values.add(primitive.getAsString());
        }

        this.currentValue = ImmutableList.copyOf(values);
    }

    @Override
    public boolean isDifferentFromDefault() {
        return currentValue != defaultValue;
    }

    @Override
    public String getDefaultAsString() {
        return String.join(",", defaultValue);
    }

    @Override
    public String getCurrentValueAsString() {
        return String.join(",", currentValue);
    }
}
