
package appeng.fluids.util;


import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AELog;
import appeng.util.Platform;


public class AEFluidInventory implements IAEFluidTank
{
	private final IAEFluidStack[] fluids;
	private final IAEFluidInventory handler;

	public AEFluidInventory( final IAEFluidInventory handler, final int capacity )
	{
		this.fluids = new IAEFluidStack[capacity];
		this.handler = handler;
	}

	@Override
	public void setFluidInSlot( final int slot, final IAEFluidStack fluid )
	{
		if( slot >= 0 && slot < this.getSlots() )
		{
			if( Objects.equals( this.fluids[slot], fluid ) )
			{
				if( fluid != null && fluid.getStackSize() != this.fluids[slot].getStackSize() )
				{
					this.fluids[slot].setStackSize( fluid.getStackSize() );
					this.onContentChanged( slot );
				}
			}
			else
			{
				this.fluids[slot] = fluid == null ? null : fluid.copy();
				this.onContentChanged( slot );
			}
		}
	}

	private void onContentChanged( int slot )
	{
		if( this.handler != null && Platform.isServer() )
		{
			this.handler.onFluidInventoryChanged( this, slot );
		}
	}

	@Override
	public IAEFluidStack getFluidInSlot( final int slot )
	{
		if( slot >= 0 && slot < this.getSlots() )
		{
			return this.fluids[slot];
		}
		return null;
	}

	@Override
	public int getSlots()
	{
		return fluids.length;
	}

	public void writeToNBT( final NBTTagCompound data, final String name )
	{
		final NBTTagCompound c = new NBTTagCompound();
		this.writeToNBT( c );
		data.setTag( name, c );
	}

	private void writeToNBT( final NBTTagCompound target )
	{
		for( int x = 0; x < this.fluids.length; x++ )
		{
			try
			{
				final NBTTagCompound c = new NBTTagCompound();

				if( this.fluids[x] != null )
				{
					this.fluids[x].writeToNBT( c );
				}

				target.setTag( "#" + x, c );
			}
			catch( final Exception ignored )
			{
			}
		}
	}

	public void readFromNBT( final NBTTagCompound data, final String name )
	{
		final NBTTagCompound c = data.getCompoundTag( name );
		if( c != null )
		{
			this.readFromNBT( c );
		}
	}

	private void readFromNBT( final NBTTagCompound target )
	{
		for( int x = 0; x < this.fluids.length; x++ )
		{
			try
			{
				final NBTTagCompound c = target.getCompoundTag( "#" + x );

				if( c != null )
				{
					this.fluids[x] = AEFluidStack.fromNBT( c );
				}
			}
			catch( final Exception e )
			{
				AELog.debug( e );
			}
		}
	}
}
