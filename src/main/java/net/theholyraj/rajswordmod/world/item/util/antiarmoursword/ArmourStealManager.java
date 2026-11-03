package net.theholyraj.rajswordmod.world.item.util.antiarmoursword;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.theholyraj.rajswordmod.SwordMod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = SwordMod.MODID)
public class ArmourStealManager {
    private static final Map<UUID, ArmourStealPlayerData> playersMap = new HashMap<>();

    private static void reduceTimers(MinecraftServer server){
        Iterator<Map.Entry<UUID, ArmourStealPlayerData>> iterator = playersMap.entrySet().iterator();

        while (iterator.hasNext()){
            Map.Entry<UUID, ArmourStealPlayerData> entry = iterator.next();
            entry.getValue().setPlayersStats(server);
            if (entry.getValue().decreaseTimer(server)){
                entry.getValue().setPlayersStats(server);
                iterator.remove();
            }
        }
    }

    public static void addPlayerToMap(UUID playerUUID, UUID targetUUID, double armour, double toughness){
        if (playersMap.containsKey(playerUUID)){
            playersMap.get(playerUUID).addTargetToMap(targetUUID, armour, toughness);
        }
        else {
            playersMap.put(playerUUID, new ArmourStealPlayerData(playerUUID));
            playersMap.get(playerUUID).addTargetToMap(targetUUID, armour, toughness);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        reduceTimers(event.getServer());
    }
}
