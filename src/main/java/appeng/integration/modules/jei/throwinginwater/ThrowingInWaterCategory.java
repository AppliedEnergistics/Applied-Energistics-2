package appeng.integration.modules.jei.throwinginwater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import org.apache.commons.lang3.time.DurationFormatUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.entity.GrowingCrystalEntity;
import appeng.items.misc.CrystalSeedItem;

public class ThrowingInWaterCategory implements IRecipeCategory<ThrowingInWaterDisplay> {
    private static final ResourceLocation TEXTURE = AppEng.makeId("textures/guis/jei.png");

    public static final ResourceLocation ID = AppEng.makeId("throwing_in_water");

    private final IDrawable icon;

    private final IDrawable background;

    private final IDrawable arrow;

    private final IDrawable slotBackground;

    private final IDrawableAnimated animatedArrow;

    public ThrowingInWaterCategory(IGuiHelper guiHelper) {
        var stage1 = AEItems.CERTUS_CRYSTAL_SEED.stack();
        CrystalSeedItem.setGrowthTicks(stage1, 0);
        var stage2 = AEItems.CERTUS_CRYSTAL_SEED.stack();
        CrystalSeedItem.setGrowthTicks(stage2, (int) (CrystalSeedItem.GROWTH_TICKS_REQUIRED * 0.4f));
        var stage3 = AEItems.CERTUS_CRYSTAL_SEED.stack();
        CrystalSeedItem.setGrowthTicks(stage3, (int) (CrystalSeedItem.GROWTH_TICKS_REQUIRED * 0.7f));
        var result = AEItems.CERTUS_QUARTZ_CRYSTAL.stack();

        background = guiHelper.createBlankDrawable(130, 62);
        slotBackground = guiHelper.createDrawable(TEXTURE, 0, 34, 18, 18);
        icon = new GrowingSeedIconRenderer(guiHelper, List.of(
                stage1,
                stage2,
                stage3,
                result));
        arrow = guiHelper.createDrawable(TEXTURE, 0, 17, 24, 17);
        animatedArrow = guiHelper.createAnimatedDrawable(
                guiHelper.createDrawable(TEXTURE, 0, 0, 24, 17),
                60, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }

    @Override
    public Class<? extends ThrowingInWaterDisplay> getRecipeClass() {
        return ThrowingInWaterDisplay.class;
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
    public void setIngredients(ThrowingInWaterDisplay recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(recipe.getIngredients());
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getResult());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ThrowingInWaterDisplay recipe, IIngredients ingredients) {

        var slotIndex = 0;
        var guiItemStacks = recipeLayout.getItemStacks();

        var y = 5;
        for (var input : recipe.getIngredients()) {
            guiItemStacks.init(slotIndex++, true, 5, y);
            y += 18;
        }

        // To center everything but the ingredients vertically
        int yOffset = getYOffset(recipe);
        guiItemStacks.init(slotIndex, false, 105, yOffset);

        guiItemStacks.set(ingredients);
    }

    @Override
    public void draw(ThrowingInWaterDisplay recipe, PoseStack stack, double mouseX, double mouseY) {

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
        if (recipe.isSupportsAccelerators()) {
            // If the recipe doesn't support accelerators, it's probably instant
            animatedArrow.draw(stack, 76, yOffset);
        }

        // Fifth column is the result
        slotBackground.draw(stack, 105, yOffset);

        // Add descriptive text explaining the duration centered on the water block
        if (recipe.isSupportsAccelerators()) {
            var durationY = 10 + recipe.getIngredients().size() * 18 + 2;

            var defaultDuration = GrowingCrystalEntity.getGrowthDuration(0).toMillis();

            var minecraft = Minecraft.getInstance();

            var text = new TextComponent(DurationFormatUtils.formatDurationWords(
                    defaultDuration, true, true));
            var textWidth = minecraft.font.width(text);
            minecraft.font.draw(stack,
                    text,
                    (background.getWidth() - textWidth) / 2.0f,
                    durationY,
                    0xFF404040);
        }

    }

    private int getYOffset(ThrowingInWaterDisplay recipe) {
        return (recipe.getIngredients().size() - 1) / 2 * 18 + 5;
    }

    @Override
    public List<Component> getTooltipStrings(ThrowingInWaterDisplay recipe, double mouseX, double mouseY) {
        var yOffset = getYOffset(recipe);

        if (recipe.isSupportsAccelerators() && mouseY >= yOffset + 18 && mouseY <= yOffset + 31) {
            List<Component> tooltipLines = new ArrayList<>();
            tooltipLines.add(
                    new TranslatableComponent("rei.ae2.with_crystal_growth_accelerators"));
            for (var i = 1; i <= 5; i++) {
                var duration = GrowingCrystalEntity.getGrowthDuration(i).toMillis();
                tooltipLines.add(new TextComponent(i + ": " + DurationFormatUtils.formatDurationWords(
                        duration, true, true)));
            }
            return tooltipLines;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("rei.ae2.throwing_in_water_category");
    }

}
