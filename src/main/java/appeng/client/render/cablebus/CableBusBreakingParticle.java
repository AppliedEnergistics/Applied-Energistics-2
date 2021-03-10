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

    private final float uCoord;
    private final float vCoord;

    public CableBusBreakingParticle(ClientWorld world, double x, double y, double z, double speedX, double speedY,
            double speedZ, TextureAtlasSprite sprite) {
        super(world, x, y, z, speedX, speedY, speedZ);
        this.setSprite(sprite);
        this.gravity = 1.0F;
        this.quadSize /= 2.0F;
        this.uCoord = this.random.nextFloat() * 3.0F;
        this.vCoord = this.random.nextFloat() * 3.0F;
    }

    public CableBusBreakingParticle(ClientWorld world, double x, double y, double z, TextureAtlasSprite sprite) {
        this(world, x, y, z, 0, 0, 0, sprite);
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((this.uCoord + 1.0F) / 4.0F * 16.0F);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.uCoord / 4.0F * 16.0F);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.vCoord / 4.0F * 16.0F);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.vCoord + 1.0F) / 4.0F * 16.0F);
    }

}
