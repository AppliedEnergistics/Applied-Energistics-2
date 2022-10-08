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

package appeng.parts.misc;

import net.minecraft.resources.ResourceLocation;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;

public class InvertedToggleBusPart extends ToggleBusPart {
    @PartModels
    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID,
            "part/inverted_toggle_bus_base");

    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_STATUS_OFF);
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_STATUS_ON);
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_STATUS_HAS_CHANNEL);

    public InvertedToggleBusPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().setIdlePowerUsage(0.0);
        this.getOuterNode().setIdlePowerUsage(0.0);
        this.getMainNode().setFlags();
        this.getOuterNode().setFlags();
    }

    @Override
    protected boolean isEnabled() {
        return !super.isEnabled();
    }

    @Override
    public IPartModel getStaticModels() {
        if (isEnabled() && this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (isEnabled() && this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

}
