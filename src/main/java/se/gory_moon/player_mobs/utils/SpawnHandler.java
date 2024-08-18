package se.gory_moon.player_mobs.utils;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import se.gory_moon.player_mobs.Configs;
import se.gory_moon.player_mobs.Constants;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class SpawnHandler {

    @SubscribeEvent
    public static void onCheckSpawn(FinalizeSpawnEvent event) {
        if (event.getEntity() instanceof PlayerMobEntity) {
            ResourceKey<Level> worldKey = event.getLevel().getLevel().dimension();
            if (Configs.COMMON.isDimensionBlocked(worldKey)) {
                event.setSpawnCancelled(true);
            }
        }
    }
}
