package se.gory_moon.player_mobs;

import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import se.gory_moon.player_mobs.data.PlayerMobsItemModelProvider;
import se.gory_moon.player_mobs.data.PlayerMobsSoundDefinitionsProvider;
import se.gory_moon.player_mobs.data.PlayerMobsLanguageProvider;
import se.gory_moon.player_mobs.data.loot.PlayerMobsLootTableProvider;
import se.gory_moon.player_mobs.entity.EntityRegistry;
import se.gory_moon.player_mobs.utils.NameManager;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static se.gory_moon.player_mobs.entity.EntityRegistry.*;
import static se.gory_moon.player_mobs.sound.SoundRegistry.*;
import static se.gory_moon.player_mobs.world.BiomeModifierRegistry.BIOME_MODIFIER_SERIALIZERS;

@Mod(Constants.MOD_ID)
public class PlayerMobs {

    public PlayerMobs(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(EntityRegistry::registerEntityAttributes);
        modEventBus.addListener(EntityRegistry::registerEntitySpawnPlacement);

        modEventBus.addListener(this::buildTabContents);
        modEventBus.addListener(this::gatherData);

        IEventBus eventBus = NeoForge.EVENT_BUS;
        eventBus.addListener(this::registerCommands);
        eventBus.addListener(this::serverAboutToStart);

        SOUNDS.register(modEventBus);
        ENTITIES.register(modEventBus);
        ENTITY_DATA_SERIALIZERS.register(modEventBus);
        ITEMS.register(modEventBus);
        BIOME_MODIFIER_SERIALIZERS.register(modEventBus);

        container.registerConfig(ModConfig.Type.COMMON, Configs.commonSpec);
        modEventBus.register(Configs.COMMON);
    }

    private void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        gen.addProvider(true, new PackMetadataGenerator(packOutput)
                .add(PackMetadataSection.TYPE, new PackMetadataSection(
                        Component.translatable(LangKeys.PACK_DESCRIPTION.key()),
                        DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA),
                        Optional.of(new InclusiveRange<>(0, Integer.MAX_VALUE)))));

        gen.addProvider(event.includeServer(), new PlayerMobsLootTableProvider(packOutput, lookupProvider));

        gen.addProvider(event.includeClient(), new PlayerMobsItemModelProvider(packOutput, existingFileHelper));
        gen.addProvider(event.includeClient(), new PlayerMobsSoundDefinitionsProvider(packOutput, existingFileHelper));
        gen.addProvider(event.includeClient(), new PlayerMobsLanguageProvider(packOutput));
    }

    private void serverAboutToStart(ServerAboutToStartEvent event) {
        NameManager.INSTANCE.init();
    }

    private void registerCommands(RegisterCommandsEvent event) {
        PlayerMobsCommand.register(event.getDispatcher());
    }

    public void buildTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS)
            event.accept(EntityRegistry.PLAYER_MOD_SPAWN_EGG.get());
    }
}
