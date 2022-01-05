package se.gory_moon.player_mobs.utils;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings;
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

    private static MobSpawnSettings.SpawnerData getPlayerMobSpawner() {
        return new MobSpawnSettings.SpawnerData(EntityRegistry.PLAYER_MOB_ENTITY.get(), Configs.COMMON.spawnWeight.get(), Configs.COMMON.spawnMinSize.get(), Configs.COMMON.spawnMaxSize.get());
    }

    @SubscribeEvent
    public static void onBiomeLoad(BiomeLoadingEvent event) {
        List<MobSpawnSettings.SpawnerData> spawnersList = event.getSpawns().getSpawner(MobCategory.MONSTER);
        boolean hasZombies = false;
        for (MobSpawnSettings.SpawnerData spawners : spawnersList) {
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
            ResourceKey<Level> worldKey = Level.OVERWORLD;
            if (event.getWorld() instanceof ServerLevelAccessor) {
                worldKey = ((ServerLevelAccessor) event.getWorld()).getLevel().dimension();
            } else if (event.getWorld() instanceof Level) {
                worldKey = ((Level) event.getWorld()).dimension();
            }

            if (Configs.COMMON.isDimensionBlocked(worldKey)) {
                event.setResult(Event.Result.DENY);
            }
        }
    }
}
