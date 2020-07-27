package appeng.core.config;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class EnumOption<T extends Enum<T>> extends BaseOption {

    private final T defaultValue;
    private T currentValue;

    public EnumOption(ConfigSection parent, String id, String comment, T defaultValue) {
        super(parent, id, comment);
        this.defaultValue = defaultValue;
        this.currentValue = defaultValue;
    }

    public T get() {
        return currentValue;
    }

    public void set(T value) {
        Preconditions.checkNotNull(value);
        if (value == currentValue) {
            return;
        }
        currentValue = value;
        parent.markDirty();
    }

    @Override
    protected JsonElement write() {
        return new JsonPrimitive(currentValue.name().toLowerCase());
    }

    @Override
    protected void read(JsonElement element) {
        if (!element.isJsonPrimitive()) {
            throw new ConfigValidationException(this, "Expected a JSON primitive, but found: " + element);
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isString()) {
            throw new ConfigValidationException(this, "Expected a JSON string, but found: " + primitive);
        }
        String enumName = primitive.getAsString();

        T[] enumConstants = defaultValue.getDeclaringClass().getEnumConstants();
        for (T enumConstant : enumConstants) {
            if (enumConstant.name().equalsIgnoreCase(enumName)) {
                currentValue = enumConstant;
                return;
            }
        }

        String allowedValues = Arrays.stream(enumConstants).map(e -> e.name().toLowerCase())
                .collect(Collectors.joining(", "));
        throw new ConfigValidationException(this, "Expected one of: " + allowedValues);
    }

    @Override
    public boolean isDifferentFromDefault() {
        return currentValue != defaultValue;
    }

    @Override
    public String getDefaultAsString() {
        return String.valueOf(defaultValue.name().toLowerCase());
    }

    @Override
    public String getCurrentValueAsString() {
        return String.valueOf(currentValue.name().toLowerCase());
    }

}
