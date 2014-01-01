package appeng.parts.automation;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.client.texture.CableBusTextures;
import appeng.parts.PartBasicState;

public class PartFormationPlane extends PartBasicState
{

	public PartFormationPlane(ItemStack is) {
		super( PartFormationPlane.class, is );
	}

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 1, 1, 15, 15, 15, 16 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		int minX = 1;
		int minY = 1;
		int maxX = 15;
		int maxY = 15;

		ForgeDirection e = rh.getWorldX();
		ForgeDirection u = rh.getWorldY();

		TileEntity te = getHost().getTile();

		if ( isTransitionPlane( te.worldObj.getBlockTileEntity( x - e.offsetX, y - e.offsetY, z - e.offsetZ ), side ) )
			minX = 0;

		if ( isTransitionPlane( te.worldObj.getBlockTileEntity( x + e.offsetX, y + e.offsetY, z + e.offsetZ ), side ) )
			maxX = 16;

		if ( isTransitionPlane( te.worldObj.getBlockTileEntity( x - u.offsetX, y - u.offsetY, z - u.offsetZ ), side ) )
			minY = 0;

		if ( isTransitionPlane( te.worldObj.getBlockTileEntity( x + u.offsetX, y + u.offsetY, z + u.offsetZ ), side ) )
			maxY = 16;

		boolean isActive = (clientFlags & (POWERED_FLAG | CHANNEL_FLAG)) == (POWERED_FLAG | CHANNEL_FLAG);

		rh.useSimpliedRendering( x, y, z, this );
		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(),
				CableBusTextures.PartTransitionPlaneBack.getIcon(), isActive ? CableBusTextures.BlockFormPlaneOn.getIcon() : is.getIconIndex(),
				CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( minX, minY, 15, maxX, maxY, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartTransitionPlaneBack.getIcon(), isActive ? CableBusTextures.BlockFormPlaneOn.getIcon() : is.getIconIndex(),
				CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderBlock( x, y, z, renderer );

		renderLights( x, y, z, rh, renderer );
	}

	private boolean isTransitionPlane(TileEntity blockTileEntity, ForgeDirection side)
	{
		if ( blockTileEntity instanceof IPartHost )
		{
			IPart p = ((IPartHost) blockTileEntity).getPart( side );
			return p instanceof PartFormationPlane;
		}
		return false;
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		int minX = 1;
		int minY = 1;
		int maxX = 15;
		int maxY = 15;

		TileEntity te = getHost().getTile();

		int x = te.xCoord;
		int y = te.yCoord;
		int z = te.zCoord;

		ForgeDirection e = bch.getWorldX();
		ForgeDirection u = bch.getWorldY();

		if ( isTransitionPlane( te.worldObj.getBlockTileEntity( x - e.offsetX, y - e.offsetY, z - e.offsetZ ), side ) )
			minX = 0;

		if ( isTransitionPlane( te.worldObj.getBlockTileEntity( x + e.offsetX, y + e.offsetY, z + e.offsetZ ), side ) )
			maxX = 16;

		if ( isTransitionPlane( te.worldObj.getBlockTileEntity( x - u.offsetX, y - u.offsetY, z - u.offsetZ ), side ) )
			minY = 0;

		if ( isTransitionPlane( te.worldObj.getBlockTileEntity( x + u.offsetX, y + u.offsetY, z + u.offsetZ ), side ) )
			maxY = 16;

		bch.addBox( 5, 5, 14, 11, 11, 15 );
		bch.addBox( minX, minY, 15, maxX, maxY, 16 );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 1;
	}

}
