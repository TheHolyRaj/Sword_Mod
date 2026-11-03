package net.theholyraj.rajswordmod.client.sound.custom;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.theholyraj.rajswordmod.client.sound.ModSounds;
import net.theholyraj.rajswordmod.world.item.ModItems;

public class ChargeTickableSound extends AbstractTickableSoundInstance {
    private final Player player;

    public ChargeTickableSound(float volume, Player player) {
        super(ModSounds.CHARGE_KNOCKBACK.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.volume = volume;
        this.pitch = volume;
        this.looping = false;
        this.delay = 0;
        this.relative = true;
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }
    @Override
    public void tick() {
        if (this.player.isRemoved() || !this.player.isUsingItem()) {
            this.stop();
        }
    }
}