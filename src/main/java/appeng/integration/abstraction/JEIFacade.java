package appeng.integration.abstraction;

import com.google.common.base.Preconditions;

public final class JEIFacade {

    private JEIFacade() {
    }

    private static IJEI instance = new IJEI.Stub();

    public static IJEI instance() {
        return instance;
    }

    public static void setInstance(IJEI jei) {
        instance = Preconditions.checkNotNull(jei);
    }

}
