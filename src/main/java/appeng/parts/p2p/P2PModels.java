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

package appeng.parts.p2p;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.Identifier;

import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.parts.PartModel;

/**
 * Helper for maintaining the models used for a variant of the P2P bus.
 */
class P2PModels {

    public static final Identifier MODEL_STATUS_OFF = new Identifier(AppEng.MOD_ID, "part/p2p/p2p_tunnel_status_off");
    public static final Identifier MODEL_STATUS_ON = new Identifier(AppEng.MOD_ID, "part/p2p/p2p_tunnel_status_on");
    public static final Identifier MODEL_STATUS_HAS_CHANNEL = new Identifier(AppEng.MOD_ID,
            "part/p2p/p2p_tunnel_status_has_channel");
    public static final Identifier MODEL_FREQUENCY = new Identifier(AppEng.MOD_ID, "part/p2p/p2p_tunnel_frequency");

    private final IPartModel modelsOff;
    private final IPartModel modelsOn;
    private final IPartModel modelsHasChannel;

    public P2PModels(String frontModelPath) {
        Identifier frontModel = new Identifier(AppEng.MOD_ID, frontModelPath);

        this.modelsOff = new PartModel(MODEL_STATUS_OFF, MODEL_FREQUENCY, frontModel);
        this.modelsOn = new PartModel(MODEL_STATUS_ON, MODEL_FREQUENCY, frontModel);
        this.modelsHasChannel = new PartModel(MODEL_STATUS_HAS_CHANNEL, MODEL_FREQUENCY, frontModel);
    }

    public IPartModel getModel(boolean hasPower, boolean hasChannel) {
        if (hasPower && hasChannel) {
            return this.modelsHasChannel;
        } else if (hasPower) {
            return this.modelsOn;
        } else {
            return this.modelsOff;
        }
    }

    public List<IPartModel> getModels() {
        List<IPartModel> result = new ArrayList<>();
        result.add(this.modelsOff);
        result.add(this.modelsOn);
        result.add(this.modelsHasChannel);
        return result;
    }

}
