package se.gory_moon.player_mobs.data;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import se.gory_moon.player_mobs.Constants;
import se.gory_moon.player_mobs.LangKeys;
import se.gory_moon.player_mobs.entity.EntityRegistry;

public class PlayerMobsLanguageProvider extends LanguageProvider {
    public PlayerMobsLanguageProvider(PackOutput output) {
        super(output, Constants.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(EntityRegistry.PLAYER_MOB_ENTITY.get(), "Player Mob");
        add(EntityRegistry.PLAYER_MOD_SPAWN_EGG.get(), "Player Mob Spawn Egg");

        add(LangKeys.COMMANDS_RELOAD_START.key(), "[PlayerMobs] Reloading remote links... ");
        add(LangKeys.COMMANDS_RELOAD_DONE.key(), "[PlayerMobs] Reloaded remote links with %d names");
        add(LangKeys.COMMANDS_SPAWN_SUCCESS.key(), "[PlayerMobs] Spawned %s");
        add(LangKeys.COMMANDS_SPAWN_FAILED.key(), "[PlayerMobs] Unable to spawn entity");
        add(LangKeys.COMMANDS_SPAWN_UUID.key(), "[PlayerMobs] Unable to spawn entity due to duplicate UUIDs");
        add(LangKeys.COMMANDS_SPAWN_INVALID_POS.key(), "[PlayerMobs] Invalid position for spawn");

        add(LangKeys.PACK_DESCRIPTION.key(), "PlayerMobs data/resource pack");

        add(LangKeys.SOUND_PLAYER_MOB_HURT.key(), "Player Mob hurts");
        add(LangKeys.SOUND_PLAYER_MOB_DEATH.key(), "Player Mob dies");

        add("player_mobs.configuration.general", "General Settings");
        add("player_mobs.configuration.names", "Name settings");
        add("player_mobs.configuration.spawning", "Spawning Settings");

        add("player_mobs.configuration.pickupItemsChance", "Pickup Item Chance");
        add("player_mobs.configuration.playerHeadDropChance", "Player Head Drop Chance");
        add("player_mobs.configuration.offhandSpawnDifficulty", "Offhand Spawn Difficulty");
        add("player_mobs.configuration.mobHeadDropChance", "Mob Head Drop Chance");
        add("player_mobs.configuration.allowTippedArrows", "Spawn Tipped Arrows");
        add("player_mobs.configuration.offhandSpawnChance", "Offhand Spawn Chance");
        add("player_mobs.configuration.dimensionBlocklist", "Dimension Blocklist");
        add("player_mobs.configuration.spawnItems", "Spawn Items");
        add("player_mobs.configuration.nameLinks", "Name Links");
        add("player_mobs.configuration.tippedArrowBlocklist", "Tipped Arrow Blocklist");
        add("player_mobs.configuration.openDoors", "Open Doors");
        add("player_mobs.configuration.openDoorsDifficulty", "Open Doors Difficulty");
        add("player_mobs.configuration.forceItemSpawn", "Force Items Spawn");
        add("player_mobs.configuration.useWhitelist", "Use Whitelist");
        add("player_mobs.configuration.spawnOffhandItems", "Spawn Items Offhand");
        add("player_mobs.configuration.mobNames", "Mob Names");
        add("player_mobs.configuration.attackTwin", "Attack Twin");
        add("player_mobs.configuration.babySpawnChance", "Baby Spawn Chance");
        add("player_mobs.configuration.nameLinksReloadInterval", "Reload Interval");
    }
}
