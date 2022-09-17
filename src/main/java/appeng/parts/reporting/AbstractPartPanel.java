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

package appeng.parts.reporting;


import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;


/**
 * A very simple part for emitting light.
 * <p>
 * Opposed to the other subclass of {@link AbstractPartReporting}, it will only use the bright front texture.
 *
 * @author AlgorithmX2
 * @author yueh
 * @version rv3
 * @since rv3
 */
public abstract class AbstractPartPanel extends AbstractPartReporting {

    @PartModels
    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/monitor_base");

    public AbstractPartPanel(final ItemStack is) {
        super(is, false);
    }

    @Override
    public boolean isLightSource() {
        return true;
    }

    /**
     * How bright the color the panel should appear. Usually it depends on a {@link AEColor} variant.
     * This does not affect the actual light level of the part.
     *
     * @return the brightness to be used.
     */
    protected abstract int getBrightnessColor();

}
