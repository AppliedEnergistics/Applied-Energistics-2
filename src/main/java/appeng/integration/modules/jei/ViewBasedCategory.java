package appeng.integration.modules.jei;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;

import appeng.integration.modules.jei.widgets.View;
import appeng.integration.modules.jei.widgets.WidgetFactory;

public abstract class ViewBasedCategory<T> implements IRecipeCategory<T> {
    private final WidgetFactory widgetFactory;

    protected ViewBasedCategory(IJeiHelpers helpers) {
        this.widgetFactory = new WidgetFactory(helpers);
    }

    protected abstract View<T> getView(T recipe);

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        buildView(recipe).buildSlots(builder);
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        buildView(recipe).draw(stack, recipeSlotsView, mouseX, mouseY);
    }

    @Override
    public List<Component> getTooltipStrings(T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        return buildView(recipe).getTooltipStrings(mouseX, mouseY);
    }

    private View<T> buildView(T recipe) {
        var view = getView(recipe);
        view.createWidgets(widgetFactory);
        return view;
    }
}
