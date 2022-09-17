/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.core.stats;


import appeng.bootstrap.ICriterionTriggerRegistry;


public class AdvancementTriggers {
    private final AppEngAdvancementTrigger networkApprentice = new AppEngAdvancementTrigger("network_apprentice");
    private final AppEngAdvancementTrigger networkEngineer = new AppEngAdvancementTrigger("network_engineer");
    private final AppEngAdvancementTrigger networkAdmin = new AppEngAdvancementTrigger("network_admin");
    private final AppEngAdvancementTrigger spatialExplorer = new AppEngAdvancementTrigger("spatial_explorer");

    public AdvancementTriggers(ICriterionTriggerRegistry registry) {
        registry.register(this.networkApprentice);
        registry.register(this.networkEngineer);
        registry.register(this.networkAdmin);
        registry.register(this.spatialExplorer);
    }

    public IAdvancementTrigger getNetworkApprentice() {
        return this.networkApprentice;
    }

    public IAdvancementTrigger getNetworkEngineer() {
        return this.networkEngineer;
    }

    public IAdvancementTrigger getNetworkAdmin() {
        return this.networkAdmin;
    }

    public IAdvancementTrigger getSpatialExplorer() {
        return this.spatialExplorer;
    }
}
