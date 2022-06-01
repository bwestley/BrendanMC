package us.westley.brendan.BrendanMC;

import org.bukkit.Location;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DefenseTowers implements Serializable {
    @Serial
    private static final long serialVersionUID = 2732760277327932787L;

    public final ArrayList<DefenseTower> defenseTowers;

    public DefenseTowers() {
        this.defenseTowers = new ArrayList<>();
    }

    public DefenseTowers(ArrayList<DefenseTower> defenseTowers) {
        this.defenseTowers = defenseTowers;
    }

    public DefenseTowers(DefenseTowers loadedData) {
        this.defenseTowers = loadedData.defenseTowers;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean saveData(File file) {
        try {
            // Save data to file.
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(new FileOutputStream(file));
            out.writeObject(this);
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public DefenseTower getDefenseTower(Location location) {
        for (DefenseTower defenseTower: defenseTowers) if (defenseTower.location.getLocation().equals(location)) return defenseTower;
        return null;
    }

    public static DefenseTowers loadData(String filePath) {
        try {
            // Load data from file.
            BukkitObjectInputStream in = new BukkitObjectInputStream(new FileInputStream(filePath));
            DefenseTowers data = (DefenseTowers) in.readObject();
            in.close();
            return data;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            // The file does not exist or is invalid: provide a new DefenseTowers object.
            return new DefenseTowers();
        }
    }
}
