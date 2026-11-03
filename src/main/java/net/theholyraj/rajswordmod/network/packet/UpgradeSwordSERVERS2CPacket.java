package net.theholyraj.rajswordmod.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.theholyraj.rajswordmod.world.item.ModItems;
import net.theholyraj.rajswordmod.world.item.custom.DeflectSwordItem;
import net.theholyraj.rajswordmod.world.item.util.upgradesword.UpgradeSwordItem;

import java.util.function.Supplier;

public class UpgradeSwordSERVERS2CPacket {
    private final int entityId;

    public UpgradeSwordSERVERS2CPacket(int entityId) {
        this.entityId = entityId;
    }

    public UpgradeSwordSERVERS2CPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Entity entity = mc.level.getEntity(this.entityId);
            if (entity instanceof Player player) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof UpgradeSwordItem){
                    UpgradeSwordItem.setUpgraded(stack, true);
                }
            }
        });
        return true;
    }
}
