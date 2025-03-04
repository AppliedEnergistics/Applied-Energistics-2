package appeng.block.networking;

import java.util.List;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

import appeng.client.render.cablebus.CableBusBakedModel;
import appeng.client.render.cablebus.CableBusBreakingParticle;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.parts.ICableBusContainer;

public class CableBusBlockClientExtensions implements IClientBlockExtensions {

    private final CableBusBlock block;

    public CableBusBlockClientExtensions(CableBusBlock block) {
        this.block = block;
    }

    @Override
    public boolean addHitEffects(BlockState state, Level level, HitResult target,
            ParticleEngine effectRenderer) {

        // Half the particle rate. Since we're spawning concentrated on a specific spot,
        // our particle effect otherwise looks too strong
        if (level.getRandom().nextBoolean()) {
            return true;
        }

        if (target.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos blockPos = BlockPos.containing(target.getLocation().x, target.getLocation().y,
                target.getLocation().z);

        ICableBusContainer cb = block.cb(level, blockPos);

        // Our built-in model has the actual baked sprites we need
        BakedModel model = Minecraft.getInstance().getBlockRenderer()
                .getBlockModel(block.defaultBlockState());

        // We cannot add the effect if we don't have the model
        if (!(model instanceof CableBusBakedModel cableBusModel)) {
            return true;
        }

        CableBusRenderState renderState = cb.getRenderState();

        // Spawn a particle for one of the particle textures
        var textures = cableBusModel.getParticleTextures(renderState);
        if (!textures.isEmpty()) {
            var texture = Util.getRandom(textures, level.getRandom());
            double x = target.getLocation().x;
            double y = target.getLocation().y;
            double z = target.getLocation().z;
            // FIXME: Check how this looks, probably like shit, maybe provide parts the
            // ability to supply particle textures???
            effectRenderer.add(
                    new CableBusBreakingParticle((ClientLevel) level, x, y, z, texture).scale(0.8F));
        }

        return true;
    }

    @Override
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos,
            ParticleEngine effectRenderer) {
        ICableBusContainer cb = block.cb(level, pos);

        // Our built-in model has the actual baked sprites we need
        BakedModel model = Minecraft.getInstance().getBlockRenderer()
                .getBlockModel(block.defaultBlockState());

        // We cannot add the effect if we dont have the model
        if (!(model instanceof CableBusBakedModel cableBusModel)) {
            return true;
        }

        CableBusRenderState renderState = cb.getRenderState();

        List<TextureAtlasSprite> textures = cableBusModel.getParticleTextures(renderState);

        if (!textures.isEmpty()) {
            // Shamelessly inspired by ParticleManager.addBlockDestroyEffects
            for (int j = 0; j < 4; ++j) {
                for (int k = 0; k < 4; ++k) {
                    for (int l = 0; l < 4; ++l) {
                        // Randomly select one of the textures if the cable bus has more than just one
                        // possibility here
                        var texture = Util.getRandom(textures, level.getRandom());

                        final double x = pos.getX() + (j + 0.5D) / 4.0D;
                        final double y = pos.getY() + (k + 0.5D) / 4.0D;
                        final double z = pos.getZ() + (l + 0.5D) / 4.0D;

                        // FIXME: Check how this looks, probably like shit, maybe provide parts the
                        // ability to supply particle textures???
                        Particle effect = new CableBusBreakingParticle((ClientLevel) level, x, y, z,
                                x - pos.getX() - 0.5D, y - pos.getY() - 0.5D, z - pos.getZ() - 0.5D, texture);
                        effectRenderer.add(effect);
                    }
                }
            }
        }

        return true;
    }
}
