package appeng.parts.p2p;

import java.util.Collection;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.PowerUnits;
import appeng.api.config.TunnelType;
import appeng.api.implementations.IMemoryCard;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;
import appeng.me.cache.P2PCache;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.parts.PartBasicState;
import appeng.util.Platform;

public class PartP2PTunnel<T extends PartP2PTunnel> extends PartBasicState
{

	public boolean output;
	public long freq;
	TunnelCollection type = new TunnelCollection<T>( null, getClass() );

	public PartP2PTunnel(ItemStack is) {
		super( PartP2PTunnel.class, is );
		if ( getClass() == PartP2PTunnel.class )
			throw new RuntimeException( "Don't construct the root tunnel!" );
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		data.setBoolean( "output", output );
		data.setLong( "freq", freq );
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		output = data.getBoolean( "output" );
		freq = data.getLong( "freq" );
	}

	@Override
	public boolean onActivate(EntityPlayer player, Vec3 pos)
	{
		ItemStack is = player.inventory.getCurrentItem();

		TunnelType tt = AEApi.instance().registries().p2pTunnel().getTunnelTypeByItem( is );
		if ( is != null && is.getItem() instanceof IMemoryCard )
		{
			IMemoryCard mc = (IMemoryCard) is.getItem();
			NBTTagCompound data = mc.getData( is );

			ItemStack newType = ItemStack.loadItemStackFromNBT( data );
			long freq = data.getLong( "freq" );

			getHost().removePart( side );
			ForgeDirection dir = getHost().addPart( newType, side );
			IPart newBus = getHost().getPart( dir );

			if ( newBus instanceof PartP2PTunnel )
			{
				PartP2PTunnel newTunnel = (PartP2PTunnel) newBus;
				newTunnel.output = true;
				newTunnel.onChange();

				try
				{
					P2PCache p2p = newTunnel.proxy.getP2P();
					p2p.updateFreq( newTunnel, freq );
				}
				catch (GridAccessException e)
				{
					// :P
				}
			}

		}
		else if ( tt != null ) // attune-ment
		{
			ItemStack newType = null;

			switch (tt)
			{

			case BC_POWER:
				newType = AEApi.instance().parts().partP2PTunnelMJ.stack( 1 );
				break;

			case FLUID:
				newType = AEApi.instance().parts().partP2PTunnelLiquids.stack( 1 );
				break;

			case IC2_POWER:
				newType = AEApi.instance().parts().partP2PTunnelEU.stack( 1 );
				break;

			case ITEM:
				newType = AEApi.instance().parts().partP2PTunnelItems.stack( 1 );
				break;

			case ME:
				newType = AEApi.instance().parts().partP2PTunnelME.stack( 1 );
				break;

			case REDSTONE:
				newType = AEApi.instance().parts().partP2PTunnelRedstone.stack( 1 );
				break;

			}

			if ( newType != null && !Platform.isSameItem( newType, this.is ) )
			{
				boolean oldOutput = output;
				long myFreq = freq;

				getHost().removePart( side );
				ForgeDirection dir = getHost().addPart( newType, side );
				IPart newBus = getHost().getPart( dir );

				if ( newBus instanceof PartP2PTunnel )
				{
					PartP2PTunnel newTunnel = (PartP2PTunnel) newBus;
					newTunnel.output = oldOutput;
					newTunnel.onChange();

					try
					{
						P2PCache p2p = newTunnel.proxy.getP2P();
						p2p.updateFreq( newTunnel, myFreq );
					}
					catch (GridAccessException e)
					{
						// :P
					}
				}

			}
		}

		return false;
	}

	@Override
	public boolean onShiftActivate(EntityPlayer player, Vec3 pos)
	{
		ItemStack is = player.inventory.getCurrentItem();
		if ( is != null && is.getItem() instanceof IMemoryCard )
		{
			IMemoryCard mc = (IMemoryCard) is.getItem();
			NBTTagCompound data = new NBTTagCompound();

			long newFreq = freq;
			output = false;

			if ( output || freq == 0 )
			{
				newFreq = System.currentTimeMillis();
				try
				{
					proxy.getP2P().updateFreq( this, newFreq );
				}
				catch (GridAccessException e)
				{
					// :P
				}
			}

			onChange();

			ItemStack p2pItem = getItemStack( PartItemStack.Wrench );
			String type = p2pItem.getUnlocalizedName();

			p2pItem.writeToNBT( data );
			data.setLong( "freq", freq );

			mc.setMemoryCardContents( is, type + ".name", data );
			return true;
		}
		return false;
	}

	public ItemStack getItemStack(PartItemStack type)
	{
		if ( type == PartItemStack.World || type == PartItemStack.Network || type == PartItemStack.Wrench )
			return super.getItemStack( type );

		return AEApi.instance().parts().partP2PTunnelME.stack( 1 );
	}

	public TunnelCollection<T> getCollection(Collection<PartP2PTunnel> collection)
	{
		type.setSource( collection );
		return type;
	}

	public T getInput()
	{
		PartP2PTunnel tunn;
		try
		{
			tunn = proxy.getP2P().getInput( freq );
			if ( getClass().isInstance( tunn ) )
				return (T) tunn;
		}
		catch (GridAccessException e)
		{
			// :P
		}
		return null;
	}

	public TunnelCollection<T> getOutputs() throws GridAccessException
	{
		return (TunnelCollection<T>) proxy.getP2P().getOutputs( freq, getClass() );
	}

	public void onChange()
	{

	}

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( getTypeTexture() );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderInventoryBox( renderer );

		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.BlockP2PTunnel2.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderInventoryBox( renderer );
	}

	protected Icon getTypeTexture()
	{
		return AEApi.instance().blocks().blockQuartz.block().getIcon( 0, 0 );
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.useSimpliedRendering( x, y, z, this );
		rh.setTexture( getTypeTexture() );

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

	protected void QueueTunnelDrain(PowerUnits mj, float f)
	{
		// TODO Auto-generated method stub

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
