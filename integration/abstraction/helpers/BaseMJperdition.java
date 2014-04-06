package appeng.integration.abstraction.helpers;

import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.transformer.annotations.integration.Method;
import buildcraft.api.power.PowerHandler.PowerReceiver;

public abstract class BaseMJperdition extends AETileEventHandler
{

	public BaseMJperdition() {
		super( TileEventType.TICK, TileEventType.WORLD_NBT );
	}

	@Method(iname = "MJ")
	public abstract PowerReceiver getPowerReceiver();

	public abstract double useEnergy(float f, float requred, boolean b);

	public abstract void addEnergy(float failed);

	public abstract void configure(int i, int j, float f, int k);

}
