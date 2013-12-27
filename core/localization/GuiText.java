package appeng.core.localization;

import net.minecraft.util.StatCollector;
import appeng.core.AELog;

public enum GuiText
{
	inventory("container"), // mc's default Inventory localization.

	Chest, StoredEnergy, Of, Condenser, Drive, GrindStone, VibrationChamber, SpatialIOPort, NetworkStatus, LevelEmitter, Terminal;

	String root;

	GuiText() {
		root = "gui.appliedenergistics2";
		AELog.localization( "gui", getName() );
	}

	GuiText(String r) {
		root = r;
	}

	private String getName()
	{
		return root + "." + toString();
	}

	public String getLocal()
	{
		return StatCollector.translateToLocal( getName() );
	}

}
