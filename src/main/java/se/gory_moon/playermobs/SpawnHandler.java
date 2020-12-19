package se.gory_moon.playermobs;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Util;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.gory_moon.playermobs.entity.EntityRegistry;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class SpawnHandler {

    private static MobSpawnInfo.Spawners PLAYER_MOB_SPAWNER;

    @SubscribeEvent(priority = EventPriority.LOW)
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
                    PLAYER_MOB_SPAWNER = new MobSpawnInfo.Spawners(EntityRegistry.PLAYER_MOB_ENTITY.get(), Configs.COMMON.spawnWeight.get(), Configs.COMMON.spawnMinSize.get(), Configs.COMMON.spawnMaxSize.get());
                }
                event.getList().add(PLAYER_MOB_SPAWNER);
            }
        }
    }

    public static void invalidateSpawner() {
        Util.getServerExecutor().execute(() -> PLAYER_MOB_SPAWNER = null);
    }
}
