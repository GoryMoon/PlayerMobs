package se.gory_moon.player_mobs;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.NonNullLazyValue;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import se.gory_moon.player_mobs.entity.EntityRegistry;
import se.gory_moon.player_mobs.utils.NameManager;
import se.gory_moon.player_mobs.sound.SoundRegistry;
import se.gory_moon.player_mobs.utils.CustomRegistrate;

@Mod(Constants.MOD_ID)
public class PlayerMobs {

    public static final NonNullLazyValue<CustomRegistrate> REGISTRATE = new NonNullLazyValue<>(() -> CustomRegistrate.create(Constants.MOD_ID));

    public PlayerMobs() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        modBus.addListener(this::gatherData);
        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(this::registerCommands);
        eventBus.addListener(this::serverAboutToStart);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configs.commonSpec);
        modBus.register(Configs.COMMON);
        EntityRegistry.init();
        SoundRegistry.init();
    }

    public static CustomRegistrate getRegistrate() {
        return REGISTRATE.get();
    }

    private void setup(FMLCommonSetupEvent event) {
        EntityRegistry.registerEntityAttributes();
    }

    private void gatherData(GatherDataEvent event) {
        getRegistrate().addDataGenerator(ProviderType.LANG, prov -> {
            prov.add(LangKeys.COMMANDS_RELOAD_START.key(), "[PlayerMobs] Reloading remote links... ");
            prov.add(LangKeys.COMMANDS_RELOAD_DONE.key(), "[PlayerMobs] Reloaded remote links with %d names");
            prov.add(LangKeys.COMMANDS_SPAWN_SUCCESS.key(), "[PlayerMobs] Spawned %s");
            prov.add(LangKeys.COMMANDS_SPAWN_FAILED.key(), "[PlayerMobs] Unable to spawn entity");
            prov.add(LangKeys.COMMANDS_SPAWN_UUID.key(), "[PlayerMobs] Unable to spawn entity due to duplicate UUIDs");
            prov.add(LangKeys.COMMANDS_SPAWN_INVALID_POS.key(), "[PlayerMobs] Invalid position for spawn");

            prov.add(LangKeys.MOB_SUBTITLE_DIES.key(), "Player Mob dies");
            prov.add(LangKeys.MOB_SUBTITLE_HURT.key(), "Player Mob hurts");
        });
    }

    private void serverAboutToStart(FMLServerAboutToStartEvent event) {
        NameManager.INSTANCE.init();
    }

    private void registerCommands(RegisterCommandsEvent event) {
        PlayerMobsCommand.register(event.getDispatcher());
    }
}
