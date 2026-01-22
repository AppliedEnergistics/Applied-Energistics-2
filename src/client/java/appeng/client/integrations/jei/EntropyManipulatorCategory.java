package appeng.client.integrations.jei;

import java.util.List;

import com.mojang.datafixers.util.Either;

import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;

import appeng.client.integrations.jei.widgets.View;
import appeng.client.integrations.jei.widgets.Widget;
import appeng.client.integrations.jei.widgets.WidgetFactory;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ItemModText;
import appeng.items.tools.powered.EntropyManipulatorItem;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.entropy.EntropyRecipe;

public class EntropyManipulatorCategory extends ViewBasedCategory<RecipeHolder<EntropyRecipe>> {
    public static final IRecipeType<RecipeHolder<EntropyRecipe>> TYPE = IRecipeType.create(AERecipeTypes.ENTROPY);

    private final IDrawable slotBackground;
    private final IDrawable icon;
    private final IPlatformFluidHelper<?> fluidHelper;
    private final IDrawable blockDestroyOverlay;
    private final IDrawable iconHeat;
    private final IDrawable iconCool;
    private final int centerX;

    public EntropyManipulatorCategory(IJeiHelpers helpers) {
        super(helpers);
        var guiHelper = helpers.getGuiHelper();
        this.slotBackground = guiHelper.getSlotDrawable();
        this.fluidHelper = helpers.getPlatformFluidHelper();
        // We don't use an item drawable here because it would show the charge bar
        this.icon = guiHelper.drawableBuilder(
                AppEng.makeId("textures/item/entropy_manipulator.png"),
                0,
                0,
                16,
                16).setTextureSize(16, 16).build();
        this.blockDestroyOverlay = guiHelper.createDrawable(JEIPlugin.TEXTURE, 0, 52, 16, 16);
        this.iconHeat = guiHelper.createDrawable(JEIPlugin.TEXTURE, 0, 68, 6, 6);
        this.iconCool = guiHelper.createDrawable(JEIPlugin.TEXTURE, 6, 68, 6, 6);
        this.centerX = getWidth() / 2;
    }

    @Override
    public int getWidth() {
        return 130;
    }

    @Override
    public int getHeight() {
        return 50;
    }

    @Override
    protected View getView(RecipeHolder<EntropyRecipe> holder) {
        var recipe = holder.value();

        return new View() {
            @Override
            public void createWidgets(WidgetFactory factory, List<Widget> widgets) {
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

                var modeLabel = factory.label(centerX + 4, 2, labelText).bodyText();
                widgets.add(modeLabel);
                widgets.add(factory.drawable(modeLabel.getBounds().getX() - 9, 3, icon));
                widgets.add(factory.unfilledArrow(centerX - 12, 14));
                widgets.add(factory.label(centerX, 38, interaction).bodyText());
            }

            @Override
            public void buildSlots(IRecipeLayoutBuilder builder) {
                var input = builder.addSlot(RecipeIngredientRole.INPUT, centerX - 36, 15)
                        .setBackground(slotBackground, -1, -1);
                var inputFluid = recipe.getInput().fluid().map(EntropyRecipe.FluidInput::fluid).orElse(null);
                var inputBlock = recipe.getInput().block().map(EntropyRecipe.BlockInput::block).orElse(null);
                setFluidOrBlockSlot(input, inputBlock, inputFluid);

                int x = centerX + 20;

                var outputBlock = recipe.getOutput().block().map(EntropyRecipe.BlockOutput::block).orElse(null);
                var outputFluid = recipe.getOutput().fluid().map(EntropyRecipe.FluidOutput::fluid).orElse(null);

                if (outputBlock == Blocks.AIR
                        && (outputFluid == null || outputFluid == Fluids.EMPTY)) {
                    // If the recipe destroys the block and produces no fluid in return,
                    // show the input again, but overlay it with an X.
                    var destroyed = builder.addSlot(RecipeIngredientRole.RENDER_ONLY, x, 15)
                            .setBackground(slotBackground, -1, -1);
                    setFluidOrBlockSlot(destroyed, inputBlock, inputFluid);
                    destroyed.setOverlay(blockDestroyOverlay, 0, 0);
                    destroyed.addRichTooltipCallback((recipeSlotView, tooltip) -> {
                        tooltip.add(ItemModText.CONSUMED.text().withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                    });
                    x += 18;
                } else if (outputBlock != null || outputFluid != null) {
                    var output = builder.addSlot(RecipeIngredientRole.OUTPUT, x, 15)
                            .setBackground(slotBackground, -1, -1);
                    setFluidOrBlockSlot(output, outputBlock, outputFluid);
                    x += 18;
                }

                for (var drop : recipe.getDrops()) {
                    var output = builder.addSlot(RecipeIngredientRole.OUTPUT, x, 15)
                            .setBackground(slotBackground, -1, -1);
                    output.add(drop.create());
                    x += 18;
                }
            }
        };
    }

    private void setFluidOrBlockSlot(IRecipeSlotBuilder slot, @Nullable Block block, @Nullable Fluid fluid) {
        if (fluid != null) {
            // The volume does assume BUCKET == BLOCK in terms of volume. But most of the time this should be true.

            // On Fabric, we cannot add fluid variants for flowing fluids so rendering would fail.
            // But we need to tell the player that they need to use the manipulator on the *flowing* variant
            // anyway, so this if-block would be needed in any case.
            if (!fluid.isSource(fluid.defaultFluidState())) {
                if (fluid instanceof FlowingFluid flowingFluid) {
                    slot.add(flowingFluid.getSource(), fluidHelper.bucketVolume());
                } else {
                    // Don't really know how to get the source :-(
                    slot.add(fluid, fluidHelper.bucketVolume());
                    AELog.warn("Don't know how to get the source fluid for %s", fluid);
                }
                slot.addRichTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.getLines().set(
                            0,
                            Either.left(ItemModText.FLOWING_FLUID_NAME.text(fluid.getFluidType().getDescription())));
                });
            } else {
                slot.add(fluid, fluidHelper.bucketVolume());
            }
        } else if (block != null) {
            slot.add(block.asItem().getDefaultInstance());
        }
    }

    @Override
    public IRecipeType<RecipeHolder<EntropyRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return AEItems.ENTROPY_MANIPULATOR.getName();
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }
}
