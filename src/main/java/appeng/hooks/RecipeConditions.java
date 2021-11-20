package appeng.hooks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;

/**
 * Our own scrappy recipe condition system.
 */
public class RecipeConditions {

    private RecipeConditions() {
    }

    public static boolean areSatisfied(JsonElement json) {
        if (!(json instanceof JsonObject recipe)) {
            return true;
        }

        var hasTagCondition = recipe.getAsJsonPrimitive("ae2:has_tag");
        if (hasTagCondition != null) {
            var tagId = new ResourceLocation(hasTagCondition.getAsString());

            // Need to take it from here because the tag container might not have been updated yet
            var tag = SerializationTags.getInstance().getOrEmpty(Registry.ITEM_REGISTRY).getTag(tagId);
            if (tag == null || tag.getValues().isEmpty()) {
                return false;
            }
        }

        var hasItemCondition = recipe.getAsJsonPrimitive("ae2:has_item");
        if (hasItemCondition != null) {
            var itemId = new ResourceLocation(hasItemCondition.getAsString());
            if (!Registry.ITEM.containsKey(itemId)) {
                return false;
            }
        }

        return true;

    }

}
