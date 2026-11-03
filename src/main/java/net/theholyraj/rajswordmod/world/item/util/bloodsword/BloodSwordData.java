package net.theholyraj.rajswordmod.world.item.util.bloodsword;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class BloodSwordData implements IBloodSwordData {
    private float data;
    private int upgradeData;
    private boolean upgraded;
    private Map<UUID, Float> storedDamage = new HashMap<>();

    @Override
    public void setData(float data) {
        this.data = data;
    }

    @Override
    public float getData() {
        return data;
    }

    @Override
    public void setUpgradeData(int upgradeData) {
        this.upgradeData = upgradeData;
    }

    @Override
    public int getUpgradeData() {
        return this.upgradeData;
    }

    @Override
    public void setUpgraded(boolean upgraded) {
        this.upgraded = upgraded;
    }

    @Override
    public boolean isUpgraded() {
        return this.upgraded;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        this.data = nbt.getFloat("data");
        this.upgradeData = nbt.getInt("upgrade_data");
        this.upgraded = nbt.getBoolean("upgraded");
        storedDamage.clear();
        ListTag damageList =
                nbt.getList("stored_damage", Tag.TAG_COMPOUND);
        for(Tag tag : damageList) {
            CompoundTag damageTag = (CompoundTag) tag;
            storedDamage.put(damageTag.getUUID("entity"), damageTag.getFloat("damage"));
        }
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        nbt.putFloat("data", this.data);
        nbt.putInt("upgrade_data", this.upgradeData);
        nbt.putBoolean("upgraded", this.upgraded);
        ListTag damageList = new ListTag();
        for(Map.Entry<UUID, Float> entry : storedDamage.entrySet()) {

            CompoundTag damageTag = new CompoundTag();
            damageTag.putUUID("entity", entry.getKey());
            damageTag.putFloat("damage", entry.getValue());
            damageList.add(damageTag);
        }
        nbt.put(
                "stored_damage",
                damageList
        );
    }
    //UPGRADE ABILITY INFO
    @Override
    public void addStoredDamage(UUID entity, float damage) {

        storedDamage.put(
                entity,
                storedDamage.getOrDefault(entity, 0f) + damage);
    }
    @Override
    public float getStoredDamage(UUID entity) {

        return storedDamage.getOrDefault(entity, 0f);
    }
    @Override
    public void removeStoredDamage(UUID entity, float damage) {
        float remaining = storedDamage.getOrDefault(entity, 0f) - damage;
        storedDamage.put(entity, remaining);

    }
    @Override
    public void clearStoredDamage() {

        storedDamage.clear();
    }

    @Override
    public void removeEmptyEntites() {
        Iterator<Map.Entry<UUID, Float>> iterator = storedDamage.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<UUID, Float> entry = iterator.next();

            if (entry.getValue() <= 0) {
                iterator.remove();
            }
        }
    }

    @Override
    public Map<UUID, Float> getStoredDamageMap() {

        return storedDamage;
    }
}

