package appeng.parts.automation;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.client.texture.CableBusTextures;
import appeng.parts.PartBasicState;

public class PartExportBus extends PartBasicState
{

	public PartExportBus(ItemStack is) {
		super( PartExportBus.class, is );
	}

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{

		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 4, 4, 12, 12, 12, 14 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 6, 6, 15, 10, 10, 16 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 4, 4, 12, 12, 12, 14 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 6, 6, 15, 10, 10, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartMonitorBack.getIcon(), is.getIconIndex(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 6, 6, 11, 10, 10, 12 );
		rh.renderBlock( x, y, z, renderer );

		renderLights( x, y, z, rh, renderer );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox( 4, 4, 12, 12, 12, 14 );
		bch.addBox( 5, 5, 14, 11, 11, 15 );
		bch.addBox( 6, 6, 15, 10, 10, 16 );
		bch.addBox( 6, 6, 11, 10, 10, 12 );
	}

}
