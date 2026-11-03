package net.theholyraj.rajswordmod.world.mobeffects.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class HolyFireEffectInstance extends MobEffectInstance {
    public HolyFireEffectInstance(MobEffect pEffect, int duration, int amplifier) {
        super(pEffect, duration, amplifier);
    }

    @Override
    public boolean update(MobEffectInstance pOther) {
        return super.update(pOther);
    }

    @Override
    public boolean showIcon() {
        return false;
    }

    @Override
    public boolean isVisible() {
        return false;
    }
}
