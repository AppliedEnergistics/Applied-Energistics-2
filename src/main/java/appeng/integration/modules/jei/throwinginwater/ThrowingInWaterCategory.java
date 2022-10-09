package appeng.integration.modules.jei.throwinginwater;

import appeng.core.definitions.AEBlocks;
import appeng.entity.GrowingCrystalEntity;
import appeng.items.misc.CrystalSeedItem;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.drawable.IDrawableAnimated;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

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
import appeng.integration.modules.jei.JEIPlugin;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.compress.archivers.dump.DumpArchiveEntry;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThrowingInWaterCategory implements IRecipeCategory<ThrowingInWaterDisplay> {

    public static final RecipeType<ThrowingInWaterDisplay> RECIPE_TYPE = RecipeType.create(AppEng.MOD_ID,
            "throwing_in_water", ThrowingInWaterDisplay.class);

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
        slotBackground = guiHelper.createDrawable(JEIPlugin.TEXTURE, 0, 34, 18, 18);
        icon = new GrowingSeedIconRenderer(guiHelper, List.of(
                stage1,
                stage2,
                stage3,
                result));
        arrow = guiHelper.createDrawable(JEIPlugin.TEXTURE, 0, 17, 24, 17);
        animatedArrow = guiHelper.createAnimatedDrawable(
                guiHelper.createDrawable(JEIPlugin.TEXTURE, 0, 0, 24, 17),
                60, IDrawableAnimated.StartDirection.LEFT, false);
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

        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST)
                .addItemStack(AEBlocks.QUARTZ_GROWTH_ACCELERATOR.stack());
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
    public List<Component> getTooltipStrings(ThrowingInWaterDisplay recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        var yOffset = getYOffset(recipe);

        if (recipe.isSupportsAccelerators() && mouseY >= yOffset + 18 && mouseY <= yOffset + 31) {
            List<Component> tooltipLines = new ArrayList<>();
            tooltipLines.add(ItemModText.WITH_CRYSTAL_GROWTH_ACCELERATORS.text());
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
        return ItemModText.THROWING_IN_WATER_CATEGORY.text();
    }

    @Override
    public ResourceLocation getUid() {
        return getRecipeType().getUid();
    }

    @Override
    public Class<? extends ThrowingInWaterDisplay> getRecipeClass() {
        return getRecipeType().getRecipeClass();
    }
}
