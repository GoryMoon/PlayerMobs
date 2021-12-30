package se.gory_moon.player_mobs.utils;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
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

    private static MobSpawnInfo.Spawners getPlayerMobSpawner() {
        return new MobSpawnInfo.Spawners(EntityRegistry.PLAYER_MOB_ENTITY.get(), Configs.COMMON.spawnWeight.get(), Configs.COMMON.spawnMinSize.get(), Configs.COMMON.spawnMaxSize.get());
    }

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
            RegistryKey<World> worldKey = World.OVERWORLD;
            if (event.getWorld() instanceof IServerWorld) {
                worldKey = ((IServerWorld) event.getWorld()).getLevel().dimension();
            } else if (event.getWorld() instanceof World) {
                worldKey = ((World) event.getWorld()).dimension();
            }

            if (Configs.COMMON.isDimensionBlocked(worldKey)) {
                event.setResult(Event.Result.DENY);
            }
        }
    }
}
