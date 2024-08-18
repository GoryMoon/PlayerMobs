package se.gory_moon.player_mobs.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;
import se.gory_moon.player_mobs.utils.TextureUtils;

public class PlayerMobRenderer extends HumanoidMobRenderer<PlayerMobEntity, PlayerModel<PlayerMobEntity>> {

    private final PlayerModel<PlayerMobEntity> playerModel;
    private final PlayerModel<PlayerMobEntity> slimModel;
    private final RenderLayer<PlayerMobEntity, PlayerModel<PlayerMobEntity>> playerArmorModel;
    private final RenderLayer<PlayerMobEntity, PlayerModel<PlayerMobEntity>> slimArmorModel;

    private final int armorLayerIndex;

    public PlayerMobRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new PlayerModel<>(pContext.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        playerModel = this.model;
        slimModel = new PlayerModel<>(pContext.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        playerArmorModel = new HumanoidArmorLayer<>(
                this,
                new HumanoidArmorModel<>(pContext.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(pContext.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                pContext.getModelManager()
        );
        slimArmorModel = new HumanoidArmorLayer<>(
                this,
                new HumanoidArmorModel<>(pContext.bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR)),
                new HumanoidArmorModel<>(pContext.bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR)),
                pContext.getModelManager()
        );

        var arrowLayer = new ArrowLayer<>(pContext, this);
        this.addLayer(arrowLayer);
        armorLayerIndex = layers.indexOf(arrowLayer);

        this.addLayer(new PlayerMobDeadmau5EarsLayer(this));
        this.addLayer(new PlayerMobCapeLayer(this));
    }

    @Override
    public void render(@NotNull PlayerMobEntity pEntity, float pEntityYaw, float pPartialTicks, @NotNull PoseStack pMatrixStack, @NotNull MultiBufferSource pBuffer, int pPackedLight) {
        boolean slim = TextureUtils.getPlayerSkinType(pEntity) == PlayerSkin.Model.SLIM;

        model = slim ? slimModel : playerModel;
        layers.remove(playerArmorModel);
        layers.remove(slimArmorModel);
        // Make sure we add the armor layer before most other layers as you normally do
        layers.add(armorLayerIndex, slim ? slimArmorModel : playerArmorModel);

        model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        ItemStack stack = pEntity.getMainHandItem();
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof CrossbowItem) {
                if (pEntity.isChargingCrossbow())
                    setHandPose(pEntity, HumanoidModel.ArmPose.CROSSBOW_CHARGE);
                else
                    setHandPose(pEntity, HumanoidModel.ArmPose.CROSSBOW_HOLD);
            } else if (stack.getItem() instanceof BowItem && pEntity.isAggressive())
                setHandPose(pEntity, HumanoidModel.ArmPose.BOW_AND_ARROW);
            else
                setHandPose(pEntity, HumanoidModel.ArmPose.ITEM);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    private void setHandPose(PlayerMobEntity entity, HumanoidModel.ArmPose pose) {
        if (entity.getMainArm() == HumanoidArm.RIGHT) {
            model.rightArmPose = pose;
        } else {
            model.leftArmPose = pose;
        }
    }

    @Override
    protected void scale(@NotNull PlayerMobEntity entity, PoseStack matrix, float partialTickTime) {
        matrix.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull PlayerMobEntity entity) {
        return TextureUtils.getPlayerSkin(entity);
    }
}
