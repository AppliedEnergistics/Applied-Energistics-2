package appeng.integration.modules.emi;

import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.misc.CrankBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.localization.ItemModText;
import appeng.recipes.handlers.ChargerRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;

class EmiChargerRecipe extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new AppEngRecipeCategory("charger", EmiStack.of(AEBlocks.CHARGER), EmiText.CATEGORY_CHARGER);
    private final ChargerRecipe recipe;

    public EmiChargerRecipe(RecipeHolder<ChargerRecipe> holder) {
        super(CATEGORY, holder.id(), 130, 50);
        recipe = holder.value();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var widgets = new ArrayList<Widget>();

         widgets.add(
         Widgets.createSlot(new Point(x + 31, y + 8))
         .markInput()
         .backgroundEnabled(true)
         .entries(EntryIngredients.ofIngredient(display.recipe().getIngredient())));
         widgets.add(
         Widgets.createSlot(new Point(x + 81, y + 8))
         .markOutput()
         .backgroundEnabled(true)
         .entry(EntryStacks.of(display.recipe().getResultItem())));

         widgets.add(
         Widgets.createSlot(new Point(x + 3, y + 30))
         .unmarkInputOrOutput()
         .backgroundEnabled(false)
         .entry(EntryStacks.of(AEBlocks.CRANK.stack())));

        widgets.add(Widgets.createArrow(new Point(x + 52, y + 8)));

         var turns = (ChargerBlockEntity.POWER_MAXIMUM_AMOUNT + CrankBlockEntity.POWER_PER_CRANK_TURN - 1)
         / CrankBlockEntity.POWER_PER_CRANK_TURN;
         widgets.add(Widgets
         .createLabel(new Point(x + 20, y + 35),
         ItemModText.CHARGER_REQUIRED_POWER.text(turns, ChargerBlockEntity.POWER_MAXIMUM_AMOUNT))
         .color(0x7E7E7E)
         .noShadow()
         .leftAligned());

    }
}
