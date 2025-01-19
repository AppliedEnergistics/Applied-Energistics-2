
package appeng.client.guidebook;

import guideme.render.GuiAssets;
import net.minecraft.world.item.crafting.RecipeHolder;

import guideme.document.LytRect;
import guideme.document.block.LytSlot;
import guideme.document.block.recipes.LytRecipeBox;
import guideme.layout.LayoutContext;
import guideme.render.RenderContext;

import appeng.recipes.handlers.InscriberRecipe;

public class LytInscriberRecipe extends LytRecipeBox {
    private final InscriberRecipe recipe;

    private final LytSlot topSlot;
    private final LytSlot middleSlot;
    private final LytSlot bottomSlot;
    private final LytSlot resultSlot;

    public LytInscriberRecipe(RecipeHolder<InscriberRecipe> holder) {
        super(holder);
        this.recipe = holder.value();

        append(topSlot = new LytSlot(recipe.getTopOptional()));
        append(middleSlot = new LytSlot(recipe.getMiddleInput()));
        append(bottomSlot = new LytSlot(recipe.getBottomOptional()));
        append(resultSlot = new LytSlot(recipe.getResultItem()));
        resultSlot.setLargeSlot(true);

        setPadding(5);
    }

    @Override
    protected LytRect computeBoxLayout(LayoutContext context, int x, int y, int availableWidth) {
        topSlot.layout(context, x, y, availableWidth);
        middleSlot.layout(context, x + 18, y + 23, availableWidth);
        bottomSlot.layout(context, x, y + 46, availableWidth);
        resultSlot.layout(context, x + 64, y + 20, availableWidth);

        return new LytRect(x, y, 90, 64);
    }

    @Override
    public void render(RenderContext context) {
        var bounds = getBounds();
        context.renderPanel(bounds);

        context.fillIcon(new LytRect(
                bounds.x() + 23, bounds.y() + 12, 46, 50), GuiAssets.ARROW
        );

        super.render(context);
    }
}
