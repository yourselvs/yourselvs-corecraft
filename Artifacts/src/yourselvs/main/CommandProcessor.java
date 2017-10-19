package yourselvs.main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

public class CommandProcessor {

	public static class Cmd {
		public CommandSender sender;
		public Command cmd;
		public String label;
		public String[] args;
		
		public Cmd(CommandSender sender, Command cmd, String label, String[] args){
			this.sender = sender;
			this.cmd = cmd;
			this.label = label;
			this.args = args;
		}
	}
	
	private Plugin plugin;

	public CommandProcessor(Plugin plugin) {
		this.plugin = plugin;
	}

	public void parseCommand(Cmd cmd) {
		switch(cmd.label) {
		case "redeem":
			parseRedeem(cmd);
			break;
		case "artifact":
			parseArtifact(cmd);
			break;		
		}
	}
	
	private void parseRedeem(Cmd cmd) {
		Player player;
		int numRedeemed = 0, amountRedeemed = 0, first;
		ItemStack air = new ItemStack(Material.AIR);
		
		// If user doesn't have permission, send error message
		if(!plugin.checkPermission("artifact.redeem", cmd.sender)) {
			return;
		}

		// If user isn't a player, send error message
		if(!(cmd.sender instanceof Player)) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "You must be a player to do this.");
			return;
		}
		
		// Get player
		player = (Player) cmd.sender;
		
		// Loop through all stacks of artifacts in player inventory
		// Get location and of first found artifact
		while((first = findFirstArtifact(player, plugin.getArtifactHandler().getArtifact())) != -1) {			
			// Get ItemStack of said artifact
			ItemStack artifacts = player.getInventory().getItem(first);
			
			// Loop through all artifacts in stack
			for(int i = 0; i < artifacts.getAmount(); i++) {
				// Calculate artifact value for each one in stack
				amountRedeemed += plugin.getArtifactHandler().redeemArtifact();
			}
			
			// Add number of artifacts to total amount
			numRedeemed += artifacts.getAmount();
			
			// Replace artifact with air
			player.getInventory().setItem(first, air);
		}
		
		// If no artifacts were found, send error message
		if(numRedeemed <= 0) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "You do not have any " + 
					ChatColor.YELLOW + "Artifacts" + ChatColor.RESET + " to redeem.");
		}
		else {
			// Make artifact string
			String artifacts = ChatColor.YELLOW + "" + numRedeemed + " Artifact";
			
			// Add plural
			if(numRedeemed > 1) {
				artifacts += "s";
			}
			
			// Send message to player with how much they earned from artifacts
			plugin.getMessenger().sendMessage(cmd.sender, artifacts + ChatColor.RESET + 
					" redeemed for a total of $" + ChatColor.YELLOW + amountRedeemed);
		}
	}
	
	private int findFirstArtifact(Player player, ItemStack artifact) {
		ItemStack[] contents = player.getInventory().getContents();
		ItemStack air = new ItemStack(Material.AIR);
		
		// Get artifact info
		String artifactName = artifact.getItemMeta().getDisplayName();
		
		// Iterate through player inventory
		for(int i = 0; i < contents.length; i++) {
			// Get current item info
			if(contents[i] != null && !contents[i].equals(air)) {
				String itemName = contents[i].getItemMeta().getDisplayName();
				
				// If it matches artifact, return success
				if(itemName != null && itemName.equals(artifactName)) {
					return i;
				}
			}
		}
		
		// No matches, return failure
		return -1;
	}
	
	private void parseArtifact(Cmd cmd) {
		// If user doesn't have permission, send error message
		if(!plugin.checkPermission("artifact.use", cmd.sender)) {
			return;
		}
		
		if(cmd.args.length < 1) {
			// If param isn't included, send help message
			parseArtifactHelp(cmd);
			return;
		}
		
		// Determine sub-command to execute
		switch(cmd.args[0]) {
		case "help":
			parseArtifactHelp(cmd);
			break;
		case "set":
			parseArtifactSet(cmd);
			break;
		case "time":
			parseArtifactTime(cmd);
			break;
		case "start":
			parseArtifactStart(cmd);
			break;
		case "stop":
			parseArtifactStop(cmd);
			break;
		case "state":
			parseArtifactState(cmd);
			break;
		case "range":
			parseArtifactRange(cmd);
			break;
		case "debug":
			parseArtifactDebug(cmd);
			break;
		default:
			parseArtifactError(cmd);
		}
	}
	
	
	private void parseArtifactHelp(Cmd cmd) {
		List<String> args = new ArrayList<String>();
		
		// If user doesn't have permission, send error message
		if(!plugin.checkPermission("artifact.help", cmd.sender)) {
			return;
		}
		
		addCommand(args, "redeem", "Redeems all artifacts in a player's inventory.");
		addCommand(args, "artifact help", "View information on artifact commands.");
		addCommand(args, "artifact state", "Retrieves information about the state of the plugin.");
		addCommand(args, "artifact set", "Sets the artifact to the item in your main hand.");
		addCommand(args, "artifact time [seconds]", "Sets the number of seconds in between artifact drops.");
		addCommand(args, "artifact range [min] [max]", "Sets the integer range of artifact values.");
		addCommand(args, "artifact stop", "Stops artifacts from dropping.");
		addCommand(args, "artifact start", "Begins dropping artifacts.");
		
		plugin.getMessenger().sendMessages(cmd.sender, args, "Artifact Commands");
	}
	
	private void addCommand(List<String> args, String key, String value) {
		args.add(ChatColor.YELLOW + "/" + key + ChatColor.RESET + " : " + value);
	}
	
	private void parseArtifactSet(Cmd cmd) {
		ItemStack handStack;
		Player player;
		
		// If user doesn't have permission, send error message
		if(!plugin.checkPermission("artifact.control", cmd.sender)) {
			return;
		}

		// If user isn't a player, send error message
		if(!(cmd.sender instanceof Player)) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "You must be a player to do this.");
			return;
		}
		
		// Abstract player
		player = (Player) cmd.sender;
		
		// Get player main hand
		handStack = player.getInventory().getItemInMainHand();

		// If holding nothing in main hand, send error message
		if(handStack.getType() == Material.AIR) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "You aren't holding anything in your main hand.");
			return;
		}
		
		// Set the amount in an artifact stack to 1, as artifacts are given one at a time
		handStack.setAmount(1);

		// Set the new artifact
		plugin.getArtifactHandler().setArtifact(handStack);
		
		// Send user message saying artifact has been updated
		plugin.getMessenger().sendMessage(cmd.sender, "The artifact has been set to: " + handStack.getItemMeta().getDisplayName());
	}
	
	private void parseArtifactTime(Cmd cmd) {
		// If sender doesn't have permission, send error message
		if(!plugin.checkPermission("artifact.control", cmd.sender)) {
			return;
		}

		// If no params are included, send user the current time
		if(cmd.args.length == 1) { 
			int time = plugin.getArtifactHandler().getSeconds();
			
			plugin.getMessenger().sendMessage(cmd.sender, "Artifacts drop every " + ChatColor.YELLOW + time + ChatColor.RESET + " seconds.");
		}
		// If integer param is included, set drop time
		else {
			// If time param is not an integer, send error message
			if(!isParsableInteger(cmd.args[1])) {
				plugin.getMessenger().sendErrorMessage(cmd.sender, "The time must be an integer in seconds.");
				return;
			}
			
			// Parse time param
			int time = Integer.parseInt(cmd.args[1]);
			
			// Set new timer
			plugin.getArtifactHandler().setSeconds(time);
			
			// Send user message notifying them of the change
			plugin.getMessenger().sendMessage(cmd.sender, "Artifact drop time set to " + ChatColor.YELLOW + time + ChatColor.RESET + " seconds.");
		}
	}
	
	private void parseArtifactStart(Cmd cmd) {
		// If sender doesn't have permission, send error message
		if(!plugin.checkPermission("artifact.control", cmd.sender)) {
			return;
		}
		
		// If dropper is already running, send error message
		if(plugin.getArtifactHandler().isRunning()) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "The artifact dropper is already running.");
		}
		// Set dropper to ON
		else {
			plugin.getArtifactHandler().setRunning(true);
			plugin.getMessenger().sendMessage(cmd.sender, "The artifact dropper is now running.");
		}
	}
	
	private void parseArtifactStop(Cmd cmd) {
		// If sender doesn't have permission, send error message
		if(!plugin.checkPermission("artifact.control", cmd.sender)) {
			return;
		}
		
		// If dropper is already running, send error message
		if(!plugin.getArtifactHandler().isRunning()) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "The artifact dropper is already stopped.");
		}
		// Set dropper to OFF
		else {
			plugin.getArtifactHandler().setRunning(false);
			plugin.getMessenger().sendMessage(cmd.sender, "The artifact dropper is now stopped.");
		}
	}
	
	private void parseArtifactState(Cmd cmd) {
		// If sender doesn't have permission, send error message
		if(!plugin.checkPermission("artifact.state", cmd.sender)) {
			return;
		}
		
		List<String> msgs = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
		
		// Add drop status message, green for running, red for stopped
		String running = (plugin.getArtifactHandler().isRunning() ? ChatColor.GREEN + "Running" : ChatColor.RED + "Stopped");
		msgs.add("Drop status: " + running);
		
		// Get and format time it takes for drops to refresh
		Date refreshTime = new Date(1000 * plugin.getArtifactHandler().getSeconds());
		msgs.add("Drop refresh time: " + ChatColor.YELLOW + sdf.format(refreshTime));
		
		// Get and format time (mm:ss) until next drop
		Date dropTime = new Date(plugin.getArtifactHandler().getDropTime() - System.currentTimeMillis());
		msgs.add("Time to next drop: " + ChatColor.YELLOW + sdf.format(dropTime));
		
		// Add artifact display name
		String itemName = plugin.getArtifactHandler().getArtifact().getItemMeta().getDisplayName();
		msgs.add("Artifact name: " + itemName);
		
		// Send messages to player
		plugin.getMessenger().sendMessages(cmd.sender, msgs, "Artifact Plugin State");
	}
	
	private void parseArtifactRange(Cmd cmd) {
		// If sender doesn't have permission, send error message
		if(!plugin.checkPermission("artifact.control", cmd.sender)) {
			return;
		}
		
		// If no param is included, display range to user
		if(cmd.args.length == 1) {
			// Get min and max values
			int rangeMin = plugin.getArtifactHandler().getRangeMin();
			int rangeMax = plugin.getArtifactHandler().getRangeMax();
			
			// Send user range information
			plugin.getMessenger().sendMessage(cmd.sender, "Artifact value range: " + 
					ChatColor.YELLOW + rangeMin + 
					ChatColor.RESET + "-" + 
					ChatColor.YELLOW + rangeMax);
		}
		else {
			// If only one num param is included, set min and max to that param
			if(cmd.args.length == 2) {
				// Check if num param is parsable
				if(!isParsableInteger(cmd.args[1])) {
					plugin.getMessenger().sendErrorMessage(cmd.sender, "The range must be an integer.");
					return;
				}
				
				// Parse num param
				int range = Integer.parseInt(cmd.args[1]);
				
				// Set min and max to the same number
				plugin.getArtifactHandler().setArtifactMinMax(range, range);
				
				// Send user confirmation
				plugin.getMessenger().sendMessage(cmd.sender, "Artifact value set to: " + ChatColor.YELLOW + range);
			}
			
			// If more than one param is included, set min/max to first two and ignore the rest
			else {
				// Check if params are parsable
				if(!isParsableInteger(cmd.args[1]) || !isParsableInteger(cmd.args[2])) {
					plugin.getMessenger().sendErrorMessage(cmd.sender, "Both range values must be integers.");
					return;
				}
				
				// Parse range values
				int range1 = Integer.parseInt(cmd.args[1]);
				int range2 = Integer.parseInt(cmd.args[2]);
				
				// Set min and max to the two values
				plugin.getArtifactHandler().setArtifactMinMax(range1, range2);
				
				// Send user confirmation
				plugin.getMessenger().sendMessage(cmd.sender, "Artifact value range set to: " + 
						ChatColor.YELLOW + plugin.getArtifactHandler().getRangeMin() + 
						ChatColor.RESET + "-" + 
						ChatColor.YELLOW + plugin.getArtifactHandler().getRangeMax());
			}
		}
	}
	
	private void parseArtifactDebug(Cmd cmd) {
		switch(cmd.args[1]) {
		case "seconds":		plugin.getMessenger().sendMessage(cmd.sender, "seconds: " + plugin.getArtifactHandler().getSeconds());
							break;
		case "droptime":	plugin.getMessenger().sendMessage(cmd.sender, "droptime: " + plugin.getArtifactHandler().getDropTime());
							plugin.getMessenger().sendMessage(cmd.sender, "currtime: " + System.currentTimeMillis());
							plugin.getMessenger().sendMessage(cmd.sender, "diff: " + (plugin.getArtifactHandler().getDropTime() - System.currentTimeMillis()));
							break;
		}
	}
	
	private void parseArtifactError(Cmd cmd) {
		plugin.getMessenger().sendErrorMessage(cmd.sender, "Command not recognized. Use " 
				+ ChatColor.YELLOW + ChatColor.BOLD + "/artifact help" + ChatColor.RESET + " to see commands.");
	}
	
	private boolean isParsableInteger(String input) {
		try {
			Integer.parseInt(input);
		} catch(Exception e) {
			return false;
		}
		
		return true;
	}
	
	/*
	private boolean isParsableBoolean(String input) {
		return input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false");
	}
	
	private boolean parseBoolean(String input) {
		return input.equalsIgnoreCase("true");
	}
	
	private boolean isParsableDouble(String input) {
		try {
			Double.parseDouble(input);
		} catch(Exception e) {
			return false;
		}
		
		return true;
	}
	*/
}
