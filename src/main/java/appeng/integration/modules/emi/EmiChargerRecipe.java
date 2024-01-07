package appeng.integration.modules.emi;

import net.minecraft.world.item.crafting.RecipeHolder;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.misc.CrankBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.localization.ItemModText;
import appeng.recipes.handlers.ChargerRecipe;

class EmiChargerRecipe extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new AppEngRecipeCategory("charger", EmiStack.of(AEBlocks.CHARGER),
            EmiText.CATEGORY_CHARGER);
    private final ChargerRecipe recipe;

    private final EmiIngredient ingredient;
    private final EmiStack result;

    public EmiChargerRecipe(RecipeHolder<ChargerRecipe> holder) {
        super(CATEGORY, holder.id(), 130, 50);
        recipe = holder.value();
        this.ingredient = EmiIngredient.of(recipe.getIngredient());
        inputs.add(this.ingredient);
        this.result = EmiStack.of(recipe.getResultItem());
        outputs.add(this.result);

        catalysts.add(EmiStack.of(AEBlocks.CRANK));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {

        widgets.addSlot(ingredient, 30, 7);
        widgets.addSlot(result, 80, 7);
        widgets.addSlot(EmiStack.of(AEBlocks.CRANK), 2, 29)
                .drawBack(false);

        widgets.addTexture(EmiTexture.EMPTY_ARROW, 52, 8);

        var turns = (ChargerBlockEntity.POWER_MAXIMUM_AMOUNT + CrankBlockEntity.POWER_PER_CRANK_TURN - 1)
                / CrankBlockEntity.POWER_PER_CRANK_TURN;
        widgets.addText(
                ItemModText.CHARGER_REQUIRED_POWER.text(turns, ChargerBlockEntity.POWER_MAXIMUM_AMOUNT),
                20, 35,
                0x7E7E7E,
                false);
    }
}
