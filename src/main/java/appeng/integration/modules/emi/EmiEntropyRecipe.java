package appeng.integration.modules.emi;

import java.util.List;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.localization.ItemModText;
import appeng.items.tools.powered.EntropyManipulatorItem;
import appeng.recipes.entropy.EntropyRecipe;

/**
 * Adapts {@link appeng.recipes.entropy.EntropyRecipe} for EMI.
 */
public class EmiEntropyRecipe extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new AppEngRecipeCategory("entropy", createIcon(),
            EmiText.CATEGORY_ENTROPY_MANIPULATOR);

    private static final int BODY_TEXT_COLOR = 0x7E7E7E;

    private final EntropyRecipe recipe;
    private final EmiStack inputBlockIngredient;
    private final boolean inputFluidFlowing;
    private final EmiStack outputBlockIngredient;
    private final boolean outputFluidFlowing;
    private final List<EmiStack> additionalDrops;
    private final boolean inputConsumed;

    public EmiEntropyRecipe(RecipeHolder<EntropyRecipe> holder) {
        super(CATEGORY, holder.id(), 130, 50);
        recipe = holder.value();

        // In-World Block/Fluid input
        var input = recipe.getInput();
        inputBlockIngredient = createIngredient(
                input.block().map(EntropyRecipe.BlockInput::block).orElse(null),
                input.fluid().map(EntropyRecipe.FluidInput::fluid).orElse(null));
        inputFluidFlowing = input.fluid().map(EntropyRecipe.FluidInput::fluid).map(this::isFlowing).orElse(false);
        inputs.add(inputBlockIngredient);

        // In-World Block/Fluid output
        var output = recipe.getOutput();
        outputBlockIngredient = createIngredient(
                output.block().map(EntropyRecipe.BlockOutput::block).orElse(null),
                output.fluid().map(EntropyRecipe.FluidOutput::fluid).orElse(null));
        outputFluidFlowing = output.fluid().map(EntropyRecipe.FluidOutput::fluid).map(this::isFlowing).orElse(false);
        if (!outputBlockIngredient.isEmpty()) {
            outputs.add(outputBlockIngredient);
        }
        inputConsumed = output.block().isPresent() && output.block().get().block().defaultBlockState().isAir()
                && (output.fluid().isEmpty() || output.fluid().get().fluid() == Fluids.EMPTY);
        if (!inputConsumed) {
            inputBlockIngredient.setRemainder(inputBlockIngredient);
        }

        // Additional item drops
        additionalDrops = recipe.getDrops()
                .stream()
                .map(EmiStack::of)
                .toList();
        outputs.addAll(additionalDrops);
    }

    private boolean isFlowing(Fluid fluid) {
        return fluid != Fluids.EMPTY && !fluid.isSource(fluid.defaultFluidState());
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var centerX = width / 2;
        var labelText = switch (recipe.getMode()) {
            case HEAT -> ItemModText.ENTROPY_MANIPULATOR_HEAT.text(EntropyManipulatorItem.ENERGY_PER_USE);
            case COOL -> ItemModText.ENTROPY_MANIPULATOR_COOL.text(EntropyManipulatorItem.ENERGY_PER_USE);
        };
        var interaction = switch (recipe.getMode()) {
            case HEAT -> ItemModText.RIGHT_CLICK.text();
            case COOL -> ItemModText.SHIFT_RIGHT_CLICK.text();
        };

        var modeLabel = widgets.addText(labelText, centerX + 4, 2, BODY_TEXT_COLOR, false)
                .horizontalAlign(TextWidget.Alignment.CENTER);
        var modeLabelX = modeLabel.getBounds().x();
        switch (recipe.getMode()) {
            case HEAT -> widgets.addTexture(AppEngEmiPlugin.TEXTURE, modeLabelX - 9, 3, 6, 6, 0, 68);
            case COOL -> widgets.addTexture(AppEngEmiPlugin.TEXTURE, modeLabelX - 9, 3, 6, 6, 6, 68);
        }

        widgets.addTexture(EmiTexture.EMPTY_ARROW, centerX - 12, 14);
        widgets.addText(interaction, centerX, 38, BODY_TEXT_COLOR, false)
                .horizontalAlign(TextWidget.Alignment.CENTER);

        widgets.add(new EmiEntropySlot(inputBlockIngredient, false, inputFluidFlowing, width / 2 - 35, 14));

        int x = centerX + 20;

        // In-World Block or Fluid output
        if (inputConsumed) {
            widgets.add(new EmiEntropySlot(inputBlockIngredient, true, outputFluidFlowing, x - 1, 14));
            x += 18;
        } else if (!outputBlockIngredient.isEmpty()) {
            widgets.add(new EmiEntropySlot(outputBlockIngredient, false, outputFluidFlowing, x - 1, 14)
                    .recipeContext(this));
            x += 18;
        }

        for (var drop : additionalDrops) {
            widgets.addSlot(drop, x - 1, 14).recipeContext(this);
            x += 18;
        }
    }

    private static EmiRenderable createIcon() {
        return new EmiTexture(
                AppEng.makeId("textures/item/entropy_manipulator.png"),
                0,
                0,
                16,
                16,
                16,
                16,
                16,
                16);
    }

    private static EmiStack createIngredient(Block block, Fluid fluid) {
        if (fluid != null) {
            // We need to tell the player that they need to use the manipulator on the *flowing* variant
            // anyway, so this if-block would be needed in any case.
            if (!fluid.isSource(fluid.defaultFluidState())) {
                if (fluid instanceof FlowingFluid flowingFluid) {
                    return EmiStack.of(flowingFluid.getSource());
                } else {
                    // Don't really know how to get the source :-(
                    AELog.warn("Don't know how to get the source fluid for %s", fluid);
                    return EmiStack.of(fluid);
                }
            } else {
                return EmiStack.of(fluid);
            }
        } else if (block != null) {
            return EmiStack.of(block.asItem().getDefaultInstance());
        } else {
            return EmiStack.EMPTY;
        }
    }
}
