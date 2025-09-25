package appeng.integration.modules.rei;

import java.util.Objects;

import com.google.common.base.Strings;

import me.shedaniel.rei.api.client.REIRuntime;

import appeng.integration.abstraction.ItemListModAdapter;

class ReiItemListModAdapter implements ItemListModAdapter {

    private final REIRuntime runtime;

    ReiItemListModAdapter() {
        this.runtime = Objects.requireNonNull(REIRuntime.getInstance(), "REI helper was null");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getShortName() {
        return "REI";
    }

    @Override
    public String getSearchText() {
        var searchField = this.runtime.getSearchTextField();
        if (searchField == null) {
            return "";
        }
        return Strings.nullToEmpty(searchField.getText());
    }

    @Override
    public void setSearchText(String text) {
        var searchField = this.runtime.getSearchTextField();
        if (searchField != null) {
            searchField.setText(text);
        }
    }

    @Override
    public boolean hasSearchFocus() {
        var searchField = this.runtime.getSearchTextField();
        return searchField != null && searchField.isFocused();
    }
}
