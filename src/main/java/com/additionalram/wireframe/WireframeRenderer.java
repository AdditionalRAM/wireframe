package com.additionalram.wireframe;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Wireframe.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class WireframeRenderer {
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event){
        if(!WireframeState.isEnabled) return;

        // delete sky if blind
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            if (WireframeState.blind) {
                RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
                RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
            }
            return;
        }

        // wireframe renderer
        if(event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if(level == null) return;

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();

        // Frustum frustum = event.getFrustum();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        // RenderSystem.setShaderFogColor(1.0F, 1.0F, 1.0F, 1.0F);
        // net.minecraft.client.renderer.FogRenderer.setupNoFog();

        // RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(2.0F);

        float oldFogStart = RenderSystem.getShaderFogStart();
        float oldFogEnd = RenderSystem.getShaderFogEnd();
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
        RenderSystem.setShaderFogEnd(Float.MAX_VALUE);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        float radius = WireframeState.renderDistance;
        BlockPos center = new BlockPos(camPos.x, camPos.y, camPos.z);
        int searchRadius = (int)radius + 1;

        java.util.List<AABB> visibleBlocks = new java.util.ArrayList<>();
        
        if (WireframeState.renderBlocks) {
            for (int x = -searchRadius; x <= searchRadius; x++) {
                for (int y = -searchRadius; y <= searchRadius; y++) {
                    for (int z = -searchRadius; z <= searchRadius; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        BlockState state = level.getBlockState(pos);
                        if (!state.isAir() && !state.getShape(level, pos).isEmpty()) {
                            visibleBlocks.add(state.getShape(level, pos).bounds().move(pos));
                        }
                    }
                }
            }
        }

        if(WireframeState.blind && !visibleBlocks.isEmpty()){
            RenderSystem.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            poseStack.pushPose();
            poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

            for (AABB bounds : visibleBlocks) {
                drawSolidBox(poseStack, bufferBuilder, bounds); // Un-inflated
            }

            tesselator.end();
            poseStack.popPose();
        }

        if(!visibleBlocks.isEmpty()){
            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
            bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            poseStack.pushPose();
            poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
            for(AABB bounds : visibleBlocks){
                drawFadingBox(poseStack, bufferBuilder, bounds.inflate(0.002), camPos, radius);
            }

            tesselator.end();
            poseStack.popPose();
        }

        RenderSystem.enableTexture();
        // RenderSystem.enableDepthTest();
        RenderSystem.lineWidth(1.0F);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        RenderSystem.setShaderFogStart(oldFogStart);
        RenderSystem.setShaderFogEnd(oldFogEnd);

        // net.minecraft.client.renderer.FogRenderer.setupFog(
        //     camera,
        //     net.minecraft.client.renderer.FogRenderer.FogMode.FOG_TERRAIN,
        //     Math.max(mc.options.renderDistance, 2) * 16.0F,
        //     false
        // );
    }

    private static void drawFadingBox(PoseStack poseStack, BufferBuilder builder, AABB bounds, Vec3 camPos, float radius) {
        float minX = (float) bounds.minX; float minY = (float) bounds.minY; float minZ = (float) bounds.minZ;
        float maxX = (float) bounds.maxX; float maxY = (float) bounds.maxY; float maxZ = (float) bounds.maxZ;

        // X axis lines
        drawFadingLine(poseStack, builder, camPos, radius, minX, minY, minZ, maxX, minY, minZ, 1, 0, 0);
        drawFadingLine(poseStack, builder, camPos, radius, minX, maxY, minZ, maxX, maxY, minZ, 1, 0, 0);
        drawFadingLine(poseStack, builder, camPos, radius, minX, minY, maxZ, maxX, minY, maxZ, 1, 0, 0);
        drawFadingLine(poseStack, builder, camPos, radius, minX, maxY, maxZ, maxX, maxY, maxZ, 1, 0, 0);

        // Y axis lines
        drawFadingLine(poseStack, builder, camPos, radius, minX, minY, minZ, minX, maxY, minZ, 0, 1, 0);
        drawFadingLine(poseStack, builder, camPos, radius, maxX, minY, minZ, maxX, maxY, minZ, 0, 1, 0);
        drawFadingLine(poseStack, builder, camPos, radius, minX, minY, maxZ, minX, maxY, maxZ, 0, 1, 0);
        drawFadingLine(poseStack, builder, camPos, radius, maxX, minY, maxZ, maxX, maxY, maxZ, 0, 1, 0);

        // Z axis lines
        drawFadingLine(poseStack, builder, camPos, radius, minX, minY, minZ, minX, minY, maxZ, 0, 0, 1);
        drawFadingLine(poseStack, builder, camPos, radius, maxX, minY, minZ, maxX, minY, maxZ, 0, 0, 1);
        drawFadingLine(poseStack, builder, camPos, radius, minX, maxY, minZ, minX, maxY, maxZ, 0, 0, 1);
        drawFadingLine(poseStack, builder, camPos, radius, maxX, maxY, minZ, maxX, maxY, maxZ, 0, 0, 1);
    }

    private static void drawFadingLine(PoseStack poseStack, BufferBuilder builder, Vec3 camPos, float radius, float x1, float y1, float z1, float x2, float y2, float z2, float nx, float ny, float nz){
        
        float dX1 = x1 - (float)camPos.x;
        float dY1 = y1 - (float)camPos.y;
        float dZ1 = z1 - (float)camPos.z;

        if(dY1 < 0) dY1 *= 0.4f; // points below the camera are calculated as being closer than they actually are

        float dist1 = (float)Math.sqrt(dX1*dX1 + dY1*dY1 + dZ1*dZ1);

        float dX2 = x2 - (float)camPos.x;
        float dY2 = y2 - (float)camPos.y;
        float dZ2 = z2 - (float)camPos.z;

        if(dY2 < 0) dY2 *= 0.4f; // points below the camera are calculated as being closer than they actually are

        float dist2 = (float)Math.sqrt(dX2*dX2 + dY2*dY2 + dZ2*dZ2);

        float fadeStart = Math.max(radius - WireframeState.fadeStartDistance, 0);
        float a1 = 1.0f - Mth.clamp((dist1 - fadeStart) / (radius - fadeStart), 0.0f, 1.0f);
        float a2 = 1.0f - Mth.clamp((dist2 - fadeStart) / (radius - fadeStart), 0.0f, 1.0f);

        if(a1 <= 0.0f && a2 <= 0.0f) return;

        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        builder.vertex(pose, x1, y1, z1).color(1.0F, 1.0F, 1.0F, a1).normal(normal, nx, ny, nz).endVertex();
        builder.vertex(pose, x2, y2, z2).color(1.0F, 1.0F, 1.0F, a2).normal(normal, nx, ny, nz).endVertex();
    }

    // holy spaghetti
    private static void drawSolidBox(PoseStack poseStack, BufferBuilder builder, AABB bounds) {
        float minX = (float) bounds.minX; float minY = (float) bounds.minY; float minZ = (float) bounds.minZ;
        float maxX = (float) bounds.maxX; float maxY = (float) bounds.maxY; float maxZ = (float) bounds.maxZ;
        Matrix4f pose = poseStack.last().pose();

        // Down
        builder.vertex(pose, minX, minY, minZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, maxX, minY, minZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, maxX, minY, maxZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, minX, minY, maxZ).color(0f, 0f, 0f, 1f).endVertex();
        // Up
        builder.vertex(pose, minX, maxY, minZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, minX, maxY, maxZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, maxX, maxY, maxZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, maxX, maxY, minZ).color(0f, 0f, 0f, 1f).endVertex();
        // North
        builder.vertex(pose, minX, minY, minZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, minX, maxY, minZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, maxX, maxY, minZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, maxX, minY, minZ).color(0f, 0f, 0f, 1f).endVertex();
        // South
        builder.vertex(pose, minX, minY, maxZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, maxX, minY, maxZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, maxX, maxY, maxZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, minX, maxY, maxZ).color(0f, 0f, 0f, 1f).endVertex();
        // West
        builder.vertex(pose, minX, minY, minZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, minX, minY, maxZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, minX, maxY, maxZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, minX, maxY, minZ).color(0f, 0f, 0f, 1f).endVertex();
        // East
        builder.vertex(pose, maxX, minY, minZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, maxX, maxY, minZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, maxX, maxY, maxZ).color(0f, 0f, 0f, 1f).endVertex();
        builder.vertex(pose, maxX, minY, maxZ).color(0f, 0f, 0f, 1f).endVertex();
    }
}
