/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;

import appeng.api.AEApi;
import appeng.api.util.AEAxisAlignedBB;
import appeng.parts.misc.PartCableAnchor;
import appeng.thirdparty.codechicken.lib.model.CachedFormat;
import appeng.thirdparty.codechicken.lib.model.Quad;
import appeng.thirdparty.codechicken.lib.model.pipeline.BakedPipeline;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadAlphaOverride;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadClamper;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadCornerKicker;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadFaceStripper;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadReInterpolator;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadTinter;


/**
 * The FacadeBuilder builds for facades..
 *
 * @author covers1624
 */
public class FacadeBuilder
{

	public static double THICK_THICKNESS = 2D / 16D;
	public static double THIN_THICKNESS = 1D / 16D;

	public static final AxisAlignedBB[] THICK_FACADE_BOXES = new AxisAlignedBB[] {
			new AxisAlignedBB( 0.0, 0.0, 0.0, 1.0, THICK_THICKNESS, 1.0 ),
			new AxisAlignedBB( 0.0, 1.0 - THICK_THICKNESS, 0.0, 1.0, 1.0, 1.0 ),
			new AxisAlignedBB( 0.0, 0.0, 0.0, 1.0, 1.0, THICK_THICKNESS ),
			new AxisAlignedBB( 0.0, 0.0, 1.0 - THICK_THICKNESS, 1.0, 1.0, 1.0 ),
			new AxisAlignedBB( 0.0, 0.0, 0.0, THICK_THICKNESS, 1.0, 1.0 ),
			new AxisAlignedBB( 1.0 - THICK_THICKNESS, 0.0, 0.0, 1.0, 1.0, 1.0 )
	};

	public static final AxisAlignedBB[] THIN_FACADE_BOXES = new AxisAlignedBB[] {
			new AxisAlignedBB( 0.0, 0.0, 0.0, 1.0, THIN_THICKNESS, 1.0 ),
			new AxisAlignedBB( 0.0, 1.0 - THIN_THICKNESS, 0.0, 1.0, 1.0, 1.0 ),
			new AxisAlignedBB( 0.0, 0.0, 0.0, 1.0, 1.0, THIN_THICKNESS ),
			new AxisAlignedBB( 0.0, 0.0, 1.0 - THIN_THICKNESS, 1.0, 1.0, 1.0 ),
			new AxisAlignedBB( 0.0, 0.0, 0.0, THIN_THICKNESS, 1.0, 1.0 ),
			new AxisAlignedBB( 1.0 - THIN_THICKNESS, 0.0, 0.0, 1.0, 1.0, 1.0 )
	};

	private ThreadLocal<BakedPipeline> pipelines = ThreadLocal.withInitial( () -> BakedPipeline.builder()//
			.addElement( "clamper", QuadClamper.FACTORY )// Clamper is responsible for clamping the vertex to the bounds specified.
			.addElement( "face_stripper", QuadFaceStripper.FACTORY )//Strips faces if they match a mask.
			.addElement( "corner_kicker", QuadCornerKicker.FACTORY )//Kicks the edge inner corners in, solves Z fighting
			.addElement( "interp", QuadReInterpolator.FACTORY )//Re-Interpolates the UV's for the quad.
			.addElement( "tinter", QuadTinter.FACTORY, false )// Tints the quad if we need it to. Disabled by default.
			.addElement( "transparent", QuadAlphaOverride.FACTORY, false, e -> e.setAlphaOverride( 0x4C / 255F ) )//Overrides the quad's alpha if we are forcing transparent facades.
			.build()//
	);
	private ThreadLocal<Quad> collectors = ThreadLocal.withInitial( Quad::new );

