package appeng.core.settings;

import appeng.core.AEConfig;

public enum TickRates
{

	Interface(5, 120),

	ImportBus(5, 40),

	ExportBus(5, 60),

	AnnihilationPlane(2, 120),

	MJTunnel(1, 20),

	METunnel(5, 20),

	Inscriber(1, 1),

	IOPort(1, 5),

	VibrationChamber(10, 40),

	StorageBus(5, 60),

	ItemTunnel(5, 60),

	LightTunnel(5, 120);

	public int min;
	public int max;

	private TickRates(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public void Load(AEConfig config)
	{
		config.addCustomCategoryComment(
				"TickRates",
				" Min / Max Tickrates for dynamic ticking, most of these components also use sleeping, to prevent constant ticking, adjust with care, non standard rates are not supported or tested." );
		min = config.get( "TickRates", name() + ".min", min ).getInt( min );
		max = config.get( "TickRates", name() + ".max", max ).getInt( max );
	}

}
