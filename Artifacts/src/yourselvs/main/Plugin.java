package yourselvs.main;


import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import yourselvs.main.CommandProcessor.Cmd;
import yourselvs.utils.DateFormatter;
import yourselvs.utils.Messenger;

public class Plugin extends JavaPlugin 
{	
	public static final String version = "0.1";
	public static final String pluginName = "Artifacts";
	public static final String prefix = "Artifacts";
	
	public final static Object commandLock = new Object();
	public final static Object configLock = new Object();
	
	private ArtifactHandler artifactHandler;
	private CommandProcessor commandProcessor;
	private DateFormatter formatter;
	private Messenger messenger;
	
	private String normalPrefix = "[" + ChatColor.DARK_GREEN + prefix + ChatColor.RESET + "] ";
	private String linkPrefix = ChatColor.AQUA + "[" + ChatColor.DARK_GREEN + prefix + ChatColor.RESET + ChatColor.AQUA + "]" + ChatColor.RESET + " ";
	
	public String getPluginName() {return pluginName;}
	
	public Messenger getMessenger() {return messenger;}
	public DateFormatter getFormatter() {return formatter;}
	public CommandProcessor getCommandProcessor() {return commandProcessor;}
	public ArtifactHandler getArtifactHandler() {return artifactHandler;}
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		saveConfig();
    	
    	formatter = new DateFormatter();
    	messenger = new Messenger(this, normalPrefix, linkPrefix, ChatColor.YELLOW);

    	commandProcessor = new CommandProcessor(this);
    	artifactHandler = new ArtifactHandler(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Thread commandThread = new Thread(new Runnable() {
	        public void run() {
	        	synchronized(commandLock) {
	        		commandProcessor.parseCommand(new Cmd(sender, command, label, args));
	        	}
	        }
		});
		
		commandThread.setName(pluginName + " Command Processor");
		commandThread.start();
		
		return true;
	}
	
	public void updateConfig(String str, Object obj) {
		Thread configThread = new Thread(new Runnable() {
			public void run() {
				synchronized(configLock) {
					getConfig().set(str, obj);
					
					saveConfig();
				}
			}
		});
		
		configThread.setName(pluginName + " Config Updater");
		configThread.start();
	}
	
	public boolean checkPermission(String permission, CommandSender player) {
		if(!player.hasPermission(permission)) {
			messenger.sendErrorMessage(player, "You don't have permission to do this.");
			getLogger().log(Level.WARNING, "Player \"" + player.getName() + "\" attempted to do something that required permission \"" + permission + "\".");
			return false;
		}
		
		return true;
	}
}