package net.stickmix.prisonevo.items;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.athena.annotation.Id;
import net.stickmix.game.api.util.ChatUtil;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.utils.ItemUtils;
import net.stickmix.prisonevo.utils.UtilItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@SuppressWarnings("Duplicates")
public class EvoItems {

    @Id
    private final String owner;
    @Getter
    private final Set<Integer> bought;


    public void onItemBought(EvoItem item) {
        bought.remove(item.getPrevious());
        bought.add(item.getId());

        PrisonEvo.getInstance().getAthenaManager().getEvoItemsObjectPool().save(this.owner);
    }

    public void grantItem(int id) {
        Player p = Bukkit.getPlayerExact(owner);
        PlayerInventory pi = p == null || !p.isOnline() ? null : p.getInventory();
        EvoItem item = EvoItemsManager.getItem(id);
        if (item.getPrevious() != 0) {
            EvoItem prev = item;
            while ((prev = EvoItemsManager.getItem(prev.getPrevious())).getPrevious() != 0) {
                bought.remove(prev.getId());
                if (pi != null)
                    pi.remove(prev.getItem());
            }
            bought.remove(prev.getId());
            if (pi != null)
                pi.remove(prev.getItem());
        }
        if (item.getNext() != 0) {
            EvoItem next = item;
            while ((next = EvoItemsManager.getItem(next.getNext())).getNext() != 0) {
                bought.remove(next.getId());
                if (pi != null)
                    pi.remove(next.getItem());
            }
            bought.remove(next.getId());
            if (pi != null)
                pi.remove(next.getItem());
        }
        bought.add(item.getId());
        if (pi != null) {
            ItemStack stack = item.getItem();
            if (UtilItem.isHelmet(stack)) {
                pi.setHelmet(stack);
            } else if (UtilItem.isChestplate(stack)) {
                pi.setChestplate(stack);
            } else if (UtilItem.isLeggings(stack)) {
                pi.setLeggings(stack);
            } else if (UtilItem.isBoots(stack)) {
                pi.setBoots(stack);
            } else {
                pi.addItem(stack);
            }
        }
        PrisonEvo.getInstance().getAthenaManager().getEvoItemsObjectPool().save(this.owner);
    }

    public void removeItem(int id) {
        Player p = Bukkit.getPlayerExact(owner);
        PlayerInventory pi = p == null || !p.isOnline() ? null : p.getInventory();
        EvoItem item = EvoItemsManager.getItem(id);
        if (item.getPrevious() != 0) {
            EvoItem prev = item;
            while ((prev = EvoItemsManager.getItem(prev.getPrevious())).getPrevious() != 0) {
                bought.remove(prev.getId());
                if (pi != null)
                    pi.remove(prev.getItem());
            }
            bought.remove(prev.getId());
            if (pi != null)
                pi.remove(prev.getItem());
        }
        if (item.getNext() != 0) {
            EvoItem next = item;
            while ((next = EvoItemsManager.getItem(next.getNext())).getNext() != 0) {
                bought.remove(next.getId());
                if (pi != null)
                    pi.remove(next.getItem());
            }
            bought.remove(next.getId());
            if (pi != null)
                pi.remove(next.getItem());
        }
        bought.remove(item.getId());
        if (pi != null)
            pi.remove(item.getItem());
        String items = bought.stream().map(String::valueOf).collect(Collectors.joining(" "));

        PrisonEvo.getInstance().getAthenaManager().getEvoItemsObjectPool().save(this.owner);
    }

    public LinkedHashSet<Integer> getItemsToBeDisplayed() {
        LinkedHashSet<Integer> result = new LinkedHashSet<>(EvoItemsManager.ZEROS);
        bought.forEach(i -> {
            int prev = i;

            for (int id = prev; (id = EvoItemsManager.getItem(prev).getPrevious()) != 0; ) {
                prev = id;
            }
            result.remove(prev);
            int next = EvoItemsManager.getItem(i).getNext();
            if (next != 0)
                result.add(next);
        });
        return result;
    }

    public boolean hasItem(int id) {
        return bought.contains(id);
    }

    public void checkAndGive() {
        Player p = Bukkit.getPlayerExact(this.owner);
        if (p == null) {
            return;
        }
        PlayerInventory pi = p.getInventory();
        List<ItemStack> bought = this.bought.stream().map(EvoItemsManager::getItem).map(EvoItem::getItem).collect(Collectors.toList());
        ItemStack[] contents = pi.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack content = contents[i];
            if (content == null) {
                continue;
            }
            ItemMeta meta = content.getItemMeta();
            if (!meta.hasLore()) {
                continue;
            }
            boolean equals = meta.getLore().get(meta.getLore().size() - 1).equals(ItemUtils.SAFE_ITEM);
            if (equals) {
                if (bought.stream().noneMatch(item -> item.equals(content))) {
                    contents[i] = null;
                    if (!meta.hasDisplayName()) {
                        continue;
                    }
                    String nameUncolored = GameApi.getUserManager().get(this.owner).getFullDisplayName();
                    String displayName = meta.getDisplayName();
                    EvoItemsManager.DUPE.add(ChatUtil.colorize("%s &7(%s&7)", nameUncolored, displayName));
                }
            }
        }
        pi.setContents(contents);
        bought.forEach(i -> {
            if (UtilItem.isHelmet(i)) {
                pi.setHelmet(i);
            } else if (UtilItem.isChestplate(i)) {
                pi.setChestplate(i);
            } else if (UtilItem.isLeggings(i)) {
                pi.setLeggings(i);
            } else if (UtilItem.isBoots(i)) {
                pi.setBoots(i);
            } else if (!pi.contains(i) && !p.getEnderChest().contains(i)) {
                pi.addItem(i);
            }
        });
    }
}
