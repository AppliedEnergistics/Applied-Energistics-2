package appeng.core.config;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

public class ConfigSection {

    private final ConfigSection parent;

    private final List<ConfigSection> subsections = new ArrayList<>();

    private final List<BaseOption> options = new ArrayList<>();

    private final String id;

    private final String fullId;

    private final String comment;

    private Runnable changeListener;

    private ConfigSection(String id, String comment) {
        this.parent = null;
        this.id = id;
        this.fullId = id;
        this.comment = comment;
    }

    private ConfigSection(ConfigSection parent, String id, String comment) {
        this.parent = parent;
        this.id = id;
        this.comment = comment;
        if (parent.fullId != null) {
            this.fullId = parent.fullId + "." + id;
        } else {
            this.fullId = id;
        }
    }

    public static ConfigSection createRoot() {
        return new ConfigSection(null, null);
    }

    public ConfigSection subsection(String id) {
        return this.subsection(id, null);
    }

    public ConfigSection subsection(String id, String comment) {
        ConfigSection section = new ConfigSection(this, id, comment);
        subsections.add(section);
        return section;
    }

    private <T extends BaseOption> T addOption(T option) {
        this.options.add(option);
        return option;
    }

    public StringOption addString(String id, String defaultValue) {
        return addString(id, defaultValue, comment);
    }

    public StringOption addString(String id, String defaultValue, String comment) {
        return addOption(new StringOption(this, id, comment, defaultValue));
    }

    public IntegerOption addInt(String id, int defaultValue) {
        return addInt(id, defaultValue, comment);
    }

    public IntegerOption addInt(String id, int defaultValue, String comment) {
        return addInt(id, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, comment);
    }

    public IntegerOption addInt(String id, int defaultValue, int minValue, int maxValue, String comment) {
        return addOption(new IntegerOption(this, id, comment, defaultValue, minValue, maxValue));
    }

    public DoubleOption addDouble(String id, double defaultValue) {
        return addDouble(id, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public DoubleOption addDouble(String id, double defaultValue, String comment) {
        return addDouble(id, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE, comment);
    }

    public DoubleOption addDouble(String id, double defaultValue, double minValue, double maxValue) {
        return addDouble(id, defaultValue, minValue, maxValue, null);
    }

    public DoubleOption addDouble(String id, double defaultValue, double minValue, double maxValue, String comment) {
        return addOption(new DoubleOption(this, id, comment, defaultValue, minValue, maxValue));
    }

    public BooleanOption addBoolean(String id, boolean defaultValue) {
        return addBoolean(id, defaultValue, null);
    }

    public BooleanOption addBoolean(String id, boolean defaultValue, String comment) {
        return addOption(new BooleanOption(this, id, comment, defaultValue));
    }

    public StringListOption addStringList(String id, List<String> defaultValue) {
        return addStringList(id, defaultValue, null);
    }

    public StringListOption addStringList(String id, List<String> defaultValue, String comment) {
        return addOption(new StringListOption(this, id, comment, defaultValue));
    }

    public <T extends Enum<T>> EnumOption<T> addEnum(String id, T defaultValue) {
        return addEnum(id, defaultValue, null);
    }

    public <T extends Enum<T>> EnumOption<T> addEnum(String id, T defaultValue, String comment) {
        return addOption(new EnumOption<>(this, id, comment, defaultValue));
    }

    public void setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
    }

    public void markDirty() {
        if (changeListener != null) {
            this.changeListener.run();
        }
        if (this.parent != null) {
            this.parent.markDirty();
        }
    }

    public JsonObject write() {
        JsonObject obj = new JsonObject();

        if (comment != null) {
            obj.addProperty("__comment", comment);
        }

        for (BaseOption option : options) {
            if (option.comment != null) {
                obj.addProperty("__comment", option.comment);
            }
            obj.add(option.id, option.write());
        }

        for (ConfigSection subsection : subsections) {
            obj.add(subsection.id, subsection.write());
        }

        return obj;
    }

    public void read(JsonObject obj) {

        for (BaseOption option : options) {
            if (obj.has(option.id)) {
                option.read(obj.get(option.id));
            }
        }

        for (ConfigSection subsection : subsections) {
            if (obj.has(subsection.id)) {
                subsection.read(obj.getAsJsonObject(subsection.id));
            }
        }

    }
}
