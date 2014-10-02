package appeng.client.texture;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MissingIcon implements IIcon
{

	final boolean isBlock;

	public MissingIcon(Object forWhat) {
		isBlock = forWhat instanceof Block;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getMissing()
	{
		return ((TextureMap) Minecraft.getMinecraft().getTextureManager()
				.getTexture( isBlock ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture )).getAtlasSprite( "missingno" );
	}

	@Override
	public int getIconWidth()
	{
		return getMissing().getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return getMissing().getIconHeight();
	}

	@Override
	public float getMinU()
	{
		return getMissing().getMinU();
	}

	@Override
	public float getMaxU()
	{
		return getMissing().getMaxU();
	}

	@Override
	public float getInterpolatedU(double var1)
	{
		return getMissing().getInterpolatedU( var1 );
	}

	@Override
	public float getMinV()
	{
		return getMissing().getMinV();
	}

	@Override
	public float getMaxV()
	{
		return getMissing().getMaxV();
	}

	@Override
	public float getInterpolatedV(double var1)
	{
		return getMissing().getInterpolatedV( var1 );
	}

	@Override
	public String getIconName()
	{
		return getMissing().getIconName();
	}

}
