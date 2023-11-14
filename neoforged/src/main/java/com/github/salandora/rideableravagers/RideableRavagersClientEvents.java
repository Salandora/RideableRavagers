package com.github.salandora.rideableravagers;

import eu.midnightdust.lib.config.MidnightConfig;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RideableRavagersMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = { Dist.CLIENT })
public class RideableRavagersClientEvents {
	@SubscribeEvent
	public static void onPostInit(FMLClientSetupEvent event) {
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "SERVER_ONLY", (remote, server) -> true));
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
				new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> MidnightConfig.getScreen(parent, RideableRavagersMod.MODID)));
	}
}
