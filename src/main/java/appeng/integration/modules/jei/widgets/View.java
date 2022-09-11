package appeng.integration.modules.jei.widgets;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;

public class View<T> {
    protected final T recipe;
    protected final List<Widget> widgets = new ArrayList<>();

    public View(T recipe) {
        this.recipe = recipe;
    }

    public void createWidgets(WidgetFactory factory) {
    }

    public void buildSlots(IRecipeLayoutBuilder builder) {
    }

    public void draw(PoseStack stack, IRecipeSlotsView slots, double mouseX, double mouseY) {
        for (var widget : widgets) {
            widget.draw(stack);
        }
    }

    public List<Component> getTooltipStrings(double mouseX, double mouseY) {
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
}
