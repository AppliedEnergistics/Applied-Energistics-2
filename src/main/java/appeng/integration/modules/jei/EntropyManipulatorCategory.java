package appeng.integration.modules.jei;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;

import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.jei.widgets.View;
import appeng.integration.modules.jei.widgets.Widget;
import appeng.integration.modules.jei.widgets.WidgetFactory;
import appeng.items.tools.powered.EntropyManipulatorItem;
import appeng.recipes.entropy.EntropyRecipe;

public class EntropyManipulatorCategory extends ViewBasedCategory<EntropyRecipe> {
    public static final RecipeType<EntropyRecipe> TYPE = RecipeType.create(AppEng.MOD_ID, "entropy",
            EntropyRecipe.class);

    private final IDrawable slotBackground;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable blockDestroyOverlay;
    private final IDrawable iconHeat;
    private final IDrawable iconCool;
    private final int centerX;

    public EntropyManipulatorCategory(IJeiHelpers helpers) {
        super(helpers);
        var guiHelper = helpers.getGuiHelper();
        this.slotBackground = guiHelper.getSlotDrawable();
        this.background = guiHelper.createBlankDrawable(130, 50);
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
        this.centerX = background.getWidth() / 2;
    }

    @Override
    protected View getView(EntropyRecipe recipe) {
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
                setFluidOrBlockSlot(input, recipe.getInputBlock(), recipe.getInputFluid());

                int x = centerX + 20;

                if (recipe.getOutputBlock() == Blocks.AIR
                        && (recipe.getOutputFluid() == null || recipe.getOutputFluid() == Fluids.EMPTY)) {
                    // If the recipe destroys the block and produces no fluid in return,
                    // show the input again, but overlay it with an X.
                    var destroyed = builder.addSlot(RecipeIngredientRole.RENDER_ONLY, x, 15)
                            .setBackground(slotBackground, -1, -1);
                    setFluidOrBlockSlot(destroyed, recipe.getInputBlock(), recipe.getInputFluid());
                    destroyed.setOverlay(blockDestroyOverlay, 0, 0);
                    destroyed.addTooltipCallback((recipeSlotView, tooltip) -> {
                        tooltip.add(ItemModText.CONSUMED.text().withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                    });
                    x += 18;
                } else if (recipe.getOutputBlock() != null || recipe.getOutputFluid() != null) {
                    var output = builder.addSlot(RecipeIngredientRole.OUTPUT, x, 15)
                            .setBackground(slotBackground, -1, -1);
                    setFluidOrBlockSlot(output, recipe.getOutputBlock(), recipe.getOutputFluid());
                    x += 18;
                }

                for (var drop : recipe.getDrops()) {
                    var output = builder.addSlot(RecipeIngredientRole.OUTPUT, x, 15)
                            .setBackground(slotBackground, -1, -1);
                    output.addItemStack(drop);
                    x += 18;
                }
            }
        };
    }

    private void setFluidOrBlockSlot(IRecipeSlotBuilder slot, Block block, Fluid fluid) {
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
                slot.addTooltipCallback((recipeSlotView, tooltip) -> {
                    var firstLine = tooltip.get(0);
                    tooltip.set(0, ItemModText.FLOWING_FLUID_NAME.text(firstLine));
                });
            } else {
                addFluidStack(slot, fluid);
            }
        } else if (block != null) {
            slot.addItemStack(block.asItem().getDefaultInstance());
        }
    }

    private static void addFluidStack(IRecipeSlotBuilder slot, Fluid fluid) {
        slot.addIngredient(ForgeTypes.FLUID_STACK, new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME));
    }

    @Override
    public RecipeType<EntropyRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return AEItems.ENTROPY_MANIPULATOR.asItem().getDescription();
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }
}
