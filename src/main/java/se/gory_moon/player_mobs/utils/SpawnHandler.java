package se.gory_moon.player_mobs.utils;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.gory_moon.player_mobs.Configs;
import se.gory_moon.player_mobs.Constants;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;


@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class SpawnHandler {

    @SubscribeEvent
    public static void onCheckSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getEntity() instanceof PlayerMobEntity) {
            ResourceKey<Level> worldKey = event.getLevel().getLevel().dimension();
            if (Configs.COMMON.isDimensionBlocked(worldKey)) {
                event.setSpawnCancelled(true);
            }
        }
    }
}
