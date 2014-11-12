package appeng.tile.misc;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.core.AEConfig;
import appeng.me.GridAccessException;
import appeng.tile.grid.AENetworkTile;
import appeng.util.Platform;

public class TileTeleporter extends AENetworkTile {

	public TileTeleporter() {
		gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
	}

	int freq = 0;

	public void teleport(EntityPlayer p) throws GridAccessException 
	{
		List<TileTeleporter> tps = new ArrayList<TileTeleporter>();

		for (IGridNode gn : gridProxy.getGrid().getMachines(
				TileTeleporter.class)) {
			TileTeleporter tt = (TileTeleporter) gn.getMachine();
			if (freq == tt.freq && tt != this)
				tps.add(tt);
		}

		if (tps.size() > 1 || tps.size() < 1)
			return;

		TileTeleporter tt = tps.get(0);

		boolean dimTravel = p.worldObj != tt.worldObj;
		double distance = Math.abs(xCoord - tt.xCoord)
				+ Math.abs(yCoord - tt.yCoord) + Math.abs(zCoord - tt.zCoord);
		double drain = AEConfig.instance.teleporter_getDrain(distance,
				dimTravel);
		if (drain > 0 && gridProxy.getEnergy().getStoredPower() >= drain) {
			System.out.println(gridProxy.getEnergy().extractAEPower(drain,
					Actionable.MODULATE, PowerMultiplier.CONFIG));
			if (p.worldObj != tt.worldObj)
				p.travelToDimension(tt.worldObj.provider.dimensionId);
			p.mountEntity(null);
			p.setPositionAndUpdate(tt.xCoord + 0.5, tt.yCoord + 1,
					tt.zCoord + 0.5);
			p.worldObj.playSoundEffect(p.posX, p.posY, p.posZ,
					"mob.endermen.portal", 1F, 1F);
			return;
		}
	}

	public void setFrequency(int newValue) {
		freq = newValue;
	}

	public int getFrequency() {
		return freq;
	}
}
