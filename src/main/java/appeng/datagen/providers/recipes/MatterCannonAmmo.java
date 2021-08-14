package appeng.datagen.providers.recipes;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ItemExistsCondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;

import appeng.recipes.mattercannon.MatterCannonAmmoSerializer;

record MatterCannonAmmo(ResourceLocation id, Tag.Named<Item> tag, Item item, float weight) implements FinishedRecipe {

    public void serializeRecipeData(JsonObject json) {
        JsonArray conditions = new JsonArray();
        if (tag != null) {
            json.add("ammo", Ingredient.of(tag).toJson());
            conditions.add(
                    CraftingHelper.serialize(
                            new NotCondition(
                                    new TagEmptyCondition(tag.getName()))));
        } else if (item != null) {
            json.add("ammo", Ingredient.of(item).toJson());
            conditions.add(
                    CraftingHelper.serialize(
                            new ItemExistsCondition(item.getRegistryName())));
        }
        json.addProperty("weight", this.weight);
        json.add("conditions", conditions);
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public RecipeSerializer<?> getType() {
        return MatterCannonAmmoSerializer.INSTANCE;
    }

    @Nullable
    public JsonObject serializeAdvancement() {
        return null;
    }

    @Nullable
    public ResourceLocation getAdvancementId() {
        return null;
    }
}
