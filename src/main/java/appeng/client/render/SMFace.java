
package appeng.client.render;


import org.lwjgl.util.vector.Vector3f;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;


public class SMFace
{

	private final EnumFacing face;
	private final boolean isEdge;

	private final Vector3f to;
	private final Vector3f from;

	private final float[] uv;

	private final TextureAtlasSprite spite;

	private final int color;

	public SMFace(
			final EnumFacing face, final boolean isEdge,
			final int color,
			final Vector3f to,
			final Vector3f from,
			final float[] defUVs2,
			final TextureAtlasSprite iconUnwrapper )
	{
		this.color = color;
		this.face = face;
		this.isEdge = isEdge;
		this.to = to;
		this.from = from;
		this.uv = defUVs2;
		this.spite = iconUnwrapper;
	}

	public int getColor()
	{
		return this.color;
	}

	public EnumFacing getFace()
	{
		return this.face;
	}

	public Vector3f getFrom()
	{
		return this.from;
	}

	public Vector3f getTo()
	{
		return this.to;
	}

	public TextureAtlasSprite getSpite()
	{
		return this.spite;
	}

	public boolean isEdge()
	{
		return this.isEdge;
	}

}
