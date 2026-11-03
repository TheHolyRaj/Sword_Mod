package net.theholyraj.rajswordmod.world.item.util.bloodsword;

import net.minecraft.nbt.CompoundTag;

import java.util.Map;
import java.util.UUID;

public interface IBloodSwordData {
    float getData();
    void setData(float data);

    int getUpgradeData();
    void setUpgradeData(int data);

    boolean isUpgraded();
    void setUpgraded(boolean upgraded);

    void readFromNBT(CompoundTag nbt);
    void writeToNBT(CompoundTag nbt);

    void addStoredDamage(UUID entity, float damage);

    float getStoredDamage(UUID entity);

    void clearStoredDamage();

    void removeEmptyEntites();

    void removeStoredDamage(UUID entity, float damage);

    Map<UUID, Float> getStoredDamageMap();
}
