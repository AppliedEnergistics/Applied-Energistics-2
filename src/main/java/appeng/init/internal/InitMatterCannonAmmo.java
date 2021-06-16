package appeng.init.internal;

import appeng.api.features.IRegistryContainer;
import appeng.core.Api;
import appeng.core.api.definitions.ApiItems;

public final class InitMatterCannonAmmo {

    private InitMatterCannonAmmo() {
    }

    public static void init() {
        final IRegistryContainer registries = Api.INSTANCE.registries();
        registries.matterCannon().registerAmmoItem(ApiItems.MATTER_BALL.item(), 32);
    }

}
