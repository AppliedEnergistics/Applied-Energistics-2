package appeng.datagen.providers.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.handlers.GrinderOptionalResult;
import appeng.recipes.handlers.GrinderRecipeSerializer;

public class GrinderRecipes extends AE2RecipeProvider {

    public GrinderRecipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {

        grinder(Items.BONE, new ItemStack(Items.BONE_MEAL, 4))
                .save(consumer, "bonemeal");

        grinder(ConventionTags.CERTUS_QUARTZ, AEItems.CERTUS_QUARTZ_DUST.stack())
                .setTurns(4)
                .save(consumer, "certus_quartz_dust");
        grinder(ConventionTags.CERTUS_QUARTZ_ORE, AEItems.CERTUS_QUARTZ_DUST.stack())
                .addOptionalResult(AEItems.CERTUS_QUARTZ_DUST.stack())
                .save(consumer, "certus_quartz_dust_ore");

        grinder(ConventionTags.ENDER_PEARL, AEItems.ENDER_DUST.stack())
                .setTurns(4)
                .save(consumer, "ender_dust");

        grinder(Items.GRAVEL, new ItemStack(Items.FLINT))
                .save(consumer, "flint");

        grinder(ConventionTags.WHEAT_CROP, AEItems.FLOUR.stack())
                .setTurns(4)
                .save(consumer, "flour");

        grinder(AEItems.FLUIX_CRYSTAL, AEItems.FLUIX_DUST.stack())
                .setTurns(4)
                .save(consumer, "fluix_dust");

        grinder(ConventionTags.GOLD_INGOT, AEItems.GOLD_DUST.stack())
                .setTurns(4)
                .save(consumer, "gold_dust_ingot");
        grinder(ConventionTags.GOLD_ORE, AEItems.GOLD_DUST.stack())
                .addOptionalResult(AEItems.GOLD_DUST.stack())
                .save(consumer, "gold_dust_ore");

        grinder(ConventionTags.IRON_INGOT, AEItems.IRON_DUST.stack())
                .setTurns(4)
                .save(consumer, "iron_dust_ingot");
        grinder(ConventionTags.IRON_ORE, AEItems.IRON_DUST.stack())
                .addOptionalResult(AEItems.IRON_DUST.stack())
                .save(consumer, "iron_dust_ore");

        grinder(ConventionTags.NETHER_QUARTZ, AEItems.NETHER_QUARTZ_DUST.stack())
                .setTurns(4)
                .save(consumer, "nether_quartz_dust");
        grinder(ConventionTags.NETHER_QUARTZ_ORE, AEItems.NETHER_QUARTZ_DUST.stack())
                .addOptionalResult(AEItems.NETHER_QUARTZ_DUST.stack())
                .save(consumer, "nether_quartz_dust_ore");

        grinder(AEBlocks.SKY_STONE_BLOCK, AEItems.SKY_DUST.stack())
                .save(consumer, "sky_dust");

    }

    private static GrinderRecipeBuilder grinder(ItemLike input, ItemStack result) {
        return new GrinderRecipeBuilder(Ingredient.of(input), result);
    }

    private static GrinderRecipeBuilder grinder(Tag.Named<Item> input, ItemStack result) {
        return new GrinderRecipeBuilder(Ingredient.of(input), result);
    }

    private static class GrinderRecipeBuilder {
        private final Ingredient input;
        private int inputCount = 1;
        private final ItemStack result;
        private final List<GrinderOptionalResult> optionalResults = new ArrayList<>();
        private int turns = GrinderRecipeSerializer.DEFAULT_TURNS;

        public GrinderRecipeBuilder(Ingredient input, ItemStack result) {
            this.input = input;
            this.result = result;
        }

        public GrinderRecipeBuilder setInputCount(int inputCount) {
            this.inputCount = inputCount;
            return this;
        }

        public GrinderRecipeBuilder addOptionalResult(float chance, ItemStack result) {
            this.optionalResults.add(new GrinderOptionalResult(chance, result));
            return this;
        }

        public GrinderRecipeBuilder addOptionalResult(ItemStack result) {
            return addOptionalResult(Float.NaN, result);
        }

        public GrinderRecipeBuilder setTurns(int turns) {
            this.turns = turns;
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
                json.add("input", input.toJson());
                if (inputCount > 1) {
                    json.addProperty("count", inputCount);
                }

                var resultJson = new JsonObject();
                resultJson.add("primary", toJson(result));
                if (!optionalResults.isEmpty()) {
                    var optionalJson = new JsonArray();
                    for (var optionalResult : optionalResults) {
                        var optionalResultJson = toJson(optionalResult.getResult());
                        if (!Float.isNaN(optionalResult.getChance())) {
                            optionalResultJson.addProperty("percentageChance", optionalResult.getChance() * 100);
                        }
                        optionalJson.add(optionalResultJson);
                    }
                    resultJson.add("optional", optionalJson);
                }
                json.add("result", resultJson);
                json.addProperty("turns", turns);
            }

            @Override
            public ResourceLocation getId() {
                return AppEng.makeId("grinder/" + name);
            }

            @Override
            public RecipeSerializer<?> getType() {
                return GrinderRecipeSerializer.INSTANCE;
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
