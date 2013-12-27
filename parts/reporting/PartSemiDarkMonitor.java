package appeng.parts.reporting;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.parts.IPartRenderHelper;
import appeng.client.texture.CableBusTextures;

public class PartSemiDarkMonitor extends PartMonitor
{

	public PartSemiDarkMonitor(ItemStack is) {
		super( PartSemiDarkMonitor.class, is );

		notLightSource = false;
	}

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setBounds( 2, 2, 14, 14, 14, 16 );

		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				frontSolid.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );
		rh.renderInventoryBox( renderer );

		int light = getColor().whiteVariant;
		int dark = getColor().mediumVariant;
		rh.setInvColor( (((((light >> 16) & 0xff) + ((dark >> 16) & 0xff)) / 2) << 16) | (((((light >> 8) & 0xff) + ((dark >> 8) & 0xff)) / 2) << 8)
				| ((((light) & 0xff) + ((dark) & 0xff)) / 2) );
		rh.renderInventoryFace( frontBright.getIcon(), ForgeDirection.SOUTH, renderer );

		rh.setBounds( 4, 4, 13, 12, 12, 14 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				frontSolid.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderBlock( x, y, z, renderer );

		if ( getLightLevel() > 0 )
		{
			int l = 13;
			Tessellator.instance.setBrightness( l << 20 | l << 4 );
		}

		int light = getColor().whiteVariant;
		int dark = getColor().mediumVariant;
		Tessellator.instance.setColorOpaque( (((light >> 16) & 0xff) + ((dark >> 16) & 0xff)) / 2, (((light >> 8) & 0xff) + ((dark >> 8) & 0xff)) / 2,
				(((light) & 0xff) + ((dark) & 0xff)) / 2 );
		rh.renderFace( x, y, z, frontBright.getIcon(), ForgeDirection.SOUTH, renderer );

		rh.setBounds( 4, 4, 13, 12, 12, 14 );
		rh.renderBlock( x, y, z, renderer );
	}

}
