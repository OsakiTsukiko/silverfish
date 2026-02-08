package osaki.silverfish;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WaypointDB {
    private Connection connection;
    private JavaPlugin plugin;
    private final Object dbLock = new Object();

    WaypointDB(Connection connection, JavaPlugin plugin) {
        this.connection = connection;
        this.plugin = plugin;
    }

    public void createTables() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS waypoints (
                player_uuid TEXT NOT NULL,
                name TEXT NOT NULL,
                world TEXT NOT NULL,
                x REAL NOT NULL,
                y REAL NOT NULL,
                z REAL NOT NULL,
                yaw REAL NOT NULL,
                pitch REAL NOT NULL,
                PRIMARY KEY (player_uuid, name)
            );
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void setWaypoint(Player player, String name) throws SQLException {
        synchronized (dbLock) {
            Location loc = player.getLocation();

            String sql = """
                INSERT OR REPLACE INTO waypoints(player_uuid, name, world, x, y, z, yaw, pitch)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, player.getUniqueId().toString());
                ps.setString(2, name);
                ps.setString(3, loc.getWorld().getName());
                ps.setDouble(4, loc.getX());
                ps.setDouble(5, loc.getY());
                ps.setDouble(6, loc.getZ());
                ps.setFloat(7, loc.getYaw());
                ps.setFloat(8, loc.getPitch());
                ps.executeUpdate();
            }
        }
    }

    public Location getWaypoint(Player player, String name) throws SQLException {
        synchronized (dbLock) {
            String sql = """
                        SELECT world, x, y, z, yaw, pitch
                        FROM waypoints
                        WHERE player_uuid = ? AND name = ?
                        LIMIT 1
                    """;

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, player.getUniqueId().toString());
                ps.setString(2, name);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;

                    World world = plugin.getServer().getWorld(rs.getString("world"));
                    if (world == null) return null;

                    return new Location(
                            world,
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getFloat("yaw"),
                            rs.getFloat("pitch")
                    );
                }
            }
        }
    }

    public boolean deleteWaypoint(Player player, String name) throws SQLException {
        synchronized (dbLock) {
            String sql = """
                        DELETE FROM waypoints
                        WHERE player_uuid = ? AND name = ?
                    """;

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, player.getUniqueId().toString());
                ps.setString(2, name);

                return ps.executeUpdate() > 0;
            }
        }
    }

    public List<String> listWaypoints(Player player) throws SQLException {
        synchronized (dbLock) {
            List<String> result = new ArrayList<>();

            String sql = """
                        SELECT name FROM waypoints
                        WHERE player_uuid = ?
                        ORDER BY name ASC
                    """;

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, player.getUniqueId().toString());

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(rs.getString("name"));
                    }
                }
            }

            return result;
        }
    }
}
