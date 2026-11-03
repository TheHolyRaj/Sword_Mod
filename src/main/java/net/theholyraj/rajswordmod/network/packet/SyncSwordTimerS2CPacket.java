package net.theholyraj.rajswordmod.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.theholyraj.rajswordmod.world.item.custom.DeflectSwordItem;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordTimer;

import java.util.function.Supplier;

public class SyncSwordTimerS2CPacket {
    private final int remainingTime;
    private final int slot;

    public SyncSwordTimerS2CPacket(int remainingTime, int slot) {
        this.remainingTime = remainingTime;
        this.slot = slot;
    }

    public SyncSwordTimerS2CPacket(FriendlyByteBuf buf) {
        this.remainingTime = buf.readInt();
        this.slot = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(remainingTime);
        buf.writeInt(slot);
    }

    public static void handle(SyncSwordTimerS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                ItemStack stack = player.getInventory().getItem(msg.slot);
                UpgradeSwordTimer.setUpgradeTimer(stack, msg.remainingTime);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
