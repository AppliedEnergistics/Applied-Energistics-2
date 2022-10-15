package appeng.integration.modules.rei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.client.entry.FluidEntryDefinition;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ItemModText;
import appeng.recipes.transform.TransformCircumstance;

public class TransformCategory implements DisplayCategory<TransformRecipeWrapper> {
    public static final CategoryIdentifier<TransformRecipeWrapper> ID = CategoryIdentifier
            .of(AppEng.makeId("item_transformation"));

    private final Renderer icon;

    public TransformCategory() {
        icon = EntryStacks.of(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED);
    }

    @Override
    public Renderer getIcon() {
        return icon;
    }

    @Override
    public Component getTitle() {
        return ItemModText.TRANSFORM_CATEGORY.text();
    }

    @Override
    public CategoryIdentifier<TransformRecipeWrapper> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public List<Widget> setupDisplay(TransformRecipeWrapper display, Rectangle bounds) {

        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        // First column contains ingredients
        int col1 = bounds.x + 10;
        int x = col1;
        int y = bounds.y + 10;
        int nInputs = display.getInputEntries().size();
        if (nInputs < 3) {
            // so ingredients lists with less than two rows get centered vertically
            y += 9 * (3 - nInputs);
        }
        for (var input : display.getInputEntries()) {
            var slot = Widgets.createSlot(new Point(x, y))
                    .entries(input)
                    .markInput();
            y += slot.getBounds().height;
            if (y >= bounds.y + 64) {
                // we don't actually have room to make multiple columns of ingredients look nice,
                // but this is better than just overflowing downwards.
                y -= 54;
                x += 18;
            }
            widgets.add(slot);
        }

        // To center everything but the ingredients vertically
        int yOffset = bounds.y + 28;

        // Second column is arrow pointing into water
        final int col2 = col1 + 25;
        var arrow1 = Widgets.createArrow(new Point(col2, yOffset));
        widgets.add(arrow1);

        // Third column is water block
        final int col3 = col2 + arrow1.getBounds().getWidth() + 6;
        var catalystSlot = Widgets.createSlot(new Point(col3, yOffset))
                .entries(getCatalystForRendering(display))
                .markInput()
                .backgroundEnabled(false);
        widgets.add(catalystSlot);

        // Fourth column is arrow pointing to results
        final int col4 = col3 + 16 + 5;
        var arrow2 = Widgets.createArrow(new Point(col4, yOffset));
        widgets.add(arrow2);

        // Fifth column is the result
        final int col5 = arrow2.getBounds().getMaxX() + 10;
        var slot = Widgets.createSlot(new Point(col5, yOffset))
                .entries(display.getOutputEntries().get(0))
                .markOutput();
        widgets.add(slot);

        Component circumstance;
        if (display.getTransformCircumstance().isExplosion()) {
            circumstance = ItemModText.EXPLOSION.text();
        } else {
            circumstance = ItemModText.SUBMERGE_IN.text();
        }

        widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.y + 15), circumstance)
                .color(0x7E7E7E)
                .noShadow());

        return widgets;
    }

    private Collection<? extends EntryStack<?>> getCatalystForRendering(TransformRecipeWrapper display) {

        TransformCircumstance circumstance = display.getTransformCircumstance();
        if (circumstance.isFluid()) {
            return circumstance.getFluidsForRendering().stream().map(TransformCategory::makeCustomRenderingFluidEntry)
                    .toList();
        } else if (circumstance.isExplosion()) {
            return List.of(EntryStacks.of(AEBlocks.TINY_TNT), EntryStacks.of(Blocks.TNT));
        } else {
            return List.of();
        }
    }

    private static EntryStack<FluidStack> makeCustomRenderingFluidEntry(Fluid fluid) {
        return EntryStack.of(new FluidEntryDefinition() {
            @Override
            public EntryRenderer<FluidStack> getRenderer() {
                return new FluidBlockRenderer();
            }
        }, FluidStack.create(fluid, FluidStack.bucketAmount()))
                .setting(EntryStack.Settings.FLUID_AMOUNT_VISIBLE, false);
    }

    @Override
    public int getDisplayHeight() {
        return 72;
    }
}
