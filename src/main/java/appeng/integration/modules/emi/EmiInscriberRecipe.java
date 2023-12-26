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
        super(CATEGORY, holder.id(), 97, 64);

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

        widgets.addTexture(background, 0, 0, 97, 64, 44, 15);

        widgets.addAnimatedTexture(background, 91, 24, 6, 18, 135, 177,
                2000, false, true, false);

        widgets.addSlot(EmiIngredient.of(recipe.getTopOptional()), 0, 0)
                .drawBack(false);
        widgets.addSlot(EmiIngredient.of(recipe.getMiddleInput()), 18, 23)
                .drawBack(false);
        widgets.addSlot(EmiIngredient.of(recipe.getBottomOptional()), 0, 46)
                .drawBack(false);
        widgets.addSlot(EmiStack.of(recipe.getResultItem()), 68, 24)
                .drawBack(false);
    }
}
