package appeng.recipes.handlers;

import java.util.Locale;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class InscriberRecipeBuilder {
    private final Ingredient middleInput;
    private Ingredient topOptional;
    private Ingredient bottomOptional;
    private final ItemLike output;
    private final int count;
    private InscriberProcessType mode = InscriberProcessType.INSCRIBE;

    public InscriberRecipeBuilder(Ingredient middleInput, ItemLike output, int count) {
        this.middleInput = middleInput;
        this.output = output;
        this.count = count;
    }

    public static InscriberRecipeBuilder inscribe(ItemLike middle, ItemLike output, int count) {
        return new InscriberRecipeBuilder(Ingredient.of(middle), output, count);
    }

    public static InscriberRecipeBuilder inscribe(TagKey<Item> middle, ItemLike output, int count) {
        return new InscriberRecipeBuilder(Ingredient.of(middle), output, count);
    }

    public static InscriberRecipeBuilder inscribe(Ingredient middle, ItemLike output, int count) {
        return new InscriberRecipeBuilder(middle, output, count);
    }

    public InscriberRecipeBuilder setTop(Ingredient topOptional) {
        this.topOptional = topOptional;
        return this;
    }

    public InscriberRecipeBuilder setBottom(Ingredient bottomOptional) {
        this.bottomOptional = bottomOptional;
        return this;
    }

    public InscriberRecipeBuilder setMode(InscriberProcessType processType) {
        this.mode = processType;
        return this;
    }

    public void save(RecipeOutput consumer, ResourceLocation id) {
        consumer.accept(new Result(id));
    }

    class Result implements FinishedRecipe {
        private final ResourceLocation id;

        public Result(ResourceLocation id) {
            this.id = id;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("mode", mode.name().toLowerCase(Locale.ROOT));

            var result = new JsonObject();
            result.addProperty("item", BuiltInRegistries.ITEM.getKey(output.asItem()).toString());
            if (count > 1) {
                result.addProperty("count", count);
            }
            json.add("result", result);

            var ingredients = new JsonObject();
            ingredients.add("middle", middleInput.toJson(false));
            if (topOptional != null) {
                ingredients.add("top", topOptional.toJson(true));
            }
            if (bottomOptional != null) {
                ingredients.add("bottom", bottomOptional.toJson(true));
            }
            json.add("ingredients", ingredients);
        }

        @Override
        public ResourceLocation id() {
            return id;
        }

        @Override
        public RecipeSerializer<?> type() {
            return InscriberRecipeSerializer.INSTANCE;
        }

        @Nullable
        @Override
        public AdvancementHolder advancement() {
            return null;
        }
    }
}
