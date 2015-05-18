package net.azirale.geosharer.mod;

import scala.Console;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

public class GeoEventHandler
{
	// the active core module to pass event triggers on to
	private GeoSharerCore geoCore;
	
	// constructor
	public GeoEventHandler(GeoSharerCore core)
	{
		this.geoCore = core;
		MinecraftForge.EVENT_BUS.register(this);
    	FMLCommonHandler.instance().bus().register(this);
		
	}
	
	// catch individual chunk unloads - occurs when moving around the world
	@SubscribeEvent
	public void onChunkChange(ChunkEvent.Unload chunksave)
	{
		if (chunksave == null) return;
		if (chunksave.getChunk() == null) return;
		geoCore.addChunk(chunksave.getChunk());
	}
	
	// catch bulk chunk unloads - happens during dimension switches
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload unloading)
	{
		geoCore.catchUnload(unloading.world);
	}
	
	// activate mod - happens when connecting to a MP server
    @SubscribeEvent
	public void onServerConnected(FMLNetworkEvent.ClientConnectedToServerEvent event)
	{
		geoCore.activate();
	}
	
    // deactivate mod - happens when disconnecting from a MP server
    @SubscribeEvent
	public void onServerDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
	{
    	geoCore.catchUnload(Minecraft.getMinecraft().theWorld);
		geoCore.deactivate();
	}
	
}
