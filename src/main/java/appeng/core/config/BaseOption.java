package appeng.core.config;

import com.google.gson.JsonElement;

import org.jetbrains.annotations.Nullable;

public abstract class BaseOption {

    protected final ConfigSection parent;

    protected final String id;

    @Nullable
    protected final String comment;

    public BaseOption(ConfigSection parent, String id, @Nullable String comment) {
        this.parent = parent;
        this.id = id;
        this.comment = comment;
    }

    protected abstract JsonElement write();

    protected abstract void read(JsonElement element);

    public abstract boolean isDifferentFromDefault();

    public abstract String getDefaultAsString();

    public abstract String getCurrentValueAsString();

}
