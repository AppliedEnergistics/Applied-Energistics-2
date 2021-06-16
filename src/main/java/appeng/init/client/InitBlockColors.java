package appeng.init.client;

import net.minecraft.client.renderer.color.BlockColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.util.AEColor;
import appeng.block.networking.CableBusColor;
import appeng.client.render.ColorableTileBlockColor;
import appeng.client.render.StaticBlockColor;
import appeng.core.api.definitions.ApiBlocks;

@OnlyIn(Dist.CLIENT)
public final class InitBlockColors {

    private InitBlockColors() {
    }

    public static void init(BlockColors blockColors) {
        blockColors.register(new StaticBlockColor(AEColor.TRANSPARENT), ApiBlocks.wirelessAccessPoint.block());
        blockColors.register(new CableBusColor(), ApiBlocks.multiPart.block());
        blockColors.register(ColorableTileBlockColor.INSTANCE, ApiBlocks.securityStation.block());
        blockColors.register(new ColorableTileBlockColor(), ApiBlocks.chest.block());
    }

}
