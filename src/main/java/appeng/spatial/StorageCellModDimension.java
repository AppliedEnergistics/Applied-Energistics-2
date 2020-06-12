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

package appeng.spatial;


import appeng.client.render.SpatialSkyRender;
import appeng.core.AppEng;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.ModDimension;

import java.util.function.BiFunction;

public class StorageCellModDimension extends ModDimension
{

	public static final StorageCellModDimension INSTANCE = new StorageCellModDimension();

	static {
		INSTANCE.setRegistryName(AppEng.MOD_ID, "storage_cell");
	}

	@Override
	public BiFunction<World, DimensionType, ? extends Dimension> getFactory() {
		return StorageCellDimension::new;
	}

	@Override
	public void write(PacketBuffer buffer, boolean network) {
		super.write(buffer, network);
	}

	@Override
	public void read(PacketBuffer buffer, boolean network) {
		super.read(buffer, network);
	}

}
