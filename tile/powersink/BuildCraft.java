package appeng.tile.powersink;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.config.PowerUnits;
import appeng.core.AppEng;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.util.Platform;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;

@Interface(modid = "BuildCraftAPI|power", iface = "buildcraft.api.power.IPowerReceptor")
public abstract class BuildCraft extends AERootPoweredTile implements IPowerReceptor
{

	private class BCPerdition extends AETileEventHandler
	{

		final protected PowerHandler bcPowerHandler;

		public BCPerdition(IPowerReceptor te) {
			super( TileEventType.TICK, TileEventType.WORLD_NBT );
			bcPowerHandler = new PowerHandler( te, Type.MACHINE );
		}

		@Override
		public void Tick()
		{
			bcPowerHandler.update();
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
			bcPowerHandler.writeToNBT( data, "bcPowerHandler" );
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			bcPowerHandler.readFromNBT( data, "bcPowerHandler" );
		}

	};

	BCPerdition bcPowerWrapper;

	public BuildCraft() {
		if ( Platform.isServer() )
		{
			try
			{
				if ( Loader.isModLoaded( "BuildCraftAPI|power" ) )
				{
					if ( AppEng.instance.isIntegrationEnabled( "" ) )
						addNewHandler( bcPowerWrapper = new BCPerdition( this ) );
					bcPowerWrapper.bcPowerHandler.configure( 1, 380, 1.0f / 5.0f, 1000 );
				}
			}
			catch (Throwable t)
			{
				// ignore.. no bc?
			}
		}
	}

	@Override
	@Method(modid = "BuildCraftAPI|power")
	final public PowerReceiver getPowerReceiver(ForgeDirection side)
	{
		if ( internalCanAcceptPower && getPowerSides().contains( side ) )
			return bcPowerWrapper.bcPowerHandler.getPowerReceiver();
		return null;
	}

	@Override
	@Method(modid = "BuildCraftAPI|power")
	final public void doWork(PowerHandler workProvider)
	{
		float requred = (float) getExternalPowerDemand( PowerUnits.MJ );
		double failed = injectExternalPower( PowerUnits.MJ, bcPowerWrapper.bcPowerHandler.useEnergy( 0.0f, requred, true ) );
		if ( failed > 0.01 )
			bcPowerWrapper.bcPowerHandler.addEnergy( (float) failed );
	}

	@Override
	@Method(modid = "BuildCraftAPI|power")
	final public World getWorld()
	{
		return worldObj;
	}

}
