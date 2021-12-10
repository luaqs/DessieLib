package me.dessie.dessielib.resourcepack.webhost;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import me.dessie.dessielib.resourcepack.ResourcePack;
import me.dessie.dessielib.core.utils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.server.PluginDisableEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class ResourcePackServer implements HttpHandler, Listener {

    private HttpServer server;
    private ResourcePack resourcePack;
    private boolean required;
    private String context;
    private String address;
    private int port;

    private String kickMessage = Colors.color("&cYou are required to accept the Server Resource Pack to join this server!\nMake sure Server Resource Packs are enabled in &6Edit -> Server Resource Packs &cfor this server!");

    /*
    The way Minecraft hashing works, is that it stores the Server Resources as the Download link in SHA-1 format.
    This means to update the texture pack, we need to update the URL itself.

    This can be done, by simply attaching /<zip hash> at the end of the URL when it's changed.
    This means an example URL would look like http://localhost:8080/resourcepack/c8695ca42a9c90a6187e0f1e01a0f935b4b4e0f6
    Where `c8695ca42a9c90a6187e0f1e01a0f935b4b4e0f6` is the SHA-1 Hash of the generated .zip file.
    */
    private String packUrl;

    public ResourcePackServer(String address, int port, boolean required, String context) {
        this(address, port, required, context, null);
    }

    public ResourcePackServer(String address, int port, boolean required, ResourcePack pack) {
        this(address, port, required, "resourcepack", pack);
    }

    public ResourcePackServer(String address, int port, boolean required, String context, ResourcePack pack) {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
            this.required = required;
            this.context = context;
            this.address = address;
            this.port = port;

            //Delay starting until the resource pack is set.
            if(this.resourcePack != null) {
                this.setResourcePack(pack);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPort() { return port; }
    public String getAddress() {return address;}
    public String getContext() {return context;}
    public HttpServer getServer() {
        return server;
    }
    public ResourcePack getResourcePack() {return resourcePack;}
    public boolean isRequired() {return required;}
    public String getPackUrl() {return packUrl;}

    public String getKickMessage() {
        return kickMessage;
    }

    public void setResourcePack(ResourcePack pack) {
        this.resourcePack = pack;

        //Setup the webserver context
        String urlPath = "/" + this.getContext() + "/" + pack.getBuilder().getHash();
        server.createContext(urlPath, this);
        this.packUrl = "http://" + this.getAddress() + ":" + this.getPort() + urlPath;

        //Register the EventHandler
        ResourcePack.getPlugin().getServer().getPluginManager().registerEvents(this, ResourcePack.getPlugin());
        this.getServer().start();
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().set("Content-type", "application/zip");
            exchange.sendResponseHeaders(200, this.getResourcePack().getResourcePack().length());
            OutputStream outputStream = exchange.getResponseBody();
            Files.copy(this.getResourcePack().getResourcePack().toPath(), outputStream);
            outputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @EventHandler
    public void onResourceStatus(PlayerResourcePackStatusEvent event) {
        if(!this.isRequired()) return;

        if(event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED) {
            event.getPlayer().kickPlayer(this.getKickMessage());
        } else if(event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            event.getPlayer().kickPlayer(Colors.color("&cSomething went wrong while downloading the resource pack!"));
        }
    }

    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        //Shutdown the WebServer when the plugin disables.
        if(event.getPlugin() == ResourcePack.getPlugin()) {
            this.getResourcePack().getBuilder().getResourcePackServer().getServer().stop(0);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(ResourcePack.getPlugin(), () -> {
            //When the player joins, send them the resource pack.
            event.getPlayer().setResourcePack(this.getPackUrl(), this.getResourcePack().getBuilder().getHashBytes());
        });
    }
}
