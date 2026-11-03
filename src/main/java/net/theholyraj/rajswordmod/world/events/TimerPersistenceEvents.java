package net.theholyraj.rajswordmod.world.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordItem;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordTimer;


@Mod.EventBusSubscriber(modid = SwordMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TimerPersistenceEvents {

    private static final String TIMER_NBT_TAG = "UpgradeTimerValue";


    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getInventory().items.forEach(TimerPersistenceEvents::loadTimerFromNBT);
        }
    }

    private static void loadTimerFromNBT(ItemStack stack) {
        if (stack.getItem() instanceof UpgradeSwordItem && stack.hasTag()) {
            int time = stack.getOrCreateTag().getInt(TIMER_NBT_TAG);
            if (time > 0) {
                UpgradeSwordTimer.setUpgradeTimer(stack, time);
                stack.getOrCreateTag().remove(TIMER_NBT_TAG);
            }
        }
    }


    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getInventory().items.forEach(TimerPersistenceEvents::saveTimerToNBT);
        }
    }

    private static void saveTimerToNBT(ItemStack stack) {
        if (stack.getItem() instanceof UpgradeSwordItem) {
            int remainingTime = UpgradeSwordTimer.getUpgradeTimer(stack);
            if (remainingTime > 0) {
                stack.getOrCreateTag().putInt(TIMER_NBT_TAG, remainingTime);
                UpgradeSwordTimer.removeUpgradeTimer(stack);
            }
        }
    }
}
