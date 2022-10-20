package se.gory_moon.player_mobs.utils;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.gory_moon.player_mobs.Configs;
import se.gory_moon.player_mobs.Constants;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;


@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class SpawnHandler {

    @SubscribeEvent
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (event.getEntity() instanceof PlayerMobEntity) {
            ResourceKey<Level> worldKey = Level.OVERWORLD;
            if (event.getLevel() instanceof ServerLevelAccessor) {
                worldKey = ((ServerLevelAccessor) event.getLevel()).getLevel().dimension();
            } else if (event.getLevel() instanceof Level) {
                worldKey = ((Level) event.getLevel()).dimension();
            }

            if (Configs.COMMON.isDimensionBlocked(worldKey)) {
                event.setResult(Event.Result.DENY);
            }
        }
    }
}
