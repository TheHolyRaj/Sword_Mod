package net.theholyraj.rajswordmod.world.item.util.upgradesword;

import net.minecraft.nbt.CompoundTag;

public class UpgradeSwordData implements IUpgradeSwordData{
    private int data;
    private boolean upgraded;

    @Override
    public void setData(int data) {
        this.data=data;
    }

    @Override
    public int getData() {return data;}

    @Override
    public void setUpgraded(boolean data) {
        this.upgraded = data;
    }

    @Override
    public boolean isUpgraded() {
        return upgraded;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        data = nbt.getInt("data");
        upgraded = nbt.getBoolean("upgraded");
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        nbt.putInt("data", data);
        nbt.putBoolean("upgraded", upgraded);
    }
}
