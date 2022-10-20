package se.gory_moon.player_mobs;

import com.tterrag.registrate.providers.ProviderType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.data.event.GatherDataEvent;
import se.gory_moon.player_mobs.entity.EntityRegistry;
import se.gory_moon.player_mobs.sound.SoundRegistry;
import se.gory_moon.player_mobs.utils.CustomRegistrate;
import se.gory_moon.player_mobs.utils.NameManager;
import se.gory_moon.player_mobs.world.BiomeModifierRegistry;

@Mod(Constants.MOD_ID)
public class PlayerMobs {

    private static final Lazy<CustomRegistrate> REGISTRATE = Lazy.of(() -> CustomRegistrate.create(Constants.MOD_ID));


    public PlayerMobs() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(EntityRegistry::registerEntityAttributes);
        modBus.addListener(this::gatherData);
        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(this::registerCommands);
        eventBus.addListener(this::serverAboutToStart);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configs.commonSpec);
        modBus.register(Configs.COMMON);
        EntityRegistry.init();
        SoundRegistry.init();
        BiomeModifierRegistry.BIOME_MODIFIER_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static CustomRegistrate getRegistrate() {
        return REGISTRATE.get();
    }

    private void gatherData(GatherDataEvent event) {
        getRegistrate().addDataGenerator(ProviderType.LANG, prov -> {
            prov.add(LangKeys.COMMANDS_RELOAD_START.key(), "[PlayerMobs] Reloading remote links... ");
            prov.add(LangKeys.COMMANDS_RELOAD_DONE.key(), "[PlayerMobs] Reloaded remote links with %d names");
            prov.add(LangKeys.COMMANDS_SPAWN_SUCCESS.key(), "[PlayerMobs] Spawned %s");
            prov.add(LangKeys.COMMANDS_SPAWN_FAILED.key(), "[PlayerMobs] Unable to spawn entity");
            prov.add(LangKeys.COMMANDS_SPAWN_UUID.key(), "[PlayerMobs] Unable to spawn entity due to duplicate UUIDs");
            prov.add(LangKeys.COMMANDS_SPAWN_INVALID_POS.key(), "[PlayerMobs] Invalid position for spawn");
        });
    }

    private void serverAboutToStart(ServerAboutToStartEvent event) {
        NameManager.INSTANCE.init();
    }

    private void registerCommands(RegisterCommandsEvent event) {
        PlayerMobsCommand.register(event.getDispatcher());
    }
}
