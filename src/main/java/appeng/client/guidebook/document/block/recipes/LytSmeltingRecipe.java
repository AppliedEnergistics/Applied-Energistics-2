package appeng.client.guidebook.document.block.recipes;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Blocks;

import appeng.client.guidebook.document.DefaultStyles;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.LytBox;
import appeng.client.guidebook.document.block.LytSlot;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;
import appeng.core.AppEng;

public class LytSmeltingRecipe extends LytBox {
    private static final ResourceLocation ARROW_LIGHT = AppEng.makeId("ae2guide/gui/recipe_arrow_light.png");
    private static final ResourceLocation ARROW_DARK = AppEng.makeId("ae2guide/gui/recipe_arrow_dark.png");

    private final SmeltingRecipe recipe;

    private final LytSlot inputSlot;

    private final LytSlot resultSlot;

    public LytSmeltingRecipe(SmeltingRecipe recipe) {
        this.recipe = recipe;
        setPadding(5);
        paddingTop = 15;

        append(inputSlot = new LytSlot(recipe.getIngredients().get(0)));
        append(resultSlot = new LytSlot(recipe.getResultItem(Minecraft.getInstance().level.registryAccess())));
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
                Blocks.FURNACE.asItem().getDefaultInstance(),
                bounds.x() + paddingLeft,
                bounds.y() + 4,
                8,
                8);
        context.renderText(
                "Smelting",
                DefaultStyles.CRAFTING_RECIPE_TYPE.mergeWith(DefaultStyles.BASE_STYLE),
                bounds.x() + paddingLeft + 10,
                bounds.y() + 4);

        context.fillTexturedRect(
                new LytRect(bounds.right() - 25 - 24, bounds.y() + 10 + (bounds.height() - 27) / 2, 24, 17),
                context.isDarkMode() ? ARROW_DARK : ARROW_LIGHT);

        super.render(context);
    }
}