	public void buildFacadeQuads( BlockRenderLayer layer, CableBusRenderState renderState, long rand, List<BakedQuad> quads, Function<ResourceLocation, IBakedModel> modelLookup )
	{
		BakedPipeline pipeline = pipelines.get();
		Quad collectorQuad = collectors.get();
		boolean transparent = AEApi.instance().partHelper().getCableRenderMode().transparentFacades;
		Map<EnumFacing, FacadeRenderState> facadeStates = renderState.getFacades();
		List<AxisAlignedBB> partBoxes = renderState.getBoundingBoxes();
		Set<EnumFacing> sidesWithParts = renderState.getAttachments().keySet();
		IBlockAccess parentWorld = renderState.getWorld();
		BlockPos pos = renderState.getPos();
		BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
		boolean thinFacades = isUseThinFacades( partBoxes );

		for( Entry<EnumFacing, FacadeRenderState> entry : facadeStates.entrySet() )
		{
			EnumFacing side = entry.getKey();
			int sideIndex = side.ordinal();
			FacadeRenderState facadeRenderState = entry.getValue();
			boolean renderStilt = !sidesWithParts.contains( side );
			if( layer == BlockRenderLayer.CUTOUT && renderStilt )
			{
				for( ResourceLocation part : PartCableAnchor.FACADE_MODELS.getModels() )
				{
					IBakedModel partModel = modelLookup.apply( part );
					QuadRotator rotator = new QuadRotator();
					quads.addAll( rotator.rotateQuads( gatherQuads( partModel, null, rand ), side, EnumFacing.UP ) );
				}
			}
			//If we are forcing transparency and this isn't the Translucent layer.
			if( transparent && layer != BlockRenderLayer.TRANSLUCENT )
			{
				continue;
			}

			IBlockState blockState = facadeRenderState.getSourceBlock();
			//If we aren't forcing transparency let the block decide if it should render.
			if( !transparent )
			{
				if( !blockState.getBlock().canRenderInLayer( blockState, layer ) )
				{
					continue;
				}
			}

			AxisAlignedBB fullBounds = thinFacades ? THIN_FACADE_BOXES[sideIndex] : THICK_FACADE_BOXES[sideIndex];
			AxisAlignedBB facadeBox = fullBounds;
			//If we are a transparent facade, we need to modify out BB.
			if( facadeRenderState.isTransparent() )
			{
				double offset = thinFacades ? THIN_THICKNESS : THICK_THICKNESS;
				AEAxisAlignedBB tmpBB = null;
				for( EnumFacing face : EnumFacing.VALUES )
				{
					//Only faces that aren't on our axis
					if( face.getAxis() != side.getAxis() )
					{
						FacadeRenderState otherState = facadeStates.get( face );
						if( otherState != null && !otherState.isTransparent() )
						{
							if( tmpBB == null )
							{
								tmpBB = AEAxisAlignedBB.fromBounds( facadeBox );
							}
							switch( face )
							{
								case DOWN:
									tmpBB.minY += offset;
									break;
								case UP:
									tmpBB.maxY -= offset;
									break;
								case NORTH:
									tmpBB.minZ += offset;
									break;
								case SOUTH:
									tmpBB.maxZ -= offset;
									break;
								case WEST:
									tmpBB.minX += offset;
									break;
								case EAST:
									tmpBB.maxX -= offset;
									break;
								default:
									throw new RuntimeException( "Switch falloff. " + String.valueOf( face ) );
							}
						}
					}
				}
				if( tmpBB != null )
				{
					facadeBox = tmpBB.getBoundingBox();
				}
			}

			AEAxisAlignedBB cutOutBox = getCutOutBox( facadeBox, partBoxes );
			List<AxisAlignedBB> holeStrips = getBoxes( facadeBox, cutOutBox, side.getAxis() );
			IBlockAccess facadeAccess = new FacadeBlockAccess( parentWorld, pos, side, blockState );

			BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

			try
			{
				blockState = blockState.getActualState( facadeAccess, pos );
			}
			catch( Exception ignored )
			{
			}
			IBakedModel model = dispatcher.getModelForState( blockState );
			try
			{
				blockState = blockState.getBlock().getExtendedState( blockState, facadeAccess, pos );
			}
			catch( Exception ignored )
			{
			}

			List<BakedQuad> modelQuads = new ArrayList<>();
			//If we are forcing transparent facades, fake the render layer, and grab all quads.
			if( transparent )
			{
				for( BlockRenderLayer forcedLayer : BlockRenderLayer.values() )
				{
					//Check if the block renders on the layer we want to force.
					if( blockState.getBlock().canRenderInLayer( blockState, forcedLayer ) )
					{
						//Force the layer and gather quads.
						ForgeHooksClient.setRenderLayer( forcedLayer );
						modelQuads.addAll( gatherQuads( model, blockState, rand ) );
					}
				}

				//Reset.
				ForgeHooksClient.setRenderLayer( layer );
			}
			else
			{
				modelQuads.addAll( gatherQuads( model, blockState, rand ) );
			}

			//No quads.. Cool, next!
			if( modelQuads.isEmpty() )
			{
				continue;
			}

			//Grab out pipeline elements.
			QuadClamper clamper = pipeline.getElement( "clamper", QuadClamper.class );
			QuadFaceStripper edgeStripper = pipeline.getElement( "face_stripper", QuadFaceStripper.class );
			QuadTinter tinter = pipeline.getElement( "tinter", QuadTinter.class );
			QuadCornerKicker kicker = pipeline.getElement( "corner_kicker", QuadCornerKicker.class );

			//Set global element states.

			//calculate the side mask.
			int facadeMask = 0;
			for( Entry<EnumFacing, FacadeRenderState> ent : facadeStates.entrySet() )
			{
				EnumFacing s = ent.getKey();
				if( s.getAxis() != side.getAxis() )
				{
					FacadeRenderState otherState = ent.getValue();
					if( !otherState.isTransparent() )
					{
						facadeMask |= 1 << s.ordinal();
					}
				}
			}
			//Setup the edge stripper.
			edgeStripper.setBounds( fullBounds );
			edgeStripper.setMask( facadeMask );

			//Setup the kicker.
			kicker.setSide( sideIndex );
			kicker.setFacadeMask( facadeMask );
			kicker.setBox( fullBounds );
			kicker.setThickness( thinFacades ? THIN_THICKNESS : THICK_THICKNESS );

			for( BakedQuad quad : modelQuads )
			{
				//lookup the format in CachedFormat.
				CachedFormat format = CachedFormat.lookup( quad.getFormat() );
				//If this quad has a tint index, setup the tinter.
				if( quad.hasTintIndex() )
				{
					tinter.setTint( blockColors.colorMultiplier( blockState, facadeAccess, pos, quad.getTintIndex() ) );
				}
				for( AxisAlignedBB box : holeStrips )
				{
					//setup the clamper for this box
					clamper.setClampBounds( box );
					//Reset the pipeline, clears all enabled/disabled states.
					pipeline.reset( format );
					//Reset out collector.
					collectorQuad.reset( format );
					//Enable / disable the optional elements
					pipeline.setElementState( "tinter", quad.hasTintIndex() );
					pipeline.setElementState( "transparent", transparent );
					//Prepare the pipeline for a quad.
					pipeline.prepare( collectorQuad );

					//Pipe our quad into the pipeline.
					quad.pipe( pipeline );
					//Check if the collector got any data.
					if( collectorQuad.full )
					{
						//Add the result.
						quads.add( collectorQuad.bake() );
					}
				}
			}
		}
	}

