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

package appeng.parts.automation;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.ResourceLocation;

import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.parts.PartModel;

/**
 * Contains a mapping from a Plane's connections to the models to use for that state.
 */
public class PlaneModels {

    public static final ResourceLocation MODEL_CHASSIS_OFF = new ResourceLocation(AppEng.MOD_ID,
            "part/transition_plane_off");
    public static final ResourceLocation MODEL_CHASSIS_ON = new ResourceLocation(AppEng.MOD_ID,
            "part/transition_plane_on");
    public static final ResourceLocation MODEL_CHASSIS_HAS_CHANNEL = new ResourceLocation(AppEng.MOD_ID,
            "part/transition_plane_has_channel");

    private final IPartModel modelOff;

    private final IPartModel modelOn;

    private final IPartModel modelHasChannel;

    public PlaneModels(String planeOffLocation, String planeOnLocation) {
        ResourceLocation planeOff = new ResourceLocation(AppEng.MOD_ID, planeOffLocation);
        ResourceLocation planeOn = new ResourceLocation(AppEng.MOD_ID, planeOnLocation);

        this.modelOff = new PartModel(MODEL_CHASSIS_OFF, planeOff);
        this.modelOn = new PartModel(MODEL_CHASSIS_ON, planeOff);
        this.modelHasChannel = new PartModel(MODEL_CHASSIS_HAS_CHANNEL, planeOn);
    }

    public IPartModel getModel(boolean hasPower, boolean hasChannel) {
        if (hasPower && hasChannel) {
            return modelHasChannel;
        } else if (hasPower) {
            return modelOn;
        } else {
            return modelOff;
        }
    }

    public List<IPartModel> getModels() {
        return ImmutableList.of(modelOff, modelOn, modelHasChannel);
    }

}
