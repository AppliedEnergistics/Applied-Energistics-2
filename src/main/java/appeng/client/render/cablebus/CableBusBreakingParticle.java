package appeng.client.render.cablebus;

import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// Derived from Vanilla's BreakingParticle, but allows
// a texture to be specified directly rather than via an itemstack
@OnlyIn(Dist.CLIENT)
public class CableBusBreakingParticle extends SpriteTexturedParticle {

    private final float field_217571_C;
    private final float field_217572_F;

    public CableBusBreakingParticle(ClientWorld world, double x, double y, double z, double speedX, double speedY,
            double speedZ, TextureAtlasSprite sprite) {
        super(world, x, y, z, speedX, speedY, speedZ);
        this.setSprite(sprite);
        this.particleGravity = 1.0F;
        this.particleScale /= 2.0F;
        this.field_217571_C = this.rand.nextFloat() * 3.0F;
        this.field_217572_F = this.rand.nextFloat() * 3.0F;
    }

    public CableBusBreakingParticle(ClientWorld world, double x, double y, double z, TextureAtlasSprite sprite) {
        this(world, x, y, z, 0, 0, 0, sprite);
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    protected float getMinU() {
        return this.sprite.getInterpolatedU((this.field_217571_C + 1.0F) / 4.0F * 16.0F);
    }

    @Override
    protected float getMaxU() {
        return this.sprite.getInterpolatedU(this.field_217571_C / 4.0F * 16.0F);
    }

    @Override
    protected float getMinV() {
        return this.sprite.getInterpolatedV(this.field_217572_F / 4.0F * 16.0F);
    }

    @Override
    protected float getMaxV() {
        return this.sprite.getInterpolatedV((this.field_217572_F + 1.0F) / 4.0F * 16.0F);
    }

}
