package appeng.items.materials;

import java.util.EnumSet;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.oredict.OreDictionary;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.entity.EntityChargedQuartz;
import appeng.entity.EntityIds;
import appeng.entity.EntitySingularity;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public enum MaterialType
{
	CertusQuartzCrystal(AEFeature.Core, "crystalCertusQuartz"), CertusQuartzCrystalCharged(AEFeature.Core, EntityChargedQuartz.class),

	CertusQuartzDust(AEFeature.Core, "dustCertusQuartz"), NetherQuartzDust(AEFeature.Core, "dustNetherQuartz"), Flour(AEFeature.Flour, "dustWheat"), GoldDust(
			AEFeature.Core, "dustGold"), IronDust(AEFeature.Core, "dustIron"), IronNugget(AEFeature.Core, "nuggetIron"),

	Silicon(AEFeature.Core, "itemSilicon"), MatterBall,

	FluixCrystal(AEFeature.Core, "crystalFluix"), FluixDust(AEFeature.Core, "dustFluix"), FluixPearl(AEFeature.Core, "pearlFluix"),

	PureifiedCertusQuartzCrystal, PureifiedNetherQuartzCrystal, PureifiedFluixCrystal,

	CalcProcessorPress, EngProcessorPress, LogicProcessorPress,

	CalcProcessorPrint, EngProcessorPrint, LogicProcessorPrint,

	SiliconPress, SiliconPrint,

	NamePress,

	LogicProcessor, CalcProcessor, EngProcessor,

	// Basic Cards
	BasicCard, CardRedstone, CardCapacity,

	// Adv Cards
	AdvCard, CardFuzzy, CardSpeed, CardInverter,

	Cell2SpatialPart(AEFeature.SpatialIO), Cell16SpatialPart(AEFeature.SpatialIO), Cell128SpatialPart(AEFeature.SpatialIO),

	Cell1kPart(AEFeature.StorageCells), Cell4kPart(AEFeature.StorageCells), Cell16kPart(AEFeature.StorageCells), Cell64kPart(AEFeature.StorageCells), EmptyStorageCell(
			AEFeature.StorageCells),

	WoodenGear(AEFeature.GrindStone, "gearWood"),

	BlankPattern,

	Wireless(AEFeature.WirelessAccessTerminal), WirelessBooster(AEFeature.WirelessAccessTerminal),

	FormationCore, AnnihilationCore,

	SkyDust(AEFeature.Core),

	EnderDust(AEFeature.QuantumNetworkBridge, "dustEnder", EntitySingularity.class), Singularity(AEFeature.QuantumNetworkBridge, EntitySingularity.class), QESingularity(
			AEFeature.QuantumNetworkBridge, EntitySingularity.class);

	private String oreName;
	private EnumSet<AEFeature> features;
	private Class<? extends Entity> droppedEntity;

	// IIcon for the material.
	@SideOnly(Side.CLIENT)
	public IIcon IIcon;
	public int damageValue = -1;

	MaterialType() {
		features = EnumSet.of( AEFeature.Core );
	}

	MaterialType(AEFeature part) {
		features = EnumSet.of( part );
	}

	MaterialType(AEFeature part, Class<? extends Entity> c) {
		features = EnumSet.of( part );
		droppedEntity = c;

		EntityRegistry.registerModEntity( droppedEntity, droppedEntity.getSimpleName(), EntityIds.get( droppedEntity ), AppEng.instance, 16, 4, true );
	}

	MaterialType(AEFeature part, String oreDictionary, Class<? extends Entity> c) {
		features = EnumSet.of( part );
		oreName = oreDictionary;
		if ( OreDictionary.getOres( oreDictionary ).size() > 0 )
			features.add( AEFeature.DuplicateItems );

		droppedEntity = c;
		EntityRegistry.registerModEntity( droppedEntity, droppedEntity.getSimpleName(), EntityIds.get( droppedEntity ), AppEng.instance, 16, 4, true );
	}

	MaterialType(AEFeature part, String oreDictionary) {
		features = EnumSet.of( part );
		oreName = oreDictionary;
		if ( OreDictionary.getOres( oreDictionary ).size() > 0 )
			features.add( AEFeature.DuplicateItems );
	}

	public ItemStack stack(int size)
	{
		return new ItemStack( ItemMaterial.instance, size, damageValue );
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

}
