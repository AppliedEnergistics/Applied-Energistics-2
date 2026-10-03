package appeng.client.block.cablebus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

import appeng.block.networking.CableBusBlock;
import appeng.block.networking.CableBusRenderState;
import appeng.client.render.cablebus.CableBusBreakingParticle;
import appeng.client.render.cablebus.CableBusModel;
import appeng.parts.ICableBusContainer;

public class CableBusBlockClientExtensions implements IClientBlockExtensions {

    private final CableBusBlock block;

    public CableBusBlockClientExtensions(CableBusBlock block) {
        this.block = block;
    }

    @Override
    public boolean addHitEffects(BlockState state, Level level, BlockPos blockPos, Direction face,
            ParticleEngine effectRenderer) {

        // Half the particle rate. Since we're spawning concentrated on a specific spot,
        // our particle effect otherwise looks too strong
        if (level.getRandom().nextBoolean()) {
            return true;
        }

        // Spawn the particle where the player is actually looking, if possible
        Vec3 location;
        if (Minecraft.getInstance().hitResult instanceof BlockHitResult hitResult
                && hitResult.getBlockPos().equals(blockPos)) {
            location = hitResult.getLocation();
        } else {
            location = Vec3.atCenterOf(blockPos).relative(face, 0.5);
        }

        ICableBusContainer cb = block.cb(level, blockPos);

        // Our built-in model has the actual baked sprites we need
        var model = Minecraft.getInstance().getModelManager()
                .getBlockModelSet()
                .get(block.defaultBlockState());

        // We cannot add the effect if we don't have the model
        if (!(model instanceof CableBusModel cableBusModel)) {
            return true;
        }

        CableBusRenderState renderState = cb.getRenderState();

        // Spawn a particle for one of the particle textures
        var textures = cableBusModel.getParticleMaterials(renderState);
        if (!textures.isEmpty()) {
            var texture = Util.getRandom(textures, level.getRandom());
            double x = location.x;
            double y = location.y;
            double z = location.z;
            // FIXME: Check how this looks, probably like shit, maybe provide parts the ability to supply particle
            // textures???
            effectRenderer.add(
                    new CableBusBreakingParticle((ClientLevel) level, x, y, z, texture.sprite()).scale(0.8F));
        }

        return true;
    }

    @Override
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos,
            ParticleEngine effectRenderer) {
        ICableBusContainer cb = block.cb(level, pos);

        // Our built-in model has the actual baked sprites we need
        var model = Minecraft.getInstance().getModelManager()
                .getBlockModelSet()
                .get(block.defaultBlockState());

        //

        // We cannot add the effect if we don't have the model
        if (!(model instanceof CableBusModel cableBusModel)) {
            return true;
        }

        CableBusRenderState renderState = cb.getRenderState();

        var particleMaterials = cableBusModel.getParticleMaterials(renderState);

        if (!particleMaterials.isEmpty()) {
            // Shamelessly inspired by ParticleManager.addBlockDestroyEffects
            for (int j = 0; j < 4; ++j) {
                for (int k = 0; k < 4; ++k) {
                    for (int l = 0; l < 4; ++l) {
                        // Randomly select one of the textures if the cable bus has more than just one
                        // possibility here
                        var material = Util.getRandom(particleMaterials, level.getRandom());

                        final double x = pos.getX() + (j + 0.5D) / 4.0D;
                        final double y = pos.getY() + (k + 0.5D) / 4.0D;
                        final double z = pos.getZ() + (l + 0.5D) / 4.0D;

                        // FIXME: Check how this looks, probably like shit, maybe provide parts the
                        // ability to supply particle textures???
                        Particle effect = new CableBusBreakingParticle((ClientLevel) level, x, y, z,
                                x - pos.getX() - 0.5D, y - pos.getY() - 0.5D, z - pos.getZ() - 0.5D, material.sprite());
                        effectRenderer.add(effect);
                    }
                }
            }
        }

        return true;
    }
}
