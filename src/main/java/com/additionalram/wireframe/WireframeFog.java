package com.additionalram.wireframe;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Wireframe.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class WireframeFog {

    @SubscribeEvent
    public static void onFogColor(EntityViewRenderEvent.FogColors event) {
        if(!WireframeState.isEnabled || !WireframeState.blind) return;

        event.setRed(0.0F);
        event.setGreen(0.0F);
        event.setBlue(0.0F);
    }

    @SubscribeEvent
    public static void onRenderFog(EntityViewRenderEvent.RenderFogEvent event){
        if(!WireframeState.isEnabled || !WireframeState.blind) return;

        event.setCanceled(true);
        event.setNearPlaneDistance(0);
        event.setFarPlaneDistance(0.0001f);

        RenderSystem.setShaderFogStart(event.getNearPlaneDistance());
        RenderSystem.setShaderFogEnd(event.getFarPlaneDistance());
    }
}
