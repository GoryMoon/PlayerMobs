package se.gory_moon.playermobs.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeadLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import se.gory_moon.playermobs.entity.PlayerMobEntity;
import se.gory_moon.playermobs.client.TextureUtils;

public class PlayerMobRenderer extends BipedRenderer<PlayerMobEntity, PlayerModel<PlayerMobEntity>> {

    private static final PlayerModel<PlayerMobEntity> STEVE = new PlayerModel<>(0, false);
    private static final PlayerModel<PlayerMobEntity> ALEX = new PlayerModel<>(0, true);

    public PlayerMobRenderer(EntityRendererManager renderManager) {
        super(renderManager, STEVE, 0.5F);
        this.addLayer(new BipedArmorLayer<>(this, new BipedModel<>(0.5F), new BipedModel<>(1.0F)));
        this.addLayer(new ArrowLayer<>(this));
        this.addLayer(new HeadLayer<>(this));
    }

    @Override
    public void render(PlayerMobEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLightIn) {
        entityModel = "slim".equals(TextureUtils.getPlayerSkinType(entity.getProfile())) ? ALEX: STEVE;
        PlayerModel<PlayerMobEntity> model = getEntityModel();

        model.leftArmPose = BipedModel.ArmPose.EMPTY;
        model.rightArmPose = BipedModel.ArmPose.EMPTY;
        ItemStack stack = entity.getHeldItemMainhand();
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof BowItem && entity.isAggressive()) {
                setHandPose(model, entity, BipedModel.ArmPose.BOW_AND_ARROW);
            } else {
                setHandPose(model, entity, BipedModel.ArmPose.ITEM);
            }
        }

        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLightIn);
    }

    private void setHandPose(PlayerModel<PlayerMobEntity> model, PlayerMobEntity entity, BipedModel.ArmPose pose) {
        if (entity.getPrimaryHand() == HandSide.RIGHT) {
            model.rightArmPose = pose;
        } else {
            model.leftArmPose = pose;
        }
    }

    @Override
    public PlayerModel<PlayerMobEntity> getEntityModel() {
        return super.getEntityModel();
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
