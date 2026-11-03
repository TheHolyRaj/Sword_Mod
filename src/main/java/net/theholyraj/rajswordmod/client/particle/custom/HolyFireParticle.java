package net.theholyraj.rajswordmod.client.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public class HolyFireParticle extends TextureSheetParticle {
    HolyFireParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.lifetime=5;
        this.quadSize=0.5f;
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public float getQuadSize(float pScaleFactor) {
        float lifetimeProgress = ((float)this.age + pScaleFactor) / (float)this.lifetime;
        return this.quadSize * (1.0F - lifetimeProgress * lifetimeProgress * 0.5F);
    }

    public int getLightColor(float pPartialTick) {
        float lifetimeProgress = ((float)this.age + pPartialTick) / (float)this.lifetime;
        lifetimeProgress = Mth.clamp(lifetimeProgress, 0.0F, 1.0F);
        int lightColor = super.getLightColor(pPartialTick);
        int blockLight = lightColor & 255;
        int skyLight = lightColor >> 16 & 255;
        blockLight += (int)(lifetimeProgress * 15.0F * 16.0F);
        if (blockLight > 240) {
            blockLight = 240;
        }
        return blockLight | skyLight << 16;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            HolyFireParticle holyFlameparticle = new HolyFireParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
            holyFlameparticle.pickSprite(this.sprite);
            return holyFlameparticle;
        }
    }
}
