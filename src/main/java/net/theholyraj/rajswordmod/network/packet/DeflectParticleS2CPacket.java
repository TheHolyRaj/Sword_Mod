package net.theholyraj.rajswordmod.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import net.theholyraj.rajswordmod.client.particle.ModParticles;
import net.theholyraj.rajswordmod.client.sound.ModSounds;

import java.util.function.Supplier;

public class DeflectParticleS2CPacket {
    private final double x;
    private final double y;
    private final double z;
    private final double pX;
    private final double pY;
    private final double pZ;

    public DeflectParticleS2CPacket(double x, double y, double z, double pX, double pY, double pZ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pX = pX;
        this.pY = pY;
        this.pZ = pZ;
    }

    public DeflectParticleS2CPacket(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.pX = buf.readDouble();
        this.pY = buf.readDouble();
        this.pZ = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(pX);
        buf.writeDouble(pY);
        buf.writeDouble(pZ);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // HERE WE ARE ON THE CLIENT!
            Minecraft.getInstance().level.addParticle(ModParticles.DEFLECT_PARTICLES.get(),true,x,y,z,pX,pY,pZ);
            Minecraft.getInstance().level.playSound(null, x,y,z, ModSounds.PROJECTILE_SLASH.get(), SoundSource.PLAYERS,0.1f,1f);
        });
        return true;
    }
}
