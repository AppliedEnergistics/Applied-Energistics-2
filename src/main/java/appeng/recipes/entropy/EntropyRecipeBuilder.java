/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.recipes.entropy;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class EntropyRecipeBuilder {
    private ResourceLocation id;
    private EntropyMode mode;

    private Block inputBlock;
    private CompoundNBT inputBlockProperties;
    private List<StateMatcher> inputBlockMatchers;

    private Fluid inputFluid;
    private CompoundNBT inputFluidProperties;
    private List<StateMatcher> inputFluidMatchers;
    private Block outputBlock;
    private CompoundNBT outputBlockProperties;
    private boolean outputBlockKeep;
    private Fluid outputFluid;
    private CompoundNBT outputFluidProperties;
    private boolean outputFluidKeep;

    public EntropyRecipeBuilder() {
    }

    EntropyRecipeBuilder setId(ResourceLocation id) {
        this.id = id;
        return this;
    }

    public EntropyRecipeBuilder setMode(EntropyMode mode) {
        this.mode = mode;
        return this;
    }

}
