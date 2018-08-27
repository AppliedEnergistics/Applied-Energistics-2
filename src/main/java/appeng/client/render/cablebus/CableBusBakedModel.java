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

package appeng.client.render.cablebus;


import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

import appeng.api.parts.IPartBakedModel;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.block.networking.BlockCableBus;


public class CableBusBakedModel implements IBakedModel
{

	private static final Map<CableBusRenderState, List<BakedQuad>> CABLE_MODEL_CACHE = new HashMap<>();

	private final CableBuilder cableBuilder;

	private final FacadeBuilder facadeBuilder;

	private final Map<ResourceLocation, IBakedModel> partModels;

	private final TextureAtlasSprite particleTexture;

	private final TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();

	CableBusBakedModel( CableBuilder cableBuilder, FacadeBuilder facadeBuilder, Map<ResourceLocation, IBakedModel> partModels, TextureAtlasSprite particleTexture )
	{
		this.cableBuilder = cableBuilder;
		this.facadeBuilder = facadeBuilder;
		this.partModels = partModels;
		this.particleTexture = particleTexture;
	}

	@Override
	public List<BakedQuad> getQuads( @Nullable IBlockState state, @Nullable EnumFacing side, long rand )
	{
		CableBusRenderState renderState = getRenderingState( state );

		if( renderState == null || side != null )
		{
			return Collections.emptyList();
		}

		BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();

		List<BakedQuad> quads = new ArrayList<>();

		// The core parts of the cable will only be rendered in the CUTOUT layer.
		// Facades will add them selves to what ever the block would be rendered with,
		// except when transparent facades are enabled, they are forced to TRANSPARENT.
		if( layer == BlockRenderLayer.CUTOUT )
		{

			// First, handle the cable at the center of the cable bus
			final List<BakedQuad> cableModel = CABLE_MODEL_CACHE.computeIfAbsent( renderState, k -> {
				final List<BakedQuad> model = new ArrayList<>();
				this.addCableQuads( renderState, model );
				return model;
			} );
			quads.addAll( cableModel );

			// Then handle attachments
			for( EnumFacing facing : EnumFacing.values() )
			{
				final IPartModel partModel = renderState.getAttachments().get( facing );
				if( partModel == null )
				{
					continue;
				}

				for( ResourceLocation model : partModel.getModels() )
				{
					IBakedModel bakedModel = this.partModels.get( model );

					if( bakedModel == null )
					{
						throw new IllegalStateException( "Trying to use an unregistered part model: " + model );
					}

					List<BakedQuad> partQuads;
					if( bakedModel instanceof IPartBakedModel )
					{
						partQuads = ( (IPartBakedModel) bakedModel ).getPartQuads( renderState.getPartFlags().get( facing ), rand );
					}
					else
					{
						partQuads = bakedModel.getQuads( state, null, rand );
					}

					// Rotate quads accordingly
					QuadRotator rotator = new QuadRotator();
					partQuads = rotator.rotateQuads( partQuads, facing, EnumFacing.UP );

					quads.addAll( partQuads );
				}
			}
		}
		facadeBuilder.buildFacadeQuads( layer, renderState, rand, quads, partModels::get );

		return quads;
	}

	// Determines whether a cable is connected to exactly two sides that are opposite each other
	private static boolean isStraightLine( AECableType cableType, EnumMap<EnumFacing, AECableType> sides )
	{
		final Iterator<Entry<EnumFacing, AECableType>> it = sides.entrySet().iterator();
		if( !it.hasNext() )
		{
			return false; // No connections
		}

		final Entry<EnumFacing, AECableType> nextConnection = it.next();
		final EnumFacing firstSide = nextConnection.getKey();
		final AECableType firstType = nextConnection.getValue();

		if( !it.hasNext() )
		{
			return false; // Only a single connection
		}
		if( firstSide.getOpposite() != it.next().getKey() )
		{
			return false; // Connected to two sides that are not opposite each other
		}
		if( it.hasNext() )
		{
			return false; // Must not have any other connection points
		}

		final AECableType secondType = sides.get( firstSide.getOpposite() );

		return firstType == secondType;
	}

