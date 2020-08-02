package me.dessie.DessieLib.inventoryapi;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class InventoryBuilder {

    public static HashMap<String, InventoryBuilder> inventories = new HashMap<>();

    private int size;
    private String name;
    private Runnable closeEvent;
    private Runnable pageChange;
    private Inventory inventory;
    private List<InventoryBuilder> pages = new ArrayList<>();
    private Player player;
    private int page;

    ItemBuilder clickedItem;
    boolean preventClose = false;

    public HashMap<Integer, ItemBuilder> items = new HashMap<>();

    public InventoryBuilder(int size, String name) {
        if(InventoryAPI.isRegistered()) {
            this.size = size;
            this.name = name;
            this.page = 0;

            this.pages.add(this);
        } else {
            throw new NullPointerException("You need to register the listeners with a new InventoryAPI before creating InventoryBuilders!");
        }
    }

    public String getName() {
        return this.name;
    }
    public int getSize() {
        return this.size;
    }
    public ItemBuilder getItem(int slot) { return this.items.get(slot); }
    public ItemBuilder getClickedItem() { return this.clickedItem; }
    public Inventory getInventory() { return this.inventory; }
    public Player getPlayer() { return this.player; }
    public HashMap<Integer, ItemBuilder> getItems() {return this.items;};
    public int getCurrentPage() { return this.page + 1; }

    //Open the Inventory.
    public InventoryBuilder open(Player player) {
        Inventory inv = Bukkit.createInventory(null, this.size, InventoryAPI.color(this.name));
        for(int i : this.items.keySet()) {
            inv.setItem(i, this.items.get(i).getItem());
        }

        this.player = player;
        this.inventory = inv;
        player.openInventory(inv);
        inventories.put(player.getName(), this);

        return this;
    }

    //Closes the inventory. Useful if you have prevented the user from closing it.
    public InventoryBuilder close(Player player) {
        this.preventClose = false;
        player.closeInventory();

        return this;
    }

    //Updates the Inventory to reflect the InventoryBuilder if it exists
    void update() {
        if(this.getInventory() != null) {
            for(int i = 0; i < this.getSize(); i++) {
                if(this.getItem(i) == null) {
                    this.getInventory().setItem(i, new ItemStack(Material.AIR));
                } else {
                    this.getInventory().setItem(i, this.items.get(i).getItem());
                }
            }
        }

    }

    //Updates the InventoryBuilder to reflect the Inventory.
    void updateBuilder() {
        for(int i = 0; i < this.getInventory().getSize(); i++) {
            if(this.getItem(i) == null && this.getInventory().getItem(i) == null) continue;

            if(this.getItem(i) == null || !this.getItem(i).getItem().isSimilar(this.getInventory().getItem(i))) {
                setItem(this.getInventory().getItem(i), i, false);
            } else {
                //Make sure the amounts are the same as well.
                if(this.getItem(i) != null) {
                    if(this.getItem(i).getAmount() != this.getInventory().getItem(i).getAmount()) {
                        this.getItem(i).setAmount(this.getInventory().getItem(i).getAmount(), false);
                    }
                }
            }
        }
    }

    public InventoryBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public InventoryBuilder setSize(int size) {
        this.size = size;
        return this;
    }

    //Adds the item in the first available slot.
    public ItemBuilder addItem(ItemBuilder item) {
        for(int i = 0; i < this.getSize(); i++) {
            if(this.items.get(i) == null) {
                return setItem(item, i);
            }
        }

        Bukkit.getLogger().log(Level.INFO, "Inventory is full, could not find blank space. Returning null for ItemBuilder.");
        return null;
    }

    //Adds the item in the first available slot.
    public ItemBuilder addItem(ItemStack item) {
        for(int i = 0; i < this.getSize(); i++) {
            if(this.items.get(i) == null) {
                return setItem(item, i);
            }
        }

        Bukkit.getLogger().log(Level.INFO, "Inventory is full, could not find blank space. Returning null for ItemBuilder.");
        return null;
    }



    public ItemBuilder setItem(ItemStack item, int slot, boolean update) {
        if(item == null || item.getType() == Material.AIR) {
            this.items.remove(slot);
            if(update) this.update();

            return null;
        } else {
            return setItem(new ItemBuilder(item), slot, update);
        }
    }

    public ItemBuilder setItem(ItemBuilder item, int slot, boolean update) {
        if(item == null || item.getItem().getType() == Material.AIR) {
            this.items.remove(slot);

            if(update) this.update();

            return null;
        } else {
            this.items.put(slot, item);
            item.setBuilder(this);
            item.slot = slot;

            if(update) this.update();

            return item;
        }
    }

    public InventoryBuilder setItems(ItemBuilder item, boolean update, Integer... slots) {
        for(int i : slots) {
            setItem(item, i, update);
        }
        return this;
    }

    public InventoryBuilder setItems(ItemStack item, boolean update, Integer... slots) {
        for(int i : slots) {
            setItem(item, i, update);
        }
        return this;
    }

    //Sets a new ItemBuilder in the slot. Returns this new ItemBuilder.
    //If item is null or AIR, the slot will be removed. Returns null.
    public InventoryBuilder setItems(ItemBuilder item, Integer... slots) {
        return setItems(item, true, slots);
    }

    //Sets a new ItemBuilder in the slot. Returns this new ItemBuilder.
    //If item is null or AIR, the slot will be removed. Returns null.
    public InventoryBuilder setItems(ItemStack item, Integer... slots) {
        return setItems(item, true, slots);
    }

    //Sets a new ItemBuilder in the slot. Returns this new ItemBuilder.
    //If item is null or AIR, the slot will be removed. Returns null.
    public ItemBuilder setItem(ItemStack item, int slot) {
        return setItem(item, slot, true);
    }

    //Sets a the ItemBuilder in the slot. Returns this ItemBuilder.
    //If item is null or AIR, the slot will be removed. Returns null.
    public ItemBuilder setItem(ItemBuilder item, int slot) {
        return this.setItem(item, slot, true);
    }

    public InventoryBuilder setContents(Inventory inventory) {
        return setContents(inventory.getContents());
    }

    //Sets the contents of the InventoryBuilder with an ItemStack[]
    public InventoryBuilder setContents(ItemStack[] items) {
        for(int i = 0; i < items.length; i++) {
            if(items[i] == null || items[i].getType() == Material.AIR) continue;

            setItem(items[i], i);
        }
        return this;
    }

    //Sets the contents of the InventoryBuilder to the list.
    public InventoryBuilder setContents(List<ItemBuilder> items) {
        return setContents(items, true);
    }

    //Sets the contents of the InventoryBuilder to the list.
    //If bySlot is true, set them by their slot.
    //If bySlot if false, set them by their order in the list.
    public InventoryBuilder setContents(List<ItemBuilder> items, boolean bySlot) {
        if(bySlot) {
            for(ItemBuilder item : items) {
                setItem(item, item.getSlot());
            }
        } else {
            for(int i = 0; i < items.size() && i < this.getSize(); i++) {
                setItem(items.get(i), i);
            }
        }

        return this;
    }

    //Sets the contents of this InventoryBuilder as the other InventoryBuilder.
    public InventoryBuilder setContents(InventoryBuilder builder) {
        setContents(new ArrayList<>(builder.getItems().values()));
        return this;
    }

    //Clears the inventory completely.
    public InventoryBuilder clear() {
        this.items.clear();
        this.update();
        return this;
    }

    //Code to run when inventory is closed.
    public InventoryBuilder onClose(Runnable runnable) {
        this.closeEvent = runnable;
        return this;
    }

    public InventoryBuilder onPageChange(Runnable runnable) {
        this.pageChange = runnable;
        return this;
    }

    //Prevents player from closing the inventory
    public InventoryBuilder togglePreventClose() {
        if(!this.preventClose) {
            this.preventClose = true;
        } else {
            this.preventClose = false;
        }

        return this;
    }

    public static InventoryBuilder getBuilder(Player player) {
        return inventories.get(player.getName());
    }

    //Fills all null slots with the specified ItemBuilder.
    public InventoryBuilder fill(ItemBuilder item) {
        for(int i = 0; i < this.size; i++) {
            if(getItem(i) == null) {
                setItem(new ItemBuilder(item), i);
            }
        }
        return this;
    }


    //Shifts all the items to the top most left position.
    //If byItems is true, items will be categorized
    //by their type. Otherwise they will.

    //byItems will also organize them by their amount.

    //Organizes items alphabetically.
    public InventoryBuilder organize(boolean byItems) {

        //Move all the items to the top left.
        for(int i = 0; i < this.getSize(); i++) {
            if(this.getItem(i) == null) continue;

            for(int j = 0; j < this.getSize(); j++) {
                if(this.getItem(j) == null) {
                    setItem(this.getItem(i), j);
                    setItem((ItemStack) null, i);
                    break;
                }
            }
        }

        if(byItems) {
            Comparator<ItemBuilder> nameSort = Comparator.comparing(item -> item.getItem().getType().toString());
            Comparator<ItemBuilder> amountSort = Comparator.comparing(ItemBuilder::getAmount).reversed();

            //Sort by name then by amount
            this.setContents(this.getItems().values().stream().map(ItemBuilder::new)
                    .sorted(nameSort.thenComparing(amountSort))
                    .collect(Collectors.toList()), false);
        }

        //Update the inventory.
        this.update();

        return this;
    }

    public InventoryBuilder organize() {
        return organize(false);
    }


    //Combines all of the same type
    //of ItemStack to their maximum stack size.
    public InventoryBuilder condense() {
        for(int i = 0; i < this.getSize(); i++) {
            if(!this.getItems().containsKey(i)) continue;
            ItemBuilder item = this.getItem(i);

            //Work through the list backwards so we condense at the most top left item.
            for(int j = this.size - 1; j > 0 && j >= i; j--) {
                if(!this.getItems().containsKey(j)) continue;
                ItemBuilder condensed = this.getItem(j);

                //Don't combine to itself.
                if(item == condensed) continue;

                if (item.isSimilar(condensed) && item.getAmount() <= item.getItem().getMaxStackSize()) {
                    while(condensed.getAmount() > 0 && item.getAmount() < item.getItem().getMaxStackSize()) {
                        item.setAmount(item.getAmount() + 1);

                        if(condensed.getAmount() - 1 != 0) {
                            condensed.setAmount(condensed.getAmount() - 1);
                        } else {
                            condensed.setAmount(0);
                            this.setItem((ItemStack) null, condensed.getSlot());
                        }

                    }
                }
            }
        }

        return this;
    }

    //Replaces specified ItemBuilders with another.
    public InventoryBuilder replaceAll(ItemBuilder oldItem, ItemBuilder replacement) {
        for(int i = 0; i < this.size; i++) {
            if(getItem(i).isSimilar(oldItem)) {
                setItem(replacement, i);
            }
        }
        return this;
    }

    //Finds the first ItemBuilder that is an exact copy of the specified ItemBuilder.
    public ItemBuilder findFirst(ItemBuilder item) {
        for(int i = 0; i < this.size; i++) {
            if(getItem(i) == item) {
                return getItem(i);
            }
        }

        return null;
    }

    //Finds the first ItemBuilder in the Inventory with the specified ItemStack.
    public ItemBuilder findFirst(ItemStack item) {
        for(int i = 0; i < this.size; i++) {
            if(getItem(i).getItem().isSimilar(item)) {
                return getItem(i);
            }
        }

        return null;
    }

    //Finds the first ItemBuilder in the Inventory with the specified material.
    public ItemBuilder findFirst(Material material) {
        for(int i = 0; i < this.size; i++) {
            if(getItem(i).getItem().getType() == material) {
                return getItem(i);
            }
        }

        return null;
    }

    //Sets the page arrows in the Inventory in the correct places.
    //Used if you need to force the arrows at a specific time.
    public InventoryBuilder setPageArrows() {
        this.setItem(ItemBuilder.buildItem(Material.ARROW, 1, "&cNext"), this.getSize() - 1)
                .onClick(this::nextPage).cancel();

        this.setItem(ItemBuilder.buildItem(Material.ARROW, 1, "&cPrevious"), this.getSize() - 9)
                .onClick(this::previousPage).cancel();

        return this;
    }

    //Adds an already defined builder to the page list.
    //This builder will have the current page automatically added
    //to it's list for backing up to work.
    public InventoryBuilder addPage(InventoryBuilder builder) {
        builder.setPageArrows();
        this.setPageArrows();

        this.pages.add(builder);
        builder.pages = this.pages;
        builder.page = this.pages.size() - 1;
        return this;
    }

    //Adds a new InventoryBuilder to the page list.
    //This builder will have the current page automatically added
    //to it's list for backing up to work.
    public InventoryBuilder addPage(int size, String name) {
        InventoryBuilder newBuilder = new InventoryBuilder(size, name);

        //Set the page arrows for both pages.
        newBuilder.setPageArrows();
        this.setPageArrows();

        this.pages.add(newBuilder);

        newBuilder.pages = this.pages;
        newBuilder.page = this.pages.size() - 1;
        return newBuilder;
    }

    //Puts the specified ItemBuilder in the Inventory borders.
    //Inventory must be atleast 27 in size.
    public InventoryBuilder createBorder(ItemBuilder item) {
        if(this.getSize() < 18) {
            throw new IllegalStateException("Inventory size must be atleast 27 to add borders!");
        }

        int[] edgeSlots54 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
        int[] edgeSlots45 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44};
        int[] edgeSlots36 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35};
        int[] edgeSlots27 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};

        switch (this.getSize()) {
            case 54: {
                for (int i : edgeSlots54) {
                    if (this.getItem(i) == null) {
                        setItem(item, i);
                    }
                }
                break;
            }

            case 45: {
                for (int i : edgeSlots45) {
                    if (this.getItem(i) == null) {
                        setItem(item, i);
                    }
                }
                break;
            }

            case 36: {
                for (int i : edgeSlots36) {
                    if (this.getItem(i) == null) {
                        setItem(item, i);
                    }
                }
                break;
            }

            case 27: {
                for (int i : edgeSlots27) {
                    if (this.getItem(i) == null) {
                        setItem(item, i);
                    }
                }
                break;
            }
        }

        return this;

    }

    //Internal method.
    //Executes the onClose Runnable
    void executeClose() {
        if(this.closeEvent != null) {
            this.closeEvent.run();
        }
    }

    //Internal method.
    //Opens the next page in the pages list.
    private void nextPage() {
        InventoryBuilder newPage = null;

        if(this.page + 1 <= this.pages.size()) {
            try {
                newPage = this.pages.get(this.page + 1);
            } catch (IndexOutOfBoundsException ignored) {}
        }

        if(newPage != null) {
            if(this.pageChange != null) {
                this.pageChange.run();
            }

            newPage.open(this.player);
        }
    }

    //Internal method.
    //Opens the previous page in the pages list.
    private void previousPage() {
        InventoryBuilder newPage = null;

        if(this.page - 1 >= 0) {
            try {
                newPage = this.pages.get(this.page - 1);
            } catch(ArrayIndexOutOfBoundsException e) { e.printStackTrace();}
        }

        if(newPage != null) {
            if(this.pageChange != null) {
                this.pageChange.run();
            }
            newPage.open(this.player);
        }
    }
}
