package appeng.client.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public enum ExtraItemTextures
{
	White("White"), ItemPaintBallShimmer("ItemPaintBallShimmer"),

	ToolColorApplicatorTip_Medium("ToolColorApplicatorTip_Medium"),

	ToolColorApplicatorTip_Dark("ToolColorApplicatorTip_Dark"),

	ToolColorApplicatorTip_Light("ToolColorApplicatorTip_Light");

	final private String name;
	public IIcon IIcon;

	public static ResourceLocation GuiTexture(String string)
	{
		return new ResourceLocation( "appliedenergistics2", "textures/" + string );
	}

	public String getName()
	{
		return name;
	}

	private ExtraItemTextures(String name) {
		this.name = name;
	}

	public IIcon getIcon()
	{
		return IIcon;
	}

	public void registerIcon(TextureMap map)
	{
		IIcon = map.registerIcon( "appliedenergistics2:" + name );
	}

	@SideOnly(Side.CLIENT)
	public static IIcon getMissing()
	{
		return ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture( TextureMap.locationItemsTexture )).getAtlasSprite( "missingno" );
	}
}
