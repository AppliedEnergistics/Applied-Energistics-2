package appeng.api.storage;

import appeng.api.storage.data.AEKey;

class NoOpKeyFilter implements AEKeyFilter {
    static NoOpKeyFilter INSTANCE = new NoOpKeyFilter();

    @Override
    public boolean matches(AEKey what) {
        return true;
    }

    @Override
    public boolean isEnumerable() {
        return true;
    }
}
