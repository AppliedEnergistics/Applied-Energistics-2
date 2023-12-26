package appeng.integration.modules.emi;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ItemModText;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

class EmiTransformRecipe extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new AppEngRecipeCategory("item_transformation", EmiStack.of(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED), ItemModText.TRANSFORM_CATEGORY);

    private final TransformRecipe recipe;

    public EmiTransformRecipe(RecipeHolder<TransformRecipe> holder) {
        super(CATEGORY, holder.id(), 150, 72);
        recipe = holder.value();

        for (var ingredient : recipe.getIngredients()) {
            inputs.add(EmiIngredient.of(ingredient));
        }
        outputs.add(EmiStack.of(recipe.getResultItem()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {

        // First column contains ingredients
        int col1 = 10;
        int x = col1;
        int y = 10;
        int nInputs = recipe.getIngredients().size();
        if (nInputs < 3) {
            // so ingredients lists with less than two rows get centered vertically
            y += 9 * (3 - nInputs);
        }
        for (var input : inputs) {
            var slot = widgets.addSlot(input, x - 1, y - 1);
            y += slot.getBounds().height();
            if (y >= 64) {
                // we don't actually have room to make multiple columns of ingredients look nice,
                // but this is better than just overflowing downwards.
                y -= 54;
                x += 18;
            }
            widgets.add(slot);
        }

        // To center everything but the ingredients vertically
        int yOffset = 28;

        // Second column is arrow pointing into water
        final int col2 = col1 + 25;
        var arrow1 = widgets.addTexture(EmiTexture.EMPTY_ARROW, col2, yOffset);

        // Third column is water block
        final int col3 = col2 + arrow1.getBounds().width() + 6;
        if (recipe.circumstance.isFluid()) {
            var ingredient = EmiIngredient.of(
                    recipe.circumstance.getFluidsForRendering().stream().map(EmiStack::of).toList()
            );
            widgets.add(new EmiFluidBlockSlot(ingredient, col3 - 1, yOffset - 1)
                    .drawBack(false));
        } else if (recipe.circumstance.isExplosion()) {
            var ingredient = EmiIngredient.of(List.of(EmiStack.of(AEBlocks.TINY_TNT), EmiStack.of(Blocks.TNT)));
            widgets.addSlot(ingredient, col3 - 1, yOffset - 1)
                    .drawBack(false);
        }

        // Fourth column is arrow pointing to results
        final int col4 = col3 + 16 + 5;
        var arrow2 = widgets.addTexture(EmiTexture.EMPTY_ARROW, col4, yOffset);

        // Fifth column is the result
        final int col5 = arrow2.getBounds().right() + 10;
        widgets.addSlot(EmiStack.of(recipe.getResultItem()), col5 - 1, yOffset - 1).recipeContext(this);

        Component circumstanceText;
        if (recipe.circumstance.isExplosion()) {
            circumstanceText = ItemModText.EXPLOSION.text();
        } else {
            circumstanceText = ItemModText.SUBMERGE_IN.text();
        }

        widgets.addText(circumstanceText, width / 2, 15, 0x7E7E7E, false)
                .horizontalAlign(TextWidget.Alignment.CENTER);
    }
}
