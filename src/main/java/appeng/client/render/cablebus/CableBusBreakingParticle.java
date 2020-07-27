package appeng.client.render.cablebus;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;

// Derived from Vanilla's BreakingParticle, but allows
// a texture to be specified directly rather than via an itemstack
@Environment(EnvType.CLIENT)
public class CableBusBreakingParticle extends SpriteBillboardParticle {

    private final float field_217571_C;
    private final float field_217572_F;

    public CableBusBreakingParticle(ClientWorld world, double x, double y, double z, double speedX, double speedY,
            double speedZ, Sprite sprite) {
        super(world, x, y, z, speedX, speedY, speedZ);
        this.setSprite(sprite);
        this.gravityStrength = 1.0F;
        this.scale /= 2.0F;
        this.field_217571_C = this.random.nextFloat() * 3.0F;
        this.field_217572_F = this.random.nextFloat() * 3.0F;
    }

    public CableBusBreakingParticle(ClientWorld world, double x, double y, double z, Sprite sprite) {
        this(world, x, y, z, 0, 0, 0, sprite);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.TERRAIN_SHEET;
    }

    @Override
    protected float getMinU() {
        return this.sprite.getFrameU((this.field_217571_C + 1.0F) / 4.0F * 16.0F);
    }

    @Override
    protected float getMaxU() {
        return this.sprite.getFrameU(this.field_217571_C / 4.0F * 16.0F);
    }

    @Override
    protected float getMinV() {
        return this.sprite.getFrameV(this.field_217572_F / 4.0F * 16.0F);
    }

    @Override
    protected float getMaxV() {
        return this.sprite.getFrameV((this.field_217572_F + 1.0F) / 4.0F * 16.0F);
    }

}
