package appeng.client.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public enum ExtraBlockTextures
{
	BlockVibrationChamberFrontOn("BlockVibrationChamberFrontOn"),

	OreQuartzStone("OreQuartzStone"),

	MEChest("BlockMEChest"), BlockMEChestItems("BlockMEChestItems"),

	BlockControllerPowered("BlockControllerPowered"), BlockControllerColumnPowered("BlockControllerColumnPowered"), BlockControllerColumn(
			"BlockControllerColumn"), BlockControllerLights("BlockControllerLights"), BlockControllerColumnLights("BlockControllerColumnLights"), BlockControllerColumnConflict(
			"BlockControllerColumnConflict"), BlockControllerConflict("BlockControllerConflict"), BlockControllerInsideA("BlockControllerInsideA"), BlockControllerInsideB(
			"BlockControllerInsideB"),

	BlockMolecularAssemblerLights("BlockMolecularAssemblerLights"),

	BlockChargerInside("BlockChargerInside"),

	BlockInterfaceAlternate("BlockInterfaceAlternate"), BlockInterfaceAlternateArrow("BlockInterfaceAlternateArrow"),

	MEStorageCellTextures("MEStorageCellTextures"), White("White"),

	BlockMatterCannonParticle("BlockMatterCannonParticle"), BlockEnergyParticle("BlockEnergyParticle"),

	GlassFrame("BlockQuartzGlassFrame"),

	BlockQRingCornerLight("BlockQRingCornerLight"), BlockQRingEdgeLight("BlockQRingEdgeLight"),

	MEDenseEnergyCell0("BlockDenseEnergyCell0"), MEDenseEnergyCell1("BlockDenseEnergyCell1"), MEDenseEnergyCell2("BlockDenseEnergyCell2"), MEDenseEnergyCell3(
			"BlockDenseEnergyCell3"), MEDenseEnergyCell4("BlockDenseEnergyCell4"), MEDenseEnergyCell5("BlockDenseEnergyCell5"), MEDenseEnergyCell6(
			"BlockDenseEnergyCell6"), MEDenseEnergyCell7("BlockDenseEnergyCell7"),

	MEEnergyCell0("BlockEnergyCell0"), MEEnergyCell1("BlockEnergyCell1"), MEEnergyCell2("BlockEnergyCell2"), MEEnergyCell3("BlockEnergyCell3"), MEEnergyCell4(
			"BlockEnergyCell4"), MEEnergyCell5("BlockEnergyCell5"), MEEnergyCell6("BlockEnergyCell6"), MEEnergyCell7("BlockEnergyCell7"),

	BlockSpatialPylon_dim("BlockSpatialPylon_dim"), BlockSpatialPylon_red("BlockSpatialPylon_red"),

	BlockSpatialPylonC("BlockSpatialPylon_spanned"), BlockSpatialPylonC_dim("BlockSpatialPylon_spanned_dim"), BlockSpatialPylonC_red(
			"BlockSpatialPylon_spanned_red"),

	BlockQuartzGlassB("BlockQuartzGlassB"), BlockQuartzGlassC("BlockQuartzGlassC"), BlockQuartzGlassD("BlockQuartzGlassD"),

	BlockSpatialPylonE("BlockSpatialPylon_end"), BlockSpatialPylonE_dim("BlockSpatialPylon_end_dim"), BlockSpatialPylonE_red("BlockSpatialPylon_end_red"),

	BlockMESecurityOn("BlockMESecurityOn"), BlockInscriberInside("BlockInscriberInside"),

	BlockQuartzGrowthAcceleratorOn("BlockQuartzGrowthAcceleratorOn"), BlockQuartzGrowthAcceleratorSideOn("BlockQuartzGrowthAcceleratorSideOn"),

	BlockWirelessInside("BlockWirelessInside"),

	BlockCraftingAccelerator("BlockCraftingAccelerator"), BlockCraftingMonitor("BlockCraftingMonitor"),

	BlockCraftingStorage1k("BlockCraftingStorage"), BlockCraftingStorage4k("BlockCraftingStorage4k"), BlockCraftingStorage16k("BlockCraftingStorage16k"), BlockCraftingStorage64k(
			"BlockCraftingStorage64k"),

	BlockCraftingAcceleratorFit("BlockCraftingAcceleratorFit"), BlockCraftingMonitorFit("BlockCraftingMonitorFit"),

	BlockCraftingStorage1kFit("BlockCraftingStorageFit"), BlockCraftingStorage4kFit("BlockCraftingStorage4kFit"), BlockCraftingStorage16kFit(
			"BlockCraftingStorage16kFit"), BlockCraftingStorage64kFit("BlockCraftingStorage64kFit"),

	BlockCraftingUnitRing("BlockCraftingUnitRing"), BlockCraftingUnitRingLongRotated("BlockCraftingUnitRingLongRotated"), BlockCraftingUnitRingLong(
			"BlockCraftingUnitRingLong"), BlockCraftingUnitFit("BlockCraftingUnitFit"),

	BlockCraftingMonitorOuter("BlockCraftingMonitorOuter"), BlockCraftingFitSolid("BlockCraftingFitSolid");

	final private String name;
	public IIcon IIcon;

	public static ResourceLocation GuiTexture(String string)
	{
		return new ResourceLocation( "appliedenergistics2", "textures/" + string );
	}

	public String getName()
	{
		return name;
	}

	private ExtraBlockTextures(String name) {
		this.name = name;
	}

	public IIcon getIcon()
	{
		return IIcon;
	}

	public void registerIcon(TextureMap map)
	{
		IIcon = map.registerIcon( "appliedenergistics2:" + name );
	}

	@SideOnly(Side.CLIENT)
	public static IIcon getMissing()
	{
		return ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture( TextureMap.locationBlocksTexture )).getAtlasSprite( "missingno" );
	}
}
