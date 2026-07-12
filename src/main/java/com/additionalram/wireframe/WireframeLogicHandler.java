package com.additionalram.wireframe;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Wireframe.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class WireframeLogicHandler {
    
    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event){
        if (!WireframeState.isEnabled) return;
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        float targetRadius = WireframeState.renderDistance;
        
        // add a penalty based on movement speed to encourage slower movement
        if (WireframeState.movementEffectsIntensity > 0.0f) {
            double dx = mc.player.getX() - mc.player.xo;
            double dy = mc.player.getY() - mc.player.yo;
            double dz = mc.player.getZ() - mc.player.zo;
            double speed = Math.sqrt(dx * dx + dy * dy + dz * dz);

            float penalty = (float) (speed * 3.0 * WireframeState.movementEffectsIntensity);
            
            // minimum rd either way
            targetRadius = Math.max(0.3f, WireframeState.renderDistance - penalty);
        }

        WireframeState.currentRenderDistance += (targetRadius - WireframeState.currentRenderDistance) * 0.15f;
    }
}
