package appeng.integration.modules.emi;

import appeng.core.AppEng;
import appeng.core.localization.LocalizationEnum;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.network.chat.Component;

class AppEngRecipeCategory extends EmiRecipeCategory {
    private final Component name;

    public AppEngRecipeCategory(String id, EmiRenderable icon, LocalizationEnum name) {
        super(AppEng.makeId(id), icon);
        this.name = name.text();
    }

    @Override
    public Component getName() {
        return name;
    }
}
