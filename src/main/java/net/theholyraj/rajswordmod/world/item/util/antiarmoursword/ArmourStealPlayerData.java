package net.theholyraj.rajswordmod.world.item.util.antiarmoursword;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ArmourStealPlayerData {
    private static final UUID ARMOR_INCREASE_UUID =
            UUID.fromString("91AEAA56-376B-4498-935B-2F7F42070635");
    private static final UUID ARMOR_TOUGHNESS_INCREASE_UUID =
            UUID.fromString("91AEAA56-376B-4498-935B-2F7F68074206");

    private  final Map<UUID, ArmourStealTargetData> statsMap = new HashMap<>();

    public UUID playerUUID;
    public boolean needToUpdateStats = false;


    public ArmourStealPlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public void addTargetToMap( UUID targetUUID, double armour, double toughness){
        if (statsMap.containsKey(targetUUID)){
            statsMap.get(targetUUID).timer = ModCommonConfigs.ANTI_ARMOR_EFFECT_DURATION.get();
            statsMap.get(targetUUID).armor = armour;
            statsMap.get(targetUUID).toughness = toughness;
        }
        else {
            statsMap.put(targetUUID, new ArmourStealTargetData(armour,toughness, ModCommonConfigs.ANTI_ARMOR_EFFECT_DURATION.get()));
        }
        this.needToUpdateStats = true;
    }
    public boolean decreaseTimer(MinecraftServer server){
        Iterator<Map.Entry<UUID, ArmourStealTargetData>> iterator = statsMap.entrySet().iterator();
        boolean noStatsLeft = true;
        while (iterator.hasNext()){
            Map.Entry<UUID, ArmourStealTargetData> entry = iterator.next();
            if (!entry.getValue().decreaseTimer(server)){
                noStatsLeft = false;
            }
            else {
                iterator.remove();
                this.needToUpdateStats = true;
            }
        }
        return noStatsLeft;
    }

    public void setPlayersStats(MinecraftServer server) {
        if (needToUpdateStats){
            double armour = 0;
            double toughness = 0;
            for (ArmourStealTargetData data : statsMap.values()) {
                armour += data.armor;
                toughness += data.toughness;
            }
            ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
            if (player == null) {
                return;
            }
            if (player.getAttribute(Attributes.ARMOR) != null) {

                player.getAttribute(Attributes.ARMOR)
                        .removeModifier(ARMOR_INCREASE_UUID);

                player.getAttribute(Attributes.ARMOR)
                        .addTransientModifier(new AttributeModifier(
                                ARMOR_INCREASE_UUID,
                                "Armor Increase",
                                armour,
                                AttributeModifier.Operation.ADDITION));
            }
            if (player.getAttribute(Attributes.ARMOR_TOUGHNESS) != null) {

                player.getAttribute(Attributes.ARMOR_TOUGHNESS)
                        .removeModifier(ARMOR_TOUGHNESS_INCREASE_UUID);

                player.getAttribute(Attributes.ARMOR_TOUGHNESS)
                        .addTransientModifier(new AttributeModifier(
                                ARMOR_TOUGHNESS_INCREASE_UUID,
                                "Armor Increase",
                                toughness,
                                AttributeModifier.Operation.ADDITION));
            }
            this.needToUpdateStats = false;
        }
    }
}
