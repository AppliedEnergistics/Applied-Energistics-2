package appeng.client.integrations.jei;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;

import appeng.client.integrations.jei.widgets.View;
import appeng.client.integrations.jei.widgets.Widget;
import appeng.client.integrations.jei.widgets.WidgetFactory;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ItemModText;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.transform.TransformRecipe;

public class TransformCategory extends ViewBasedCategory<RecipeHolder<TransformRecipe>> {

    public static final IRecipeType<RecipeHolder<TransformRecipe>> RECIPE_TYPE = IRecipeType
            .create(AERecipeTypes.TRANSFORM);

    private final IDrawable icon;

    private final IDrawable arrow;

    private final IDrawable slotBackground;

    private final IPlatformFluidHelper<?> fluidHelper;

    private final FluidBlockRenderer fluidRenderer;

    public TransformCategory(IJeiHelpers helpers) {
        super(helpers);
        IGuiHelper guiHelper = helpers.getGuiHelper();
        slotBackground = guiHelper.createDrawable(JEIPlugin.TEXTURE, 0, 34, 18, 18);
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.stack());
        arrow = guiHelper.createDrawable(JEIPlugin.TEXTURE, 0, 17, 24, 17);
        fluidHelper = helpers.getPlatformFluidHelper();
        fluidRenderer = new FluidBlockRenderer();
    }

    @Override
    public int getWidth() {
        return 130;
    }

    @Override
    public int getHeight() {
        return 62;
    }

    @Override
    public IRecipeType<RecipeHolder<TransformRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    protected View getView(RecipeHolder<TransformRecipe> holder) {
        var recipe = holder.value();
        int yOffset = 23;

        return new View() {
            @Override
            public void createWidgets(WidgetFactory factory, List<Widget> widgets) {
                // Second column is arrow pointing into water
                widgets.add(factory.unfilledArrow(25, yOffset));
                // Fourth column is arrow pointing to results
                widgets.add(factory.unfilledArrow(76, yOffset));

                Component circumstance;
                if (recipe.circumstance.isExplosion()) {
                    circumstance = ItemModText.EXPLOSION.text();
                } else {
                    circumstance = ItemModText.SUBMERGE_IN.text();
                }

                // Text label describing the transform circumstances
                widgets.add(factory.label(getWidth() / 2f, 10, circumstance)
                        .bodyText());
            }

            @Override
            public void buildSlots(IRecipeLayoutBuilder builder) {
                var slotIndex = 0;

                var y = 5;
                var x = 5;
                if (recipe.ingredients().size() < 3) {
                    // so ingredients lists with less than two rows get centered vertically
                    y += (3 - recipe.ingredients().size()) * 18 / 2;
                }
                for (var input : recipe.ingredients()) {
                    builder.addSlot(RecipeIngredientRole.INPUT, x + 1, y + 1)
                            .setSlotName("input" + (slotIndex++))
                            .setBackground(slotBackground, -1, -1)
                            .add(input);
                    y += 18;
                    if (y >= 54) {
                        // we don't actually have room to make multiple columns of ingredients look nice,
                        // but this is better than just overflowing downwards.
                        y -= 54;
                        x += 18;
                    }
                }

                if (recipe.circumstance.isFluid()) {
                    var slot = builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 55 + 1, yOffset + 1)
                            .setSlotName("fluid");

                    for (var fluid : recipe.circumstance.getFluidsForRendering()) {
                        if (!fluid.isSource(fluid.defaultFluidState()))
                            continue;
                        slot.add(fluid, fluidHelper.bucketVolume());
                    }

                    slot.setCustomRenderer(NeoForgeTypes.FLUID_STACK, fluidRenderer);
                } else if (recipe.circumstance.isExplosion()) {
                    builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 55 + 1, yOffset + 1)
                            .setSlotName("explosion")
                            .add(new ItemStack(Blocks.TNT))
                            .add(AEBlocks.TINY_TNT.stack());
                }

                builder.addSlot(RecipeIngredientRole.OUTPUT, 105 + 1, yOffset + 1)
                        .setSlotName("output")
                        .setBackground(slotBackground, -1, -1)
                        .add(recipe.result().create());
            }
        };
    }

    @Override
    public Component getTitle() {
        return ItemModText.TRANSFORM_CATEGORY.text();
    }

}
