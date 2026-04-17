package appeng.client.integrations.jei;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.InscriberRecipe;

class InscriberRecipeCategory implements IRecipeCategory<RecipeHolder<InscriberRecipe>> {

    private static final String TITLE_TRANSLATION_KEY = "block.ae2.inscriber";

    public static final IRecipeType<RecipeHolder<InscriberRecipe>> RECIPE_TYPE = IRecipeType
            .create(AERecipeTypes.INSCRIBER);

    private final IDrawable background;
    private final IDrawable progress;
    private final IDrawableAnimated progressOverlay;
    private final IDrawable icon;

    public InscriberRecipeCategory(IGuiHelper guiHelper) {
        Identifier location = AppEng.makeId("textures/guis/jei.png");
        this.background = guiHelper.createDrawable(location, 24, 0, 48, 64);

        this.progress = guiHelper.createDrawable(location, 72, 0, 6, 18);
        var progressDrawable = guiHelper.drawableBuilder(location, 78, 0, 6, 18).build();
        this.progressOverlay = guiHelper.createAnimatedDrawable(progressDrawable, 40,
                IDrawableAnimated.StartDirection.BOTTOM,
                false);

        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, AEBlocks.INSCRIBER.stack());
    }

    @Override
    public IRecipeType<RecipeHolder<InscriberRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(TITLE_TRANSLATION_KEY);
    }

    @Override
    public int getWidth() {
        return 97;
    }

    @Override
    public int getHeight() {
        return 64;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<InscriberRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();
        var top = builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .setStandardSlotBackground()
                .setSlotName("top");
        recipe.getTopOptional().ifPresent(top::add);
        builder.addSlot(RecipeIngredientRole.INPUT, 22, 24)
                .setSlotName("middle")
                .setStandardSlotBackground()
                .add(recipe.getMiddleInput());
        var bottom = builder.addSlot(RecipeIngredientRole.INPUT, 1, 47)
                .setStandardSlotBackground()
                .setSlotName("bottom");
        recipe.getBottomOptional().ifPresent(bottom::add);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 69, 24)
                .setSlotName("output")
                .setOutputSlotBackground()
                .add(recipe.result().create());
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(RecipeHolder<InscriberRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics,
            double mouseX,
            double mouseY) {
        this.background.draw(guiGraphics, 16, 0);
        this.progress.draw(guiGraphics, 91, 23);
        this.progressOverlay.draw(guiGraphics, 91, 23);
    }
}
