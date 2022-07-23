package appeng.integration.modules.jei;

import com.google.common.base.Strings;

import mezz.jei.api.runtime.IJeiRuntime;

import appeng.integration.abstraction.IJEI;

class JeiRuntimeAdapter implements IJEI {

    private final IJeiRuntime runtime;

    JeiRuntimeAdapter(IJeiRuntime jeiRuntime) {
        this.runtime = jeiRuntime;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public IJeiRuntime getRuntime() {
        return runtime;
    }

    @Override
    public String getSearchText() {
        return Strings.nullToEmpty(this.runtime.getIngredientFilter().getFilterText());
    }
}
