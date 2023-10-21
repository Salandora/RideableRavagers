package com.github.salandora.rideableravagers;

import com.github.salandora.rideableravagers.config.RideableRavagersConfig;
import com.github.salandora.rideableravagers.init.BiomeCodecInit;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;

@Mod(RideableRavagersMod.MODID)
public class RideableRavagersMod {
	public static final String MODID = "rideableravagers";

	public RideableRavagersMod() {
		MidnightConfig.init(MODID, RideableRavagersConfig.class);
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (remote, server) -> true));
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
				new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> MidnightConfig.getScreen(parent, MODID)));

		MinecraftForge.EVENT_BUS.register(this);
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

		BiomeCodecInit.BIOME_MODIFIER_SERIALIZERS.register(bus);
	}
}
