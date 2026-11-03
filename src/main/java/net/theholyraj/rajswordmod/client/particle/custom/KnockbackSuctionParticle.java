package net.theholyraj.rajswordmod.client.particle.custom;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class KnockbackSuctionParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float startSize = 1.5f;

    protected KnockbackSuctionParticle(ClientLevel pLevel, double pX, double pY, double pZ, SpriteSet pSprites, double dx, double dy, double dz) {
        super(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
        this.sprites = pSprites;

        this.xd = dx;
        this.yd = dy;
        this.zd = dz;

        this.lifetime = 10;
        this.quadSize = startSize;
        this.hasPhysics = false;
        this.setSpriteFromAge(pSprites);
    }

    @Override
    public void move(double pX, double pY, double pZ) {
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
        } else {
            this.quadSize *= 0.82F;
            this.alpha = 1.0F - ((float)this.age / (float)this.lifetime);
            this.setSpriteFromAge(this.sprites);
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 vec3 = camera.getPosition();
        float particleX = (float)(Mth.lerp(partialTicks, this.xo, this.x) - vec3.x());
        float particleY = (float)(Mth.lerp(partialTicks, this.yo, this.y) - vec3.y());
        float particleZ = (float)(Mth.lerp(partialTicks, this.zo, this.z) - vec3.z());

        float yaw = (float)(Mth.atan2(this.xd, this.zd));
        float pitch = (float)(Mth.atan2(-this.yd, Mth.sqrt((float)(this.xd * this.xd + this.zd * this.zd))));

        Quaternionf quaternion = new Quaternionf().rotationYXZ(yaw, pitch, 0);
        Vector3f[] corners = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };

        float size = this.getQuadSize(partialTicks);

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = corners[i];
            vector3f.mul(size);
            vector3f.rotate(quaternion);
            vector3f.add(particleX, particleY, particleZ);
        }

        float minU = this.getU0();
        float maxU = this.getU1();
        float minZ = this.getV0();
        float maxZ = this.getV1();
        int light = this.getLightColor(partialTicks);

        buffer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(maxU, maxZ).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        buffer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(maxU, minZ).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        buffer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(minU, minZ).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        buffer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(minU, maxZ).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet pSprites) { this.sprites = pSprites; }

        @Override
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return new KnockbackSuctionParticle(pLevel, pX, pY, pZ, this.sprites, pXSpeed, pYSpeed, pZSpeed);
        }
    }
}