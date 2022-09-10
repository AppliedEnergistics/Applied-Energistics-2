package appeng.integration.modules.jei;

import net.minecraft.network.chat.Component;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;

import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.integration.modules.jei.widgets.View;
import appeng.integration.modules.jei.widgets.WidgetFactory;
import appeng.recipes.handlers.ChargerRecipe;

public class ChargerCategory extends ViewBasedCategory<ChargerRecipe> {

    public static RecipeType<ChargerRecipe> RECIPE_TYPE = RecipeType.create(AppEng.MOD_ID, "charger",
            ChargerRecipe.class);
    private final IDrawableStatic background;
    private final IDrawable icon;
    private final IDrawable slotBackground;

    public ChargerCategory(IJeiHelpers helpers) {
        super(helpers);
        var guiHelper = helpers.getGuiHelper();
        this.background = guiHelper.createBlankDrawable(130, 50);
        this.icon = guiHelper.createDrawableItemStack(AEBlocks.CHARGER.stack());
        this.slotBackground = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<ChargerRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return AEBlocks.CHARGER.stack().getHoverName();
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
    protected View<ChargerRecipe> getView(ChargerRecipe recipe) {
        return new View<>(recipe) {
            @Override
            public void buildSlots(IRecipeLayoutBuilder builder) {
                builder.addSlot(RecipeIngredientRole.INPUT, 31, 8)
                        .setBackground(slotBackground, -1, -1)
                        .addIngredients(recipe.getIngredient());

                builder.addSlot(RecipeIngredientRole.OUTPUT, 81, 8)
                        .setBackground(slotBackground, -1, -1)
                        .addItemStack(recipe.getResultItem());

                builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 3, 30)
                        .addItemStack(AEBlocks.CRANK.stack());
            }

            @Override
            public void createWidgets(WidgetFactory factory) {
                widgets.add(factory.unfilledArrow(52, 8));

                var turns = (ChargerBlockEntity.POWER_MAXIMUM_AMOUNT + ChargerBlockEntity.POWER_PER_CRANK_TURN - 1)
                        / ChargerBlockEntity.POWER_PER_CRANK_TURN;
                widgets.add(factory
                        .label(20, 35,
                                Component.literal(
                                        turns + " turns or " + ChargerBlockEntity.POWER_MAXIMUM_AMOUNT + " AE"))
                        .bodyColor()
                        .noShadow()
                        .alignLeft());
            }
        };
    }
}
