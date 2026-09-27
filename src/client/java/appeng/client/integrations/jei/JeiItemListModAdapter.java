package appeng.client.integrations.jei;

import com.google.common.base.Strings;

import mezz.jei.api.runtime.IJeiRuntime;

import appeng.integration.abstraction.ItemListModAdapter;

class JeiItemListModAdapter implements ItemListModAdapter {

    private final IJeiRuntime runtime;

    JeiItemListModAdapter(IJeiRuntime jeiRuntime) {
        this.runtime = jeiRuntime;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getShortName() {
        return "JEI";
    }

    @Override
    public String getSearchText() {
        return Strings.nullToEmpty(this.runtime.getIngredientFilter().getFilterText());
    }

    @Override
    public void setSearchText(String text) {
        this.runtime.getIngredientFilter().setFilterText(text);
    }

    @Override
    public boolean hasSearchFocus() {
        return this.runtime.getIngredientListOverlay().hasKeyboardFocus();
    }
}
