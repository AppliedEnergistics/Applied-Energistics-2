package appeng.items.materials;

import java.util.EnumSet;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.core.features.MaterialStackSrc;
import appeng.entity.EntityChargedQuartz;
import appeng.entity.EntityIds;
import appeng.entity.EntitySingularity;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public enum MaterialType
{
	InvalidType(-1, AEFeature.Core),

	CertusQuartzCrystal(0, AEFeature.Core, "crystalCertusQuartz"), CertusQuartzCrystalCharged(1, AEFeature.Core, EntityChargedQuartz.class),

	CertusQuartzDust(2, AEFeature.Core, "dustCertusQuartz"), NetherQuartzDust(3, AEFeature.Core, "dustNetherQuartz"), Flour(4, AEFeature.Flour, "dustWheat"), GoldDust(
			51, AEFeature.Core, "dustGold"), IronDust(49, AEFeature.Core, "dustIron"), IronNugget(50, AEFeature.Core, "nuggetIron"),

	Silicon(5, AEFeature.Core, "itemSilicon"), MatterBall(6),

	FluixCrystal(7, AEFeature.Core, "crystalFluix"), FluixDust(8, AEFeature.Core, "dustFluix"), FluixPearl(9, AEFeature.Core, "pearlFluix"),

	PurifiedCertusQuartzCrystal(10), PurifiedNetherQuartzCrystal(11), PurifiedFluixCrystal(12),

	CalcProcessorPress(13), EngProcessorPress(14), LogicProcessorPress(15),

	CalcProcessorPrint(16), EngProcessorPrint(17), LogicProcessorPrint(18),

	SiliconPress(19), SiliconPrint(20),

	NamePress(21),

	LogicProcessor(22), CalcProcessor(23), EngProcessor(24),

	// Basic Cards
	BasicCard(25), CardRedstone(26), CardCapacity(27),

	// Adv Cards
	AdvCard(28), CardFuzzy(29), CardSpeed(30), CardInverter(31),

	Cell2SpatialPart(32, AEFeature.SpatialIO), Cell16SpatialPart(33, AEFeature.SpatialIO), Cell128SpatialPart(34, AEFeature.SpatialIO),

	Cell1kPart(35, AEFeature.StorageCells), Cell4kPart(36, AEFeature.StorageCells), Cell16kPart(37, AEFeature.StorageCells), Cell64kPart(38,
			AEFeature.StorageCells), EmptyStorageCell(39, AEFeature.StorageCells),

	WoodenGear(40, AEFeature.GrindStone, "gearWood"),

	Wireless(41, AEFeature.WirelessAccessTerminal), WirelessBooster(42, AEFeature.WirelessAccessTerminal),

	FormationCore(43), AnnihilationCore(44),

	SkyDust(45, AEFeature.Core),

	EnderDust(46, AEFeature.QuantumNetworkBridge, "dustEnder,dustEnderPearl", EntitySingularity.class), Singularity(47, AEFeature.QuantumNetworkBridge,
			EntitySingularity.class), QESingularity(48, AEFeature.QuantumNetworkBridge, EntitySingularity.class),

	BlankPattern(52), CardCrafting(53);

	private String oreName;
	private final EnumSet<AEFeature> features;
	private Class<? extends Entity> droppedEntity;

	// IIcon for the material.
	@SideOnly(Side.CLIENT)
	public IIcon IIcon;

	public Item itemInstance;
	public int damageValue;

	private boolean isRegistered = false;

	// stack!
	public MaterialStackSrc stackSrc;

	MaterialType(int metaValue) {
		damageValue = metaValue;
		features = EnumSet.of( AEFeature.Core );
	}

	MaterialType(int metaValue, AEFeature part) {
		damageValue = metaValue;
		features = EnumSet.of( part );
	}

	MaterialType(int metaValue, AEFeature part, Class<? extends Entity> c) {
		features = EnumSet.of( part );
		damageValue = metaValue;
		droppedEntity = c;

		EntityRegistry.registerModEntity( droppedEntity, droppedEntity.getSimpleName(), EntityIds.get( droppedEntity ), AppEng.instance, 16, 4, true );
	}

	MaterialType(int metaValue, AEFeature part, String oreDictionary, Class<? extends Entity> c) {
		features = EnumSet.of( part );
		damageValue = metaValue;
		oreName = oreDictionary;
		droppedEntity = c;
		EntityRegistry.registerModEntity( droppedEntity, droppedEntity.getSimpleName(), EntityIds.get( droppedEntity ), AppEng.instance, 16, 4, true );
	}

	MaterialType(int metaValue, AEFeature part, String oreDictionary) {
		features = EnumSet.of( part );
		damageValue = metaValue;
		oreName = oreDictionary;
	}

	public ItemStack stack(int size)
	{
		return new ItemStack( itemInstance, size, damageValue );
	}

	public EnumSet<AEFeature> getFeature()
	{
		return features;
	}

	public String getOreName()
	{
		return oreName;
	}

	public boolean hasCustomEntity()
	{
		return droppedEntity != null;
	}

	public Class<? extends Entity> getCustomEntityClass()
	{
		return droppedEntity;
	}

	public boolean isRegistered()
	{
		return isRegistered;
	}

	public void markReady()
	{
		isRegistered = true;
	}

}
