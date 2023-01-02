package appeng.client.guidebook.document.block.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import appeng.client.guidebook.document.DefaultStyles;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.LytBox;
import appeng.client.guidebook.document.block.LytSlot;
import appeng.client.guidebook.document.block.LytSlotGrid;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;
import appeng.core.AppEng;
import appeng.recipes.transform.TransformRecipe;
import appeng.util.Platform;

public class LytTransformRecipe extends LytBox {
    private static final ResourceLocation ARROW_LIGHT = AppEng.makeId("ae2guide/gui/recipe_arrow_light.png");
    private static final ResourceLocation ARROW_DARK = AppEng.makeId("ae2guide/gui/recipe_arrow_dark.png");

    private final TransformRecipe recipe;

    private final LytSlotGrid inputGrid;

    private final LytSlot resultSlot;

    public LytTransformRecipe(TransformRecipe recipe) {
        this.recipe = recipe;
        setPadding(5);
        paddingTop = 15;

        append(inputGrid = LytSlotGrid.column(recipe.getIngredients(), true));
        append(resultSlot = new LytSlot(recipe.getResultItem()));
    }

    @Override
    protected LytRect computeBoxLayout(LayoutContext context, int x, int y, int availableWidth) {
        var gridBounds = inputGrid.layout(
                context,
                x,
                y,
                availableWidth);

        var resultBounds = resultSlot.layout(
                context,
                gridBounds.right() + 28,
                // Center the slot vertically in relation to the grid
                Math.max(y, gridBounds.y() + (gridBounds.height() - 18) / 2),
                availableWidth);
        return LytRect.union(gridBounds, resultBounds);
    }

    @Override
    public void render(RenderContext context) {
        context.renderPanel(getBounds());

        if (recipe.circumstance.isExplosion()) {
            context.renderItem(
                    Blocks.TNT.asItem().getDefaultInstance(),
                    bounds.x() + paddingLeft,
                    bounds.y() + 4,
                    8,
                    8);
            context.renderText(
                    "Explode",
                    DefaultStyles.CRAFTING_RECIPE_TYPE.mergeWith(DefaultStyles.BASE_STYLE),
                    bounds.x() + paddingLeft + 10,
                    bounds.y() + 4);
        } else if (recipe.circumstance.isFluid()) {
            Fluid fluid = Fluids.EMPTY;
            // Special-case water since a lot of mods add their fluids to the tag
            if (recipe.circumstance.isFluidTag(FluidTags.WATER)) {
                fluid = Fluids.WATER;
            } else {
                var fluidsForRendering = recipe.circumstance.getFluidsForRendering();
                if (!fluidsForRendering.isEmpty()) {
                    var cycle = System.currentTimeMillis() / 1500;
                    fluid = fluidsForRendering.get((int) (cycle % fluidsForRendering.size()));
                }
            }
            context.renderFluid(
                    fluid,
                    null,
                    bounds.x() + paddingLeft,
                    bounds.y() + 4,
                    0,
                    8,
                    8);
            context.renderText(
                    "Throw in " + Platform.getFluidDisplayName(fluid, null).getString(),
                    DefaultStyles.CRAFTING_RECIPE_TYPE.mergeWith(DefaultStyles.BASE_STYLE),
                    bounds.x() + paddingLeft + 10,
                    bounds.y() + 4);
        }

        context.fillTexturedRect(
                new LytRect(bounds.right() - 25 - 24, bounds.y() + 10 + (bounds.height() - 27) / 2, 24, 17),
                context.isDarkMode() ? ARROW_DARK : ARROW_LIGHT);

        super.render(context);
    }
}
