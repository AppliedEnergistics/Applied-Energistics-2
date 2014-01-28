package appeng.tile.powersink;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.config.PowerUnits;
import appeng.core.AppEng;
import appeng.integration.abstraction.IBC;
import appeng.util.Platform;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;

@Interface(modid = "BuildCraftAPI|power", iface = "buildcraft.api.power.IPowerReceptor")
public abstract class BuildCraft extends AERootPoweredTile implements IPowerReceptor
{

	BaseBCperdition bcPowerWrapper;

	public BuildCraft() {
		if ( Platform.isServer() )
		{
			try
			{
				if ( Loader.isModLoaded( "BuildCraftAPI|power" ) )
				{
					IBC bcIntegration = (IBC) AppEng.instance.getIntegration( "BC" );
					if ( bcIntegration != null )
					{
						addNewHandler( bcPowerWrapper = bcIntegration.createPerdition( this ) );
						bcPowerWrapper.configure( 1, 380, 1.0f / 5.0f, 1000 );
					}
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
		if ( internalCanAcceptPower && getPowerSides().contains( side ) && bcPowerWrapper != null )
			return bcPowerWrapper.getPowerReceiver();
		return null;
	}

	@Override
	@Method(modid = "BuildCraftAPI|power")
	final public void doWork(PowerHandler workProvider)
	{
		float requred = (float) getExternalPowerDemand( PowerUnits.MJ );
		double failed = injectExternalPower( PowerUnits.MJ, bcPowerWrapper.useEnergy( 0.0f, requred, true ) );
		if ( failed > 0.01 )
			bcPowerWrapper.addEnergy( (float) failed );
	}

	@Override
	@Method(modid = "BuildCraftAPI|power")
	final public World getWorld()
	{
		return worldObj;
	}

}
