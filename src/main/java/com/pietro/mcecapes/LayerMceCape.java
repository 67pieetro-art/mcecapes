package com.pietro.mcecapes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

/**
 * Same as vanilla LayerCape, but for the local player it uses the cape
 * selected with /cape. Everyone else keeps their normal (vanilla) cape.
 */
public class LayerMceCape implements LayerRenderer<AbstractClientPlayer> {

    private final RenderPlayer playerRenderer;

    public LayerMceCape(RenderPlayer playerRenderer) {
        this.playerRenderer = playerRenderer;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (player.isInvisible() || !player.isWearing(EnumPlayerModelParts.CAPE)) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        ResourceLocation texture = null;

        if (player == mc.thePlayer) {
            texture = CapeManager.getActiveTexture();
        }
        if (texture == null) {
            if (!player.hasPlayerInfo()) {
                return;
            }
            texture = player.getLocationCape(); // vanilla cape (may be null)
        }
        if (texture == null) {
            return;
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(texture);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, 0.125F);

        double dx = player.prevChasingPosX + (player.chasingPosX - player.prevChasingPosX) * (double) partialTicks
                - (player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks);
        double dy = player.prevChasingPosY + (player.chasingPosY - player.prevChasingPosY) * (double) partialTicks
                - (player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks);
        double dz = player.prevChasingPosZ + (player.chasingPosZ - player.prevChasingPosZ) * (double) partialTicks
                - (player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks);

        float yawOffset = player.prevRenderYawOffset
                + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
        double sin = (double) MathHelper.sin(yawOffset * (float) Math.PI / 180.0F);
        double cos = (double) (-MathHelper.cos(yawOffset * (float) Math.PI / 180.0F));

        float lift = (float) dy * 10.0F;
        lift = MathHelper.clamp_float(lift, -6.0F, 32.0F);
        float forward = (float) (dx * sin + dz * cos) * 100.0F;
        float side = (float) (dx * cos - dz * sin) * 100.0F;
        if (forward < 0.0F) {
            forward = 0.0F;
        }

        float camYaw = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
        lift += MathHelper.sin((player.prevDistanceWalkedModified
                + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * partialTicks) * 6.0F)
                * 32.0F * camYaw;

        if (player.isSneaking()) {
            lift += 25.0F;
        }

        GlStateManager.rotate(6.0F + forward / 2.0F + lift, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(side / 2.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-side / 2.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        this.playerRenderer.getMainModel().renderCape(0.0625F);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
