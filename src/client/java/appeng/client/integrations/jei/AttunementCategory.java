package appeng.client.integrations.jei;

import java.util.Arrays;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;

import appeng.client.integrations.jei.widgets.View;
import appeng.client.integrations.jei.widgets.Widget;
import appeng.client.integrations.jei.widgets.WidgetFactory;
import appeng.core.AppEng;
import appeng.core.definitions.AEParts;
import appeng.core.localization.ItemModText;

public class AttunementCategory extends ViewBasedCategory<AttunementDisplay> {

    public static final IRecipeType<AttunementDisplay> TYPE = IRecipeType.create(AppEng.MOD_ID, "attunement",
            AttunementDisplay.class);

    private final IDrawable icon;
    private final IDrawable slotBackground;

    public AttunementCategory(IJeiHelpers helpers) {
        super(helpers);
        var guiHelpers = helpers.getGuiHelper();
        this.icon = guiHelpers.createDrawableItemStack(AEParts.ME_P2P_TUNNEL.stack());
        this.slotBackground = guiHelpers.getSlotDrawable();
    }

    @Override
    public int getWidth() {
        return 130;
    }

    @Override
    public int getHeight() {
        return 36;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public Component getTitle() {
        return ItemModText.P2P_TUNNEL_ATTUNEMENT.text();
    }

    @Override
    public IRecipeType<AttunementDisplay> getRecipeType() {
        return TYPE;
    }

    @Override
    protected View getView(AttunementDisplay recipe) {
        var x = getWidth() / 2 - 41;
        var y = getHeight() / 2 - 13;

        return new View() {
            @Override
            public void buildSlots(IRecipeLayoutBuilder builder) {
                builder.addSlot(RecipeIngredientRole.INPUT, x + 4, y + 5)
                        .setBackground(slotBackground, -1, -1)
                        .add(recipe.inputs())
                        .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                            tooltip.addAll(Arrays.asList(recipe.description()));
                        });
                builder.addSlot(RecipeIngredientRole.OUTPUT, x + 61, y + 5)
                        .setBackground(slotBackground, -1, -1)
                        .add(new ItemStack(recipe.tunnel()));
            }

            @Override
            public void createWidgets(WidgetFactory factory, List<Widget> widgets) {
                widgets.add(factory.unfilledArrow(x + 27, y + 4));
            }
        };
    }
}
