package eu.gnomino.goldshops;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class InteractionListener implements Listener {
	private final GoldShopsPlugin plugin;
	public InteractionListener(GoldShopsPlugin pl) {
		plugin = pl;
	}
	
	
	@EventHandler
	public void onPlacement(SignChangeEvent event) {
		if (event.getBlock().getType() == Material.WALL_SIGN || event.getBlock().getType() == Material.SIGN_POST ) {
			if (event.getLine(0).equalsIgnoreCase(ChatColor.AQUA + plugin.getConfig().getString("admin_shop_owner"))) {
				event.setCancelled(true);
			}
			if (event.getLine(0).equalsIgnoreCase("sell") || event.getLine(0).equalsIgnoreCase("adminsell")) {
				if (event.getLine(1).matches("^[0-9]+:[0-9]+$")) {
					int price = Integer.parseInt(event.getLine(1).split(":")[0]);
					int quantity = Integer.parseInt(event.getLine(1).split(":")[1]);
					if (!event.getLine(2).isEmpty()) {
						Material material = Material.getMaterial(event.getLine(2).toUpperCase());
						if (material == null) {
							material = Material.getMaterial(Integer.parseInt(event.getLine(2)));
						}
						if (material != null) {
							event.getPlayer().sendMessage(plugin.getConfig().getString("sell_notice").replace("${quantity}", "" + quantity).replace("${material}", material.name()).replace("${price}", price + ""));
							if (event.getLine(0).equalsIgnoreCase("adminsell")) {
								if (event.getPlayer().hasPermission("GoldShops.adminshop")) {
									event.setLine(0, ChatColor.AQUA + plugin.getConfig().getString("admin_shop_owner"));
								}
								else {
									event.getPlayer().sendMessage(ChatColor.RED + plugin.getConfig().getString("no_permission"));
									event.getBlock().breakNaturally();
									event.setCancelled(true);
									return;
								}
							}
							else {
								event.setLine(0, ChatColor.AQUA + event.getPlayer().getName());
							}
							event.setLine(1, plugin.getConfig().getString("sell_sign"));
							event.setLine(2, quantity + " " + ChatColor.GREEN + material.name() + ":0");
							event.setLine(3, plugin.getConfig().getString("price_prefix") + " " + ChatColor.BOLD + price + " " + plugin.getConfig().getString("price_unit"));
							int data = 0;
							if (Integer.parseInt(event.getLine(3)) > 0) {
								data = Integer.parseInt(event.getLine(3));
							}
							
							event.setLine(2, quantity + " " + ChatColor.GREEN + material.name() + ":" + data);
							
						}
					}
					
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Inventory i = plugin.getServer().createInventory(null, InventoryType.MERCHANT);
			event.getPlayer().openInventory(i);
			if (event.getClickedBlock().getType() == Material.WALL_SIGN || event.getClickedBlock().getType() == Material.SIGN) {
				Sign sign = (Sign) event.getClickedBlock().getState();
				if (sign.getLine(1).equalsIgnoreCase(plugin.getConfig().getString("sell_sign"))) {
						Material currency = Material.getMaterial(plugin.getConfig().getString("currency"));
						String author = sign.getLine(0).replace("" + ChatColor.AQUA, "");
						int quantity = Integer.parseInt(sign.getLine(2).split(" " + ChatColor.GREEN)[0]);
						if (quantity <= 0) {
							event.getPlayer().sendMessage(ChatColor.RED + plugin.getConfig().getString("bad_quantity"));
							return;
						}
						String[] parsed = sign.getLine(2).split("" + ChatColor.GREEN)[1].split(":");
						if (parsed.length < 2) {
							parsed[1] = "0";
						}
						Material material = Material.getMaterial(parsed[0]);
						ItemStack sold = new ItemStack(material, quantity, (short) 0, (byte) Integer.parseInt(parsed[1]));
						
						if (material == null) {
							event.getPlayer().sendMessage(ChatColor.RED + plugin.getConfig().getString("bad_material"));
							return;
						}
						int price = Integer.parseInt(sign.getLine(3).replace(ChatColor.BOLD + "", "").replace(plugin.getConfig().getString("price_prefix"), "").replace(plugin.getConfig().getString("price_unit"), "").replace(" ", ""));
						if (price >= 1 && event.getPlayer().getInventory().contains(currency, price)) {
							if (!author.equalsIgnoreCase(plugin.getConfig().getString("admin_shop_owner"))) {
								Block below = sign.getWorld().getBlockAt(sign.getX(), sign.getY() - 1, sign.getZ());
								if (below.getType() == Material.CHEST) {
									Chest chest = (Chest) below.getState();
									Inventory chestinv = chest.getBlockInventory();
									if (chestinv.contains(material, quantity)) {
										chestinv.removeItem(sold);
										chestinv.addItem(new ItemStack(currency, price));
									}
									else {
										event.getPlayer().sendMessage(ChatColor.RED + plugin.getConfig().getString("not_enough_in_chest"));
										return;
									}
								}
								else {
									event.getPlayer().sendMessage(ChatColor.RED + plugin.getConfig().getString("below_not_chest"));
									return;
								}
							}
							event.getPlayer().getInventory().removeItem(new ItemStack(currency, price));
							event.getPlayer().getInventory().addItem(sold);
							event.getPlayer().sendMessage(ChatColor.GREEN + plugin.getConfig().getString("buy_notice").replace("${quantity}", quantity + "").replace("${material}", material.name()).replace("${price}", "" + price));
							event.getPlayer().updateInventory();
							
						}
						else {
							event.getPlayer().sendMessage(ChatColor.RED + plugin.getConfig().getString("cannot_pay"));
						}
				}
			}
		}
	}
}
