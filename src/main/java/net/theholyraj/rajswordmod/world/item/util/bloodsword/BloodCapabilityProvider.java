package net.theholyraj.rajswordmod.world.item.util.bloodsword;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.theholyraj.rajswordmod.world.item.util.ModCapabilities;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.IUpgradeSwordData;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class BloodCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    private final IBloodSwordData instance = new BloodSwordData();
    private final IUpgradeSwordData instance2 = new UpgradeSwordData();


    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ModCapabilities.BLOOD_DATA_CAPABILITY) {
            return LazyOptional.of(() -> instance).cast();
        } else if (cap == ModCapabilities.UPGRADE_DATA_CAPABILITY) {
            return LazyOptional.of(() -> instance2).cast();
        } else {
            return LazyOptional.empty();
        }
    }
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        instance.writeToNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.readFromNBT(nbt);
    }

}