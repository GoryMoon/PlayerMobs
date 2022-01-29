package se.gory_moon.player_mobs.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;
import se.gory_moon.player_mobs.utils.TextureUtils;

public class PlayerMobRenderer extends HumanoidMobRenderer<PlayerMobEntity, PlayerModel<PlayerMobEntity>> {

    private final PlayerModel<PlayerMobEntity> steveModel;
    private final PlayerModel<PlayerMobEntity> alexModel;
    private final RenderLayer<PlayerMobEntity, PlayerModel<PlayerMobEntity>> steveArmorModel;
    private final RenderLayer<PlayerMobEntity, PlayerModel<PlayerMobEntity>> alexArmorModel;

    public PlayerMobRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        steveModel = this.model;
        alexModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        steveArmorModel = new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)));
        alexArmorModel = new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR)), new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR)));

        this.addLayer(new ArrowLayer<>(context, this));
        this.addLayer(new PlayerMobDeadmau5EarsLayer(this));
        this.addLayer(new PlayerMobCapeLayer(this));
    }

    @Override
    public void render(PlayerMobEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        boolean slim = TextureUtils.getPlayerSkinType(pEntity.getProfile()) == TextureUtils.SkinType.SLIM;
        model = slim ? alexModel: steveModel;
        layers.remove(steveArmorModel);
        layers.remove(alexArmorModel);
        layers.add(slim ? alexArmorModel: steveArmorModel);

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
    protected void scale(PlayerMobEntity entity, PoseStack matrix, float partialTickTime) {
        matrix.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    public ResourceLocation getTextureLocation(PlayerMobEntity entity) {
        return TextureUtils.getPlayerSkin(entity);
    }
}
