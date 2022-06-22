package appeng.datagen.providers.recipes;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.recipes.handlers.ChargerRecipeSerializer;
import com.google.gson.JsonObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ChargerRecipes extends AE2RecipeProvider {
    public ChargerRecipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildAE2CraftingRecipes(Consumer<FinishedRecipe> consumer) {

        charge(consumer, "charged_certus_quartz_crystal", AEItems.CERTUS_QUARTZ_CRYSTAL.asItem(), AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem());
        charge(consumer, "sky_compass", Items.COMPASS, AEBlocks.SKY_COMPASS.asItem());
    }

    private void charge(Consumer<FinishedRecipe> consumer, String name, Item input, Item output) {
        consumer.accept(new ChargerRecipeBuilder(name, Ingredient.of(input), new ItemStack(output)));
    }

    record ChargerRecipeBuilder(String name, Ingredient input, ItemStack output) implements FinishedRecipe {

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("ingredient", input.toJson());
            json.add("result", toJson(output));
        }

        @Override
        public ResourceLocation getId() {
            return AppEng.makeId("charger/" + name);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ChargerRecipeSerializer.INSTANCE;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
