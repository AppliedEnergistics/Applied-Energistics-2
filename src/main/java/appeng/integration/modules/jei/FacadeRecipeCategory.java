package appeng.integration.modules.jei;

import net.minecraft.network.chat.Component;

import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.client.categories.crafting.DefaultCraftingCategory;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;

import appeng.core.AppEng;
import appeng.core.FacadeCreativeTab;

/**
 * A simple copy of the crafting category to separate out the facade recipes.
 */
public class FacadeRecipeCategory extends DefaultCraftingCategory {

    public static final CategoryIdentifier<? extends DefaultCraftingDisplay<?>> ID = CategoryIdentifier
            .of(AppEng.makeId("facades"));

    @Override
    public CategoryIdentifier<? extends DefaultCraftingDisplay<?>> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(FacadeCreativeTab.getGroup().getIconItem());
    }

    @Override
    public Component getTitle() {
        return FacadeCreativeTab.getGroup().getDisplayName();
    }

}
