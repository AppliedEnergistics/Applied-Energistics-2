package appeng.integration.modules.emi;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;

class EmiInscriberRecipe extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new AppEngRecipeCategory("inscriber",
            EmiStack.of(AEBlocks.INSCRIBER), EmiText.CATEGORY_INSCRIBER);
    private final InscriberRecipe recipe;

    public EmiInscriberRecipe(RecipeHolder<InscriberRecipe> holder) {
        super(CATEGORY, holder.id(), 105, 54);

        recipe = holder.value();

        if (!recipe.getTopOptional().isEmpty()) {
            var top = EmiIngredient.of(recipe.getTopOptional());
            if (recipe.getProcessType() == InscriberProcessType.INSCRIBE) {
                top.getEmiStacks().forEach(s -> s.setRemainder(s));
            }
            inputs.add(top);
        }
        if (!recipe.getBottomOptional().isEmpty()) {
            var bottom = EmiIngredient.of(recipe.getBottomOptional());
            if (recipe.getProcessType() == InscriberProcessType.INSCRIBE) {
                bottom.getEmiStacks().forEach(s -> s.setRemainder(s));
            }
            inputs.add(bottom);
        }
        inputs.add(EmiIngredient.of(recipe.getMiddleInput()));
        outputs.add(EmiStack.of(recipe.getResultItem()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        ResourceLocation background = AppEng.makeId("textures/guis/inscriber.png");

        widgets.addTexture(background, 0, 0, 105, 54, 36, 20);

        widgets.addAnimatedTexture(background, 100, 19, 6, 18, 177, 0,
                2000, false, true, false);

        widgets.addSlot(EmiIngredient.of(recipe.getTopOptional()), 2, 2)
                .drawBack(false);
        widgets.addSlot(EmiIngredient.of(recipe.getMiddleInput()), 26, 18)
                .drawBack(false);
        widgets.addSlot(EmiIngredient.of(recipe.getBottomOptional()), 2, 34)
                .drawBack(false);
        widgets.addSlot(EmiStack.of(recipe.getResultItem()), 76, 19)
                .drawBack(false);
    }
}
