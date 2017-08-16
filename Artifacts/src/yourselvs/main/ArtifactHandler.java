package yourselvs.main;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArtifactHandler {
	private ItemStack artifact;
	private int seconds;
	private Plugin plugin;
	private Thread artifactThread;
	private boolean running;
	private long dropTime;
	private int rangeMax, rangeMin;

	private Random rand;
	
	public static final Object artifactLock = new Object();
	
	public ArtifactHandler(Plugin plugin) {
		artifact = null;
		seconds = 0;
		running = false;
		this.plugin = plugin;
		
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
	}
	
	public void setSeconds(int seconds) {
		this.seconds = seconds;
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
	}
	
	public void setRunning(boolean running) {
		this.running = running;
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
					if(running && hasValidParameters()) {
						dropArtifacts();
					}
					
					try {
						long millis = 1000 * seconds;
						dropTime = System.currentTimeMillis() + (millis);
						wait(millis);
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
