package appeng.client.render;

import javax.vecmath.Vector3f;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

public class SMFace
{

	public final EnumFacing face;
	public final boolean isEdge;

	public final Vector3f to;
	public final Vector3f from;
	
	public final float[] uv;
	
	public final TextureAtlasSprite spite;
	
	public final int color;
	
	public SMFace(
			final EnumFacing face , final boolean isEdge,
			final int color,
			final Vector3f to,
			final Vector3f from,
			final float[] defUVs2,
			final TextureAtlasSprite iconUnwrapper )
	{
		this.color = color;
		this.face=face;
		this.isEdge = isEdge;
		this.to=to;
		this.from=from;
		this.uv = defUVs2;
		this.spite = iconUnwrapper;
	}

}
