/**
 *  This core class listens for sign placements and then calls the appropriate routines.
 */
package com.github.leezallen.GPRealEstate;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Sign;

/**
 * @author Lee
 * 
 */
public class GPREListener implements Listener {
	
	@EventHandler
	public void onSignBreak (final BlockBreakEvent event) {
	
	/** This part looks at any blocks that have been broken and
	 *  asks, is it a sign? Is that sign a GPRE sign and does the
	 *  player have permission to break it? If so then delete away.
	 *  
	 *  This is to be implemented in the next version as signs 
	 *  should be protected in their corresponding GP zones anyway.
	 */
	}
	
	@EventHandler
	public void onSignChange (final SignChangeEvent event) {
		
		/** Look at the sign creation and find out if this is an GPRE sign.
		 *  If so, then process it accordingly!
		 */
		
		/** If sign has first line set as declared in GPRESigns class
		 *  then we can process it accordingly
		 */
		if (event.getLine(0).equalsIgnoreCase(GPRESigns.name) || event.getLine(0).equalsIgnoreCase(GPRESigns.nameshort)) {
			final Player signPlayer = event.getPlayer();
			final Location signLocation = event.getBlock().getLocation();
			/** we know that the sign is meant for us now check that it is formatted properly
			 *  and that we have permissions to sell the area!
			 */
			
			/* Load up the claim details for the GP area with the sign */
			GriefPrevention gp = GriefPrevention.instance;

			Claim signClaim = gp.dataStore.getClaimAt(signLocation, false, null);	
			
			/* Check the make sure that a claim still exists at the sign! */
			if (signClaim==null) {
				signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " This sign is not within a claim!");
				event.setCancelled(true);
				return;
			}
			
			/* Does the player have the right permissions to be able to sell a claim */
			if(!(GPRealEstate.perms.has(signPlayer, "GPRealEstate.sell"))) {
				signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " You do not have permissions to sell claims.");
				event.setCancelled(true);
				return;
			}
			
