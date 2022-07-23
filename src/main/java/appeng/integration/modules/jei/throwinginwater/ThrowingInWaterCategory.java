package appeng.integration.modules.jei.throwinginwater;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ItemModText;

public class ThrowingInWaterCategory implements IRecipeCategory<ThrowingInWaterDisplay> {
    private static final ResourceLocation TEXTURE = AppEng.makeId("textures/guis/jei.png");

    public static final RecipeType<ThrowingInWaterDisplay> RECIPE_TYPE = RecipeType.create(AppEng.MOD_ID,
            "throwing_in_water", ThrowingInWaterDisplay.class);

    private final IDrawable icon;

    private final IDrawable background;

    private final IDrawable arrow;

    private final IDrawable slotBackground;

    public ThrowingInWaterCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(130, 62);
        slotBackground = guiHelper.createDrawable(TEXTURE, 0, 34, 18, 18);
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.stack());
        arrow = guiHelper.createDrawable(TEXTURE, 0, 17, 24, 17);
    }

    @Override
    public RecipeType<ThrowingInWaterDisplay> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, ThrowingInWaterDisplay recipe, IFocusGroup focuses) {
        var slotIndex = 0;

        var y = 5;
        for (var input : recipe.getIngredients()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 5 + 1, y + 1)
                    .setSlotName("input" + (slotIndex++))
                    .addIngredients(input);
            y += 18;
        }

        // To center everything but the ingredients vertically
        int yOffset = getYOffset(recipe);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 105 + 1, yOffset + 1)
                .setSlotName("output")
                .addItemStack(recipe.getResult());
    }

    @Override
    public void draw(ThrowingInWaterDisplay recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX,
            double mouseY) {
        // First column contains ingredients
        var y = 5;
        for (var input : recipe.getIngredients()) {
            slotBackground.draw(stack, 5, y);
            y += 18;
        }

        // To center everything but the ingredients vertically
        int yOffset = getYOffset(recipe);

        // Second column is arrow pointing into water
        arrow.draw(stack, 25, yOffset);

        // Third column is water block
        new WaterBlockRenderer().draw(stack, 55, yOffset);

        // Fourth column is arrow pointing to results
        arrow.draw(stack, 76, yOffset);

        // Fifth column is the result
        slotBackground.draw(stack, 105, yOffset);
    }

    private int getYOffset(ThrowingInWaterDisplay recipe) {
        return (recipe.getIngredients().size() - 1) / 2 * 18 + 5;
    }

    @Override
    public Component getTitle() {
        return ItemModText.THROWING_IN_WATER_CATEGORY.text();
    }

}
