/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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


import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Collection;


public enum IntegrationRegistry {
    INSTANCE;

    private final Collection<IntegrationNode> modules = new ArrayList<>();

    public void add(final IntegrationType type) {
        if (type.side == IntegrationSide.CLIENT && FMLLaunchHandler.side() == Side.SERVER) {
            return;
        }

        if (type.side == IntegrationSide.SERVER && FMLLaunchHandler.side() == Side.CLIENT) {
            return;
        }

        this.modules.add(new IntegrationNode(type.dspName, type.modID, type));
    }

    public void preInit() {
        for (final IntegrationNode node : this.modules) {
            node.call(IntegrationStage.PRE_INIT);
        }
    }

    public void init() {
        for (final IntegrationNode node : this.modules) {
            node.call(IntegrationStage.INIT);
        }
    }

    public void postInit() {
        for (final IntegrationNode node : this.modules) {
            node.call(IntegrationStage.POST_INIT);
        }
    }

    public String getStatus() {
        final StringBuilder builder = new StringBuilder(this.modules.size() * 3);

        for (final IntegrationNode node : this.modules) {
            if (builder.length() != 0) {
                builder.append(", ");
            }

            final String integrationState = node.getType() + ":" + (node.getState() == IntegrationStage.FAILED ? "OFF" : "ON");
            builder.append(integrationState);
        }

        return builder.toString();
    }

    public boolean isEnabled(final IntegrationType name) {
        for (final IntegrationNode node : this.modules) {
            if (node.getType() == name) {
                return node.isActive();
            }
        }
        return false;
    }

}
