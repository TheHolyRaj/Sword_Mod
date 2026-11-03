package net.theholyraj.rajswordmod.client.particle.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.theholyraj.rajswordmod.world.entity.custom.AntiArmourAttackEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class AntiArmourAttackParticle extends TextureSheetParticle {
    private final int attackEntityId;

    protected AntiArmourAttackParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet, int attackEntityId) {
        super(level, x, y, z);
        this.setSpriteFromAge(spriteSet);

        this.attackEntityId = attackEntityId;

        this.lifetime = 9999;
        this.quadSize = 1.0F;

        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
    }

    @Override
    public void tick() {
        Entity attackEntity = level.getEntity(attackEntityId);

        if (attackEntity == null || !attackEntity.isAlive()) {
            remove();
            return;
        }
        super.tick();
    }

    public void makeSlashImage(VertexConsumer buffer, Camera camera, float partialTick, int imageRotation){
        Entity attackEntity = level.getEntity(attackEntityId);

        if (attackEntity == null || !attackEntity.isAlive()) {
            return;
        }

        PoseStack poseStack = getPoseStack(camera, partialTick, attackEntity);

        if (attackEntity instanceof AntiArmourAttackEntity attack) {
            Player owner = attack.getOwner();
            Vec3 look = owner.getLookAngle();

            Vector3f direction = new Vector3f((float) look.x, (float) look.y, (float) look.z).normalize();

            Quaternionf rotation = new Quaternionf()
                    .lookAlong(direction, new Vector3f(0, 1, 0))
                    .invert();

            poseStack.mulPose(rotation);

            float shake = (float) Math.sin((age + partialTick) * 3.0F) * 3.0F;

            poseStack.mulPose(Axis.YP.rotationDegrees(90));
            poseStack.mulPose(Axis.XP.rotationDegrees(imageRotation+shake));
        }

        float size = this.getQuadSize(partialTick);
        poseStack.scale(size+.5f, size+.5f, size+.5f);

        TextureAtlasSprite sprite = this.sprite;
        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        int light = LightTexture.FULL_BRIGHT;

        buffer.vertex(poseStack.last().pose(), -.75F, -0.5F, 0.0F)
                .uv(minU, maxV)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(poseStack.last().pose(), .75F, -0.5F, 0.0F)
                .uv(maxU, maxV)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(poseStack.last().pose(), .75F, 0.5F, 0.0F)
                .uv(maxU, minV)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(poseStack.last().pose(), -.75F, 0.5F, 0.0F)
                .uv(minU, minV)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(poseStack.last().pose(), -.75F, 0.5F, 0.0F)
                .uv(minU, minV)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(poseStack.last().pose(), .75F, 0.5F, 0.0F)
                .uv(maxU, minV)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(poseStack.last().pose(), .75F, -0.5F, 0.0F)
                .uv(maxU, maxV)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(poseStack.last().pose(), -.75F, -0.5F, 0.0F)
                .uv(minU, maxV)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();

        poseStack.popPose();
    }

    private static @NotNull PoseStack getPoseStack(Camera camera, float partialTick, Entity attackEntity) {
        double x = Mth.lerp(partialTick, attackEntity.xo, attackEntity.getX());
        double y = Mth.lerp(partialTick, attackEntity.yo, attackEntity.getY());
        double z = Mth.lerp(partialTick, attackEntity.zo, attackEntity.getZ());
        Vec3 cameraPos = camera.getPosition();
        float renderX = (float) (x - cameraPos.x());
        float renderY = (float) (y - cameraPos.y());
        float renderZ = (float) (z - cameraPos.z());
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(renderX, renderY+1, renderZ);
        return poseStack;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        makeSlashImage(buffer, camera, partialTick, 225);
        makeSlashImage(buffer, camera, partialTick, 315);
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

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return new AntiArmourAttackParticle(pLevel, pX, pY, pZ, this.sprites, (int) pXSpeed);
        }
    }
}
