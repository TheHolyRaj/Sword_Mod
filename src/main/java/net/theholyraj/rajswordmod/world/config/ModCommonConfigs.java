package net.theholyraj.rajswordmod.world.config;

import net.minecraftforge.common.ForgeConfigSpec;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModCommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // --- ARROW RENDER ---
    public static final ForgeConfigSpec.ConfigValue<Integer> ARROW_RENDER_UPGRADE_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<Integer> ARROW_RENDER_UPGRADE_TIME;
    public static final ForgeConfigSpec.DoubleValue ARROW_RENDER_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue ARROW_RENDER_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue ARROW_RENDER_UPGRADE_BONUS_DAMAGE;

    // --- SENTINEL SWORD ---
    public static final ForgeConfigSpec.ConfigValue<Integer> SENTINEL_SWORD_UPGRADE_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<Integer> SENTINEL_SWORD_UPGRADE_TIME;
    public static final ForgeConfigSpec.DoubleValue SENTINEL_SWORD_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue SENTINEL_SWORD_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue SENTINEL_SWORD_UPGRADE_BONUS_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> SENTINEL_SWORD_CHARGE_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> SENTINEL_SWORD_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<Integer> SENTINEL_SWORD_UPGRADE_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<Integer> SENTINEL_SWORD_UPGRADE_CHARGE_TIME;

    // --- LEG BREAKER ---
    public static final ForgeConfigSpec.ConfigValue<Integer> KNOCKBACK_SWORD_CHARGE_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> KNOCKBACK_SWORD_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue KNOCKBACK_SWORD_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue KNOCKBACK_SWORD_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue KNOCKBACK_SWORD_UPGRADE_BONUS_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> KNOCKBACK_SWORD_UPGRADE_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<Integer> KNOCKBACK_SWORD_UPGRADE_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> KNOCKBACK_SWORD_UPGRADED_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<Integer> KNOCKBACK_SWORD_POWER;

    // --- LIGHT BRINGER ---
    public static final ForgeConfigSpec.ConfigValue<Integer> HOLY_FIRE_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> MIN_HOLY_FIRE_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_HOLY_FIRE_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> HOLY_FIRE_DURATION;
    public static final ForgeConfigSpec.DoubleValue HOLY_EXPLOSION_CHANCE;
    public static final ForgeConfigSpec.DoubleValue HOLY_SWORD_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue HOLY_SWORD_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue HOLY_SWORD_UPGRADE_BONUS_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> HOLY_SWORD_UPGRADE_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<Integer> HOLY_SWORD_UPGRADE_TIME;

    // --- GAIA BLADE ---
    public static final ForgeConfigSpec.DoubleValue GAIA_BLADE_PROJECTILE_DAMAGE_PERCENTAGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> GAIA_BLADE_UPGRADE_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<Integer> GAIA_BLADE_UPGRADE_TIME;
    public static final ForgeConfigSpec.DoubleValue GAIA_BLADE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue GAIA_BLADE_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue GAIA_BLADE_UPGRADE_BONUS_DAMAGE;

    // --- BLOOD GORGER ---
    public static final ForgeConfigSpec.DoubleValue BLOOD_GORGER_STORED_HEALTH_MAX;
    public static final ForgeConfigSpec.DoubleValue BLOOD_GORGER_LIFESTEAL_PERCENT;
    public static final ForgeConfigSpec.ConfigValue<Integer> BLOOD_GORGER_HEAL_SPEED;
    public static final ForgeConfigSpec.ConfigValue<Integer> BLOOD_GORGER_UPGRADE_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<Integer> BLOOD_GORGER_UPGRADE_TIME;
    public static final ForgeConfigSpec.DoubleValue BLOOD_GORGER_UPGRADE_DAMAGE_STORED_PERCENTAGE;
    public static final ForgeConfigSpec.ConfigValue<Float> BLOOD_GORGER_UPGRADE_DAMAGE_TICK;
    public static final ForgeConfigSpec.DoubleValue BLOOD_GORGER_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue BLOOD_GORGER_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue BLOOD_GORGER_UPGRADE_BONUS_DAMAGE;


    // --- ARMOR PIERCER ---
    public static final ForgeConfigSpec.ConfigValue<Integer> ANTI_ARMOR_EFFECT_DURATION;
    public static final ForgeConfigSpec.DoubleValue ANTI_ARMOR_EFFECT_REDUCTION;
    public static final ForgeConfigSpec.ConfigValue<Integer> ANTI_ARMOR_CHARGE_DURATION;
    public static final ForgeConfigSpec.ConfigValue<Integer> ANTI_ARMOR_CHARGE_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<Integer> ANTI_ARMOR_CHARGE_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> ANTI_ARMOR_UPGRADE_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<Integer> ANTI_ARMOR_UPGRADE_TIME;
    public static final ForgeConfigSpec.DoubleValue ANTI_ARMOR_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue ANTI_ARMOR_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue ANTI_ARMOR_UPGRADE_BONUS_DAMAGE;

    // --- DARK DECEIVER ---
    public static final ForgeConfigSpec.ConfigValue<Integer> DARK_DECEIVER_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<Integer> SHADOW_CLONE_LIFESPAN;
    public static final ForgeConfigSpec.ConfigValue<Integer> DARK_DECEIVER_ENCHANT_BONUS_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> DARK_DECEIVER_UPGRADE_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<Integer> DARK_DECEIVER_UPGRADE_TIME;
    public static final ForgeConfigSpec.DoubleValue DARK_DECEIVER_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue DARK_DECEIVER_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue DARK_DECEIVER_UPGRADE_BONUS_DAMAGE;

    // --- ENCHANTMENTS ---
    public static final ForgeConfigSpec.ConfigValue<Float> ARMOURED_ENCHANT;
    public static final ForgeConfigSpec.ConfigValue<Float> REACH_ENCHANT;
    public static final ForgeConfigSpec.ConfigValue<Float> BACKSTABBER_ENCHANT;




    // --- WORLD SPAWNS ---
    public static final ForgeConfigSpec.ConfigValue<Integer> JUNGLE_TEMPLE_SWORD_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> DESERT_PYRAMID_SWORD_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> NETHER_FORTRESS_SWORD_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> BASTION_SWORD_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> STRONGHOLD_SWORD_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> WOODLAND_MANSION_SWORD_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> ABANDONED_MINESHAFT_SWORD_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> DUNGEON_SWORD_SPAWN_WEIGHT;

    static {
        BUILDER.push("Sword Mod Configs");

        BUILDER.push("Arrow Render");
        ARROW_RENDER_UPGRADE_AMOUNT = BUILDER.define("Upgrade Amount", 30);
        ARROW_RENDER_UPGRADE_TIME = BUILDER.define("Upgrade Time", 1200);
        ARROW_RENDER_DAMAGE = BUILDER.defineInRange("Base Damage", 7.0, 0.0, 100.0);
        ARROW_RENDER_ATTACK_SPEED = BUILDER.defineInRange("Attack Speed", 1.8, 0.0, 10.0);
        ARROW_RENDER_UPGRADE_BONUS_DAMAGE = BUILDER.defineInRange("Upgrade Bonus Damage", 2.0, 0.0, 100.0);
        BUILDER.pop();

        BUILDER.push("Sentinel Sword");
        SENTINEL_SWORD_UPGRADE_AMOUNT = BUILDER.define("Upgrade Amount", 25);
        SENTINEL_SWORD_UPGRADE_TIME = BUILDER.define("Upgrade Time", 1200);
        SENTINEL_SWORD_DAMAGE = BUILDER.defineInRange("Base Damage", 10.0, 0.0, 100.0);
        SENTINEL_SWORD_ATTACK_SPEED = BUILDER.defineInRange("Attack Speed", 1.2, 0.0, 10.0);
        SENTINEL_SWORD_UPGRADE_BONUS_DAMAGE = BUILDER.defineInRange("Upgrade Bonus Damage", 3.0, 0.0, 100.0);
        SENTINEL_SWORD_CHARGE_TIME = BUILDER.define("Charge Time", 10);
        SENTINEL_SWORD_COOLDOWN = BUILDER.define("Cooldown", 100);
        SENTINEL_SWORD_UPGRADE_CHARGE_TIME = BUILDER.define("Upgraded Charge Time", 15);
        SENTINEL_SWORD_UPGRADE_COOLDOWN = BUILDER.define("Upgraded Cooldown", 75);
        BUILDER.pop();

        BUILDER.push("Leg Breaker");
        KNOCKBACK_SWORD_UPGRADE_AMOUNT = BUILDER.define("Upgrade Amount", 25);
        KNOCKBACK_SWORD_UPGRADE_TIME = BUILDER.define("Upgrade Time", 1200);
        KNOCKBACK_SWORD_DAMAGE = BUILDER.defineInRange("Base Damage", 6.0, 0.0, 100.0);
        KNOCKBACK_SWORD_ATTACK_SPEED = BUILDER.defineInRange("Attack Speed", 1.6, 0.0, 10.0);
        KNOCKBACK_SWORD_UPGRADE_BONUS_DAMAGE = BUILDER.defineInRange("Upgrade Bonus Damage", 2.0, 0.0, 100.0);
        KNOCKBACK_SWORD_CHARGE_TIME = BUILDER.define("Max Charge Time", 100);
        KNOCKBACK_SWORD_COOLDOWN = BUILDER.define("Cooldown", 220);
        KNOCKBACK_SWORD_UPGRADED_COOLDOWN = BUILDER.define("Upgraded Cooldown", 110);
        KNOCKBACK_SWORD_POWER = BUILDER.define("Knockback Power", 100);
        BUILDER.pop();

        BUILDER.push("Light Bringer");
        HOLY_FIRE_DAMAGE = BUILDER.define("Holy Fire Percent", 3);
        MIN_HOLY_FIRE_DAMAGE = BUILDER.define("Fire Min Damage", 2);
        MAX_HOLY_FIRE_DAMAGE = BUILDER.define("Fire Max Damage", 99999);
        HOLY_FIRE_DURATION = BUILDER.define("Fire Duration", 50);
        HOLY_EXPLOSION_CHANCE = BUILDER.defineInRange("Explosion Chance", 2.5, 0.0, 100.0);
        HOLY_SWORD_UPGRADE_AMOUNT = BUILDER.define("Upgrade Amount", 25);
        HOLY_SWORD_UPGRADE_TIME = BUILDER.define("Upgrade Time", 1200);
        HOLY_SWORD_DAMAGE = BUILDER.defineInRange("Base Damage", 6.0, 0.0, 100.0);
        HOLY_SWORD_ATTACK_SPEED = BUILDER.defineInRange("Attack Speed", 1.6, 0.0, 10.0);
        HOLY_SWORD_UPGRADE_BONUS_DAMAGE = BUILDER.defineInRange("Upgrade Bonus Damage", 2.0, 0.0, 100.0);
        BUILDER.pop();

        BUILDER.push("Gaia Blade");
        GAIA_BLADE_PROJECTILE_DAMAGE_PERCENTAGE = BUILDER.defineInRange("Projectile Damage Percent", 0.5, 0.0, 1.0);
        GAIA_BLADE_UPGRADE_AMOUNT = BUILDER.define("Upgrade Amount", 35);
        GAIA_BLADE_UPGRADE_TIME = BUILDER.define("Upgrade Time", 1200);
        GAIA_BLADE_DAMAGE = BUILDER.defineInRange("Base Damage", 8.0, 0.0, 100.0);
        GAIA_BLADE_ATTACK_SPEED = BUILDER.defineInRange("Attack Speed", 1.2, 0.0, 10.0);
        GAIA_BLADE_UPGRADE_BONUS_DAMAGE = BUILDER.defineInRange("Upgrade Bonus Damage", 2.0, 0.0, 100.0);
        BUILDER.pop();

        BUILDER.push("Blood Gorger");
        BLOOD_GORGER_STORED_HEALTH_MAX = BUILDER.defineInRange("Max Stored Health", 20.0, 0.0, 1000.0);
        BLOOD_GORGER_LIFESTEAL_PERCENT = BUILDER.defineInRange("Lifesteal Percent", 0.25, 0.0, 1.0);
        BLOOD_GORGER_HEAL_SPEED = BUILDER.defineInRange("Heal Speed Ticks", 5, 1,1000);
        BLOOD_GORGER_UPGRADE_AMOUNT = BUILDER.define("Upgrade Amount", 50);
        BLOOD_GORGER_UPGRADE_TIME = BUILDER.define("Upgrade Time", 1200);
        BLOOD_GORGER_UPGRADE_DAMAGE_STORED_PERCENTAGE = BUILDER.defineInRange("Stored Damage Percentage", 0.5, 0.0, 1.0);
        BLOOD_GORGER_UPGRADE_DAMAGE_TICK = BUILDER.define("Max damage sword ability can deal at a time", 7.5f);
        BLOOD_GORGER_DAMAGE = BUILDER.defineInRange("Base Damage", 8.0, 0.0, 100.0);
        BLOOD_GORGER_ATTACK_SPEED = BUILDER.defineInRange("Attack Speed", 1.6, 0.0, 10.0);
        BLOOD_GORGER_UPGRADE_BONUS_DAMAGE = BUILDER.defineInRange("Upgrade Bonus Damage", 2.0, 0.0, 100.0);
        BUILDER.pop();

        BUILDER.push("Armor Piercer");
        ANTI_ARMOR_EFFECT_DURATION = BUILDER.define("Effect Duration", 300);
        ANTI_ARMOR_EFFECT_REDUCTION = BUILDER.defineInRange("Armor Reduction Percent", 0.2, 0.0, 1.0);
        ANTI_ARMOR_CHARGE_DURATION = BUILDER.define("Charge Duration", 30);
        ANTI_ARMOR_CHARGE_COOLDOWN = BUILDER.define("Cooldown", 200);
        ANTI_ARMOR_CHARGE_DAMAGE = BUILDER.define("Charge Damage", 12);
        ANTI_ARMOR_UPGRADE_AMOUNT = BUILDER.define("Upgrade Amount", 40);
        ANTI_ARMOR_UPGRADE_TIME = BUILDER.define("Upgrade Time", 1200);
        ANTI_ARMOR_DAMAGE = BUILDER.defineInRange("Base Damage", 8.0, 0.0, 100.0);
        ANTI_ARMOR_ATTACK_SPEED = BUILDER.defineInRange("Attack Speed", 1.6, 0.0, 10.0);
        ANTI_ARMOR_UPGRADE_BONUS_DAMAGE = BUILDER.defineInRange("Upgrade Bonus Damage", 2.0, 0.0, 100.0);
        BUILDER.pop();

        BUILDER.push("Dark Deceiver");
        DARK_DECEIVER_COOLDOWN = BUILDER.define("Cooldown", 840);
        SHADOW_CLONE_LIFESPAN = BUILDER.define("Clone Lifespan", 300);
        DARK_DECEIVER_ENCHANT_BONUS_TIME =  BUILDER.define("Bonus Time for Clone from Enchantment", 30);
        DARK_DECEIVER_UPGRADE_AMOUNT = BUILDER.define("Upgrade Amount", 40);
        DARK_DECEIVER_UPGRADE_TIME = BUILDER.define("Upgrade Time", 1200);
        DARK_DECEIVER_DAMAGE = BUILDER.defineInRange("Base Damage", 6.0, 0.0, 100.0);
        DARK_DECEIVER_ATTACK_SPEED = BUILDER.defineInRange("Attack Speed", 1.8, 0.0, 10.0);
        DARK_DECEIVER_UPGRADE_BONUS_DAMAGE = BUILDER.defineInRange("Upgrade Bonus Damage", 1.5, 0.0, 100.0);
        BUILDER.pop();

        BUILDER.push("Custom Enchantments");
        ARMOURED_ENCHANT = BUILDER.define("Bonus Armor per Level", 2.0f);
        REACH_ENCHANT = BUILDER.define("Bonus Reach per Level",0.15f);
        BACKSTABBER_ENCHANT = BUILDER.define("Bonus Damage Percent per Level",0.33f);
        BUILDER.pop();

        BUILDER.push("Loot Spawns");
        JUNGLE_TEMPLE_SWORD_SPAWN_WEIGHT = BUILDER.define("Jungle Temple Weight", 4);
        DESERT_PYRAMID_SWORD_SPAWN_WEIGHT = BUILDER.define("Desert Pyramid Weight", 3);
        NETHER_FORTRESS_SWORD_SPAWN_WEIGHT = BUILDER.define("Nether Fortress Weight", 3);
        BASTION_SWORD_SPAWN_WEIGHT = BUILDER.define("Bastion Weight", 3);
        STRONGHOLD_SWORD_SPAWN_WEIGHT = BUILDER.define("Stronghold Weight", 4);
        WOODLAND_MANSION_SWORD_SPAWN_WEIGHT = BUILDER.define("Woodland Mansion Weight", 4);
        ABANDONED_MINESHAFT_SWORD_SPAWN_WEIGHT = BUILDER.define("Mineshaft Weight", 3);
        DUNGEON_SWORD_SPAWN_WEIGHT = BUILDER.define("Dungeon Weight", 3);
        BUILDER.pop();

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}