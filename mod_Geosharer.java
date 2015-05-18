package net.azirale.geosharer.mod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;



@Mod(modid="GeoSharer", name="GeoSharer", version="1.8-1.5")
public class mod_Geosharer
{
        @Instance("GeoSharer")
        public static mod_Geosharer instance; // singleton enabler
        public GeoSharerCore core; // worker object
        public GeoEventHandler events; // event handler
        //public GeoSharerKeybinder keys; // keybinding handler
        //public GeoGuiHandler gui; // gui handler
        
        public GeoSharerCore getCore()
        {
        	return this.core;
        }
        
        @EventHandler
        public void preInit(FMLPreInitializationEvent event)
        {
        	// No pre-init for this mod
        }
        
        @EventHandler
        public void load(FMLInitializationEvent event) {
        	// Core module
        	this.core = new GeoSharerCore();
        	// Event binding
        	this.events = new GeoEventHandler(this.core);
        	// GUI handling and commands (NYI)
        	//this.keys = GeoSharerKeybinder.CreateNew(this.core);
        	//this.gui = GeoGuiHandler.CreateNew(this, this.core);
        }
        
        @EventHandler
        public void postInit(FMLPostInitializationEvent event)
        {
        	// no post-init for this mod
        }
}
