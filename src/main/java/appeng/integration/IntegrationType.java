package appeng.integration;

public enum IntegrationType
{
	IC2(IntegrationSide.BOTH, "Industrial Craft 2", "IC2"),

	RotaryCraft(IntegrationSide.BOTH, "Rotary Craft", "RotaryCraft"),

	RC(IntegrationSide.BOTH, "Railcraft", "Railcraft"),

	BC(IntegrationSide.BOTH, "BuildCraft", "BuildCraft|Silicon"),

	MJ6(IntegrationSide.BOTH, "BuildCraft6 Power", null),

	MJ5(IntegrationSide.BOTH, "BuildCraft5 Power", null),

	RF(IntegrationSide.BOTH, "RedstoneFlux Power - Tiles", null),

	RFItem(IntegrationSide.BOTH, "RedstoneFlux Power - Items", null),

	MFR(IntegrationSide.BOTH, "Mine Factory Reloaded", "MineFactoryReloaded"),

	DSU(IntegrationSide.BOTH, "Deep Storage Unit", null),

	FZ(IntegrationSide.BOTH, "Factorization", "factorization"),

	FMP(IntegrationSide.BOTH, "Forge MultiPart", "McMultipart"),

	RB(IntegrationSide.BOTH, "Rotatable Blocks", "RotatableBlocks"),

	CLApi(IntegrationSide.BOTH, "Colored Lights Core", "coloredlightscore"),

	Waila(IntegrationSide.CLIENT, "Waila", "Waila"),

	InvTweaks(IntegrationSide.CLIENT, "Inventory Tweaks", "inventorytweaks"),

	NEI(IntegrationSide.CLIENT, "Not Enough Items", "NotEnoughItems"),

	CraftGuide(IntegrationSide.CLIENT, "Craft Guide", "craftguide"),

	Mekanism(IntegrationSide.BOTH, "Mekanism", "Mekanism"),

	ImmibisMicroblocks(IntegrationSide.BOTH, "ImmibisMicroblocks", "ImmibisMicroblocks"),
	
	BetterStorage(IntegrationSide.BOTH, "BetterStorage", "betterstorage" );

	public final IntegrationSide side;
	public final String dspName;
	public final String modID;

	private IntegrationType(IntegrationSide side, String Name, String modid) {
		this.side = side;
		this.dspName = Name;
		this.modID = modid;
	}

}
