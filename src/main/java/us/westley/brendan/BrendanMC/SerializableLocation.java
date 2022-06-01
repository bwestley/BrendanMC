package us.westley.brendan.BrendanMC;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.*;
import java.util.UUID;

public class SerializableLocation implements Serializable {
    @Serial
    private static final long serialVersionUID = 3735674816286082699L;
    transient World world;
    double x;
    double y;
    double z;
    float yaw;
    float pitch;

    public SerializableLocation(Location location) {
        world = location.getWorld();
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        yaw = location.getYaw();
        pitch = location.getPitch();
    }

    public SerializableLocation(World world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Serial
    private void readObject(ObjectInputStream inputStream) throws ClassNotFoundException, IOException
    {
        world = Bukkit.getServer().getWorld(UUID.fromString(inputStream.readUTF()));
        x = inputStream.readDouble();
        y = inputStream.readDouble();
        z = inputStream.readDouble();
        yaw = inputStream.readFloat();
        pitch = inputStream.readFloat();
    }

    @Serial
    private void writeObject(ObjectOutputStream outputStream) throws IOException
    {
        outputStream.writeUTF(world.getUID().toString());
        outputStream.writeDouble(x);
        outputStream.writeDouble(y);
        outputStream.writeDouble(z);
        outputStream.writeFloat(yaw);
        outputStream.writeFloat(pitch);
    }

    public Location getLocation() {
        return new Location(world, x, y, z, yaw, pitch);
    }
}
