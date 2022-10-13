package appeng.integration.modules.jei;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ItemModText;
import appeng.recipes.transform.TransformRecipe;

public class TransformCategory implements IRecipeCategory<TransformRecipe> {

    public static final RecipeType<TransformRecipe> RECIPE_TYPE = RecipeType.create(AppEng.MOD_ID,
            "item_transformation", TransformRecipe.class);

    private final IDrawable icon;

    private final IDrawable background;

    private final IDrawable arrow;

    private final IDrawable slotBackground;

    private final IPlatformFluidHelper<?> fluidHelper;

    private final FluidBlockRenderer fluidRenderer;

    public TransformCategory(IJeiHelpers helpers) {
        IGuiHelper guiHelper = helpers.getGuiHelper();
        background = guiHelper.createBlankDrawable(130, 62);
        slotBackground = guiHelper.createDrawable(JEIPlugin.TEXTURE, 0, 34, 18, 18);
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.stack());
        arrow = guiHelper.createDrawable(JEIPlugin.TEXTURE, 0, 17, 24, 17);
        fluidHelper = helpers.getPlatformFluidHelper();
        fluidRenderer = new FluidBlockRenderer();
    }

    @Override
    public RecipeType<TransformRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TransformRecipe recipe, IFocusGroup focuses) {
        var slotIndex = 0;

        var y = 5;
        var x = 5;
        if (recipe.getIngredients().size() < 3) {
            // so ingredients lists with less than two rows get centered vertically
            y += (3 - recipe.getIngredients().size()) * 18 / 2;
        }
        for (var input : recipe.getIngredients()) {
            builder.addSlot(RecipeIngredientRole.INPUT, x + 1, y + 1)
                    .setSlotName("input" + (slotIndex++))
                    .addIngredients(input);
            y += 18;
            if (y >= 54) {
                // we don't actually have room to make multiple columns of ingredients look nice,
                // but this is better than just overflowing downwards.
                y -= 54;
                x += 18;
            }
        }

        // To center everything but the ingredients vertically
        int yOffset = getYOffset(recipe);

        if (recipe.circumstance.isFluid()) {
            IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.CATALYST, 55 + 1, yOffset + 1)
                    .setSlotName("fluid");

            for (Fluid fluid : recipe.circumstance.getFluidsForRendering()) {
                if (!fluid.isSource(fluid.defaultFluidState()))
                    continue;
                slot.addFluidStack(fluid, fluidHelper.bucketVolume());
            }

            slot.setCustomRenderer(FabricTypes.FLUID_STACK, fluidRenderer);
        } else if (recipe.circumstance.isExplosion()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 55 + 1, yOffset + 1)
                    .setSlotName("explosion")
                    .addItemStack(new ItemStack(Blocks.TNT))
                    .addItemStack(AEBlocks.TINY_TNT.stack());
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 105 + 1, yOffset + 1)
                .setSlotName("output")
                .addItemStack(recipe.getResultItem());

    }

    @Override
    public void draw(TransformRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX,
            double mouseY) {
        // First column contains ingredients
        var y = 5;
        var x = 5;
        if (recipe.getIngredients().size() < 3) {
            // so ingredients lists with less than two rows get centered vertically
            y += (3 - recipe.getIngredients().size()) * 18 / 2;
        }
        for (var input : recipe.getIngredients()) {
            slotBackground.draw(stack, 5, y);
            y += 18;
            if (y >= 54) {
                y -= 54;
                x += 18;
            }
        }

        // To center everything but the ingredients vertically
        int yOffset = getYOffset(recipe);

        // Second column is arrow pointing into water
        arrow.draw(stack, 25, yOffset);

        // Fourth column is arrow pointing to results
        arrow.draw(stack, 76, yOffset);

        // Fifth column is the result
        slotBackground.draw(stack, 105, yOffset);
    }

    private int getYOffset(TransformRecipe recipe) {
        // return (recipe.getIngredients().size() - 1) / 2 * 18 + 5;
        return 23;
    }

    @Override
    public Component getTitle() {
        return ItemModText.TRANSFORM_CATEGORY.text();
    }

}
