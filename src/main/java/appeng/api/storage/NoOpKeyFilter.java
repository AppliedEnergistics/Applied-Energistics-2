package appeng.api.storage;

import appeng.api.stacks.AEKey;

class NoOpKeyFilter implements AEKeyFilter {
    static NoOpKeyFilter INSTANCE = new NoOpKeyFilter();

    @Override
    public boolean matches(AEKey what) {
        return true;
    }

}
