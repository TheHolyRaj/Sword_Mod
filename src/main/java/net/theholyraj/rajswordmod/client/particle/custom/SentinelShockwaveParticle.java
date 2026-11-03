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
public class SentinelShockwaveParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float maxSize;

    public SentinelShockwaveParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pExplosionRadius, SpriteSet pSprites) {
        super(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
        this.sprites = pSprites;
        this.lifetime = 10;

        this.maxSize = (float) pExplosionRadius;

        this.quadSize = 0.1F;
        this.alpha = 1.0F;
        this.setSpriteFromAge(pSprites);
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

        float progress = (float)this.age / (float)this.lifetime;
        this.quadSize = 0.1F + (progress * (maxSize * 1.8F));
        this.alpha = 1.0F - progress;
    }

    @Override
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        Vec3 camPos = pRenderInfo.getPosition();
        float x = (float)(Mth.lerp(pPartialTicks, this.xo, this.x) - camPos.x());
        float y = (float)(Mth.lerp(pPartialTicks, this.yo, this.y) - camPos.y()) + 0.1F;
        float z = (float)(Mth.lerp(pPartialTicks, this.zo, this.z) - camPos.z());

        float progress = (float)this.age / (float)this.lifetime;
        float currentSize = Mth.lerp(pPartialTicks, this.quadSize, progress * maxSize);

        float halfSize = currentSize * 0.5F;

        TextureAtlasSprite sprite = this.sprites.get(this.age, this.lifetime);
        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        float[][] corners = {
                {-halfSize, 0.0F, -halfSize},
                {-halfSize, 0.0F,  halfSize},
                { halfSize, 0.0F,  halfSize},
                { halfSize, 0.0F, -halfSize}
        };

        int light = this.getLightColor(pPartialTicks);

        pBuffer.vertex(x + corners[0][0], y, z + corners[0][2]).uv(maxU, maxV).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        pBuffer.vertex(x + corners[1][0], y, z + corners[1][2]).uv(maxU, minV).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        pBuffer.vertex(x + corners[2][0], y, z + corners[2][2]).uv(minU, minV).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        pBuffer.vertex(x + corners[3][0], y, z + corners[3][2]).uv(minU, maxV).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
    }

    @Override
    public int getLightColor(float pPartialTick) {
        return 15728880;
    }
    
    @Override
    public boolean shouldCull() {
        return false;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet pSprites) {
            this.sprites = pSprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel,
                                       double pX, double pY, double pZ,
                                       double pXSpeed, double pYSpeed, double pZSpeed) {

            return new SentinelShockwaveParticle(pLevel, pX, pY, pZ, pYSpeed, this.sprites);
        }
    }
}