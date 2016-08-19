
package appeng.client.render.model.pipeline;


import java.lang.reflect.Field;

import com.google.common.base.Throwables;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.relauncher.ReflectionHelper;


public final class QuadVertexData
{

	private static final Field unpackedData = ReflectionHelper.findField( UnpackedBakedQuad.class, "unpackedData" );

	private static float[][][] unpackedData( UnpackedBakedQuad quad )
	{
		try
		{
			return (float[][][]) unpackedData.get( quad );
		}
		catch( Exception e )
		{
			throw Throwables.propagate( e );
		}
	}

	private VertexFormat format;
	private float[][][] data;

	private int tintIndex;
	private EnumFacing face;
	private TextureAtlasSprite sprite;
	protected boolean applyDiffuseLighting;

	public QuadVertexData( VertexFormat format, float[][][] data, int tintIndex, EnumFacing face, TextureAtlasSprite sprite, boolean applyDiffuseLighting )
	{
		this.format = format;
		this.data = data;
		this.tintIndex = tintIndex;
		this.face = face;
		this.sprite = sprite;
		this.applyDiffuseLighting = applyDiffuseLighting;
	}

	public QuadVertexData( UnpackedBakedQuad quad )
	{
		this( quad.getFormat(), unpackedData( quad ), quad.getTintIndex(), quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting() );
	}

	public UnpackedBakedQuad toQuad()
	{
		return new UnpackedBakedQuad( data, tintIndex, face, sprite, applyDiffuseLighting, format );
	}

	public VertexFormat getFormat()
	{
		return format;
	}

	public void setFormat( VertexFormat format )
	{
		this.format = format;
	}

	public float[][][] getData()
	{
		return data;
	}

	public void setData( float[][][] data )
	{
		this.data = data;
	}

	public int getTintIndex()
	{
		return tintIndex;
	}

	public void setTintIndex( int tintIndex )
	{
		this.tintIndex = tintIndex;
	}

	public EnumFacing getFace()
	{
		return face;
	}

	public void setFace( EnumFacing face )
	{
		this.face = face;
	}

	public TextureAtlasSprite getSprite()
	{
		return sprite;
	}

	public void setSprite( TextureAtlasSprite sprite )
	{
		this.sprite = sprite;
	}

	public boolean shouldApplyDiffuseLighting()
	{
		return applyDiffuseLighting;
	}

	public void setApplyDiffuseLighting( boolean applyDiffuseLighting )
	{
		this.applyDiffuseLighting = applyDiffuseLighting;
	}

}
