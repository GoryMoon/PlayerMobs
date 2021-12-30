package se.gory_moon.player_mobs.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;
import se.gory_moon.player_mobs.utils.TextureUtils;

public class PlayerMobRenderer extends BipedRenderer<PlayerMobEntity, PlayerModel<PlayerMobEntity>> {

    private static final PlayerModel<PlayerMobEntity> STEVE = new PlayerModel<>(0, false);
    private static final PlayerModel<PlayerMobEntity> ALEX = new PlayerModel<>(0, true);

    public PlayerMobRenderer(EntityRendererManager renderManager) {
        super(renderManager, STEVE, 0.5F);
        this.addLayer(new BipedArmorLayer<>(this, new BipedModel<>(0.5F), new BipedModel<>(1.0F)));
        this.addLayer(new ArrowLayer<>(this));
        this.addLayer(new PlayerMobDeadmau5HeadLayer(this));
        this.addLayer(new PlayerMobCapeLayer(this));
    }

    @Override
    public void render(PlayerMobEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLightIn) {
        entityModel = TextureUtils.getPlayerSkinType(entity.getProfile()) == TextureUtils.SkinType.SLIM ? ALEX: STEVE;

        entityModel.leftArmPose = BipedModel.ArmPose.EMPTY;
        entityModel.rightArmPose = BipedModel.ArmPose.EMPTY;
        ItemStack stack = entity.getHeldItemMainhand();
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof BowItem && entity.isAggressive()) {
                setHandPose(entity, BipedModel.ArmPose.BOW_AND_ARROW);
            } else {
                setHandPose(entity, BipedModel.ArmPose.ITEM);
            }
        }

        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLightIn);
    }

    private void setHandPose(PlayerMobEntity entity, BipedModel.ArmPose pose) {
        if (entity.getPrimaryHand() == HandSide.RIGHT) {
            entityModel.rightArmPose = pose;
        } else {
            entityModel.leftArmPose = pose;
        }
    }

    @Override
    protected void preRenderCallback(PlayerMobEntity entity, MatrixStack matrix, float partialTickTime) {
        matrix.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    public ResourceLocation getEntityTexture(PlayerMobEntity entity) {
        return TextureUtils.getPlayerSkin(entity);
    }
}
