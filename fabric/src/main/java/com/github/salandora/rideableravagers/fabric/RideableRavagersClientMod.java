package com.github.salandora.rideableravagers.fabric;

import com.github.salandora.rideableravagers.client.RideableRavagersClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

import static com.github.salandora.rideableravagers.client.RideableRavagersClient.RAVAGER_BABY;

public class RideableRavagersClientMod implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		EntityModelLayerRegistry.registerModelLayer(RAVAGER_BABY, RideableRavagersClient::createBabyRavagerLayer);
	}
}
