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
    private final IBloodSwordData bloodInstance = new BloodSwordData();

    private final LazyOptional<IBloodSwordData> bloodOptional = LazyOptional.of(() -> bloodInstance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ModCapabilities.BLOOD_DATA_CAPABILITY) {
            return bloodOptional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        CompoundTag bloodTag = new CompoundTag();
        bloodInstance.writeToNBT(bloodTag);
        nbt.put("blood_data", bloodTag);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("blood_data")) {
            bloodInstance.readFromNBT(nbt.getCompound("blood_data"));
        }
    }
}