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

package appeng.client.render.model;


import appeng.block.storage.DriveSlotState;
import appeng.block.storage.DriveSlotsState;
import appeng.client.render.DelegateBakedModel;
import appeng.tile.storage.TileDrive;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class DriveBakedModel extends DelegateBakedModel
{
	private final IBakedModel bakedBase;
	private final Map<DriveSlotState, IBakedModel> bakedCells;

	public DriveBakedModel( IBakedModel bakedBase, Map<DriveSlotState, IBakedModel> bakedCells )
	{
		super(bakedBase);
		this.bakedBase = bakedBase;
		this.bakedCells = bakedCells;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {

		List<BakedQuad> result = new ArrayList<>(this.bakedBase.getQuads(state, side, rand, extraData));

		DriveSlotsState slotsState = extraData.getData( TileDrive.SLOTS_STATE );

		if( side == null && slotsState != null )
		{
			for( int row = 0; row < 5; row++ )
			{
				for( int col = 0; col < 2; col++ )
				{
					DriveSlotState slotState = slotsState.getState( row * 2 + col );

					IBakedModel bakedCell = this.bakedCells.get( slotState );

					Matrix4f transform = new Matrix4f();
					transform.setIdentity();

					// Position this drive model copy at the correct slot. The transform is based on the
					// cell-model being in slot 0,0 at the top left of the drive.
					float xOffset = -col * 7 / 16.0f;
					float yOffset = -row * 3 / 16.0f;

					transform.setTranslation( xOffset, yOffset, 0 );

					MatrixVertexTransformer transformer = new MatrixVertexTransformer( transform );
					for( BakedQuad bakedQuad : bakedCell.getQuads( state, null, rand, extraData ) )
					{
						BakedQuadBuilder builder = new BakedQuadBuilder();
						transformer.setParent( builder );
						transformer.setVertexFormat( builder.getVertexFormat() );
						bakedQuad.pipe( transformer );
						result.add( builder.build() );
					}
				}
			}
		}

		return result;
	}

}
