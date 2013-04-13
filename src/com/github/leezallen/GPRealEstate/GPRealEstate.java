package com.github.leezallen.GPRealEstate;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class GPRealEstate extends JavaPlugin{

	Logger log;
	public static boolean vaultPresent = false;
    public static Economy econ = null;
    public static Permission perms = null;
	
	@Override 
    public void onEnable(){
	
		log = this.getLogger();
		getServer().getPluginManager().registerEvents(new GPREListener(), this);
		
		if (checkVault()) {
			/* Vault has been detected */
			log.info("Vault detected and enabled.");
			if (setupEconomy()) {
				log.info("Vault has detected and connected to " + econ.getName());	
			} else {
				log.warning("No compatible economy plugin detected [Vault].");
				log.warning("Disabling plugin.");
				getPluginLoader().disablePlugin(this);
	            return;
			}
			if (setupPermissions()) {
				log.info("Vault has detected and connected to " + perms.getName());	
			} else {
				log.warning("No compatible permissions plugin detected [Vault].");
				log.warning("Disabling plugin.");
				getPluginLoader().disablePlugin(this);
				return;
			}
		} 
		log.info("V" + this.getDescription().getVersion() + " Enabled!");
	}
	
	public void onDisable(){
		
		log.info("V" + this.getDescription().getVersion() + " Disabled!");
	}
	
    private boolean checkVault() {
   
    	vaultPresent = !(getServer().getPluginManager().getPlugin("Vault") == null); 
        return vaultPresent;

    }
	
	
	private boolean setupEconomy() {

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
}
