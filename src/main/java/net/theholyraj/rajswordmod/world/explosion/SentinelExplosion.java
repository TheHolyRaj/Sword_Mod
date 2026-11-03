package net.theholyraj.rajswordmod.world.explosion;


import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.theholyraj.rajswordmod.client.particle.ModParticles;

import java.util.*;

public class SentinelExplosion extends Explosion {
    private final Level level;
    private final Vec3 position;
    private final Player player;
    private final float radius;
    private final float storedDamage;

    public SentinelExplosion(Level level, Player player, Vec3 position, float radius, float storedDamage) {
        super(level, player, position.x, position.y, position.z, radius, false, BlockInteraction.KEEP);
        this.level = level;
        this.position = position;
        this.player = player;
        this.radius = radius;
        this.storedDamage = storedDamage;
    }

    @Override
    public void explode() {
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(
                position.x - radius, position.y - radius, position.z - radius,
                position.x + radius, position.y + radius, position.z + radius));

        for (LivingEntity entity : entities) {
            if (entity == player) continue;
            double distance = entity.distanceToSqr(position);
            if (distance > radius * radius) continue;
            double ratio = Math.max(0, 1 - Math.sqrt(distance) / radius);
            float damageToDeal = storedDamage * (float) ratio;
            Vec3 knockbackDir = entity.position().subtract(position).normalize();
            DamageSource source = player.damageSources().playerAttack(player);
            entity.hurt(source, damageToDeal);
            entity.push(knockbackDir.x * ratio, knockbackDir.y * ratio * 0.5, knockbackDir.z * ratio);
        }

        level.playSound(null, position.x, position.y, position.z,
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f, 1.0f);

        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            serverLevel.playSound(null, position.x, position.y, position.z,
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 4.0f, 1.0f);

            serverLevel.sendParticles(
                    ModParticles.SENTINEL_SHOCKWAVE_PARTICLES.get(),
                    position.x, position.y + 0.1, position.z,
                    0,
                    0.0, this.radius, 0.0,
                    1.0
            );
        }
    }

    @Override
    public boolean interactsWithBlocks() {
        return false;
    }
}