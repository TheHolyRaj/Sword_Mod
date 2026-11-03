package net.theholyraj.rajswordmod.client.particle.custom;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SentinelDefenceParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public SentinelDefenceParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pExplosionRadius, SpriteSet pSprites) {
        super(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
        this.sprites = pSprites;
        this.lifetime = 1;
        this.quadSize = 1f;
        this.alpha = 1f;
        this.setSpriteFromAge(pSprites);
    }

    @Override
    public int getLightColor(float pPartialTick) {
        return 15728880;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.setSpriteFromAge(this.sprites);
    }
    @Override
    public boolean shouldCull() {
        return false;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet pSprites) {
            this.sprites = pSprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pExplosionRadius, double pYSpeed, double pZSpeed) {
            return new SentinelDefenceParticle(pLevel, pX, pY, pZ, pExplosionRadius, this.sprites);
        }
    }
}