package appeng.facade;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.core.AppEng;
import appeng.integration.abstraction.IBC;
import appeng.items.parts.ItemFacade;

public class FacadeContainer implements IFacadeContainer
{

	final private IFacadePart facades[] = new FacadePart[6];

	public void writeToStream(ByteBuf out) throws IOException
	{
		int facadeSides = 0;
		for (int x = 0; x < facades.length; x++)
		{
			if ( getFacade( ForgeDirection.getOrientation( x ) ) != null )
				facadeSides = facadeSides | (1 << x);
		}
		out.writeByte( (byte) facadeSides );

		for (int x = 0; x < facades.length; x++)
		{
			IFacadePart part = getFacade( ForgeDirection.getOrientation( x ) );
			if ( part != null )
			{
				int itemID = Item.getIdFromItem( part.getItem() );
				int dmgValue = part.getItemDamage();
				out.writeInt( itemID * (part.isBC() ? -1 : 1) );
				out.writeInt( dmgValue );
			}
		}
	}

	public boolean readFromStream(ByteBuf out) throws IOException
	{
		int facadeSides = out.readByte();

		boolean changed = false;

		int ids[] = new int[2];
		for (int x = 0; x < facades.length; x++)
		{
			ForgeDirection side = ForgeDirection.getOrientation( x );
			int ix = (1 << x);
			if ( (facadeSides & ix) == ix )
			{
				ids[0] = out.readInt();
				ids[1] = out.readInt();
				boolean isBC = ids[0] < 0;
				ids[0] = Math.abs( ids[0] );

				if ( isBC && AppEng.instance.isIntegrationEnabled( "BC" ) )
				{
					IBC bc = (IBC) AppEng.instance.getIntegration( "BC" );
					changed = changed || facades[x] == null;
					facades[x] = bc.createFacadePart( (Block) Block.blockRegistry.getObjectById( ids[0] ), ids[1], side );
				}
				else if ( !isBC )
				{
					ItemFacade ifa = (ItemFacade) AEApi.instance().items().itemFacade.item();
					ItemStack facade = ifa.createFromInts( ids );
					if ( facade != null )
					{
						changed = changed || facades[x] == null;
						facades[x] = ifa.createPartFromItemStack( facade, side );
					}
				}
			}
			else
			{
				changed = changed || facades[x] != null;
				facades[x] = null;
			}
		}

		return changed;
	}

	public void readFromNBT(NBTTagCompound c)
	{
		for (int x = 0; x < facades.length; x++)
		{
			facades[x] = null;

			NBTTagCompound t = c.getCompoundTag( "facade:" + x );
			if ( t != null )
			{
				ItemStack is = ItemStack.loadItemStackFromNBT( t );
				if ( is != null )
				{
					Item i = is.getItem();
					if ( i instanceof IFacadeItem )
						facades[x] = ((IFacadeItem) i).createPartFromItemStack( is, ForgeDirection.getOrientation( x ) );
					else
					{
						if ( AppEng.instance.isIntegrationEnabled( "BC" ) )
						{
							IBC bc = (IBC) AppEng.instance.getIntegration( "BC" );
							if ( bc.isFacade( is ) )
								facades[x] = bc.createFacadePart( is, ForgeDirection.getOrientation( x ) );
						}
					}
				}
			}
		}
	}

	public void writeToNBT(NBTTagCompound c)
	{
		for (int x = 0; x < facades.length; x++)
		{
			if ( facades[x] != null )
			{
				NBTTagCompound data = new NBTTagCompound();
				facades[x].getItemStack().writeToNBT( data );
				c.setTag( "facade:" + x, data );
			}
		}
	}

	@Override
	public boolean addFacade(IFacadePart a)
	{
		if ( getFacade( a.getSide() ) == null )
		{
			facades[a.getSide().ordinal()] = a;
			return true;
		}
		return false;
	}

	@Override
	public void removeFacade(IPartHost host, ForgeDirection side)
	{
		if ( side != null && side != ForgeDirection.UNKNOWN )
		{
			if ( facades[side.ordinal()] != null )
			{
				facades[side.ordinal()] = null;
				if ( host != null )
					host.markForUpdate();
			}
		}
	}

	@Override
	public IFacadePart getFacade(ForgeDirection s)
	{
		return facades[s.ordinal()];
	}

	public boolean isEmpty()
	{
		for (int x = 0; x < facades.length; x++)
			if ( facades[x] != null )
				return false;
		return true;
	}

	public void rotateLeft()
	{
		IFacadePart newfacades[] = new FacadePart[6];

		newfacades[ForgeDirection.UP.ordinal()] = facades[ForgeDirection.UP.ordinal()];
		newfacades[ForgeDirection.DOWN.ordinal()] = facades[ForgeDirection.DOWN.ordinal()];

		newfacades[ForgeDirection.EAST.ordinal()] = facades[ForgeDirection.NORTH.ordinal()];
		newfacades[ForgeDirection.SOUTH.ordinal()] = facades[ForgeDirection.EAST.ordinal()];
		newfacades[ForgeDirection.WEST.ordinal()] = facades[ForgeDirection.SOUTH.ordinal()];
		newfacades[ForgeDirection.NORTH.ordinal()] = facades[ForgeDirection.WEST.ordinal()];

		for (int x = 0; x < facades.length; x++)
			facades[x] = newfacades[x];
	}
}
