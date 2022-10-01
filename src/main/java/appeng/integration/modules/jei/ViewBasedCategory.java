package appeng.integration.modules.jei;

import java.util.ArrayList;
import java.util.List;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;

import appeng.client.gui.Icon;
import appeng.integration.modules.jei.widgets.View;
import appeng.integration.modules.jei.widgets.Widget;
import appeng.integration.modules.jei.widgets.WidgetFactory;
import appeng.util.Platform;

public abstract class ViewBasedCategory<T> implements IRecipeCategory<T> {
    private final WidgetFactory widgetFactory;

    private final LoadingCache<T, CachedView> cache;

    protected final IGuiHelper guiHelper;

    protected final IJeiHelpers jeiHelpers;

    protected ViewBasedCategory(IJeiHelpers helpers) {
        this.jeiHelpers = helpers;
        this.guiHelper = helpers.getGuiHelper();
        widgetFactory = new WidgetFactory(helpers);
        cache = CacheBuilder.newBuilder()
                .maximumSize(10)
                .build(new CacheLoader<>() {
                    @Override
                    public CachedView load(T recipe) {
                        return new CachedView(getView(recipe));
                    }
                });
    }

    protected abstract View getView(T recipe);

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        cache.getUnchecked(recipe).view.buildSlots(builder);
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        var cachedView = cache.getUnchecked(recipe);
        for (var widget : getWidgets(cachedView)) {
            widget.draw(stack);
        }
        cachedView.view.draw(stack, recipeSlotsView, mouseX, mouseY);
    }

    @Override
    public List<Component> getTooltipStrings(T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        var cachedView = cache.getUnchecked(recipe);

        var tooltipLines = cachedView.view.getTooltipStrings(mouseX, mouseY);
        if (!tooltipLines.isEmpty()) {
            return tooltipLines;
        }

        var widgets = getWidgets(cachedView);
        for (int i = widgets.size() - 1; i >= 0; i--) {
            var widget = widgets.get(i);
            if (widget.hitTest(mouseX, mouseY)) {
                var lines = widget.getTooltipLines();
                if (!lines.isEmpty()) {
                    return lines;
                }
            }
        }

        return List.of();
    }

    protected final IDrawable getIconDrawable(Icon icon) {
        return guiHelper.drawableBuilder(Icon.TEXTURE, icon.x, icon.y, icon.width, icon.height)
                .setTextureSize(Icon.TEXTURE_WIDTH, Icon.TEXTURE_HEIGHT)
                .build();
    }

    private List<Widget> getWidgets(CachedView cachedView) {
        // Always re-create the widgets in a dev-env for faster prototyping.
        // otherwise cache them.
        if (Platform.isDevelopmentEnvironment() || cachedView.widgets == null) {
            cachedView.widgets = new ArrayList<>();
            cachedView.view.createWidgets(widgetFactory, cachedView.widgets);
        }
        return cachedView.widgets;
    }

    private static class CachedView {
        private final View view;
        public List<Widget> widgets;

        public CachedView(View view) {
            this.view = view;
        }
    }
}
