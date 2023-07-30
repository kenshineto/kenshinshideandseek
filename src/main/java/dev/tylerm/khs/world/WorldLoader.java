package dev.tylerm.khs.world;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.configuration.Map;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.*;
import java.nio.file.Files;

import static dev.tylerm.khs.configuration.Config.*;
import static dev.tylerm.khs.configuration.Localization.message;

public class WorldLoader {

	private final Map map;
	
	public WorldLoader(Map map) {
		this.map = map;
	}

	public World getWorld() {
		return Bukkit.getServer().getWorld(map.getGameSpawnName());
	}

	public void unloadMap() {
		World world = Bukkit.getServer().getWorld(map.getGameSpawnName());
		if (world == null) {
			Main.getInstance().getLogger().warning(map.getGameSpawnName() + " already unloaded.");
			return;
		}
		world.getPlayers().forEach(player -> exitPosition.teleport(player));
		Main.getInstance().scheduleTask(() -> {
			if (Bukkit.getServer().unloadWorld(world, false)) {
				Main.getInstance().getLogger().info("Successfully unloaded " + map.getGameSpawnName());
			} else {
				Main.getInstance().getLogger().severe("COULD NOT UNLOAD " + map.getGameSpawnName());
			}
		});
    }

    public void loadMap() {
		Main.getInstance().scheduleTask(() -> {
			Bukkit.getServer().createWorld(new WorldCreator(map.getGameSpawnName()).generator(new VoidGenerator()));
			World world = Bukkit.getServer().getWorld(map.getGameSpawnName());
			if (world == null) {
				Main.getInstance().getLogger().severe("COULD NOT LOAD " + map.getGameSpawnName());
				return;
			}
			world.setAutoSave(false);
		});
    }
 
    public void rollback() {
        unloadMap();
        loadMap();
    }
    
    public String save() {
		World world = Bukkit.getServer().getWorld(map.getSpawnName());
		if(world == null){
			return errorPrefix + message("MAPSAVE_INVALID").addAmount(map.getSpawnName());
		}
    	File current = new File(Main.getInstance().getWorldContainer()+File.separator+ map.getSpawnName());
    	if (current.exists()) {
			try {
				File destination = new File(Main.getInstance().getWorldContainer()+File.separator+ map.getGameSpawnName());
				File temp_destination = new File(Main.getInstance().getWorldContainer()+File.separator+"temp_"+ map.getGameSpawnName());
				copyFileFolder("region",true);
				copyFileFolder("entities",true);
				copyFileFolder("datapacks",false);
				copyFileFolder("data",false);
				File srcFile = new File(current, "level.dat");
                File destFile = new File(temp_destination, "level.dat");
				copyFile(srcFile,destFile);
				if (destination.exists()) {
					deleteDirectory(destination);
				}

				if (!temp_destination.renameTo(destination)) {
					return errorPrefix + message("MAPSAVE_FAIL_DIR").addAmount(temp_destination.getPath());
				}
			} catch(IOException e) {
				e.printStackTrace();
				return errorPrefix + message("COMMAND_ERROR");
			}
			return messagePrefix + message("MAPSAVE_END");
		} else {
			return errorPrefix + message("MAPSAVE_ERROR");
		}
    }
    
    private void copyFileFolder(String name, Boolean isMca) throws IOException {
    	File region = new File(Main.getInstance().getWorldContainer()+File.separator+ map.getSpawnName() +File.separator+name);
    	File temp = new File(Main.getInstance().getWorldContainer()+File.separator+"temp_"+ map.getGameSpawnName() +File.separator+name);
    	if (region.exists() && region.isDirectory()) {
    		if (!temp.exists())
    			if (!temp.mkdirs())
    				throw new IOException("Couldn't create region directory!");
    		String[] files = region.list();
			if (files == null) {
				Main.getInstance().getLogger().severe("Region directory is null or cannot be accessed");
				return;
			}
    		for (String file : files) {
    			if (isMca) {
	    			int minX = (int)Math.floor(map.getBoundsMin().getX() / 512.0);
	    			int minZ = (int)Math.floor(map.getBoundsMin().getZ() / 512.0);
	    			int maxX = (int)Math.floor(map.getBoundsMax().getX() / 512.0);
	    			int maxZ = (int)Math.floor(map.getBoundsMax().getZ() / 512.0);
	    			
	    			String[] parts = file.split("\\.");
	    			if (parts.length > 1) {
		    			if ( Integer.parseInt(parts[1]) < minX || Integer.parseInt(parts[1]) > maxX || Integer.parseInt(parts[2]) < minZ || Integer.parseInt(parts[2]) > maxZ )
		    				continue;
	    			}
    			}
    		
                File srcFile = new File(region, file);
                if (srcFile.isDirectory()) {
                	copyFileFolder(name+File.separator+file, false);
                } else {
                	File destFile = new File(temp, file);
                	copyFile(srcFile, destFile);
                }
            }
    	}
    }
    
    private void copyFile(File source, File target) throws IOException {
    	InputStream in = Files.newInputStream(source.toPath());
        OutputStream out = Files.newOutputStream(target.toPath());
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0)
            out.write(buffer, 0, length);
        in.close();
        out.close();
    }
	
	public static void deleteDirectory(File directoryToBeDeleted) {
	    File[] allContents = directoryToBeDeleted.listFiles();
	    if (allContents != null) {
	        for (File file : allContents) {
	            deleteDirectory(file);
	        }
	    }
		if (!directoryToBeDeleted.delete()) {
			throw new RuntimeException("Failed to delete directory: "+directoryToBeDeleted.getPath());
		}
	}
	
}
