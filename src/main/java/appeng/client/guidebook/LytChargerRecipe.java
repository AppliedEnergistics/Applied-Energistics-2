package appeng.client.guidebook;

import net.minecraft.world.item.crafting.RecipeHolder;

import guideme.document.DefaultStyles;
import guideme.document.LytRect;
import guideme.document.block.LytSlot;
import guideme.document.block.recipes.LytRecipeBox;
import guideme.layout.LayoutContext;
import guideme.render.GuiAssets;
import guideme.render.RenderContext;

import appeng.core.definitions.AEBlocks;
import appeng.recipes.handlers.ChargerRecipe;

public class LytChargerRecipe extends LytRecipeBox {
    private final ChargerRecipe recipe;

    private final LytSlot inputSlot;

    private final LytSlot resultSlot;

    public LytChargerRecipe(RecipeHolder<ChargerRecipe> holder) {
        super(holder);
        this.recipe = holder.value();
        setPadding(5);
        paddingTop = 15;

        append(inputSlot = new LytSlot(recipe.getIngredient()));
        append(resultSlot = new LytSlot(recipe.getResultItem()));
    }

    @Override
    protected LytRect computeBoxLayout(LayoutContext context, int x, int y, int availableWidth) {
        var inputBounds = inputSlot.layout(
                context,
                x,
                y,
                availableWidth);

        var resultBounds = resultSlot.layout(
                context,
                inputBounds.right() + 28,
                y,
                availableWidth);
        return LytRect.union(inputBounds, resultBounds);
    }

    @Override
    public void render(RenderContext context) {
        context.renderPanel(getBounds());

        context.renderItem(
                AEBlocks.CHARGER.asItem().getDefaultInstance(),
                bounds.x() + paddingLeft,
                bounds.y() + 4,
                8,
                8);
        context.renderText(
                "Charging",
                DefaultStyles.CRAFTING_RECIPE_TYPE.mergeWith(DefaultStyles.BASE_STYLE),
                bounds.x() + paddingLeft + 10,
                bounds.y() + 4);

        context.fillIcon(
                new LytRect(bounds.right() - 25 - 24, bounds.y() + 10 + (bounds.height() - 27) / 2, 24, 17),
                GuiAssets.ARROW);

        super.render(context);
    }
}
