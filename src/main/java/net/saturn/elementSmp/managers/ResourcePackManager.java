package net.saturn.elementSmp.managers;

import com.sun.net.httpserver.HttpServer;
import net.saturn.elementSmp.ElementSmp;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePackManager implements Listener {
    private final ElementSmp plugin;
    private HttpServer server;
    private byte[] packHash;
    private String packUrl;
    private boolean enabled;
    private String promptMessage;
    private boolean required;

    public ResourcePackManager(ElementSmp plugin) {
        this.plugin = plugin;
        loadConfig();
        if (enabled) {
            setupResourcePack();
        }
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        this.enabled = config.getBoolean("resource_pack.enabled", true);
        this.promptMessage = config.getString("resource_pack.prompt_message", "Please download the resource pack!");
        this.required = config.getBoolean("resource_pack.required", false);
    }

    private void setupResourcePack() {
        try {
            File packFolder = new File(plugin.getDataFolder(), "resourcepack");
            File zipFile = new File(plugin.getDataFolder(), "resourcepack.zip");

            // Extract from JAR to data folder
            extractResourcePack(packFolder);

            // Create ZIP
            createZip(packFolder, zipFile);

            // Calculate Hash
            this.packHash = calculateHash(zipFile);

            // Start HTTP Server
            startServer(zipFile);

            plugin.getLogger().info("Resource pack server started on port " + plugin.getConfig().getInt("resource_pack.port"));
            plugin.getServer().getPluginManager().registerEvents(this, plugin);

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to setup resource pack server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void extractResourcePack(File folder) throws IOException {
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String[] resources = {
                "resourcepack/pack.mcmeta",
                "resourcepack/assets/minecraft/items/custom/upgrader_1.json",
                "resourcepack/assets/minecraft/items/custom/upgrader_2.json",
                "resourcepack/assets/minecraft/items/custom/reroller.json",
                "resourcepack/assets/minecraft/items/custom/advanced_reroller.json",
                "resourcepack/assets/minecraft/models/item/custom/reroller.json",
                "resourcepack/assets/minecraft/models/item/custom/advanced_reroller.json",
                "resourcepack/assets/minecraft/models/item/custom/upgrader_1.json",
                "resourcepack/assets/minecraft/models/item/custom/upgrader_2.json",
                "resourcepack/assets/minecraft/textures/item/custom/reroller.png",
                "resourcepack/assets/minecraft/textures/item/custom/advanced_reroller.png",
                "resourcepack/assets/minecraft/textures/item/custom/upgrader_1.png",
                "resourcepack/assets/minecraft/textures/item/custom/upgrader_2.png",
                "resourcepack/pack.png"
        };

        for (String res : resources) {
            try {
                // Get the resource from JAR
                InputStream in = plugin.getResource(res);
                if (in == null) {
                    plugin.getLogger().warning("Could not find resource: " + res);
                    continue;
                }

                // Create parent directories
                File outFile = new File(plugin.getDataFolder(), res);
                outFile.getParentFile().mkdirs();

                // Copy the file
                Files.copy(in, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                in.close();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to extract resource: " + res + " - " + e.getMessage());
            }
        }
    }

    private void createZip(File sourceFolder, File zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Path sourcePath = sourceFolder.toPath();
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String relativePath = sourcePath.relativize(file).toString().replace("\\", "/");
                    zos.putNextEntry(new ZipEntry(relativePath));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private byte[] calculateHash(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return digest.digest();
    }

    private void startServer(File zipFile) throws IOException {
        int port = plugin.getConfig().getInt("resource_pack.port", 8080);
        String host = plugin.getConfig().getString("resource_pack.host", "");

        if (host.isEmpty()) {
            try {
                java.net.DatagramSocket socket = new java.net.DatagramSocket();
                socket.connect(java.net.InetAddress.getByName("8.8.8.8"), 10002);
                host = socket.getLocalAddress().getHostAddress();
                socket.close();
            } catch (Exception e) {
                host = Bukkit.getIp();
                if (host.isEmpty() || host.equals("0.0.0.0")) host = "127.0.0.1";
            }
        }

        this.packUrl = "http://" + host + ":" + port + "/resourcepack.zip";

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/resourcepack.zip", exchange -> {
            byte[] response = Files.readAllBytes(zipFile.toPath());
            exchange.getResponseHeaders().add("Content-Type", "application/zip");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        server.setExecutor(null);
        server.start();

        plugin.getLogger().info("Resource pack URL: " + packUrl);
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!enabled || packUrl == null) return;

        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setResourcePack(packUrl, packHash, promptMessage, required);
        }, 20L);
    }
}