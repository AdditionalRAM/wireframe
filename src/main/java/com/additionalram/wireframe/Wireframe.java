package com.additionalram.wireframe;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Wireframe.MODID)
public class Wireframe
{
    public static final String MODID = "wireframe";

    private static final Logger LOGGER = LogUtils.getLogger();

    public Wireframe()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Initialized Wireframe");
    }
}