	private void addCableQuads( CableBusRenderState renderState, List<BakedQuad> quadsOut )
	{
		AECableType cableType = renderState.getCableType();
		if( cableType == AECableType.NONE )
		{
			return;
		}

		AEColor cableColor = renderState.getCableColor();
		EnumMap<EnumFacing, AECableType> connectionTypes = renderState.getConnectionTypes();

		// If the connection is straight, no busses are attached, and no covered core has been forced (in case of glass
		// cables), then render the cable as a simplified straight line.
		boolean noAttachments = !renderState.getAttachments().values().stream().anyMatch( IPartModel::requireCableConnection );
		if( noAttachments && isStraightLine( cableType, connectionTypes ) )
		{
			EnumFacing facing = connectionTypes.keySet().iterator().next();

			switch( cableType )
			{
				case GLASS:
					this.cableBuilder.addStraightGlassConnection( facing, cableColor, quadsOut );
					break;
				case COVERED:
					this.cableBuilder.addStraightCoveredConnection( facing, cableColor, quadsOut );
					break;
				case SMART:
					this.cableBuilder.addStraightSmartConnection( facing, cableColor, renderState.getChannelsOnSide().get( facing ), quadsOut );
					break;
				case DENSE_COVERED:
					this.cableBuilder.addStraightDenseCoveredConnection( facing, cableColor, quadsOut );
					break;
				case DENSE_SMART:
					this.cableBuilder.addStraightDenseSmartConnection( facing, cableColor, renderState.getChannelsOnSide().get( facing ), quadsOut );
					break;
				default:
					break;
			}

			return; // Don't render the other form of connection
		}

		this.cableBuilder.addCableCore( renderState.getCoreType(), cableColor, quadsOut );

		// Render all internal connections to attachments
		EnumMap<EnumFacing, Integer> attachmentConnections = renderState.getAttachmentConnections();
		for( EnumFacing facing : attachmentConnections.keySet() )
		{
			int distance = attachmentConnections.get( facing );
			int channels = renderState.getChannelsOnSide().get( facing );

			switch( cableType )
			{
				case GLASS:
					this.cableBuilder.addConstrainedGlassConnection( facing, cableColor, distance, quadsOut );
					break;
				case COVERED:
					this.cableBuilder.addConstrainedCoveredConnection( facing, cableColor, distance, quadsOut );
					break;
				case SMART:
					this.cableBuilder.addConstrainedSmartConnection( facing, cableColor, distance, channels, quadsOut );
					break;
				case DENSE_COVERED:
				case DENSE_SMART:
					// Dense cables do not render connections to parts since none can be attached
					break;
				default:
					break;
			}
		}

		// Render all outgoing connections using the appropriate type
		for( final Entry<EnumFacing, AECableType> connection : connectionTypes.entrySet() )
		{
			final EnumFacing facing = connection.getKey();
			final AECableType connectionType = connection.getValue();
			final boolean cableBusAdjacent = renderState.getCableBusAdjacent().contains( facing );
			final int channels = renderState.getChannelsOnSide().get( facing );

			switch( cableType )
			{
				case GLASS:
					this.cableBuilder.addGlassConnection( facing, cableColor, connectionType, cableBusAdjacent, quadsOut );
					break;
				case COVERED:
					this.cableBuilder.addCoveredConnection( facing, cableColor, connectionType, cableBusAdjacent, quadsOut );
					break;
				case SMART:
					this.cableBuilder.addSmartConnection( facing, cableColor, connectionType, cableBusAdjacent, channels, quadsOut );
					break;
				case DENSE_COVERED:
					this.cableBuilder.addDenseCoveredConnection( facing, cableColor, connectionType, cableBusAdjacent, quadsOut );
					break;
				case DENSE_SMART:
					this.cableBuilder.addDenseSmartConnection( facing, cableColor, connectionType, cableBusAdjacent, channels, quadsOut );
					break;
				default:
					break;
			}
		}
	}

	/**
	 * Gets a list of texture sprites appropriate for particles (digging, etc.) given the render state for a cable bus.
	 */
	public List<TextureAtlasSprite> getParticleTextures( CableBusRenderState renderState )
	{
		CableCoreType coreType = CableCoreType.fromCableType( renderState.getCableType() );
		AEColor cableColor = renderState.getCableColor();

		List<TextureAtlasSprite> result = new ArrayList<>();

		if( coreType != null )
		{
			result.add( this.cableBuilder.getCoreTexture( coreType, cableColor ) );
		}

		// If no core is present, just use the first part that comes into play
		for( EnumFacing side : renderState.getAttachments().keySet() )
		{
			IPartModel partModel = renderState.getAttachments().get( side );

			for( ResourceLocation model : partModel.getModels() )
			{
				IBakedModel bakedModel = this.partModels.get( model );

				if( bakedModel == null )
				{
					throw new IllegalStateException( "Trying to use an unregistered part model: " + model );
				}

				TextureAtlasSprite particleTexture = bakedModel.getParticleTexture();

				// If a part sub-model has no particle texture (indicated by it being the missing texture),
				// don't add it, so we don't get ugly missing texture break particles.
				if( this.textureMap.getMissingSprite() != particleTexture )
				{
					result.add( particleTexture );
				}
			}
		}

		return result;
	}

	private static CableBusRenderState getRenderingState( IBlockState state )
	{
		if( state == null || !( state instanceof IExtendedBlockState ) )
		{
			return null;
		}

		IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
		return extendedBlockState.getValue( BlockCableBus.RENDER_STATE_PROPERTY );
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return false;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return this.particleTexture;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.NONE;
	}
}
