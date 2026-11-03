package net.theholyraj.rajswordmod.world.item.util.antiarmoursword;

import net.minecraft.server.MinecraftServer;
import net.theholyraj.rajswordmod.SwordMod;

import java.util.UUID;

public class ArmourStealTargetData {
    public double armor;
    public double toughness;
    public int timer;

    public ArmourStealTargetData(double armor, double toughness, int timer) {
        this.armor = armor;
        this.timer = timer;
        this.toughness = toughness;
    }

    public boolean decreaseTimer(MinecraftServer server){
        timer--;
        return timer<=0;
    }
}
