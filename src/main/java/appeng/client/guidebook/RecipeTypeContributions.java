package appeng.client.guidebook;

import java.util.List;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import guideme.compiler.tags.RecipeTypeMappingSupplier;
import guideme.document.LytRect;
import guideme.document.block.LytBlock;
import guideme.document.block.LytSlotGrid;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.document.block.recipes.RecipeDisplayHolder;
import guideme.layout.LayoutContext;
import guideme.render.RenderContext;

import appeng.core.definitions.AEBlocks;
import appeng.core.localization.GuiText;
import appeng.recipes.handlers.ChargerRecipeDisplay;
import appeng.recipes.handlers.InscriberRecipeDisplay;
import appeng.recipes.transform.TransformRecipeDisplay;
import appeng.util.Platform;

public class RecipeTypeContributions implements RecipeTypeMappingSupplier {
    @Override
    public void collect(RecipeTypeMappings mappings) {
        mappings.add(InscriberRecipeDisplay.class, RecipeTypeContributions::inscribing);
        mappings.add(ChargerRecipeDisplay.class, RecipeTypeContributions::charging);
        mappings.add(TransformRecipeDisplay.class, RecipeTypeContributions::transform);
    }

    private static LytStandardRecipeBox<ChargerRecipeDisplay> charging(ChargerRecipeDisplay recipe) {
        return LytStandardRecipeBox.builder()
                .icon(AEBlocks.CHARGER)
                .title(AEBlocks.CHARGER.asItem().getName().getString())
                .input(recipe.ingredient())
                .outputFromResultOf(recipe)
                .build(new RecipeDisplayHolder<>(null, recipe));
    }

    private static LytStandardRecipeBox<InscriberRecipeDisplay> inscribing(InscriberRecipeDisplay recipe) {
        return LytStandardRecipeBox.builder()
                .icon(AEBlocks.INSCRIBER)
                .title(AEBlocks.INSCRIBER.asItem().getName().getString())
                .customBody(new LytInscriberRecipe(recipe))
                .build(new RecipeDisplayHolder<>(null, recipe));
    }

    private static LytStandardRecipeBox<TransformRecipeDisplay> transform(TransformRecipeDisplay recipe) {
        var builder = LytStandardRecipeBox.builder()
                .input(LytSlotGrid.column(recipe.ingredients(), true))
                .output(LytSlotGrid.column(List.of(recipe.result()), true));

        if (recipe.circumstance().isExplosion()) {
            builder.icon(Blocks.TNT);
            builder.title(GuiText.TransformTypeExplode.text().getString());
        } else if (recipe.circumstance().isFluid()) {
            Fluid fluid = Fluids.EMPTY;
            // Special-case water since a lot of mods add their fluids to the tag
            if (recipe.circumstance().isFluidTag(FluidTags.WATER)) {
                fluid = Fluids.WATER;
            } else {
                var fluidsForRendering = recipe.circumstance().getFluidsForRendering();
                if (!fluidsForRendering.isEmpty()) {
                    var cycle = System.currentTimeMillis() / 1500;
                    fluid = fluidsForRendering.get((int) (cycle % fluidsForRendering.size()));
                }
            }

            builder.icon(new FluidIcon(fluid));
            builder.title(GuiText.TransformTypeThrowInFluid.text(Platform.getFluidDisplayName(fluid)).getString());
        }

        return builder.build(new RecipeDisplayHolder<>(null, recipe));
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
