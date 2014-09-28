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
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IBC;
import appeng.items.parts.ItemFacade;
import appeng.parts.CableBusStorage;

public class FacadeContainer implements IFacadeContainer
{

	final int facades = 6;
	final CableBusStorage storage;

	public FacadeContainer(CableBusStorage cbs) {
		storage = cbs;
	}

	public void writeToStream(ByteBuf out) throws IOException
	{
		int facadeSides = 0;
		for (int x = 0; x < facades; x++)
		{
			if ( getFacade( ForgeDirection.getOrientation( x ) ) != null )
				facadeSides = facadeSides | (1 << x);
		}
		out.writeByte( (byte) facadeSides );

		for (int x = 0; x < facades; x++)
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
		for (int x = 0; x < facades; x++)
		{
			ForgeDirection side = ForgeDirection.getOrientation( x );
			int ix = (1 << x);
			if ( (facadeSides & ix) == ix )
			{
				ids[0] = out.readInt();
				ids[1] = out.readInt();
				boolean isBC = ids[0] < 0;
				ids[0] = Math.abs( ids[0] );

				if ( isBC && AppEng.instance.isIntegrationEnabled( IntegrationType.BC ) )
				{
					IBC bc = (IBC) AppEng.instance.getIntegration( IntegrationType.BC );
					changed = changed || storage.getFacade( x ) == null;
					storage.setFacade( x, bc.createFacadePart( (Block) Block.blockRegistry.getObjectById( ids[0] ), ids[1], side ) );
				}
				else if ( !isBC )
				{
					ItemFacade ifa = (ItemFacade) AEApi.instance().items().itemFacade.item();
					ItemStack facade = ifa.createFromIDs( ids );
					if ( facade != null )
					{
						changed = changed || storage.getFacade( x ) == null;
						storage.setFacade( x, ifa.createPartFromItemStack( facade, side ) );
					}
				}
			}
			else
			{
				changed = changed || storage.getFacade( x ) != null;
				storage.setFacade( x, null );
			}
		}

		return changed;
	}

	public void readFromNBT(NBTTagCompound c)
	{
		for (int x = 0; x < facades; x++)
		{
			storage.setFacade( x, null );

			NBTTagCompound t = c.getCompoundTag( "facade:" + x );
			if ( t != null )
			{
				ItemStack is = ItemStack.loadItemStackFromNBT( t );
				if ( is != null )
				{
					Item i = is.getItem();
					if ( i instanceof IFacadeItem )
						storage.setFacade( x, ((IFacadeItem) i).createPartFromItemStack( is, ForgeDirection.getOrientation( x ) ) );
					else
					{
						if ( AppEng.instance.isIntegrationEnabled( IntegrationType.BC ) )
						{
							IBC bc = (IBC) AppEng.instance.getIntegration( IntegrationType.BC );
							if ( bc.isFacade( is ) )
								storage.setFacade( x, bc.createFacadePart( is, ForgeDirection.getOrientation( x ) ) );
						}
					}
				}
			}
		}
	}

	public void writeToNBT(NBTTagCompound c)
	{
		for (int x = 0; x < facades; x++)
		{
			if ( storage.getFacade( x ) != null )
			{
				NBTTagCompound data = new NBTTagCompound();
				storage.getFacade( x ).getItemStack().writeToNBT( data );
				c.setTag( "facade:" + x, data );
			}
		}
	}

	@Override
	public boolean addFacade(IFacadePart a)
	{
		if ( getFacade( a.getSide() ) == null )
		{
			storage.setFacade( a.getSide().ordinal(), a );
			return true;
		}
		return false;
	}

	@Override
	public void removeFacade(IPartHost host, ForgeDirection side)
	{
		if ( side != null && side != ForgeDirection.UNKNOWN )
		{
			if ( storage.getFacade( side.ordinal() ) != null )
			{
				storage.setFacade( side.ordinal(), null );
				if ( host != null )
					host.markForUpdate();
			}
		}
	}

	@Override
	public IFacadePart getFacade(ForgeDirection s)
	{
		return storage.getFacade( s.ordinal() );
	}

	public boolean isEmpty()
	{
		for (int x = 0; x < facades; x++)
			if ( storage.getFacade( x ) != null )
				return false;
		return true;
	}

	public void rotateLeft()
	{
		IFacadePart newFacades[] = new FacadePart[6];

		newFacades[ForgeDirection.UP.ordinal()] = storage.getFacade( ForgeDirection.UP.ordinal() );
		newFacades[ForgeDirection.DOWN.ordinal()] = storage.getFacade( ForgeDirection.DOWN.ordinal() );

		newFacades[ForgeDirection.EAST.ordinal()] = storage.getFacade( ForgeDirection.NORTH.ordinal() );
		newFacades[ForgeDirection.SOUTH.ordinal()] = storage.getFacade( ForgeDirection.EAST.ordinal() );

		newFacades[ForgeDirection.WEST.ordinal()] = storage.getFacade( ForgeDirection.SOUTH.ordinal() );
		newFacades[ForgeDirection.NORTH.ordinal()] = storage.getFacade( ForgeDirection.WEST.ordinal() );

		for (int x = 0; x < facades; x++)
			storage.setFacade( x, newFacades[x] );
	}
}