	/**
	 * This is slow, so should be cached.
	 *
	 * @return The model.
	 */
	public List<BakedQuad> buildFacadeItemQuads( ItemStack textureItem, EnumFacing side )
	{
		List<BakedQuad> facadeQuads = new ArrayList<>();
		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides( textureItem, null, null );
		List<BakedQuad> modelQuads = gatherQuads( model, null, 0 );

		BakedPipeline pipeline = pipelines.get();
		Quad collectorQuad = collectors.get();

		//Grab pipeline elements.
		QuadClamper clamper = pipeline.getElement( "clamper", QuadClamper.class );
		QuadTinter tinter = pipeline.getElement( "tinter", QuadTinter.class );

		for( BakedQuad quad : modelQuads )
		{
			//Lookup the CachedFormat for this quads format.
			CachedFormat format = CachedFormat.lookup( quad.getFormat() );
			//Reset the pipeline.
			pipeline.reset( format );
			//Reset the collector.
			collectorQuad.reset( format );
			//If we have a tint index, setup the tinter and enable it.
			if( quad.hasTintIndex() )
			{
				tinter.setTint( Minecraft.getMinecraft().getItemColors().colorMultiplier( textureItem, quad.getTintIndex() ) );
				pipeline.enableElement( "tinter" );
			}
			//Disable elements we don't need for items.
			pipeline.disableElement( "face_stripper" );
			pipeline.disableElement( "corner_kicker" );
			//Setup the clamper
			clamper.setClampBounds( THICK_FACADE_BOXES[side.ordinal()] );
			//Prepare the pipeline.
			pipeline.prepare( collectorQuad );
			//Pipe our quad into the pipeline.
			quad.pipe( pipeline );
			//Check the collector for data and add the quad if there was.
			if( collectorQuad.full )
			{
				facadeQuads.add( collectorQuad.bakeUnpacked() );
			}
		}
		return facadeQuads;
	}

	//Helper to gather all quads from a model into a list.
	private static List<BakedQuad> gatherQuads( IBakedModel model, IBlockState state, long rand )
	{
		List<BakedQuad> modelQuads = new ArrayList<>();
		for( EnumFacing face : EnumFacing.VALUES )
		{
			modelQuads.addAll( model.getQuads( state, face, rand ) );
		}
		modelQuads.addAll( model.getQuads( state, null, rand ) );
		return modelQuads;
	}

