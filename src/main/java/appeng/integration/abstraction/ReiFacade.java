package appeng.integration.abstraction;

import com.google.common.base.Preconditions;

public final class ReiFacade {

    private ReiFacade() {
    }

    private static IRei instance = new IRei.Stub();

    public static IRei instance() {
        return instance;
    }

    public static void setInstance(IRei jei) {
        instance = Preconditions.checkNotNull(jei);
    }

}
