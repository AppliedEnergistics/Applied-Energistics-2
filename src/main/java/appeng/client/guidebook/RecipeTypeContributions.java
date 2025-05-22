package appeng.client.guidebook;

import java.util.List;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import guideme.compiler.tags.RecipeTypeMappingSupplier;
import guideme.document.LytRect;
import guideme.document.block.LytBlock;
import guideme.document.block.LytSlotGrid;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.layout.LayoutContext;
import guideme.render.RenderContext;

import appeng.core.definitions.AEBlocks;
import appeng.core.localization.GuiText;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.transform.TransformRecipe;
import appeng.util.Platform;

public class RecipeTypeContributions implements RecipeTypeMappingSupplier {
    @Override
    public void collect(RecipeTypeMappings mappings) {
        mappings.add(InscriberRecipe.TYPE, RecipeTypeContributions::inscribing);
        mappings.add(ChargerRecipe.TYPE, RecipeTypeContributions::charging);
        mappings.add(TransformRecipe.TYPE, RecipeTypeContributions::transform);
    }

    private static LytStandardRecipeBox<ChargerRecipe> charging(ChargerRecipe recipe) {
        return LytStandardRecipeBox.builder()
                .icon(AEBlocks.CHARGER)
                .title(AEBlocks.CHARGER.asItem().getDescription().getString())
                .input(recipe.getIngredient())
                .outputFromResultOf(recipe)
                .build(recipe);
    }

    private static LytStandardRecipeBox<InscriberRecipe> inscribing(InscriberRecipe recipe) {
        return LytStandardRecipeBox.builder()
                .icon(AEBlocks.INSCRIBER)
                .title(AEBlocks.INSCRIBER.asItem().getDescription().getString())
                .customBody(new LytInscriberRecipe(recipe))
                .build(recipe);
    }

    private static LytStandardRecipeBox<TransformRecipe> transform(TransformRecipe recipe) {
        var builder = LytStandardRecipeBox.builder()
                .input(LytSlotGrid.column(recipe.getIngredients(), true))
                .output(LytSlotGrid.column(List.of(Ingredient.of(recipe.getResultItem())), true));

        if (recipe.circumstance.isExplosion()) {
            builder.icon(Blocks.TNT);
            builder.title(GuiText.TransformTypeExplode.text().getString());
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

            builder.icon(new FluidIcon(fluid));
            builder.title(GuiText.TransformTypeThrowInFluid.text(Platform.getFluidDisplayName(fluid)).getString());
        }

        return builder.build(recipe);
    }

    static class FluidIcon extends LytBlock {
        private final Fluid fluid;

        public FluidIcon(Fluid fluid) {
            this.fluid = fluid;
        }

        @Override
        protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
            return new LytRect(x, y, 8, 8);
        }

        @Override
        protected void onLayoutMoved(int deltaX, int deltaY) {

        }

        @Override
        public void renderBatch(RenderContext context, MultiBufferSource buffers) {

        }

        @Override
        public void render(RenderContext context) {
            context.renderFluid(
                    fluid,
                    bounds.x(),
                    bounds.y(),
                    0,
                    bounds.width(),
                    bounds.height());
        }
    }

}
