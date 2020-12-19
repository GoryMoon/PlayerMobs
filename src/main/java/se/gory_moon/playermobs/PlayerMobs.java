package se.gory_moon.playermobs;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.NonNullLazyValue;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.gory_moon.playermobs.entity.EntityRegistry;

@Mod(Constants.MOD_ID)
public class PlayerMobs {

    public static final NonNullLazyValue<Registrate> REGISTRATE = new NonNullLazyValue<>(() -> Registrate.create(Constants.MOD_ID));
    public static final Logger LOGGER = LogManager.getLogger();

    public PlayerMobs() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configs.commonSpec);
        EntityRegistry.init();
    }

    public static Registrate getRegistrate() {
        return REGISTRATE.get();
    }

    private void setup(FMLCommonSetupEvent event) {
        //PacketHandler.init();
        EntityRegistry.registerEntityAttributes();
    }

    private void gatherData(GatherDataEvent event) {
        getRegistrate().addDataGenerator(ProviderType.LANG, prov -> {

        });
    }
}
