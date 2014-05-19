package appeng.parts.p2p;

import java.util.LinkedList;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.PowerUnits;
import appeng.api.config.TunnelType;
import appeng.core.AppEng;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.transformer.annotations.integration.Interface;
import appeng.transformer.annotations.integration.InterfaceList;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@InterfaceList(value = { @Interface(iface = "ic2.api.energy.tile.IEnergySink", iname = "IC2"),
		@Interface(iface = "ic2.api.energy.tile.IEnergySource", iname = "IC2") })
public class PartP2PIC2Power extends PartP2PTunnel<PartP2PIC2Power> implements ic2.api.energy.tile.IEnergySink, ic2.api.energy.tile.IEnergySource
{

	public TunnelType getTunnelType()
	{
		return TunnelType.IC2_POWER;
	}

	public PartP2PIC2Power(ItemStack is) {
		super( is );

		if ( !AppEng.instance.isIntegrationEnabled( "IC2" ) )
			throw new RuntimeException( "IC2 Not installed!" );
	}

	// two packet buffering...
	double OutputPacketA;
	double OutputPacketB;

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT( tag );
		tag.setDouble( "OutputPacket", OutputPacketA );
		tag.setDouble( "OutputPacket2", OutputPacketB );
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT( tag );
		OutputPacketA = tag.getDouble( "OutputPacket" );
		OutputPacketB = tag.getDouble( "OutputPacket2" );
	}

	@SideOnly(Side.CLIENT)
	public IIcon getTypeTexture()
	{
		return Blocks.diamond_block.getBlockTextureFromSide( 0 );
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		if ( !output )
			return direction.equals( side );
		return false;
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction)
	{
		if ( output )
			return direction.equals( side );
		return false;
	}

	@Override
	public double demandedEnergyUnits()
	{
		if ( output )
			return 0;

		try
		{
			for (PartP2PIC2Power t : getOutputs())
			{
				if ( t.OutputPacketA <= 0.0001 || t.OutputPacketB <= 0.0001 )
				{
					return 2048;
				}
			}
		}
		catch (GridAccessException e)
		{
			return 0;
		}

		return 0;
	}

	@Override
	public void onChange()
	{
		getHost().partChanged();
	}

	public float getPowerDrainPerTick()
	{
		return 0.5f;
	};

	@Override
	public double injectEnergyUnits(ForgeDirection directionFrom, double amount)
	{
		TunnelCollection<PartP2PIC2Power> outs;
		try
		{
			outs = getOutputs();
		}
		catch (GridAccessException e)
		{
			return amount;
		}

		if ( outs.isEmpty() )
			return amount;

		LinkedList<PartP2PIC2Power> Options = new LinkedList();
		for (PartP2PIC2Power o : outs)
		{
			if ( o.OutputPacketA <= 0.01 )
				Options.add( o );
		}

		if ( Options.isEmpty() )
		{
			for (PartP2PIC2Power o : outs)
				if ( o.OutputPacketB <= 0.01 )
					Options.add( o );
		}

		if ( Options.isEmpty() )
		{
			for (PartP2PIC2Power o : outs)
				Options.add( o );
		}

		if ( Options.isEmpty() )
			return amount;

		PartP2PIC2Power x = (PartP2PIC2Power) Platform.pickRandom( Options );

		if ( x != null && x.OutputPacketA <= 0.001 )
		{
			QueueTunnelDrain( PowerUnits.EU, amount );
			x.OutputPacketA = amount;
			return 0;
		}

		if ( x != null && x.OutputPacketB <= 0.001 )
		{
			QueueTunnelDrain( PowerUnits.EU, amount );
			x.OutputPacketB = amount;
			return 0;
		}

		return amount;
	}

	@Override
	public int getMaxSafeInput()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public double getOfferedEnergy()
	{
		if ( output )
			return OutputPacketA;
		return 0;
	}

	@Override
	public void drawEnergy(double amount)
	{
		OutputPacketA -= amount;
		if ( OutputPacketA < 0.001 )
		{
			OutputPacketA = OutputPacketB;
			OutputPacketB = 0;
		}
	}

}
