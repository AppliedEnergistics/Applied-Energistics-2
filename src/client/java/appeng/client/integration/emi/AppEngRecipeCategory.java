package appeng.client.integration.emi;

import net.minecraft.network.chat.Component;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;

import appeng.core.AppEng;
import appeng.core.localization.LocalizationEnum;

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
