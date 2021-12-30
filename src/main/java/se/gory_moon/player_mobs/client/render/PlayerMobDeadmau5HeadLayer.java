package se.gory_moon.player_mobs.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import se.gory_moon.player_mobs.entity.PlayerMobEntity;
import se.gory_moon.player_mobs.utils.TextureUtils;

public class PlayerMobDeadmau5HeadLayer extends LayerRenderer<PlayerMobEntity, PlayerModel<PlayerMobEntity>> {

    public PlayerMobDeadmau5HeadLayer(IEntityRenderer<PlayerMobEntity, PlayerModel<PlayerMobEntity>> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, PlayerMobEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if ("deadmau5".equals(entity.getName().getString()) && !entity.isInvisible()) {
            IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEntitySolid(TextureUtils.getPlayerSkin(entity)));
            int i = LivingRenderer.getPackedOverlay(entity, 0.0F);

            for (int j = 0; j < 2; ++j) {
                float pitch = MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch);
                matrixStackIn.push();

                if (entity.isChild()) {
                    pitch *= -0.5;
                }

                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(netHeadYaw));
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(pitch));

                if (entity.isChild()) {
                    matrixStackIn.scale(0.7F, 0.7F, 0.7F);
                    matrixStackIn.translate(0.0F, 1.05F, 0.0F);
                }

                matrixStackIn.translate(0.375F * (float)(j * 2 - 1), 0.0D, 0.0D);
                matrixStackIn.translate(0.0D, -0.375D, 0.0D);

                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-pitch));
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-netHeadYaw));

                float size = 4F / 3F;
                matrixStackIn.scale(size, size, size);

                this.getEntityModel().renderEars(matrixStackIn, ivertexbuilder, packedLightIn, i);
                matrixStackIn.pop();
            }
        }
    }
}
