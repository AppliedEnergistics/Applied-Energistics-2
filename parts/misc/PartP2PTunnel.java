package appeng.parts.misc;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.client.texture.CableBusTextures;
import appeng.parts.PartBasicState;

public class PartP2PTunnel extends PartBasicState
{

	public boolean output;
	public long freq;

	public PartP2PTunnel(ItemStack is) {
		super( PartP2PTunnel.class, is );
	}

	public void onChange()
	{

	}

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( AEApi.instance().blocks().blockQuartz.block().getIcon( 0, 0 ) );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderInventoryBox( renderer );

		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.BlockP2PTunnel2.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( AEApi.instance().blocks().blockQuartz.block().getIcon( 0, 0 ) );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.BlockP2PTunnel2.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 3, 3, 13, 13, 13, 14 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.BlockP2PTunnel3.getIcon() );

		rh.setBounds( 6, 5, 12, 10, 11, 13 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 5, 6, 12, 11, 10, 13 );
		rh.renderBlock( x, y, z, renderer );

		renderLights( x, y, z, rh, renderer );
	}

	@Override
	public void setColors(boolean hasChan, boolean hasPower)
	{
		super.setColors( hasChan, hasPower );
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox( 2, 2, 14, 14, 14, 16 );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 1;
	}

}
