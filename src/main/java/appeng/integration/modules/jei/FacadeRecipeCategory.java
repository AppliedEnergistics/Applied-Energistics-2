package appeng.integration.modules.jei;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingCategory;
import net.minecraft.util.ResourceLocation;
import appeng.core.AppEng;
import appeng.core.FacadeCreativeTab;

/**
 * A simple copy of the crafting category to separate out the facade recipes.
 */
public class FacadeRecipeCategory extends DefaultCraftingCategory {

    public static final ResourceLocation ID = AppEng.makeId("facades");

    @Override
    public ResourceLocation getIdentifier() {
        return ID;
    }

    @Override
    public EntryStack getLogo() {
        return EntryStack.create(FacadeCreativeTab.getGroup().getIcon());
    }

    @Override
    public String getCategoryName() {
        return FacadeCreativeTab.getGroup().getGroupName().getString();
    }

}
