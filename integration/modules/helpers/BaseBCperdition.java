package appeng.integration.modules.helpers;

import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import cpw.mods.fml.common.Optional.Method;

public abstract class BaseBCperdition extends AETileEventHandler
{

	public BaseBCperdition() {
		super( TileEventType.TICK, TileEventType.WORLD_NBT );
	}

	@Method(modid = "BuildCraftAPI|power")
	public abstract PowerReceiver getPowerReceiver();

	public abstract double useEnergy(float f, float requred, boolean b);

	public abstract void addEnergy(float failed);

	public abstract void configure(int i, int j, float f, int k);

}
