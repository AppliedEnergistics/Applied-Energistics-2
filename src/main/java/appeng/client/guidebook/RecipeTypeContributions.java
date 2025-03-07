package appeng.client.guidebook;

import java.util.List;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
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
import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.transform.TransformRecipe;
import appeng.util.Platform;

public class RecipeTypeContributions implements RecipeTypeMappingSupplier {
    @Override
    public void collect(RecipeTypeMappings mappings) {
        mappings.add(AERecipeTypes.INSCRIBER, RecipeTypeContributions::inscribing);
        mappings.add(AERecipeTypes.CHARGER, RecipeTypeContributions::charging);
        mappings.add(AERecipeTypes.TRANSFORM, RecipeTypeContributions::transform);
    }

    private static LytStandardRecipeBox<ChargerRecipe> charging(RecipeHolder<ChargerRecipe> holder) {
        return LytStandardRecipeBox.builder()
                .icon(AEBlocks.CHARGER)
                .title(AEBlocks.CHARGER.asItem().getName().getString())
                .input(holder.value().getIngredient())
                .outputFromResultOf(holder)
                .build(holder);
    }

    private static LytStandardRecipeBox<InscriberRecipe> inscribing(RecipeHolder<InscriberRecipe> holder) {
        return LytStandardRecipeBox.builder()
                .icon(AEBlocks.INSCRIBER)
                .title(AEBlocks.INSCRIBER.asItem().getName().getString())
                .customBody(new LytInscriberRecipe(holder.value()))
                .build(holder);
    }

    private static LytStandardRecipeBox<TransformRecipe> transform(RecipeHolder<TransformRecipe> holder) {
        var recipe = holder.value();

        var builder = LytStandardRecipeBox.builder()
                .input(LytSlotGrid.column(recipe.getIngredients(), true))
                .output(LytSlotGrid.column(List.of(Ingredient.of(recipe.getResultItem().getItem())), true));

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

        return builder.build(holder);
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
