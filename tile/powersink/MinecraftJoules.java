package appeng.tile.powersink;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.PowerUnits;
import appeng.core.AppEng;
import appeng.integration.abstraction.IMJ;
import appeng.integration.abstraction.helpers.BaseMJperdition;
import appeng.transformer.annotations.integration.Interface;
import appeng.transformer.annotations.integration.Method;
import appeng.util.Platform;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;

@Interface(iname = "BC", iface = "buildcraft.api.power.IPowerReceptor")
public abstract class MinecraftJoules extends AERootPoweredTile implements IPowerReceptor
{

	BaseMJperdition bcPowerWrapper;

	public MinecraftJoules() {
		if ( Platform.isServer() )
		{
			try
			{
				if ( AppEng.instance.isIntegrationEnabled( "MJ" ) )
				{
					IMJ mjIntegration = (IMJ) AppEng.instance.getIntegration( "MJ" );
					if ( mjIntegration != null )
					{
						addNewHandler( bcPowerWrapper = (BaseMJperdition) mjIntegration.createPerdition( this ) );
						if ( bcPowerWrapper != null )
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
	@Method(iname = "BC")
	final public PowerReceiver getPowerReceiver(ForgeDirection side)
	{
		if ( internalCanAcceptPower && getPowerSides().contains( side ) && bcPowerWrapper != null )
			return bcPowerWrapper.getPowerReceiver();
		return null;
	}

	@Override
	@Method(iname = "BC")
	final public void doWork(PowerHandler workProvider)
	{
		float requred = (float) getExternalPowerDemand( PowerUnits.MJ );
		double failed = injectExternalPower( PowerUnits.MJ, bcPowerWrapper.useEnergy( 0.0f, requred, true ) );
		if ( failed > 0.01 )
			bcPowerWrapper.addEnergy( (float) failed );
	}

	@Override
	@Method(iname = "BC")
	final public World getWorld()
	{
		return worldObj;
	}

}
