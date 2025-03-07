package appeng.recipes.handlers;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class InscriberRecipeBuilder {
    private final Ingredient middleInput;
    private Ingredient topOptional = Ingredient.of();
    private Ingredient bottomOptional = Ingredient.of();
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
        var result = output.asItem().getDefaultInstance();
        result.setCount(count);

        var recipe = new InscriberRecipe(
                middleInput, result, topOptional, bottomOptional, mode);

        consumer.accept(id, recipe, null);
    }
}
