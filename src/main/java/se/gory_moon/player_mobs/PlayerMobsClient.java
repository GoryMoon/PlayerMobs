package se.gory_moon.player_mobs;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import se.gory_moon.player_mobs.client.render.PlayerMobRenderer;
import se.gory_moon.player_mobs.entity.EntityRegistry;
import se.gory_moon.player_mobs.utils.TextureUtils;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class PlayerMobsClient {

    public PlayerMobsClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(this::onEntityRenderRegister);
        modEventBus.addListener(this::onRegisterReloadListener);

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private void onEntityRenderRegister(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.PLAYER_MOB_ENTITY.get(), PlayerMobRenderer::new);
    }

    public void onRegisterReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(TextureUtils.resourceManagerReloadListener());
    }
}
