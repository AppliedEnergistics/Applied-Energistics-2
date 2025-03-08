package appeng.integration.modules.rei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;

import appeng.core.AELog;
import appeng.core.localization.ItemModText;
import appeng.recipes.entropy.EntropyRecipe;

public class EntropyRecipeDisplay implements Display {

    private final RecipeHolder<EntropyRecipe> holder;
    private final EntryIngredient input;
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> consumed;
    private final List<EntryIngredient> outputs;

    public EntropyRecipeDisplay(RecipeHolder<EntropyRecipe> holder) {
        this.holder = holder;
        var recipe = holder.value();

        // In-World Block/Fluid input
        var input = recipe.getInput();
        this.input = EntryIngredient.of(createIngredient(
                input.block().map(EntropyRecipe.BlockInput::block).orElse(null),
                input.fluid().map(EntropyRecipe.FluidInput::fluid).orElse(null)));
        inputs = List.of(this.input);

        // In-World Block/Fluid output
        var output = recipe.getOutput();

        var outputs = new ArrayList<EntryIngredient>();
        var inputConsumed = output.block().isPresent() && output.block().get().block().defaultBlockState().isAir()
                && (output.fluid().isEmpty() || output.fluid().get().fluid() == Fluids.EMPTY);
        if (inputConsumed) {
            this.consumed = List.of(this.input.map(EntropyRecipeDisplay::makeConsumed));
        } else {
            this.consumed = List.of();
            if (output.block().isPresent() || output.fluid().isPresent()) {
                outputs.add(EntryIngredient.of(createIngredient(
                        output.block().map(EntropyRecipe.BlockOutput::block).orElse(null),
                        output.fluid().map(EntropyRecipe.FluidOutput::fluid).orElse(null))));
            }

            // Additional item drops
            recipe.getDrops()
                    .stream()
                    .map(EntryIngredients::of)
                    .forEach(outputs::add);
        }
        this.outputs = List.copyOf(outputs);
    }

    private static <T> EntryStack<T> makeConsumed(EntryStack<T> entryStack) {
        entryStack = entryStack.copy();

        entryStack.tooltip(ItemModText.CONSUMED.text().withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        entryStack.withRenderer(new EntryRenderer<>() {
            @Override
            public void render(EntryStack<T> entry, GuiGraphics graphics, Rectangle bounds, int mouseX,
                    int mouseY, float delta) {
                var baseRenderer = entry.getDefinition().getRenderer();
                baseRenderer.render(entry, graphics, bounds, mouseX, mouseY, delta);
                graphics.blit(RenderType::guiTextured, ReiPlugin.TEXTURE, bounds.x, bounds.y, 0, 0,
                        0, 52, 16, 16);
            }

            @Override
            public @Nullable Tooltip getTooltip(EntryStack<T> entry, TooltipContext context) {
                var baseRenderer = entry.getDefinition().getRenderer();
                return baseRenderer.getTooltip(entry, context);
            }
        });

        return entryStack;
    }

    public RecipeHolder<EntropyRecipe> getHolder() {
        return holder;
    }

    public EntropyRecipe getRecipe() {
        return holder.value();
    }

    public EntryIngredient getInput() {
        return input;
    }

    public List<EntryIngredient> getConsumed() {
        return consumed;
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return outputs;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return EntropyRecipeCategory.ID;
    }

    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(holder.id().location());
    }

    private static EntryStack<?> createIngredient(Block block, Fluid fluid) {
        if (fluid != null) {
            // We need to tell the player that they need to use the manipulator on the *flowing* variant
            // anyway, so this if-block would be needed in any case.
            if (!fluid.isSource(fluid.defaultFluidState())) {
                EntryStack<?> entryStack;
                if (fluid instanceof FlowingFluid flowingFluid) {
                    entryStack = EntryStacks.of(flowingFluid.getSource());
                } else {
                    // Don't really know how to get the source :-(
                    AELog.warn("Don't know how to get the source fluid for %s", fluid);
                    entryStack = EntryStacks.of(fluid);
                }
                entryStack.tooltipProcessor(EntropyRecipeDisplay::addFlowingToTooltip);
                return entryStack;
            } else {
                return EntryStacks.of(fluid);
            }
        } else if (block != null) {
            return EntryStacks.of(block.asItem().getDefaultInstance());
        } else {
            return EntryStack.empty();
        }
    }

    private static Tooltip addFlowingToTooltip(EntryStack<?> entryStack, Tooltip tooltip) {
        var newTooltip = Tooltip.from().withContextStack(tooltip.getContextStack());

        // Change the first tooltip entry text to have a "Flowing" suffix
        boolean appended = false;
        for (var entry : tooltip.entries()) {
            if (!appended && entry.isText()) {
                appended = true;
                newTooltip.add(ItemModText.FLOWING_FLUID_NAME.text(entry.getAsText()));
            } else {
                if (entry.isText()) {
                    newTooltip.add(entry.getAsText());
                } else {
                    newTooltip.add(entry.getAsTooltipComponent());
                }
            }
        }

        return newTooltip;
    }
}
