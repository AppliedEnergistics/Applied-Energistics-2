package appeng.client.render.cablebus;


import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Function;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import appeng.api.util.AECableType;
import appeng.api.util.AEColor;


/**
 * A helper class that builds quads for cable connections.
 */
class CableBuilder
{

	private final VertexFormat format;

	// Textures for the cable core types, one per type/color pair
	private final EnumMap<CableCoreType, EnumMap<AEColor, TextureAtlasSprite>> coreTextures;

	// Textures for rendering the actual connection cubes, one per type/color pair
	private final EnumMap<AECableType, EnumMap<AEColor, TextureAtlasSprite>> connectionTextures;

	private final SmartCableTextures smartCableTextures;

	CableBuilder( VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
	{
		this.format = format;
		this.coreTextures = new EnumMap<>( CableCoreType.class );

		for( CableCoreType type : CableCoreType.values() )
		{
			EnumMap<AEColor, TextureAtlasSprite> colorTextures = new EnumMap<>( AEColor.class );

			for( AEColor color : AEColor.values() )
			{
				colorTextures.put( color, bakedTextureGetter.apply( type.getTexture( color ) ) );
			}

			coreTextures.put( type, colorTextures );
		}

		this.connectionTextures = new EnumMap<>( AECableType.class );

		for( AECableType type : AECableType.VALIDCABLES )
		{
			EnumMap<AEColor, TextureAtlasSprite> colorTextures = new EnumMap<>( AEColor.class );

			for( AEColor color : AEColor.values() )
			{
				colorTextures.put( color, bakedTextureGetter.apply( type.getConnectionTexture( color ) ) );
			}

			connectionTextures.put( type, colorTextures );
		}

		smartCableTextures = new SmartCableTextures( bakedTextureGetter );
	}

	/**
	 * Adds the core of a cable to the given list of quads.
	 *
	 * The type of cable core is automatically deduced from the given cable type.
	 */
	public void addCableCore( AECableType cableType, AEColor color, List<BakedQuad> quadsOut )
	{
		switch( cableType )
		{
			case GLASS:
				addCableCore( CableCoreType.GLASS, color, quadsOut );
				break;
			case COVERED:
			case SMART:
				addCableCore( CableCoreType.COVERED, color, quadsOut );
				break;
			case DENSE:
				addCableCore( CableCoreType.DENSE, color, quadsOut );
				break;
			default:
		}
	}

	public void addCableCore( CableCoreType coreType, AEColor color, List<BakedQuad> quadsOut )
	{
		CubeBuilder cubeBuilder = new CubeBuilder( format, quadsOut );

		TextureAtlasSprite texture = coreTextures.get( coreType ).get( color );
		cubeBuilder.setTexture( texture );

		switch( coreType )
		{
			case GLASS:
				cubeBuilder.addCube( 6, 6, 6, 10, 10, 10 );
				break;
			case COVERED:
				cubeBuilder.addCube( 5, 5, 5, 11, 11, 11 );
				break;
			case DENSE:
				cubeBuilder.addCube( 3, 3, 3, 13, 13, 13 );
				break;
		}
	}

	public void addGlassConnection( EnumFacing facing, AEColor cableColor, AECableType connectionType, boolean cableBusAdjacent, List<BakedQuad> quadsOut )
	{
		CubeBuilder cubeBuilder = new CubeBuilder( format, quadsOut );

		// We render all faces except the one on the connection side
		cubeBuilder.setDrawFaces( EnumSet.complementOf( EnumSet.of( facing ) ) );

		// For to-machine connections, use a thicker end-cap for the connection
		if( connectionType != AECableType.GLASS && !cableBusAdjacent )
		{
			TextureAtlasSprite texture = connectionTextures.get( AECableType.COVERED ).get( cableColor );
			cubeBuilder.setTexture( texture );

			addBigCoveredCableSizedCube( facing, cubeBuilder );
		}

		TextureAtlasSprite texture = connectionTextures.get( AECableType.GLASS ).get( cableColor );
		cubeBuilder.setTexture( texture );

		switch( facing )
		{
			case DOWN:
				cubeBuilder.addCube( 6, 0, 6, 10, 6, 10 );
				break;
			case EAST:
				cubeBuilder.addCube( 10, 6, 6, 16, 10, 10 );
				break;
			case NORTH:
				cubeBuilder.addCube( 6, 6, 0, 10, 10, 6 );
				break;
			case SOUTH:
				cubeBuilder.addCube( 6, 6, 10, 10, 10, 16 );
				break;
			case UP:
				cubeBuilder.addCube( 6, 10, 6, 10, 16, 10 );
				break;
			case WEST:
				cubeBuilder.addCube( 0, 6, 6, 6, 10, 10 );
				break;
		}
	}

	public void addStraightGlassConnection( EnumFacing facing, AEColor cableColor, List<BakedQuad> quadsOut )
	{
		CubeBuilder cubeBuilder = new CubeBuilder( format, quadsOut );

		// We render all faces except the connection caps. We can do this because the glass cable is the smallest one
		// and its ends will always be covered by something
		cubeBuilder.setDrawFaces( EnumSet.complementOf( EnumSet.of( facing, facing.getOpposite() ) ) );

		TextureAtlasSprite texture = connectionTextures.get( AECableType.GLASS ).get( cableColor );
		cubeBuilder.setTexture( texture );

		switch( facing )
		{
			case DOWN:
			case UP:
				cubeBuilder.addCube( 6, 0, 6, 10, 16, 10 );
				break;
			case NORTH:
			case SOUTH:
				cubeBuilder.addCube( 6, 6, 0, 10, 10, 16 );
				break;
			case EAST:
			case WEST:
				cubeBuilder.addCube( 0, 6, 6, 16, 10, 10 );
				break;
		}
	}

	public void addConstrainedGlassConnection( EnumFacing facing, AEColor cableColor, int distanceFromEdge, List<BakedQuad> quadsOut )
	{

		// Glass connections reach only 6 voxels from the edge
		if( distanceFromEdge >= 6 )
		{
			return;
		}

		CubeBuilder cubeBuilder = new CubeBuilder( format, quadsOut );

		TextureAtlasSprite texture = connectionTextures.get( AECableType.GLASS ).get( cableColor );
		cubeBuilder.setTexture( texture );

		switch( facing )
		{
			case DOWN:
				cubeBuilder.addCube( 6, distanceFromEdge, 6, 10, 6, 10 );
				break;
			case EAST:
				cubeBuilder.addCube( 10, 6, 6, 16 - distanceFromEdge, 10, 10 );
				break;
			case NORTH:
				cubeBuilder.addCube( 6, 6, distanceFromEdge, 10, 10, 6 );
				break;
			case SOUTH:
				cubeBuilder.addCube( 6, 6, 10, 10, 10, 16 - distanceFromEdge );
				break;
			case UP:
				cubeBuilder.addCube( 6, 10, 6, 10, 16 - distanceFromEdge, 10 );
				break;
			case WEST:
				cubeBuilder.addCube( distanceFromEdge, 6, 6, 6, 10, 10 );
				break;
		}
	}

	public void addCoveredConnection( EnumFacing facing, AEColor cableColor, AECableType connectionType, boolean cableBusAdjacent, List<BakedQuad> quadsOut )
	{

		CubeBuilder cubeBuilder = new CubeBuilder( format, quadsOut );

		// We render all faces except the one on the connection side
		cubeBuilder.setDrawFaces( EnumSet.complementOf( EnumSet.of( facing ) ) );

		TextureAtlasSprite texture = connectionTextures.get( AECableType.COVERED ).get( cableColor );
		cubeBuilder.setTexture( texture );

		// Draw a covered connection, if anything but glass is requested
		if( connectionType != AECableType.GLASS && !cableBusAdjacent )
		{
			addBigCoveredCableSizedCube( facing, cubeBuilder );
		}

		addCoveredCableSizedCube( facing, cubeBuilder );
	}

	public void addStraightCoveredConnection( EnumFacing facing, AEColor cableColor, List<BakedQuad> quadsOut )
	{
		CubeBuilder cubeBuilder = new CubeBuilder( format, quadsOut );

		TextureAtlasSprite texture = connectionTextures.get( AECableType.COVERED ).get( cableColor );
		cubeBuilder.setTexture( texture );

		setStraightCableUVs( cubeBuilder, facing, 5, 11 );

		addStraightCoveredCableSizedCube( facing, cubeBuilder );



	}

	private static void setStraightCableUVs( CubeBuilder cubeBuilder, EnumFacing facing, int x, int y )
	{
		switch( facing )
		{
			case DOWN:
			case UP:
				cubeBuilder.setCustomUv( EnumFacing.NORTH, x, 0, y, x );
				cubeBuilder.setCustomUv( EnumFacing.EAST, x, 0, y, x );
				cubeBuilder.setCustomUv( EnumFacing.SOUTH, x, 0, y, x );
				cubeBuilder.setCustomUv( EnumFacing.WEST, x, 0, y, x );
				break;
			case EAST:
			case WEST:
				cubeBuilder.setCustomUv( EnumFacing.UP, 0, x, x, y );
				cubeBuilder.setCustomUv( EnumFacing.DOWN, 0, x, x, y );
				cubeBuilder.setCustomUv( EnumFacing.NORTH, 0, x, x, y );
				cubeBuilder.setCustomUv( EnumFacing.SOUTH, 0, x, x, y );
				break;
			case NORTH:
			case SOUTH:
				cubeBuilder.setCustomUv( EnumFacing.UP, x, 0, y, x );
				cubeBuilder.setCustomUv( EnumFacing.DOWN, x, 0, y, x );
				cubeBuilder.setCustomUv( EnumFacing.EAST, 0, x, x, y );
				cubeBuilder.setCustomUv( EnumFacing.WEST, 0, x, x, y );
				break;
		}
	}

	public void addConstrainedCoveredConnection( EnumFacing facing, AEColor cableColor, int distanceFromEdge, List<BakedQuad> quadsOut )
	{

		// The core of a covered cable reaches up to 5 voxels from the block edge, so
		// drawing a connection can only occur from there onwards
		if( distanceFromEdge >= 5 )
		{
			return;
		}

		CubeBuilder cubeBuilder = new CubeBuilder( format, quadsOut );

		TextureAtlasSprite texture = connectionTextures.get( AECableType.COVERED ).get( cableColor );
		cubeBuilder.setTexture( texture );

		addCoveredCableSizedCube( facing, distanceFromEdge, cubeBuilder );

	}

	public void addSmartConnection( EnumFacing facing, AEColor cableColor, AECableType connectionType, boolean cableBusAdjacent, int channels, List<BakedQuad> quadsOut )
	{

		CubeBuilder cubeBuilder = new CubeBuilder( format, quadsOut );

		// We render all faces except the one on the connection side
		cubeBuilder.setDrawFaces( EnumSet.complementOf( EnumSet.of( facing ) ) );

		TextureAtlasSprite texture = connectionTextures.get( AECableType.SMART ).get( cableColor );
		cubeBuilder.setTexture( texture );

		TextureAtlasSprite oddChannel = smartCableTextures.getOddTextureForChannels( channels );
		TextureAtlasSprite evenChannel = smartCableTextures.getEvenTextureForChannels( channels );

		// For to-machine connections, use a thicker end-cap for the connection
		if( connectionType != AECableType.GLASS && !cableBusAdjacent )
		{
			addBigCoveredCableSizedCube( facing, cubeBuilder );

			// Render the channel indicators brightly lit at night
			cubeBuilder.setRenderFullBright( true );

			cubeBuilder.setTexture( oddChannel );
			cubeBuilder.setColorRGB( cableColor.blackVariant );
			addBigCoveredCableSizedCube( facing, cubeBuilder );

			cubeBuilder.setTexture( evenChannel );
			cubeBuilder.setColorRGB( cableColor.whiteVariant );
			addBigCoveredCableSizedCube( facing, cubeBuilder );

			// Reset back to normal rendering for the rest
			cubeBuilder.setRenderFullBright( false );
			cubeBuilder.setTexture( texture );
		}

		addCoveredCableSizedCube( facing, cubeBuilder );

		// Render the channel indicators brightly lit at night
		cubeBuilder.setRenderFullBright( true );

		cubeBuilder.setTexture( oddChannel );
		cubeBuilder.setColorRGB( cableColor.blackVariant );
		addCoveredCableSizedCube( facing, cubeBuilder );

		cubeBuilder.setTexture( evenChannel );
		cubeBuilder.setColorRGB( cableColor.whiteVariant );
		addCoveredCableSizedCube( facing, cubeBuilder );

	/*	TODO: this.setSmartConnectionRotations( of, renderer );
		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;*/
	}

	public void addStraightSmartConnection( EnumFacing facing, AEColor cableColor, int channels, List<BakedQuad> quadsOut )
	{
		CubeBuilder cubeBuilder = new CubeBuilder( format, quadsOut );

		TextureAtlasSprite texture = connectionTextures.get( AECableType.SMART ).get( cableColor );
		cubeBuilder.setTexture( texture );

		setStraightCableUVs( cubeBuilder, facing, 5, 11 );

		addStraightCoveredCableSizedCube( facing, cubeBuilder );

		TextureAtlasSprite oddChannel = smartCableTextures.getOddTextureForChannels( channels );
		TextureAtlasSprite evenChannel = smartCableTextures.getEvenTextureForChannels( channels );

		// Render the channel indicators brightly lit at night
		cubeBuilder.setRenderFullBright( true );

		cubeBuilder.setTexture( oddChannel );
		cubeBuilder.setColorRGB( cableColor.blackVariant );
		addStraightCoveredCableSizedCube( facing, cubeBuilder );

		cubeBuilder.setTexture( evenChannel );
		cubeBuilder.setColorRGB( cableColor.whiteVariant );
		addStraightCoveredCableSizedCube( facing, cubeBuilder );
	}

	public void addConstrainedSmartConnection( EnumFacing facing, AEColor cableColor, int distanceFromEdge, int channels, List<BakedQuad> quadsOut )
	{
		// Same as with covered cables, the smart cable's core extends up to 5 voxels away from the edge.
		// Drawing a connection to any point before that point is fruitless
		if( distanceFromEdge >= 5 )
		{
			return;
		}

		CubeBuilder cubeBuilder = new CubeBuilder( format, quadsOut );

		TextureAtlasSprite texture = connectionTextures.get( AECableType.SMART ).get( cableColor );
		cubeBuilder.setTexture( texture );

		addCoveredCableSizedCube( facing, distanceFromEdge, cubeBuilder );

		TextureAtlasSprite oddChannel = smartCableTextures.getOddTextureForChannels( channels );
		TextureAtlasSprite evenChannel = smartCableTextures.getEvenTextureForChannels( channels );

		// Render the channel indicators brightly lit at night
		cubeBuilder.setRenderFullBright( true );

		cubeBuilder.setTexture( oddChannel );
		cubeBuilder.setColorRGB( cableColor.blackVariant );
		addCoveredCableSizedCube( facing, distanceFromEdge, cubeBuilder );

		cubeBuilder.setTexture( evenChannel );
		cubeBuilder.setColorRGB( cableColor.whiteVariant );
		addCoveredCableSizedCube( facing, distanceFromEdge, cubeBuilder );
	}

	public void addDenseConnection( EnumFacing facing, AEColor cableColor, AECableType connectionType, boolean cableBusAdjacent, int channels, List<BakedQuad> quadsOut )
	{
		// Dense cables only render their connections as dense if the adjacent blocks actually wants that
		if( connectionType == AECableType.SMART )
		{
			addSmartConnection( facing, cableColor, connectionType, cableBusAdjacent, channels, quadsOut );
			return;
		}
		else if( connectionType != AECableType.DENSE )
		{
			addCoveredConnection( facing, cableColor, connectionType, cableBusAdjacent, quadsOut );
			return;
		}

		CubeBuilder cubeBuilder = new CubeBuilder( format, quadsOut );

		// We render all faces except the one on the connection side
		cubeBuilder.setDrawFaces( EnumSet.complementOf( EnumSet.of( facing ) ) );

		TextureAtlasSprite texture = connectionTextures.get( AECableType.DENSE ).get( cableColor );
		cubeBuilder.setTexture( texture );

		addDenseCableSizedCube( facing, cubeBuilder );

		// Dense cables show used channels in groups of 4, rounded up
		channels = (channels + 3) / 4;

		TextureAtlasSprite oddChannel = smartCableTextures.getOddTextureForChannels( channels );
		TextureAtlasSprite evenChannel = smartCableTextures.getEvenTextureForChannels( channels );

		// Render the channel indicators brightly lit at night
		cubeBuilder.setRenderFullBright( true );

		cubeBuilder.setTexture( oddChannel );
		cubeBuilder.setColorRGB( cableColor.blackVariant );
		addDenseCableSizedCube( facing, cubeBuilder );

		cubeBuilder.setTexture( evenChannel );
		cubeBuilder.setColorRGB( cableColor.whiteVariant );
		addDenseCableSizedCube( facing, cubeBuilder );

		// Reset back to normal rendering for the rest
		cubeBuilder.setRenderFullBright( false );
		cubeBuilder.setTexture( texture );

	}

	public void addStraightDenseConnection( EnumFacing facing, AEColor cableColor, int channels, List<BakedQuad> quadsOut )
	{
		CubeBuilder cubeBuilder = new CubeBuilder( format, quadsOut );

		TextureAtlasSprite texture = connectionTextures.get( AECableType.DENSE ).get( cableColor );
		cubeBuilder.setTexture( texture );

		setStraightCableUVs( cubeBuilder, facing, 5, 11 );

		addStraightDenseCableSizedCube( facing, cubeBuilder );

		// Dense cables show used channels in groups of 4, rounded up
		channels = (channels + 3) / 4;

		TextureAtlasSprite oddChannel = smartCableTextures.getOddTextureForChannels( channels );
		TextureAtlasSprite evenChannel = smartCableTextures.getEvenTextureForChannels( channels );

		// Render the channel indicators brightly lit at night
		cubeBuilder.setRenderFullBright( true );

		cubeBuilder.setTexture( oddChannel );
		cubeBuilder.setColorRGB( cableColor.blackVariant );
		addStraightDenseCableSizedCube( facing, cubeBuilder );

		cubeBuilder.setTexture( evenChannel );
		cubeBuilder.setColorRGB( cableColor.whiteVariant );
		addStraightDenseCableSizedCube( facing, cubeBuilder );
	}


	private static void addDenseCableSizedCube( EnumFacing facing, CubeBuilder cubeBuilder )
	{
		switch( facing )
		{
			case DOWN:
				cubeBuilder.addCube( 4, 0, 4, 12, 5, 12 );
				break;
			case EAST:
				cubeBuilder.addCube( 11, 4, 4, 16, 12, 12 );
				break;
			case NORTH:
				cubeBuilder.addCube( 4, 4, 0, 12, 12, 5 );
				break;
			case SOUTH:
				cubeBuilder.addCube( 4, 4, 11, 12, 12, 16 );
				break;
			case UP:
				cubeBuilder.addCube( 4, 11, 4, 12, 16, 12 );
				break;
			case WEST:
				cubeBuilder.addCube( 0, 4, 4, 5, 12, 12 );
				break;
		}
	}

	// Adds a cube to the given cube builder that has the size of a dense cable connection and spans the entire block for the given direction
	private static void addStraightDenseCableSizedCube( EnumFacing facing, CubeBuilder cubeBuilder )
	{
		switch( facing )
		{
			case DOWN:
			case UP:
				cubeBuilder.addCube( 3, 0, 3, 13, 16, 13 );
				break;
			case EAST:
			case WEST:
				/*renderer.uvRotateEast = renderer.uvRotateWest = 1;
				renderer.uvRotateBottom = renderer.uvRotateTop = 1;*/
				cubeBuilder.addCube( 0, 3, 3, 16, 13, 13 );
				break;
			case NORTH:
			case SOUTH:
				// TODO renderer.uvRotateNorth = renderer.uvRotateSouth = 1;
				cubeBuilder.addCube( 3, 3, 0, 13, 13, 16 );
				break;
		}
	}

	// Adds a cube to the given cube builder that has the size of a covered cable connection from the core of the cable to the given face
	private static void addCoveredCableSizedCube( EnumFacing facing, CubeBuilder cubeBuilder )
	{
		switch( facing )
		{
			case DOWN:
				cubeBuilder.addCube( 6, 0, 6, 10, 5, 10 );
				break;
			case EAST:
				cubeBuilder.addCube( 11, 6, 6, 16, 10, 10 );
				break;
			case NORTH:
				cubeBuilder.addCube( 6, 6, 0, 10, 10, 5 );
				break;
			case SOUTH:
				cubeBuilder.addCube( 6, 6, 11, 10, 10, 16 );
				break;
			case UP:
				cubeBuilder.addCube( 6, 11, 6, 10, 16, 10 );
				break;
			case WEST:
				cubeBuilder.addCube( 0, 6, 6, 5, 10, 10 );
				break;
		}
	}

	// Adds a cube to the given cube builder that has the size of a covered cable connection and spans the entire block for the given direction
	private static void addStraightCoveredCableSizedCube( EnumFacing facing, CubeBuilder cubeBuilder )
	{
		switch( facing )
		{
			case DOWN:
			case UP:
				cubeBuilder.addCube( 5, 0, 5, 11, 16, 11 );
				break;
			case EAST:
			case WEST:
				/*renderer.uvRotateEast = renderer.uvRotateWest = 1;
				renderer.uvRotateBottom = renderer.uvRotateTop = 1;*/
				cubeBuilder.addCube( 0, 5, 5, 16, 11, 11 );
				break;
			case NORTH:
			case SOUTH:
				// TODO renderer.uvRotateNorth = renderer.uvRotateSouth = 1;
				cubeBuilder.addCube( 5, 5, 0, 11, 11, 16 );
				break;
		}
	}

	private static void addCoveredCableSizedCube( EnumFacing facing, int distanceFromEdge, CubeBuilder cubeBuilder )
	{
		switch( facing )
		{
			case DOWN:
				cubeBuilder.addCube( 6, distanceFromEdge, 6, 10, 5, 10 );
				break;
			case EAST:
				cubeBuilder.addCube( 11, 6, 6, 16 - distanceFromEdge, 10, 10 );
				break;
			case NORTH:
				cubeBuilder.addCube( 6, 6, distanceFromEdge, 10, 10, 5 );
				break;
			case SOUTH:
				cubeBuilder.addCube( 6, 6, 11, 10, 10, 16 - distanceFromEdge );
				break;
			case UP:
				cubeBuilder.addCube( 6, 11, 6, 10, 16 - distanceFromEdge, 10 );
				break;
			case WEST:
				cubeBuilder.addCube( distanceFromEdge, 6, 6, 5, 10, 10 );
				break;
		}
	}

	/**
	 * This renders a slightly bigger covered cable connection to the specified side. This is used to connect cable cores with adjacent machines
	 * that do not want to be connected to using a glass cable connection. This applies to most machines (interfaces, etc.)
	 */
	private void addBigCoveredCableSizedCube( EnumFacing facing, CubeBuilder cubeBuilder )
	{
		switch( facing )
		{
			case DOWN:
				cubeBuilder.addCube( 5, 0, 5, 11, 4, 11 );
				break;
			case EAST:
				cubeBuilder.addCube( 12, 5, 5, 16, 11, 11 );
				break;
			case NORTH:
				cubeBuilder.addCube( 5, 5, 0, 11, 11, 4 );
				break;
			case SOUTH:
				cubeBuilder.addCube( 5, 5, 12, 11, 11, 16 );
				break;
			case UP:
				cubeBuilder.addCube( 5, 12, 5, 11, 16, 11 );
				break;
			case WEST:
				cubeBuilder.addCube( 0, 5, 5, 4, 11, 11 );
				break;
		}
	}

	// Get all textures needed for building the actual cable quads
	public static List<ResourceLocation> getTextures()
	{
		List<ResourceLocation> locations = new ArrayList<>();

		for( CableCoreType coreType : CableCoreType.values() )
		{
			for( AEColor color : AEColor.values() )
			{
				locations.add( coreType.getTexture( color ) );
			}
		}

		for( AECableType cableType : AECableType.VALIDCABLES )
		{
			for( AEColor color : AEColor.values() )
			{
				locations.add( cableType.getConnectionTexture( color ) );
			}
		}

		Collections.addAll( locations, SmartCableTextures.SMART_CHANNELS_TEXTURES );

		return locations;
	}

	public TextureAtlasSprite getCoreTexture( CableCoreType coreType, AEColor color )
	{
		return coreTextures.get( coreType ).get( color );
	}

}
