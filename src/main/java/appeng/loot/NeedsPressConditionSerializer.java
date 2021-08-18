package appeng.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.Serializer;

/**
 * Serializer for {@link NeedsPressCondition}.
 */
public class NeedsPressConditionSerializer implements Serializer<NeedsPressCondition> {
    @Override
    public void serialize(JsonObject json, NeedsPressCondition condition, JsonSerializationContext context) {
        json.addProperty("press", condition.getNeeded().getCriterionName());
    }

    @Override
    public NeedsPressCondition deserialize(JsonObject json, JsonDeserializationContext context) {
        var typeStr = GsonHelper.getAsString(json, "press");
        for (NeededPressType type : NeededPressType.values()) {
            if (type.getCriterionName().equals(typeStr)) {
                return new NeedsPressCondition(type);
            }
        }

        throw new JsonParseException("Unknown press type: " + typeStr);
    }
}
