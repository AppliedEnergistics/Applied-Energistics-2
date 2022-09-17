/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.integration;


import appeng.api.exceptions.ModNotInstalledException;
import appeng.core.AEConfig;
import appeng.core.AELog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModAPIManager;


final class IntegrationNode {

    private final String displayName;
    private final String modID;
    private final IntegrationType type;
    private IntegrationStage state = IntegrationStage.PRE_INIT;
    private Throwable exception = null;
    private IIntegrationModule mod = null;

    IntegrationNode(final String displayName, final String modID, final IntegrationType type) {
        this.displayName = displayName;
        this.type = type;
        this.modID = modID;
    }

    @Override
    public String toString() {
        return this.getType().name() + ':' + this.getState().name();
    }

    boolean isActive() {
        if (this.getState() == IntegrationStage.PRE_INIT) {
            this.call(IntegrationStage.PRE_INIT);
        }

        return this.getState() != IntegrationStage.FAILED;
    }

    void call(final IntegrationStage stage) {
        if (this.getState() != IntegrationStage.FAILED) {
            if (this.getState().ordinal() > stage.ordinal()) {
                return;
            }

            try {
                switch (stage) {
                    case PRE_INIT:
                        final ModAPIManager apiManager = ModAPIManager.INSTANCE;
                        boolean enabled = this.modID == null || Loader.isModLoaded(this.modID) || apiManager.hasAPI(this.modID);

                        AEConfig.instance()
                                .addCustomCategoryComment("ModIntegration",
                                        "Valid Values are 'AUTO', 'ON', or 'OFF' - defaults to 'AUTO' ; Suggested that you leave this alone unless your experiencing an issue, or wish to disable the integration for a reason.");
                        final String mode = AEConfig.instance().get("ModIntegration", this.displayName.replace(" ", ""), "AUTO").getString();

                        if (mode.equalsIgnoreCase("ON")) {
                            enabled = true;
                        }
                        if (mode.equalsIgnoreCase("OFF")) {
                            enabled = false;
                        }

                        if (enabled) {
                            this.mod = this.type.createInstance();
                        } else {
                            throw new ModNotInstalledException(this.modID);
                        }

                        this.mod.preInit();
                        this.setState(IntegrationStage.INIT);

                        break;
                    case INIT:
                        this.mod.init();
                        this.setState(IntegrationStage.POST_INIT);

                        break;
                    case POST_INIT:
                        this.mod.postInit();
                        this.setState(IntegrationStage.READY);

                        break;
                    case FAILED:
                    default:
                        break;
                }
            } catch (final Throwable t) {
                this.exception = t;
                this.setState(IntegrationStage.FAILED);
            }
        }

        if (stage == IntegrationStage.POST_INIT) {
            if (this.getState() == IntegrationStage.FAILED) {
                AELog.info(this.displayName + " - Integration Disabled");
                if (!(this.exception instanceof ModNotInstalledException)) {
                    AELog.integration(this.exception);
                }
            } else {
                AELog.info(this.displayName + " - Integration Enable");
            }
        }
    }

    IntegrationType getType() {
        return this.type;
    }

    IntegrationStage getState() {
        return this.state;
    }

    private void setState(final IntegrationStage state) {
        this.state = state;
    }
}
