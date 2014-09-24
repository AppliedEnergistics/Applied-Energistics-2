package appeng.core.localization;

import net.minecraft.util.StatCollector;

public enum WailaText
{

	DeviceOnline, DeviceOffline, DeviceMissingChannel,

	Locked, Unlocked, Showing,

	Contains, Channels;

	String root;

	WailaText() {
		root = "waila.appliedenergistics2";
	}

	WailaText(String r) {
		root = r;
	}

	public String getUnlocalized()
	{
		return root + "." + toString();
	}

	public String getLocal()
	{
		return StatCollector.translateToLocal( getUnlocalized() );
	}

}
