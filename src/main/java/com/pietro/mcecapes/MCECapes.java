package com.pietro.mcecapes;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * MCE Capes - cosmetic capes that only YOU can see (100% client side).
 * Nothing is sent to the server, other players never see these capes.
 */
@Mod(modid = MCECapes.MODID, name = "MCE Capes", version = MCECapes.VERSION,
        clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class MCECapes {

    public static final String MODID = "mcecapes";
    public static final String VERSION = "1.0.0";
    public static final Logger LOG = LogManager.getLogger("MCECapes");

    /** Renderers that already got our cape layer. */
    private final Set<Object> patched = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        CapeManager.init(event.getModConfigurationDirectory());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new CapeCommand());
    }

    /**
     * The player renderers ("default" and "slim" skins) are patched lazily the
     * first time they render, so we never depend on init order.
     */
    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        patch(event.renderer);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void patch(RenderPlayer renderer) {
        if (patched.contains(renderer)) {
            return;
        }
        patched.add(renderer);
        try {
            List<LayerRenderer> layers = ReflectionHelper.getPrivateValue(
                    RendererLivingEntity.class, renderer, "layerRenderers", "field_177097_h");
            boolean replaced = false;
            for (int i = 0; i < layers.size(); i++) {
                if (layers.get(i) instanceof LayerCape) {
                    // Replace the vanilla cape layer: it falls back to the vanilla
                    // cape for everybody except you (when you have a cape selected).
                    layers.set(i, new LayerMceCape(renderer));
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                layers.add(new LayerMceCape(renderer));
            }
            LOG.info("Cape layer installed.");
        } catch (Throwable t) {
            LOG.error("Could not install the cape layer", t);
        }
    }
}
