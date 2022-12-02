package appeng.client.guidebook.document.block.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Blocks;

import appeng.client.guidebook.document.DefaultStyles;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.LytBox;
import appeng.client.guidebook.document.block.LytSlot;
import appeng.client.guidebook.document.block.LytSlotGrid;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;
import appeng.core.AppEng;

public class LytCraftingRecipe extends LytBox {
    private static final ResourceLocation ARROW_LIGHT = AppEng.makeId("ae2guide/gui/recipe_arrow_light.png");
    private static final ResourceLocation ARROW_DARK = AppEng.makeId("ae2guide/gui/recipe_arrow_dark.png");

    private final CraftingRecipe recipe;

    private final LytSlotGrid grid;

    private final LytSlot resultSlot;

    public LytCraftingRecipe(CraftingRecipe recipe) {
        this.recipe = recipe;
        setPadding(5);
        paddingTop = 15;

        var ingredients = recipe.getIngredients();
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            this.grid = new LytSlotGrid(shapedRecipe.getWidth(), shapedRecipe.getHeight());

            for (var x = 0; x < shapedRecipe.getWidth(); x++) {
                for (var y = 0; y < shapedRecipe.getHeight(); y++) {
                    var index = y * shapedRecipe.getWidth() + x;
                    if (index < ingredients.size()) {
                        var ingredient = ingredients.get(index);
                        if (!ingredient.isEmpty()) {
                            grid.setIngredient(x, y, ingredient);
                        }
                    }
                }
            }
        } else {
            // For shapeless -> layout 3 ingredients per row and break
            var ingredientCount = ingredients.size();
            this.grid = new LytSlotGrid(Math.min(3, ingredientCount), (ingredientCount + 2) / 3);
            for (int i = 0; i < ingredients.size(); i++) {
                var col = i % 3;
                var row = i / 3;
                grid.setIngredient(col, row, ingredients.get(i));
            }
        }
        append(grid);

        append(resultSlot = new LytSlot(recipe.getResultItem()));
    }

    @Override
    protected LytRect computeBoxLayout(LayoutContext context, int x, int y, int availableWidth) {
        var gridBounds = grid.layout(context, x, y, availableWidth);
        var slotBounds = resultSlot.layout(
                context,
                gridBounds.right() + 28,
                // Center the slot vertically in relation to the grid
                Math.max(y, gridBounds.y() + (gridBounds.height() - 18) / 2),
                availableWidth);
        return LytRect.union(gridBounds, slotBounds);
    }

    @Override
    public void render(RenderContext context) {
        context.renderPanel(getBounds());

        context.renderItem(
                Blocks.CRAFTING_TABLE.asItem().getDefaultInstance(),
                bounds.x() + paddingLeft,
                bounds.y() + 4,
                8,
                8);
        context.renderText(
                (recipe instanceof ShapelessRecipe) ? "Crafting (Shapeless)" : "Crafting",
                DefaultStyles.CRAFTING_RECIPE_TYPE.mergeWith(DefaultStyles.BASE_STYLE),
                bounds.x() + paddingLeft + 10,
                bounds.y() + 4);

        context.fillTexturedRect(
                new LytRect(bounds.right() - 25 - 24, bounds.y() + 10 + (bounds.height() - 27) / 2, 24, 17),
                context.isDarkMode() ? ARROW_DARK : ARROW_LIGHT);

        super.render(context);
    }
}
