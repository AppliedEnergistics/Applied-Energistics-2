
package appeng.client.guidebook.document.block.recipes;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.LytBox;
import appeng.client.guidebook.document.block.LytSlot;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;
import appeng.core.AppEng;
import appeng.recipes.handlers.InscriberRecipe;
import net.minecraft.resources.ResourceLocation;

public class LytInscriberRecipe extends LytBox {

    private static final ResourceLocation ARROWS_LIGHT = AppEng.makeId("ae2guide/gui/inscriber_arrows_bg_light.png");
    private static final ResourceLocation ARROWS_DARK = AppEng.makeId("ae2guide/gui/inscriber_arrows_bg_dark.png");

    private final InscriberRecipe recipe;

    private final LytSlot topSlot;
    private final LytSlot middleSlot;
    private final LytSlot bottomSlot;
    private final LytSlot resultSlot;

    public LytInscriberRecipe(InscriberRecipe recipe) {
        this.recipe = recipe;

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

        context.fillTexturedRect(new LytRect(
                bounds.x() + 23, bounds.y() + 12, 46, 50), context.isDarkMode() ? ARROWS_DARK : ARROWS_LIGHT);

        super.render(context);
    }
}
