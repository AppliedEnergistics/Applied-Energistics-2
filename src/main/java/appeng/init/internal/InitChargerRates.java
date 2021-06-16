package appeng.init.internal;

import appeng.api.features.IChargerRegistry;
import appeng.core.Api;
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.api.definitions.ApiItems;

public class InitChargerRates {

    public static void init() {
        // Charge Rates
        IChargerRegistry charger = Api.instance().registries().charger();
        charger.addChargeRate(ApiItems.CHARGED_STAFF, 320d);
        charger.addChargeRate(ApiItems.PORTABLE_CELL1K, 800d);
        charger.addChargeRate(ApiItems.PORTABLE_CELL4k, 800d);
        charger.addChargeRate(ApiItems.PORTABLE_CELL16K, 800d);
        charger.addChargeRate(ApiItems.PORTABLE_CELL64K, 800d);
        charger.addChargeRate(ApiItems.COLOR_APPLICATOR, 800d);
        charger.addChargeRate(ApiItems.WIRELESS_TERMINAL, 8000d);
        charger.addChargeRate(ApiItems.ENTROPY_MANIPULATOR, 8000d);
        charger.addChargeRate(ApiItems.MASS_CANNON, 8000d);
        charger.addChargeRate(ApiBlocks.ENERGY_CELL, 8000d);
        charger.addChargeRate(ApiBlocks.DENSE_ENERGY_CELL, 16000d);
    }

}
