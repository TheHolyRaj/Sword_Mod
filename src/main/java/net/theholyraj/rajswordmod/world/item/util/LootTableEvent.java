package net.theholyraj.rajswordmod.world.item.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.theholyraj.rajswordmod.SwordMod;
import net.theholyraj.rajswordmod.world.config.ModCommonConfigs;
import net.theholyraj.rajswordmod.world.item.ModItems;

@Mod.EventBusSubscriber(modid = SwordMod.MODID)
public class LootTableEvent {

    public static void register() {
        MinecraftForge.EVENT_BUS.register(LootTableEvent.class);
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        //PYRAMID
        if (event.getName().equals(new ResourceLocation("minecraft", "chests/desert_pyramid"))) {
            addCustomItemToLootTable(ModItems.HOLY_SWORD.get(), event, ModCommonConfigs.DESERT_PYRAMID_SWORD_SPAWN_WEIGHT.get());
            addCustomItemToLootTable(ModItems.ANTI_ARMOR_SWORD.get(), event, ModCommonConfigs.DESERT_PYRAMID_SWORD_SPAWN_WEIGHT.get());
        }
        //JUNGLE TEMPLE
        if (event.getName().equals(new ResourceLocation("minecraft", "chests/jungle_temple"))) {
            addCustomItemToLootTable(ModItems.HOLY_SWORD.get(), event, ModCommonConfigs.JUNGLE_TEMPLE_SWORD_SPAWN_WEIGHT.get());
            addCustomItemToLootTable(ModItems.GAIA_SWORD.get(), event, ModCommonConfigs.JUNGLE_TEMPLE_SWORD_SPAWN_WEIGHT.get());
        }
        //NETHER FORTRESS
        if (event.getName().equals(new ResourceLocation("minecraft", "chests/nether_bridge"))) {
            addCustomItemToLootTable(ModItems.BLOOD_SWORD.get(), event, ModCommonConfigs.NETHER_FORTRESS_SWORD_SPAWN_WEIGHT.get());
            addCustomItemToLootTable(ModItems.SENTINEL_SWORD.get(), event, ModCommonConfigs.NETHER_FORTRESS_SWORD_SPAWN_WEIGHT.get());
        }
        //BASTION SECTIONS
        if (event.getName().equals(new ResourceLocation("minecraft", "chests/bastion_treasure"))) {
            addCustomItemToLootTable(ModItems.BLOOD_SWORD.get(), event, ModCommonConfigs.BASTION_SWORD_SPAWN_WEIGHT.get());
            addCustomItemToLootTable(ModItems.SHADOW_CLONE_SWORD.get(), event, ModCommonConfigs.BASTION_SWORD_SPAWN_WEIGHT.get());
        }
        if (event.getName().equals(new ResourceLocation("minecraft", "chests/bastion_bridge"))) {
            addCustomItemToLootTable(ModItems.BLOOD_SWORD.get(), event, ModCommonConfigs.BASTION_SWORD_SPAWN_WEIGHT.get());
            addCustomItemToLootTable(ModItems.SHADOW_CLONE_SWORD.get(), event, ModCommonConfigs.BASTION_SWORD_SPAWN_WEIGHT.get());
        }
        if (event.getName().equals(new ResourceLocation("minecraft", "chests/bastion_hoglin_stable"))) {
            addCustomItemToLootTable(ModItems.BLOOD_SWORD.get(), event, ModCommonConfigs.BASTION_SWORD_SPAWN_WEIGHT.get());
            addCustomItemToLootTable(ModItems.SHADOW_CLONE_SWORD.get(), event, ModCommonConfigs.BASTION_SWORD_SPAWN_WEIGHT.get());
        }
        if (event.getName().equals(new ResourceLocation("minecraft", "chests/bastion_hoglin_stable"))) {
            addCustomItemToLootTable(ModItems.BLOOD_SWORD.get(), event, ModCommonConfigs.BASTION_SWORD_SPAWN_WEIGHT.get());
            addCustomItemToLootTable(ModItems.SHADOW_CLONE_SWORD.get(), event, ModCommonConfigs.BASTION_SWORD_SPAWN_WEIGHT.get());
        }
        //STRONGHOLD
        if (event.getName().equals(new ResourceLocation("minecraft", "chests/stronghold_corridor"))) {
            addCustomItemToLootTable(ModItems.ANTI_ARMOR_SWORD.get(), event, ModCommonConfigs.STRONGHOLD_SWORD_SPAWN_WEIGHT.get());
            addCustomItemToLootTable(ModItems.KNOCKBACK_SWORD.get(), event, ModCommonConfigs.STRONGHOLD_SWORD_SPAWN_WEIGHT.get());
        }
        if (event.getName().equals(new ResourceLocation("minecraft", "chests/stronghold_crossing"))) {
            addCustomItemToLootTable(ModItems.ANTI_ARMOR_SWORD.get(), event, ModCommonConfigs.STRONGHOLD_SWORD_SPAWN_WEIGHT.get());
            addCustomItemToLootTable(ModItems.KNOCKBACK_SWORD.get(), event, ModCommonConfigs.STRONGHOLD_SWORD_SPAWN_WEIGHT.get());
        }
        if (event.getName().equals(new ResourceLocation("minecraft", "chests/stronghold_library"))) {
            addCustomItemToLootTable(ModItems.ANTI_ARMOR_SWORD.get(), event, ModCommonConfigs.STRONGHOLD_SWORD_SPAWN_WEIGHT.get());
            addCustomItemToLootTable(ModItems.KNOCKBACK_SWORD.get(), event, ModCommonConfigs.STRONGHOLD_SWORD_SPAWN_WEIGHT.get());
        }
        //WOODLAND MANSION
        if (event.getName().equals(new ResourceLocation("minecraft", "chests/woodland_mansion"))) {
            addCustomItemToLootTable(ModItems.SHADOW_CLONE_SWORD.get(), event, ModCommonConfigs.WOODLAND_MANSION_SWORD_SPAWN_WEIGHT.get());
            addCustomItemToLootTable(ModItems.KNOCKBACK_SWORD.get(), event, ModCommonConfigs.WOODLAND_MANSION_SWORD_SPAWN_WEIGHT.get());
        }
        //ABANDONED MINESHAFT
        if (event.getName().equals(new ResourceLocation("minecraft", "chests/abandoned_mineshaft"))) {
            addCustomItemToLootTable(ModItems.GAIA_SWORD.get(), event, ModCommonConfigs.ABANDONED_MINESHAFT_SWORD_SPAWN_WEIGHT.get());
            addCustomItemToLootTable(ModItems.DEFLECT_SWORD.get(), event, ModCommonConfigs.ABANDONED_MINESHAFT_SWORD_SPAWN_WEIGHT.get());
        }
        //DUNGEON
        if (event.getName().equals(new ResourceLocation("minecraft", "chests/simple_dungeon"))) {
            addCustomItemToLootTable(ModItems.SENTINEL_SWORD.get(), event, ModCommonConfigs.DUNGEON_SWORD_SPAWN_WEIGHT.get());
            addCustomItemToLootTable(ModItems.DEFLECT_SWORD.get(), event, ModCommonConfigs.DUNGEON_SWORD_SPAWN_WEIGHT.get());
        }
    }

    private static void addCustomItemToLootTable(Item item, LootTableLoadEvent event, int weight) {
        LootPool pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .setWeight(weight)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 1.0F))))
                .build();

        event.getTable().addPool(pool);
    }
}
