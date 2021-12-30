package se.gory_moon.player_mobs.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;
import se.gory_moon.player_mobs.utils.TextureUtils;

public class PlayerMobCapeLayer  extends LayerRenderer<PlayerMobEntity, PlayerModel<PlayerMobEntity>> {
    public PlayerMobCapeLayer(IEntityRenderer<PlayerMobEntity, PlayerModel<PlayerMobEntity>> playerModelIn) {
        super(playerModelIn);
    }

    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, PlayerMobEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ResourceLocation location = TextureUtils.getPlayerCape(entity);
        if (!entity.isInvisible() && location != null) {
            ItemStack itemstack = entity.getItemBySlot(EquipmentSlotType.CHEST);
            if (itemstack.getItem() != Items.ELYTRA) {
                matrixStackIn.pushPose();
                matrixStackIn.translate(0.0D, 0.0D, 0.125D);
                double d0 = MathHelper.lerp(partialTicks, entity.xCloakO, entity.xCloak) - MathHelper.lerp(partialTicks, entity.xo, entity.getX());
                double d1 = MathHelper.lerp(partialTicks, entity.yCloakO, entity.yCloak) - MathHelper.lerp(partialTicks, entity.yo, entity.getY());
                double d2 = MathHelper.lerp(partialTicks, entity.zCloakO, entity.zCloak) - MathHelper.lerp(partialTicks, entity.zo, entity.getZ());
                float f = entity.yBodyRotO + (entity.yBodyRot - entity.yBodyRotO);
                double d3 = MathHelper.sin(f * ((float)Math.PI / 180F));
                double d4 = -MathHelper.cos(f * ((float)Math.PI / 180F));
                float f1 = (float)d1 * 10.0F;
                f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
                float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
                f2 = MathHelper.clamp(f2, 0.0F, 150.0F);
                float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;
                f3 = MathHelper.clamp(f3, -20.0F, 20.0F);
                if (f2 < 0.0F) {
                    f2 = 0.0F;
                }

                float f4 = MathHelper.lerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
                f1 = f1 + MathHelper.sin(MathHelper.lerp(partialTicks, entity.walkDistO, entity.walkDist) * 6.0F) * 32.0F * f4;
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
                IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.entitySolid(location));
                this.getParentModel().renderCloak(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY);
                matrixStackIn.popPose();
            }
        }
    }
}

