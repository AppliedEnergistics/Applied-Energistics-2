package appeng.init.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import appeng.client.render.crafting.CraftingMonitorTESR;
import appeng.client.render.crafting.MolecularAssemblerRenderer;
import appeng.client.render.tesr.ChargerTESR;
import appeng.client.render.tesr.ChestTileEntityRenderer;
import appeng.client.render.tesr.CrankTESR;
import appeng.client.render.tesr.DriveLedTileEntityRenderer;
import appeng.client.render.tesr.InscriberTESR;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.client.render.tesr.SkyCompassTESR;
import appeng.core.api.definitions.ApiBlockEntities;
import appeng.tile.networking.CableBusTESR;

@OnlyIn(Dist.CLIENT)
public final class InitBlockEntityRenderers {

    private InitBlockEntityRenderers() {
    }

    public static void init() {

        ClientRegistry.bindTileEntityRenderer(ApiBlockEntities.inscriber, InscriberTESR::new);
        ClientRegistry.bindTileEntityRenderer(ApiBlockEntities.SKY_CHEST, SkyChestTESR::new);
        ClientRegistry.bindTileEntityRenderer(ApiBlockEntities.crank, CrankTESR::new);
        ClientRegistry.bindTileEntityRenderer(ApiBlockEntities.charger, ChargerTESR.FACTORY);
        ClientRegistry.bindTileEntityRenderer(ApiBlockEntities.drive, DriveLedTileEntityRenderer::new);
        ClientRegistry.bindTileEntityRenderer(ApiBlockEntities.chest, ChestTileEntityRenderer::new);
        ClientRegistry.bindTileEntityRenderer(ApiBlockEntities.craftingMonitor, CraftingMonitorTESR::new);
        ClientRegistry.bindTileEntityRenderer(ApiBlockEntities.molecularAssembler, MolecularAssemblerRenderer::new);
        ClientRegistry.bindTileEntityRenderer(ApiBlockEntities.multiPart, CableBusTESR::new);
        ClientRegistry.bindTileEntityRenderer(ApiBlockEntities.SKY_COMPASS, SkyCompassTESR::new);

    }

}
