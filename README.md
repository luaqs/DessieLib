## DessieLib
A generic Spigot API for making life easier

## :books: Requirements
- Java 17

## :newspaper: API

### :package: Installation / Download

#### Gradle
```groovy
maven {
  url "https://maven.pkg.github.com/dessie0/dessielib"
}

dependencies {
  compileOnly 'me.dessie:dessielib:1.1.2'
}
```

#### Maven
```xml
<dependencies>
  <dependency>
    <groupId>me.dessie</groupId>
    <artifactId>dessielib</artifactId>
    <version>1.1.2</version>
  </dependency>
</dependencies>
```

### :ledger: Documentation

The complete JavaDocs can be found [here](https://dessie0.github.io/DessieLib/) <br><br>

### :iphone: Features

DessieLib provides two main features that are cumbersome in CraftBukkit and Spigot, but easily contained within DessieLib.

- `InventoryAPI` is a powerful way to manage inventories without having to mess with events
- `ScoreboardAPI` used to create organized tablists & scoreboards without the hassle of dealing with teams.
- `ParticleAPI` can easily create complex particle patterns or shapes, and animate them dynamically.
- `EnchantmentAPI` can manage fully customizable Enchantments
- `ResourcepackAPI` Allows you to generate an entire resource pack by just dropping in files.
- `Packeteer` Allows you to listen for incoming and outgoing packets, and fire events for these packets.

<details>
<summary>Basic InventoryAPI Usage</summary>

```java
public class me.dessie.dessielib.me.dessie.dessielib.Main extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        InventoryAPI.register(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("testinventory")) {
            //Creates an ItemBuilder as 1 Diamond, named "Diamond" with AQUA color. 
            ItemBuilder item = new ItemBuilder(ItemBuilder.buildItem(Material.DIAMOND, 1, "&bDiamond"));
            //Does not allow the item to be picked up
            item.cancel();
            //When we click, the item will turn into this Emerald. Then back into the Diamond when we click again.
            item.cyclesWith(ItemBuilder.buildItem(Material.EMERALD, 1, "&aEmerald"));

            //When we click the item, we'll tell the player which item they clicked.
            item.onClick(((player, itemBuilder) -> {
                player.sendMessage("Clicked " + itemBuilder.getName() + "!");
            }));

            //Create the InventoryBuilder as a size of 9 and named "Test Inventory"
            InventoryBuilder inventoryBuilder = new InventoryBuilder(9, "Test Inventory");

            //Set the diamond item stack in slot 4
            inventoryBuilder.setItem(item, 4);

            //Tell the player they opened the inventory
            inventoryBuilder.onOpen((player, builder) -> {
                player.sendMessage("Opened " + builder.getName());
            });

            //Tell the player when they close the inventory
            inventoryBuilder.onClose((player, builder) -> {
                player.sendMessage("Closed " + builder.getName());
            });

            //Tell the player when the page changes
            inventoryBuilder.onPageChange((player, newPage) -> {
                player.sendMessage("Opening page " + newPage.getName());
            });

            //Add a new page to the Inventory with a size of 18 and name of "Page 2"
            inventoryBuilder.addPage(18, "Page 2");

            //Open the Inventory
            inventoryBuilder.open((Player) sender);
            return true;
        }
        return false;
    }
}
```
</details>

<details>
<summary>Basic ScoreboardAPI Usage</summary>

```java
public class me.dessie.dessielib.me.dessie.dessielib.Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        ScoreboardAPI.register(this);

        //Using a BukkitTask, we can get everyone's Scoreboard and update it with THEIR
        //information. Such as their current Ping and Location!

        //You can do this with any part of the Scoreboard, Header, Footer, Scores, Titles, etc.
        Bukkit.getScheduler().runTaskTimer(this, () -> {

            //Loop through all active boards
            for(ScoreboardAPI scoreboardAPI : ScoreboardAPI.getBoards()) {
                Player player = scoreboardAPI.getPlayer();

                //Get & set the footer string.
                String footer = "&6Current Ping: &a" + ((CraftPlayer) player).getHandle().ping
                        + " &2X: " + (int) player.getLocation().getX() + " &2Y: " + (int) player.getLocation().getY()
                        + " &2Z: " + (int) player.getLocation().getZ();
                scoreboardAPI.setTabFooter(footer);
            }
        }, 20, 20);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        //Get the player that joined
        Player player = event.getPlayer();

        //Create the new Scoreboard, the name is just the default title.
        ScoreboardAPI scoreboard = new ScoreboardAPI(player, "Scoreboard");

        //Add a line on the first score that says hello
        scoreboard.setLine("Hello, " + player.getName(), 0);

        //Add an empty line
        scoreboard.setLine("", 1);

        //Create a score animation. This will make the line on Score 2 go from 1, 2, 3, 4, 5 every half second (10 ticks)
        List<String> animation = Arrays.asList("&11", "&22", "&33", "&44", "&55");
        scoreboard.animateScore(animation, 10, 2);

        /*
        Create a title animation. This method will loop through each element in the array and display
        it every 20 ticks (1 second)
         */
        List<String> titleAnimation = Arrays.asList("&3Server &6Name", "&6Server &3Name");
        scoreboard.animateTitle(titleAnimation, 20);

        /*
        Add the teams to the Scoreboard, this is how we're going to organize our tablist!
        The name doesn't matter too much, but you will be using this later, so don't forget what it is!
        The Color white makes it so our player's names are white.
        The prefix is what appears before the Player's names.

        The weight is the ordering of the tablist. A lower weight means they're higher on the list.
        After team weight, players are organized alphabetically.
        */
        scoreboard.addTablistTeam("owner", ChatColor.WHITE, "&c&lOwner &f", 0);
        scoreboard.addTablistTeam("member", ChatColor.WHITE, "&7Member &f", 50);

        /*
        Set this player's team. This can be determined any way you want, and you can have up to 100 teams.
        We're using a basic permission to determine whether we add to owner or member.
         */
        if (player.hasPermission("tablist.owner")) {
            //Set their team
            scoreboard.setPlayerTeam("owner");

            //Player's on the "owner" team cannot be pushed around
            scoreboard.setCollidable(false);
        } else {
            //Set their team
            scoreboard.setPlayerTeam("member");
            //Players on the "member" team CAN be pushed around
            scoreboard.setCollidable(true);
        }

        //Set the criteria to health. This will update the number next to a player's name in tab
        //to accurately represent their current health.
        scoreboard.setTablistCriteria("health");
    }
}
```

</details>

DessieLib also features small API elements such as
- `ConfigManager`, which is used for basic external configuration loading
- `Base64`, which can read and write Base64 strings into `ItemStack`s or `List<ItemStack>`
- `LoopedRunnable` which will run a BukkitRunnable a set number of times before automatically stopping.
- `SoundUtil` which can grab the Break/Place sound of specific blocks.
 