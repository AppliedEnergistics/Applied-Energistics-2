package appeng.client.guidebook.scene;

import appeng.core.AppEng;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public final class CheckerboardFloor {
    private static final ResourceLocation NOISE = AppEng.makeId("noise");

    private CheckerboardFloor() {
    }

    public static void render(MultiBufferSource buffers, Level level, BlockPos min, BlockPos max) {
        var blockAtlas = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
        var noiseSprite = blockAtlas.apply(AppEng.makeId("block/noise"));

        var consumer = buffers.getBuffer(RenderType.solid());

        for (var pos : BlockPos.betweenClosed(min, max.atY(min.getY()))) {
            var skyLight = level.getBrightness(LightLayer.SKY, pos);
            var blockLight = level.getBrightness(LightLayer.BLOCK, pos);
            var packedLight = LightTexture.pack(blockLight, skyLight);

            quad(consumer, pos.below(), LightTexture.FULL_BRIGHT, noiseSprite);
        }
    }

    private static void quad(VertexConsumer consumer, BlockPos pos, int packedLight, TextureAtlasSprite sprite) {

        int color;
        if ((pos.getX() + pos.getZ()) % 2 == 0) {
            color = Mth.color(200, 200, 200);
        } else {
            color = Mth.color(64, 64, 64);
        }
        color |= 0xFF000000;
        Direction face = Direction.UP;

        var minX = pos.getX();
        var maxX = pos.getX() + 1;
        var minZ = pos.getZ();
        var maxZ = pos.getZ() + 1;
        var y = pos.getY() + 1;

        consumer.vertex(minX, y, minZ)
                .color(color)
                .uv(sprite.getU0(), sprite.getV0())
                .uv2(packedLight)
                .normal(face.getStepX(), face.getStepY(), face.getStepZ())
                .endVertex();

        consumer.vertex(minX, y, maxZ)
                .color(color)
                .uv(sprite.getU0(), sprite.getV1())
                .uv2(packedLight)
                .normal(face.getStepX(), face.getStepY(), face.getStepZ())
                .endVertex();

        consumer.vertex(maxX, y, maxZ)
                .color(color)
                .uv(sprite.getU1(), sprite.getV1())
                .uv2(packedLight)
                .normal(face.getStepX(), face.getStepY(), face.getStepZ())
                .endVertex();

        consumer.vertex(maxX, y, minZ)
                .color(color)
                .uv(sprite.getU1(), sprite.getV0())
                .uv2(packedLight)
                .normal(face.getStepX(), face.getStepY(), face.getStepZ())
                .endVertex();

    }
}
