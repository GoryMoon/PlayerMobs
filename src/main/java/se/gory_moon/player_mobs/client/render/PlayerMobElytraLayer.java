package se.gory_moon.player_mobs.client.render;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;
import se.gory_moon.player_mobs.utils.TextureUtils;

import java.util.Optional;

public class PlayerMobElytraLayer extends ElytraLayer<PlayerMobEntity, PlayerModel<PlayerMobEntity>> {

    public PlayerMobElytraLayer(RenderLayerParent<PlayerMobEntity, PlayerModel<PlayerMobEntity>> pRenderer, EntityModelSet pModelSet) {
        super(pRenderer, pModelSet);
    }

    @Override
    public boolean shouldRender(ItemStack stack, @NotNull PlayerMobEntity entity) {
        return stack.getItem() instanceof ElytraItem;
    }

    @Override
    public @NotNull ResourceLocation getElytraTexture(@NotNull ItemStack stack, @NotNull PlayerMobEntity entity) {
        Optional<ResourceLocation> elytra = TextureUtils.getPlayerElytra(entity);
        if (elytra.isPresent()) {
            if (elytra.get().equals(TextureUtils.HIDE_FEATURE))
                return super.getElytraTexture(stack, entity);
            else
                return elytra.get();
        }

        // Fallback to cape texture if there is no specific elytra texture
        Optional<ResourceLocation> cape = TextureUtils.getPlayerCape(entity);
        return cape.orElseGet(() -> super.getElytraTexture(stack, entity));
    }
}
