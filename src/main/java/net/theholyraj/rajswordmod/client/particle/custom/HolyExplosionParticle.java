package net.theholyraj.rajswordmod.client.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public class HolyExplosionParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected HolyExplosionParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pRadius, SpriteSet pSprites) {
        super(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
        this.lifetime = 6 + this.random.nextInt(4);
        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 0.7f;
        this.sprites = pSprites;

        float damageRadius = (float) pRadius * 2.0f;
        this.quadSize = damageRadius;

        this.setSpriteFromAge(pSprites);
    }

    public int getLightColor(float pPartialTick) {
        return 15728880;
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.sprites);
        }
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet pSprites) {
            this.sprites = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return new HolyExplosionParticle(pLevel, pX, pY, pZ, pXSpeed, this.sprites);
        }
    }
}
