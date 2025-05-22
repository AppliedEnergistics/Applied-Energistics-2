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
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;

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
        // TODO 1.19.3 for (var input : display.getInputEntries()) {
        // TODO 1.19.3 var slot = Widgets.createSlot(new Point(x, y))
        // TODO 1.19.3 .entries(input)
        // TODO 1.19.3 .markInput();
        // TODO 1.19.3 y += slot.getBounds().height;
        // TODO 1.19.3 if (y >= bounds.y + 64) {
        // TODO 1.19.3 // we don't actually have room to make multiple columns of ingredients look nice,
        // TODO 1.19.3 // but this is better than just overflowing downwards.
        // TODO 1.19.3 y -= 54;
        // TODO 1.19.3 x += 18;
        // TODO 1.19.3 }
        // TODO 1.19.3 widgets.add(slot);
        // TODO 1.19.3 }

        // To center everything but the ingredients vertically
        int yOffset = bounds.y + 28;

        // Second column is arrow pointing into water
        final int col2 = col1 + 25;
        var arrow1 = Widgets.createArrow(new Point(col2, yOffset));
        widgets.add(arrow1);

// TODO 1.19.3        // Third column is water block
// TODO 1.19.3        final int col3 = col2 + arrow1.getBounds().getWidth() + 6;
// TODO 1.19.3        var catalystSlot = Widgets.createSlot(new Point(col3, yOffset))
// TODO 1.19.3                .entries(getCatalystForRendering(display))
// TODO 1.19.3                .markInput()
// TODO 1.19.3                .backgroundEnabled(false);
// TODO 1.19.3        widgets.add(catalystSlot);
// TODO 1.19.3
// TODO 1.19.3        // Fourth column is arrow pointing to results
// TODO 1.19.3        final int col4 = col3 + 16 + 5;
// TODO 1.19.3        var arrow2 = Widgets.createArrow(new Point(col4, yOffset));
// TODO 1.19.3        widgets.add(arrow2);
// TODO 1.19.3
// TODO 1.19.3        // Fifth column is the result
// TODO 1.19.3        final int col5 = arrow2.getBounds().getMaxX() + 10;
// TODO 1.19.3        var slot = Widgets.createSlot(new Point(col5, yOffset))
// TODO 1.19.3                .entries(display.getOutputEntries().get(0))
// TODO 1.19.3                .markOutput();
// TODO 1.19.3        widgets.add(slot);
// TODO 1.19.3
// TODO 1.19.3        Component circumstance;
// TODO 1.19.3        if (display.getTransformCircumstance().isExplosion()) {
// TODO 1.19.3            circumstance = ItemModText.EXPLOSION.text();
// TODO 1.19.3        } else {
// TODO 1.19.3            circumstance = ItemModText.SUBMERGE_IN.text();
// TODO 1.19.3        }
// TODO 1.19.3
// TODO 1.19.3        widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.y + 15), circumstance)
// TODO 1.19.3                .color(0x7E7E7E)
// TODO 1.19.3                .noShadow());
// TODO 1.19.3
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

    /**
     * Creates an entry stack that renders as a 3d block instead of a slot.
     */
    private static EntryStack<FluidStack> makeCustomRenderingFluidEntry(Fluid fluid) {
        var fluidStack = EntryStacks.of(fluid);
        ClientEntryStacks.setRenderer(fluidStack, entryStack -> {
            return new FluidBlockRenderer();
        });
        return fluidStack;
    }

    @Override
    public int getDisplayHeight() {
        return 72;
    }
}
