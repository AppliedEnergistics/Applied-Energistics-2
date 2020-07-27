package appeng.integration.modules.jei;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingCategory;

import appeng.core.AppEng;
import appeng.core.FacadeCreativeTab;

/**
 * A simple copy of the crafting category to separate out the facade recipes.
 */
public class FacadeRecipeCategory extends DefaultCraftingCategory {

    public static final Identifier ID = AppEng.makeId("facades");

    @Override
    public Identifier getIdentifier() {
        return ID;
    }

    @Override
    public EntryStack getLogo() {
        return EntryStack.create(FacadeCreativeTab.getGroup().getIcon());
    }

    @Override
    public String getCategoryName() {
        return I18n.translate(FacadeCreativeTab.getGroup().getTranslationKey());
    }

}
