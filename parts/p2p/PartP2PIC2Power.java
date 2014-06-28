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
	double OutputEnergyA;
	double OutputEnergyB;

	// two packet buffering...
	double OutputVoltageA;
	double OutputVoltageB;

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT( tag );
		tag.setDouble( "OutputPacket", OutputEnergyA );
		tag.setDouble( "OutputPacket2", OutputEnergyB );
		tag.setDouble( "OutputVoltageA", OutputVoltageA );
		tag.setDouble( "OutputVoltageB", OutputVoltageB );
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT( tag );
		OutputEnergyA = tag.getDouble( "OutputPacket" );
		OutputEnergyB = tag.getDouble( "OutputPacket2" );
		OutputVoltageA = tag.getDouble( "OutputVoltageA" );
		OutputVoltageB = tag.getDouble( "OutputVoltageB" );
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
	public double getDemandedEnergy()
	{
		if ( output )
			return 0;

		try
		{
			for (PartP2PIC2Power t : getOutputs())
			{
				if ( t.OutputEnergyA <= 0.0001 || t.OutputEnergyB <= 0.0001 )
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
	public void onTunnelNetworkChange()
	{
		getHost().notifyNeighbors();
	}

	@Override
	public void onTunnelConfigChange()
	{
		getHost().partChanged();
	}

	public float getPowerDrainPerTick()
	{
		return 0.5f;
	};

	@Override
	public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage)
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
			if ( o.OutputEnergyA <= 0.01 )
				Options.add( o );
		}

		if ( Options.isEmpty() )
		{
			for (PartP2PIC2Power o : outs)
				if ( o.OutputEnergyB <= 0.01 )
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

		if ( x != null && x.OutputEnergyA <= 0.001 )
		{
			QueueTunnelDrain( PowerUnits.EU, amount );
			x.OutputEnergyA = amount;
			x.OutputVoltageA = voltage;
			return 0;
		}

		if ( x != null && x.OutputEnergyB <= 0.001 )
		{
			QueueTunnelDrain( PowerUnits.EU, amount );
			x.OutputEnergyB = amount;
			x.OutputVoltageB = voltage;
			return 0;
		}

		return amount;
	}

	@Override
	public int getSinkTier()
	{
		return 4;
	}

	@Override
	public double getOfferedEnergy()
	{
		if ( output )
			return OutputEnergyA;
		return 0;
	}

	@Override
	public void drawEnergy(double amount)
	{
		OutputEnergyA -= amount;
		if ( OutputEnergyA < 0.001 )
		{
			OutputEnergyA = OutputEnergyB;
			OutputEnergyB = 0;

			OutputVoltageA = OutputVoltageB;
			OutputVoltageB = 0;
		}
	}

	@Override
	public int getSourceTier()
	{
		if ( output )
			return calculateTierFromVoltage( OutputVoltageA );
		return 4;
	}

	private int calculateTierFromVoltage(double outputVoltageA2)
	{
		for (int x = 8; x >= 0; x--)
		{
			double top = ic2.api.energy.EnergyNet.instance.getPowerFromTier( x );
			if ( outputVoltageA2 > top && top > 0 )
				return x + 1;
		}
		return 0;
	}

}
