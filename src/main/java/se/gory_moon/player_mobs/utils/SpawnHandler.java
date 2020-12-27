package se.gory_moon.player_mobs.utils;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.gory_moon.player_mobs.Configs;
import se.gory_moon.player_mobs.Constants;
import se.gory_moon.player_mobs.entity.EntityRegistry;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;

import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class SpawnHandler {

    //private static MobSpawnInfo.Spawners PLAYER_MOB_SPAWNER;

    private static MobSpawnInfo.Spawners getPlayerMobSpawner() {
        return new MobSpawnInfo.Spawners(EntityRegistry.PLAYER_MOB_ENTITY.get(), Configs.COMMON.spawnWeight.get(), Configs.COMMON.spawnMinSize.get(), Configs.COMMON.spawnMaxSize.get());
    }

    public static void invalidateSpawner() {
        //Util.getServerExecutor().execute(() -> PLAYER_MOB_SPAWNER = null);
    }

    // Not fully ported from 1.15.x -> 1.16.x in Forge, requires #7555 to not require the events below
    /*@SubscribeEvent(priority = EventPriority.LOW)
    public static void potentialSpawns(WorldEvent.PotentialSpawns event) {
        if (event.getType() == EntityClassification.MONSTER) {

            boolean hasZombies = false;
            for (MobSpawnInfo.Spawners spawners : event.getList()) {
                if (spawners.type == EntityType.ZOMBIE) {
                    hasZombies = true;
                    break;
                }
            }
            if (hasZombies) {
                if (Configs.COMMON.isDimensionBlocked(event.getWorld().getDimensionType())) {
                    return;
                }
                if (PLAYER_MOB_SPAWNER == null) {
                    PLAYER_MOB_SPAWNER = getPlayerMobSpawner();
                }
                event.getList().add(PLAYER_MOB_SPAWNER);
            }
        }
    }*/

    @SubscribeEvent
    public static void onBiomeLoad(BiomeLoadingEvent event) {
        List<MobSpawnInfo.Spawners> spawnersList = event.getSpawns().getSpawner(EntityClassification.MONSTER);
        boolean hasZombies = false;
        for (MobSpawnInfo.Spawners spawners : spawnersList) {
            if (spawners.type == EntityType.ZOMBIE) {
                hasZombies = true;
                break;
            }
        }
        if (hasZombies) {
            spawnersList.add(getPlayerMobSpawner());
        }
    }

    @SubscribeEvent
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (event.getEntityLiving() instanceof PlayerMobEntity) {
            if (Configs.COMMON.isDimensionBlocked(event.getWorld().getDimensionType())) {
                event.setResult(Event.Result.DENY);
            }
        }
    }
}
