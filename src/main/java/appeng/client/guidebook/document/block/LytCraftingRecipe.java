package appeng.client.guidebook.document.block;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;
import appeng.core.AppEng;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class LytCraftingRecipe extends LytBox {
    private final CraftingRecipe recipe;

    private LytSlotGrid grid;

    private LytSlot resultSlot;

    public LytCraftingRecipe(CraftingRecipe recipe) {
        this.recipe = recipe;

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
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        var gridBounds = grid.layout(context, x + 5, y + 5, availableWidth);
        var slotBounds = resultSlot.layout(
                context,
                gridBounds.right() + 28,
                // Center the slot vertically in relation to the grid
                Math.max(y + 5, gridBounds.y() + (gridBounds.height() - 18) / 2),
                availableWidth
        );
        return LytRect.union(gridBounds, slotBounds).expand(5);
    }

    @Override
    public void render(RenderContext context) {
        context.renderPanel(getBounds());

        var texture = Minecraft.getInstance().getTextureManager().getTexture(AppEng.makeId("ae2guide/gui/recipe_arrow_light.png"));
        context.fillTexturedRect(
                new LytRect(bounds.right() - 25 - 24, bounds.y() + (bounds.height() - 17) / 2, 24, 17),
                texture);

        super.render(context);
    }
}
