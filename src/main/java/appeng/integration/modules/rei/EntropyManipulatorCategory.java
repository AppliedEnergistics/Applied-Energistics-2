package appeng.integration.modules.rei;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.Streams;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidAttributes;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.AbstractRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;

import appeng.client.gui.style.Blitter;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ItemModText;
import appeng.items.tools.powered.EntropyManipulatorItem;
import appeng.recipes.entropy.EntropyRecipe;

public class EntropyManipulatorCategory implements DisplayCategory<EntropyManipulatorDisplay> {
    public static final CategoryIdentifier<EntropyManipulatorDisplay> ID = CategoryIdentifier
            .of(AppEng.makeId("entropy"));

    private final Blitter icon;
    private final Widget blockDestroyOverlay;
    private final Widget iconHeat;
    private final Widget iconCool;
    private final int centerX;

    public EntropyManipulatorCategory() {
        // We don't use an item drawable here because it would show the charge bar
        this.icon = Blitter.texture("item/entropy_manipulator.png", 16, 16)
                .src(0, 0, 16, 16);
        this.blockDestroyOverlay = Widgets.createTexturedWidget(ReiPlugin.TEXTURE, 0, 0, 0, 52, 16, 16);
        this.iconHeat = Widgets.createTexturedWidget(ReiPlugin.TEXTURE, 0, 0, 0, 68, 6, 6);
        this.iconCool = Widgets.createTexturedWidget(ReiPlugin.TEXTURE, 0, 0, 6, 68, 6, 6);
        this.centerX = 130 / 2;
    }

    private void setFluidOrBlockSlot(Slot slot, Block block, Fluid fluid) {
        if (fluid != null) {
            // The volume does assume BUCKET == BLOCK in terms of volume. But most of the time this should be true.

            // On Fabric, we cannot add fluid variants for flowing fluids so rendering would fail.
            // But we need to tell the player that they need to use the manipulator on the *flowing* variant
            // anyway, so this if-block would be needed in any case.
            if (!fluid.isSource(fluid.defaultFluidState())) {
                if (fluid instanceof FlowingFluid flowingFluid) {
                    addFluidStack(slot, flowingFluid.getSource());
                } else {
                    // Don't really know how to get the source :-(
                    addFluidStack(slot, fluid);
                    AELog.warn("Don't know how to get the source fluid for %s", fluid);
                }
                for (var entry : slot.getEntries()) {
                    ClientEntryStacks.setTooltipProcessor(entry, (entryStack, tooltip) -> {
                        var entries = tooltip.entries();
                        var lines = Streams.concat(
                                Stream.of(
                                        Tooltip.entry(ItemModText.FLOWING_FLUID_NAME.text(entries.get(0).getAsText()))),
                                entries.stream().skip(1));
                        return Tooltip.from(new Point(tooltip.getX(), tooltip.getY()), lines.toList());
                    });
                }
            } else {
                addFluidStack(slot, fluid);
            }
        } else if (block != null) {
            slot.entry(EntryStacks.of(block));
        }
    }

    private static void addFluidStack(Slot slot, Fluid fluid) {
        slot.entry(EntryStacks.of(fluid, FluidAttributes.BUCKET_VOLUME));
    }

    @Override
    public CategoryIdentifier<? extends EntropyManipulatorDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public Component getTitle() {
        return AEItems.ENTROPY_MANIPULATOR.asItem().getDescription();
    }

    @Override
    public Renderer getIcon() {
        return new AbstractRenderer() {
            @Override
            public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                icon.dest(bounds.x, bounds.y).blit(matrices, getBlitOffset());
            }
        };
    }

    @Override
    public List<me.shedaniel.rei.api.client.gui.widgets.Widget> setupDisplay(EntropyManipulatorDisplay display,
            Rectangle bounds) {
        var widgets = new ArrayList<me.shedaniel.rei.api.client.gui.widgets.Widget>();
        widgets.add(Widgets.createRecipeBase(bounds));

        var recipe = display.getRecipe();

        var icon = switch (recipe.getMode()) {
            case HEAT -> iconHeat;
            case COOL -> iconCool;
        };
        var labelText = switch (recipe.getMode()) {
            case HEAT -> ItemModText.ENTROPY_MANIPULATOR_HEAT.text(EntropyManipulatorItem.ENERGY_PER_USE);
            case COOL -> ItemModText.ENTROPY_MANIPULATOR_COOL.text(EntropyManipulatorItem.ENERGY_PER_USE);
        };
        var interaction = switch (recipe.getMode()) {
            case HEAT -> ItemModText.RIGHT_CLICK.text();
            case COOL -> ItemModText.SHIFT_RIGHT_CLICK.text();
        };

        var modeLabel = Widgets.createLabel(new Point(centerX + 4, 2), labelText)
                .noShadow()
                .color(0x7E7E7E);
        widgets.add(modeLabel);
        widgets.add(Widgets.withTranslate(icon, modeLabel.getBounds().getX() - 9, 3, 0));
        widgets.add(Widgets.createArrow(new Point(centerX - 12, 14)));
        widgets.add(Widgets.createLabel(new Point(centerX, 38), interaction)
                .noShadow()
                .color(0x7E7E7E));

        setupSlots(widgets, recipe);

        for (int i = 0; i < widgets.size(); i++) {
            if (i > 0) {
                widgets.set(i, Widgets.withTranslate(widgets.get(i), bounds.x, bounds.y + 3, 0));
            }
        }

        return widgets;
    }

    private void setupSlots(ArrayList<Widget> widgets, EntropyRecipe recipe) {
        var input = Widgets.createSlot(new Point(centerX - 36, 15))
                .backgroundEnabled(true)
                .markInput();
        widgets.add(input);
        setFluidOrBlockSlot(input, recipe.getInputBlock(), recipe.getInputFluid());

        int x = centerX + 20;

        if (recipe.getOutputBlock() == Blocks.AIR
                && (recipe.getOutputFluid() == null || recipe.getOutputFluid() == Fluids.EMPTY)) {
            // If the recipe destroys the block and produces no fluid in return,
            // show the input again, but overlay it with an X.
            var destroyed = Widgets.createSlot(new Point(x, 15))
                    .backgroundEnabled(true)
                    .unmarkInputOrOutput();
            widgets.add(destroyed);
            setFluidOrBlockSlot(destroyed, recipe.getInputBlock(), recipe.getInputFluid());
            widgets.add(Widgets.withTranslate(blockDestroyOverlay, x, 15, 0));
            for (var entry : destroyed.getEntries()) {
                entry.tooltip(ItemModText.CONSUMED.text().withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            }
            x += 18;
        } else if (recipe.getOutputBlock() != null || recipe.getOutputFluid() != null) {
            var output = Widgets.createSlot(new Point(x, 15))
                    .backgroundEnabled(true)
                    .markOutput();
            widgets.add(output);
            setFluidOrBlockSlot(output, recipe.getOutputBlock(), recipe.getOutputFluid());
            x += 18;
        }

        for (var drop : recipe.getDrops()) {
            var output = Widgets.createSlot(new Point(x, 15))
                    .backgroundEnabled(true)
                    .markOutput();
            widgets.add(output);
            output.entry(EntryStacks.of(drop));
            x += 18;
        }
    }

    @Override
    public int getDisplayWidth(EntropyManipulatorDisplay display) {
        return 130;
    }

    @Override
    public int getDisplayHeight() {
        return 55;
    }
}
