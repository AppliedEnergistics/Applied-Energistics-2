package appeng.datagen.providers.recipes;

import java.util.Locale;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import appeng.api.features.InscriberProcessType;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.handlers.InscriberRecipeSerializer;

public class InscriberRecipes extends AE2RecipeProvider {
    public InscriberRecipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {

        // Silicon Press Copying & Printing
        inscribe(AEItems.SILICON_PRESS, new ItemStack(Items.IRON_BLOCK))
                .setTop(Ingredient.of(AEItems.SILICON_PRESS))
                .setMode(InscriberProcessType.PRESS)
                .save(consumer, "silicon_press");
        inscribe(Ingredient.of(ConventionTags.SILICON), AEItems.SILICON_PRINT.stack())
                .setTop(Ingredient.of(AEItems.SILICON_PRESS))
                .setMode(InscriberProcessType.PRESS)
                .save(consumer, "silicon_print");

        processor(consumer, "calculation_processor",
                AEItems.CALCULATION_PROCESSOR_PRESS,
                AEItems.CALCULATION_PROCESSOR_PRINT,
                AEItems.CALCULATION_PROCESSOR,
                Ingredient.of(AEItems.PURIFIED_CERTUS_QUARTZ_CRYSTAL));

        processor(consumer, "engineering_processor",
                AEItems.ENGINEERING_PROCESSOR_PRESS,
                AEItems.ENGINEERING_PROCESSOR_PRINT,
                AEItems.ENGINEERING_PROCESSOR,
                Ingredient.of(ConventionTags.DIAMOND));

        processor(consumer, "logic_processor",
                AEItems.LOGIC_PROCESSOR_PRESS,
                AEItems.LOGIC_PROCESSOR_PRINT,
                AEItems.LOGIC_PROCESSOR,
                Ingredient.of(ConventionTags.GOLD_INGOT));

    }

    private void processor(Consumer<FinishedRecipe> consumer,
            String name,
            ItemLike press,
            ItemLike print,
            ItemLike processor,
            Ingredient printMaterial) {
        // Making the print
        inscribe(printMaterial, new ItemStack(print))
                .setTop(Ingredient.of(press))
                .setMode(InscriberProcessType.PRESS)
                .save(consumer, name + "_print");

        // Making the processor
        inscribe(Items.REDSTONE, new ItemStack(processor))
                .setTop(Ingredient.of(print))
                .setBottom(Ingredient.of(AEItems.SILICON_PRINT))
                .setMode(InscriberProcessType.INSCRIBE)
                .save(consumer, name);

        // Copying the press
        inscribe(Items.IRON_BLOCK, new ItemStack(press))
                .setTop(Ingredient.of(press))
                .setMode(InscriberProcessType.PRESS)
                .save(consumer, name + "_press");
    }

    private InscriberRecipeBuilder inscribe(ItemLike middle, ItemStack output) {
        return new InscriberRecipeBuilder(Ingredient.of(middle), output);
    }

    private InscriberRecipeBuilder inscribe(Ingredient middle, ItemStack output) {
        return new InscriberRecipeBuilder(middle, output);
    }

    static class InscriberRecipeBuilder {
        private final Ingredient middleInput;
        private Ingredient topOptional;
        private Ingredient bottomOptional;
        private final ItemStack output;
        private InscriberProcessType mode = InscriberProcessType.INSCRIBE;

        public InscriberRecipeBuilder(Ingredient middleInput, ItemStack output) {
            this.middleInput = middleInput;
            this.output = output;
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

        public void save(Consumer<FinishedRecipe> consumer, String name) {
            consumer.accept(new Result(name));
        }

        class Result implements FinishedRecipe {
            private final String name;

            public Result(String name) {
                this.name = name;
            }

            @Override
            public void serializeRecipeData(JsonObject json) {
                json.addProperty("mode", mode.name().toLowerCase(Locale.ROOT));
                json.add("result", toJson(output));
                var ingredients = new JsonObject();
                ingredients.add("middle", middleInput.toJson());
                if (topOptional != null) {
                    ingredients.add("top", topOptional.toJson());
                }
                if (bottomOptional != null) {
                    ingredients.add("bottom", bottomOptional.toJson());
                }
                json.add("ingredients", ingredients);
            }

            @Override
            public ResourceLocation getId() {
                return AppEng.makeId("inscriber/" + name);
            }

            @Override
            public RecipeSerializer<?> getType() {
                return InscriberRecipeSerializer.INSTANCE;
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

}
