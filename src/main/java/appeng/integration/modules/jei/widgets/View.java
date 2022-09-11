package appeng.integration.modules.jei.widgets;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;

public interface View {
    default void createWidgets(WidgetFactory factory, List<Widget> widgets) {
    }

    default void buildSlots(IRecipeLayoutBuilder builder) {
    }

    default void draw(PoseStack stack, IRecipeSlotsView slots, double mouseX, double mouseY) {
    }

    default List<Component> getTooltipStrings(double mouseX, double mouseY) {
        return List.of();
    }
}