			/* Finally, has the player set the amount that it will cost */
			if (event.getLine(1).isEmpty()) {
				signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " You must enter a cost on the second line.");
				event.setCancelled(true);
				return;
			} 
			
			String signCost = event.getLine(1);
			/* Let us not assume that the player has entered a number! Lets make sure... */
			try {
				   @SuppressWarnings("unused")
				   double x;
				   x = Double.parseDouble(event.getLine(1));
				}
				catch ( NumberFormatException e ) {
					signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " You must enter a valid number on the second line.");
					event.setCancelled(true);
					return;
				}
						
			if (signClaim.parent == null) {
				/* If parent is null then this is a master claim and must be treated accordingly */
	
				/* Is the person placing the sign the owner of this claim */
				if (signPlayer.getName().equalsIgnoreCase(signClaim.ownerName)) {
					/* If true then we need to set the sign accordingly */
					
					event.setLine(0, GPRESigns.name);
					event.setLine(1, "FOR SALE");
					event.setLine(2, signPlayer.getName());
					event.setLine(3, signCost + " " + GPRealEstate.econ.currencyNamePlural());
					
					/**Sign sign = (Sign) event.getBlock().getState();
					sign.setMetadata("RE-Sign", new FixedMetadataValue(signPlayer.getName()));
					sign.setMetadata("RE-Cost", arg1);
					sign.setMetadata("RE-Player", arg1); **/
				} else {
					/* This person has placed a sale sign in an area that they do not have permissions for */
					signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " You may only sell areas that you own");
					event.setCancelled(true);
					return;
				}			
				
			} else {
				/* This is a subdivision and so it works slightly differently! */
				
				/* Is the person placing the sign the owner of the parent claim */
				if (signPlayer.getName().equalsIgnoreCase(signClaim.parent.ownerName)) {
					event.setLine(0, GPRESigns.name);
					event.setLine(1, "FOR LEASE");
					event.setLine(2, signPlayer.getName());
					event.setLine(3, signCost + " " + GPRealEstate.econ.currencyNamePlural());
				} else {
					/* This person has placed a sale sign in an area that they do not have permissions for */
					signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " You may only lease areas that you own");
					event.setCancelled(true);
					return;
				}	
			}
	
		}		
	}
	
	@EventHandler
	public void onSignInteract (final PlayerInteractEvent event) {
		
		/** Has the user right clicked on a sign? Is it one of ours?
		 *  If so act accordingly!
		 */
		
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			/** Player has right clicked on a block */
			
			final Material type = event.getClickedBlock().getType();
			if (type == Material.SIGN_POST || type == Material.WALL_SIGN) {
				/** The block is a sign - Given the event this should always be the case but lets make sure!*/

			
				/* Now we can reference the sign and see if it applies to us */
				Sign sign = (Sign) event.getClickedBlock().getState();
				
				if (sign.getLine(0).equalsIgnoreCase(GPRESigns.name)) {
					/* The sign that has been clicked is a GPRE Sign */
					
					/* before anything else, does the player have the permission to buy claims?*/
					final Player signPlayer = event.getPlayer();
					if(!(GPRealEstate.perms.has(signPlayer, "GPRealEstate.buy"))) {
						signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " You do not have permissions to buy claims.");
						event.setCancelled(true);
						return;
					}
					
					final Location signLocation = event.getClickedBlock().getLocation(); 
					GriefPrevention gp = GriefPrevention.instance;
					Claim signClaim = gp.dataStore.getClaimAt(signLocation, false, null);
					
					/* Check the make sure that a claim still exists at the sign! */
					if (signClaim==null) {
						signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " This sign is no longer within a claim!");
						return;
					}
					
					/* Make sure that the owner of the sign still has permissions to sell the claim! */
					
					if (signClaim.parent == null) {
						/* If parent is null then this is a master claim and must be treated accordingly */
						/* Is the person placing the sign the owner of this claim */
						if (!sign.getLine(2).equalsIgnoreCase(signClaim.ownerName)) {
							signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " The listed player no longer has the rights to sell this claim!");
							event.getClickedBlock().setType(Material.AIR);
							return;
						}
						if (signClaim.ownerName.equalsIgnoreCase(signPlayer.getName())) {
							/* Someone is trying to buy their own property! */
							signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " You already own this claim!");
							return;
						}
					} else {
						/* This is a subdivision and so it works slightly differently! */
						
						/* Is the person placing the sign the owner of the parent claim */
						if (!sign.getLine(2).equalsIgnoreCase(signClaim.parent.ownerName)) {
							/* This person has placed a sale sign in an area that they do not have permissions for */
							signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " The listed player no longer has the rights to lease this claim!");
							event.getClickedBlock().setType(Material.AIR);
							return;
						}
						if (signClaim.parent.ownerName.equalsIgnoreCase(signPlayer.getName())) {
							/* Someone is trying to buy their own property! */
							signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " You already own this claim!");
							return;
						}
					}

					/* Let's check that we have the right amount of money! */
				    String[] signDelimit = sign.getLine(3).split(" ");
					Double signCost =  Double.valueOf(signDelimit[0].trim()).doubleValue();
					if (!(GPRealEstate.econ.has(signPlayer.getName(), signCost))) {
						signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " You do not have enough money!");
						return;
					}
					EconomyResponse ecoresp = GPRealEstate.econ.withdrawPlayer(signPlayer.getName(), signCost);
					if (!(ecoresp.transactionSuccess())) {
						signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " ERROR - Could not withdraw money");
						return;
					}
					
					ecoresp = GPRealEstate.econ.depositPlayer(sign.getLine(2), signCost);
					if (!(ecoresp.transactionSuccess())) {
						signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " ERROR - Could not transfer money");
						return;
					}
					
					/* All checks over, lets start the transfer */
					
					if (sign.getLine(1).equalsIgnoreCase("FOR SALE")) {
						/* This means that we have a whole claim for sale */
						try {
							gp.dataStore.changeClaimOwner(signClaim, signPlayer.getName());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return;
						}

						/* Double check purchase has taken place and report */
						if (signClaim.ownerName.equalsIgnoreCase(signPlayer.getName())) {
							signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " You have successfully bought this claim.");
						} else {
							signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " ERROR - Cannot purchase claim");
							return;
						}
						
						gp.dataStore.saveClaim(signClaim);
						/* Finally remove the sign */
						event.getClickedBlock().setType(Material.AIR);						
					}
					
					if (sign.getLine(1).equalsIgnoreCase("FOR LEASE")) {
						
						/* Clear all existing permissions and add new ones */
						
						signClaim.clearPermissions();
						
						signClaim.addManager(signPlayer.getName());
						if (signClaim.allowGrantPermission(signPlayer)==null) {
							signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " You have been given the ability to change permissions on this claim.");
						} else {
							signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " ERROR - Cannot grant AllowGrantPermission. Aborting purchase");
							return;
						}
						
									
						signClaim.setPermission(signPlayer.getName(), ClaimPermission.Build);
						if (signClaim.allowBuild(signPlayer)==null) {
							signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " You have been given build permissions on this claim.");
						} else {
							signPlayer.sendMessage(ChatColor.YELLOW + GPRESigns.name + " ERROR - Cannot grant AllowBuild. Aborting purchase");
							return;
						}
						
						gp.dataStore.saveClaim(signClaim);
						/* Finally remove the sign */
						event.getClickedBlock().setType(Material.AIR);
						
					}
				}

			}
				
		}
		
	}
		
}