	/**
	 * Given the actual facade bounding box, and the bounding boxes of all parts, determine the biggest union of AABB that intersect with the facade's bounding
	 * box. This AABB will need to be "cut out" when the facade is rendered.
	 */
	@Nullable
	private static AEAxisAlignedBB getCutOutBox( AxisAlignedBB facadeBox, List<AxisAlignedBB> partBoxes )
	{
		AEAxisAlignedBB b = null;
		for( AxisAlignedBB bb : partBoxes )
		{
			if( bb.intersects( facadeBox ) )
			{
				if( b == null )
				{
					b = AEAxisAlignedBB.fromBounds( bb );
				}
				else
				{
					b.maxX = Math.max( b.maxX, bb.maxX );
					b.maxY = Math.max( b.maxY, bb.maxY );
					b.maxZ = Math.max( b.maxZ, bb.maxZ );
					b.minX = Math.min( b.minX, bb.minX );
					b.minY = Math.min( b.minY, bb.minY );
					b.minZ = Math.min( b.minZ, bb.minZ );
				}
			}
		}
		return b;
	}

	/**
	 * Generates the box segments around the specified hole. If the specified hole is null, a Singleton of the Facade box is returned.
	 *
	 * @param fb The Facade's box.
	 * @param hole The hole to 'cut'.
	 * @param axis The axis the facade is on.
	 *
	 * @return The box segments.
	 */
	private static List<AxisAlignedBB> getBoxes( AxisAlignedBB fb, AEAxisAlignedBB hole, Axis axis )
	{
		if( hole == null )
		{
			return Collections.singletonList( fb );
		}
		List<AxisAlignedBB> boxes = new ArrayList<>();
		switch( axis )
		{
			case Y:
				boxes.add( new AxisAlignedBB( fb.minX, fb.minY, fb.minZ, hole.minX, fb.maxY, fb.maxZ ) );
				boxes.add( new AxisAlignedBB( hole.maxX, fb.minY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ ) );

				boxes.add( new AxisAlignedBB( hole.minX, fb.minY, fb.minZ, hole.maxX, fb.maxY, hole.minZ ) );
				boxes.add( new AxisAlignedBB( hole.minX, fb.minY, hole.maxZ, hole.maxX, fb.maxY, fb.maxZ ) );

				break;
			case Z:
				boxes.add( new AxisAlignedBB( fb.minX, fb.minY, fb.minZ, fb.maxX, hole.minY, fb.maxZ ) );
				boxes.add( new AxisAlignedBB( fb.minX, hole.maxY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ ) );

				boxes.add( new AxisAlignedBB( fb.minX, hole.minY, fb.minZ, hole.minX, hole.maxY, fb.maxZ ) );
				boxes.add( new AxisAlignedBB( hole.maxX, hole.minY, fb.minZ, fb.maxX, hole.maxY, fb.maxZ ) );

				break;
			case X:
				boxes.add( new AxisAlignedBB( fb.minX, fb.minY, fb.minZ, fb.maxX, hole.minY, fb.maxZ ) );
				boxes.add( new AxisAlignedBB( fb.minX, hole.maxY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ ) );

				boxes.add( new AxisAlignedBB( fb.minX, hole.minY, fb.minZ, fb.maxX, hole.maxY, hole.minZ ) );
				boxes.add( new AxisAlignedBB( fb.minX, hole.minY, hole.maxZ, fb.maxX, hole.maxY, fb.maxZ ) );
				break;
			default:
				//should never happen.
				throw new RuntimeException( "switch falloff. " + String.valueOf( axis ) );
		}

		return boxes;
	}

	/**
	 * Determines if any of the part's bounding boxes intersects with the outside 2 voxel wide layer. If so, we should use thinner facades (1 voxel deep).
	 */
	private static boolean isUseThinFacades( List<AxisAlignedBB> partBoxes )
	{
		final double min = 2.0 / 16.0;
		final double max = 14.0 / 16.0;

		for( AxisAlignedBB bb : partBoxes )
		{
			int o = 0;
			o += bb.maxX > max ? 1 : 0;
			o += bb.maxY > max ? 1 : 0;
			o += bb.maxZ > max ? 1 : 0;
			o += bb.minX < min ? 1 : 0;
			o += bb.minY < min ? 1 : 0;
			o += bb.minZ < min ? 1 : 0;

			if( o >= 2 )
			{
				return true;
			}
		}
		return false;
	}
}
