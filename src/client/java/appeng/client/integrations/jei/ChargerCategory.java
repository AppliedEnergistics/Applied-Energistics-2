package appeng.client.integrations.jei;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;

import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.misc.CrankBlockEntity;
import appeng.client.integrations.jei.widgets.View;
import appeng.client.integrations.jei.widgets.Widget;
import appeng.client.integrations.jei.widgets.WidgetFactory;
import appeng.core.definitions.AEBlocks;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.ChargerRecipe;

public class ChargerCategory extends ViewBasedCategory<RecipeHolder<ChargerRecipe>> {

    public static IRecipeType<RecipeHolder<ChargerRecipe>> RECIPE_TYPE = IRecipeType.create(AERecipeTypes.CHARGER);
    private final IDrawable icon;
    private final IDrawable slotBackground;

    public ChargerCategory(IJeiHelpers helpers) {
        super(helpers);
        var guiHelper = helpers.getGuiHelper();
        this.icon = guiHelper.createDrawableItemStack(AEBlocks.CHARGER.stack());
        this.slotBackground = guiHelper.getSlotDrawable();
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
    public IRecipeType<RecipeHolder<ChargerRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return AEBlocks.CHARGER.stack().getHoverName();
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    protected View getView(RecipeHolder<ChargerRecipe> holder) {
        var recipe = holder.value();

        return new View() {
            @Override
            public void buildSlots(IRecipeLayoutBuilder builder) {
                builder.addSlot(RecipeIngredientRole.INPUT, 31, 8)
                        .setBackground(slotBackground, -1, -1)
                        .add(recipe.ingredient());

                builder.addSlot(RecipeIngredientRole.OUTPUT, 81, 8)
                        .setBackground(slotBackground, -1, -1)
                        .add(recipe.result().create());

                builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 3, 30)
                        .add(AEBlocks.CRANK.stack());
            }

            @Override
            public void createWidgets(WidgetFactory factory, List<Widget> widgets) {
                widgets.add(factory.unfilledArrow(52, 8));

                var turns = (ChargerBlockEntity.POWER_MAXIMUM_AMOUNT + CrankBlockEntity.POWER_PER_CRANK_TURN - 1)
                        / CrankBlockEntity.POWER_PER_CRANK_TURN;
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
