package yourselvs.main;

import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import yourselvs.utils.CfgVars;

public class ArtifactHandler {
	private Plugin plugin;
	
	private ItemStack artifact;
	private int seconds;
	private Thread artifactThread;
	private boolean running;
	private long dropTime;
	private int rangeMax, rangeMin;

	private Random rand;
	
	public static final Object artifactLock = new Object();
	
	public ArtifactHandler(Plugin plugin) {
		this.plugin = plugin;
		
		if(plugin.getConfig().contains(CfgVars.artifactItemStack)) {
			artifact = plugin.getConfig().getItemStack(CfgVars.artifactItemStack);
		}
		else {
			artifact = null;
		}
		
		seconds = plugin.getConfig().getInt(CfgVars.secondsBetweenDrop);
		running = plugin.getConfig().getBoolean(CfgVars.isPluginRunning);
		rangeMax = plugin.getConfig().getInt(CfgVars.artifactRangeMax);
		rangeMin = plugin.getConfig().getInt(CfgVars.artifactRangeMin);
		
		startArtifactThread();
	}
	
	public ItemStack getArtifact() {return artifact;}
	public int getSeconds() {return seconds;}
	public boolean isRunning() {return running;}
	public long getDropTime() {return dropTime;}
	public int getRangeMin() {return rangeMin;}
	public int getRangeMax() {return rangeMax;}
	
	public void setArtifact(ItemStack artifact) {
		this.artifact = artifact;
		
		plugin.updateConfig(CfgVars.artifactItemStack, artifact);
	}
	
	public void setSeconds(int seconds) {
		this.seconds = seconds;
		
		plugin.updateConfig(CfgVars.secondsBetweenDrop, seconds);
	}
	
	public void setArtifactMinMax(int val1, int val2) {
		if(val1 < val2) {
			rangeMin = val1;
			rangeMax = val2;
		}
		else {
			rangeMin = val2;
			rangeMax = val1;
		}
		plugin.updateConfig(CfgVars.artifactRangeMin, rangeMin);
		plugin.updateConfig(CfgVars.artifactRangeMax, rangeMax);
	}
	
	public void setRunning(boolean running) {
		this.running = running;
		
		plugin.updateConfig(CfgVars.isPluginRunning, running);
	}
	
	public double redeemArtifact() {
		// Get range of random numbers
		int valueRange = rangeMax - rangeMin;
		
		// Get value in cents
		valueRange *= 100;
		
		// Init rand variable
		rand = new Random(System.currentTimeMillis());
		
		// get number of cents to add to min
		double value = rand.nextInt(valueRange);
		
		// get value in dollars
		value /= 100.0;
		
		// add min
		value += rangeMin; 
		
		// return value
		return value;
	}
	
	private void startArtifactThread() {		
		running = true;
		
		artifactThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					if(!hasValidParameters()) {
						plugin.getLogger().log(Level.SEVERE, "Artifacts could not drop as parameters were invalid.");
					}
					else if(running) {
						dropArtifacts();
					}
					
					try {
						long millis = 1000 * seconds;
						dropTime = System.currentTimeMillis() + millis;
						Thread.sleep(millis);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		artifactThread.setName(plugin.getPluginName() + ": Artifact Dropper Thread");
		artifactThread.start();
	}
	
	private void dropArtifacts() {
		if(artifact != null) {
    		for(Player player : Bukkit.getOnlinePlayers()) {
    			int firstEmpty = player.getInventory().firstEmpty();
    			int firstArtifact = player.getInventory().first(artifact);
    			
    			if(firstEmpty < 0) {
    				plugin.getMessenger().sendErrorMessage(player, "You could not receive an artifact because your inventory is full.");
    				
    			}
    			else if(firstArtifact >= 0) {
    				plugin.getMessenger().sendErrorMessage(player, "You could not receive an artifact because you have one in your inventory already.");
    			}
    			else {
    				player.getInventory().setItem(firstEmpty, artifact);
    			}
    		}
		}
		
		plugin.getMessenger().sendServerMessage("Artifacts have been dropped to all players! Artifacts drop again in 10 minutes. Use " + ChatColor.YELLOW + "/redeem" + ChatColor.RESET + " to redeem them to the server.");
	}
	
	private boolean hasValidParameters() {
		if(artifact == null || artifact.getAmount() != 1 || seconds < 1) {
			return false;
		}
		
		return true;
	}
}
