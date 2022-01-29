package se.gory_moon.player_mobs.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;
import se.gory_moon.player_mobs.utils.TextureUtils;

import java.util.Optional;

public class PlayerMobCapeLayer extends RenderLayer<PlayerMobEntity, PlayerModel<PlayerMobEntity>> {
    public PlayerMobCapeLayer(RenderLayerParent<PlayerMobEntity, PlayerModel<PlayerMobEntity>> playerModelIn) {
        super(playerModelIn);
    }

    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, PlayerMobEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        Optional<ResourceLocation> location = TextureUtils.getPlayerCape(entity);
        if (!entity.isInvisible() && location.isPresent()) {
            ItemStack itemstack = entity.getItemBySlot(EquipmentSlot.CHEST);
            if (itemstack.getItem() != Items.ELYTRA) {
                matrixStackIn.pushPose();
                matrixStackIn.translate(0.0D, 0.0D, 0.125D);
                double d0 = Mth.lerp(partialTicks, entity.xCloakO, entity.xCloak) - Mth.lerp(partialTicks, entity.xo, entity.getX());
                double d1 = Mth.lerp(partialTicks, entity.yCloakO, entity.yCloak) - Mth.lerp(partialTicks, entity.yo, entity.getY());
                double d2 = Mth.lerp(partialTicks, entity.zCloakO, entity.zCloak) - Mth.lerp(partialTicks, entity.zo, entity.getZ());
                float f = entity.yBodyRotO + (entity.yBodyRot - entity.yBodyRotO);
                double d3 = Mth.sin(f * ((float) Math.PI / 180F));
                double d4 = -Mth.cos(f * ((float) Math.PI / 180F));
                float f1 = (float) d1 * 10.0F;
                f1 = Mth.clamp(f1, -6.0F, 32.0F);
                float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
                f2 = Mth.clamp(f2, 0.0F, 150.0F);
                float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;
                f3 = Mth.clamp(f3, -20.0F, 20.0F);
                if (f2 < 0.0F) {
                    f2 = 0.0F;
                }

                float f4 = Mth.lerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
                f1 = f1 + Mth.sin(Mth.lerp(partialTicks, entity.walkDistO, entity.walkDist) * 6.0F) * 32.0F * f4;
                if (entity.isCrouching()) {
                    f1 += 25.0F;
                }

                if (entity.isBaby()) {
                    matrixStackIn.scale(0.5F, 0.5F, 0.5F);
                    matrixStackIn.translate(0.0F, 1.5F, -0.1F);
                }

                matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
                matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(f3 / 2.0F));
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180.0F - f3 / 2.0F));
                VertexConsumer consumer = bufferIn.getBuffer(RenderType.entitySolid(location.get()));
                this.getParentModel().renderCloak(matrixStackIn, consumer, packedLightIn, OverlayTexture.NO_OVERLAY);
                matrixStackIn.popPose();
            }
        }
    }
}

