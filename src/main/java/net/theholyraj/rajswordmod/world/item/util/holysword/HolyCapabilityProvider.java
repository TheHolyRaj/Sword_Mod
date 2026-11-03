package net.theholyraj.rajswordmod.world.item.util.holysword;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.theholyraj.rajswordmod.world.item.util.ModCapabilities;
import net.theholyraj.rajswordmod.world.item.util.bloodsword.BloodSwordData;
import net.theholyraj.rajswordmod.world.item.util.bloodsword.IBloodSwordData;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.IUpgradeSwordData;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class HolyCapabilityProvider  implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    private final IHolySwordData holyInstance = new HolySwordData();
    private final IUpgradeSwordData upgradeInstance = new UpgradeSwordData();

    private final LazyOptional<IHolySwordData> holyOptional = LazyOptional.of(() -> holyInstance);
    private final LazyOptional<IUpgradeSwordData> upgradeOptional = LazyOptional.of(() -> upgradeInstance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ModCapabilities.HOLY_DATA_CAPABILITY) {
            return holyOptional.cast();
        }
        if (cap == ModCapabilities.UPGRADE_DATA_CAPABILITY) {
            return upgradeOptional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        holyInstance.writeToNBT(nbt);

        CompoundTag upgradeTag = new CompoundTag();
        upgradeInstance.writeToNBT(upgradeTag);
        nbt.put("upgrade_data", upgradeTag);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        holyInstance.readFromNBT(nbt);
        if (nbt.contains("upgrade_data")) {
            upgradeInstance.readFromNBT(nbt.getCompound("upgrade_data"));
        }
    }
}
