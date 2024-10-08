package net.theholyraj.rajswordmod.world.item.util.upgradesword;

import net.minecraft.nbt.CompoundTag;

public interface IUpgradeSwordData {
    void setData(int data);

    int getData();

    void setUpgraded(boolean data);

    boolean isUpgraded();

    void readFromNBT(CompoundTag nbt);

    void writeToNBT(CompoundTag nbt);
}
