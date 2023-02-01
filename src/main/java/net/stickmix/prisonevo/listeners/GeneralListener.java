package net.stickmix.prisonevo.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.val;
import net.stickmix.game.api.GameApi;
import net.stickmix.game.api.item.GameItemStack;
import net.stickmix.game.api.user.User;
import net.stickmix.game.api.user.permission.UserPermission;
import net.stickmix.game.api.util.ChatUtil;
import net.stickmix.prisonevo.MenuItemHandler;
import net.stickmix.prisonevo.PrisonEvo;
import net.stickmix.prisonevo.data.GamePlayer;
import net.stickmix.prisonevo.data.Perks;
import net.stickmix.prisonevo.entity.event.PhantomEntityDamagePlayerEvent;
import net.stickmix.prisonevo.entity.event.PlayerDamagePhantomEntityEvent;
import net.stickmix.prisonevo.plot.PvPManager;
import net.stickmix.prisonevo.utils.*;
import net.villenium.os.util.AlgoUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Set;

@SuppressWarnings("Duplicates")
public class GeneralListener implements Listener {

    private static final ItemStack MENU = new GameItemStack(Material.NETHER_STAR, "&a&lМеню", Lists.newArrayList("&7Нажми, чтобы открыть менюшку (:"));

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        if (!PrisonEvo.isLoaded()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatUtil.colorize("&cДоступ к серверу ограничен: сервер загружается"));
            return;
        }
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        player.setGameMode(GameMode.SURVIVAL);
        GamePlayer gamePlayer = PrisonEvo.getInstance().getAthenaManager().get(player);
        player.setLevel(gamePlayer.getLevel());
        val exp = (float) (Level.getPercentsToNextLevel(gamePlayer) / 100D);
        player.setExp(Math.min(0.99F, exp));
        gamePlayer.fulfillInventory();
        gamePlayer.getEnderChest().fillEnderChest();
        PlayerInventory inventory = gamePlayer.getHandle().getInventory();
        if (!inventory.contains(Material.NETHER_STAR)) {
            inventory.setItem(8, MENU);
        }
        gamePlayer.getEvoItems().checkAndGive();
        MainScoreboard.setup(gamePlayer);
        player.teleport(PrisonEvo.SPAWN);
        Modifiers.addGroup(gamePlayer.asUser().getPermission());

//        Bonuses bonuses = gamePlayer.getBonuses();
//        bonuses.getAllActive().forEach(bonus -> player.sendMessage(bonuses.getBonusInfo(bonus)));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        onQuit(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        onQuit(event.getPlayer());
    }

    private void onQuit(Player player) {
        GamePlayer gamePlayer = PrisonEvo.getInstance().getAthenaManager().get(player.getName());
        UserPermission permissions = GameApi.getUserManager().get(player.getName()).getPermission();
        Modifiers.removeGroup(permissions);
        if (gamePlayer != null) {
            gamePlayer.getLocalData().clear();
            if (AntiPvPQuit.CACHE.remove(player) != null) {
                PlayerUtil.dropItems(player);
                PrisonEvo.getInstance().getLogger().info(String.format("%s вышел в пвп!", gamePlayer.getOwner()));
            }
        }
        gamePlayer.saveItems();
        PrisonEvo.getInstance().getAthenaManager().getEnderChestObjectPool().save(player.getName(), true);
        PrisonEvo.getInstance().getAthenaManager().getGamePlayerObjectPool().save(player.getName(), true);
        PrisonEvo.getInstance().getAthenaManager().getEvoItemsObjectPool().save(player.getName(), true);
        PrisonEvo.getInstance().getAthenaManager().getDeathMessageObjectPool().save(player.getName(), true);
        PrisonEvo.getInstance().getAthenaManager().getDailyChestObjectPool().save(player.getName(), true);
        PrisonEvo.getInstance().getAthenaManager().getPerksObjectPool().save(player.getName(), true);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        GamePlayer gamePlayer = PrisonEvo.getInstance().getAthenaManager().get(event.getPlayer());
        User dmsPlayer = gamePlayer.asUser();
        val eventMessage = event.getMessage();
        String message = ChatUtil.colorize("&7[%s&7] %s&f: ",
                Level.getColorizedLevel(gamePlayer.getLevel()),
                dmsPlayer.getFullDisplayName()
        ) + (dmsPlayer.getPermission().isSponsor() ? ChatUtil.colorize(eventMessage) : eventMessage);
        Bukkit.broadcastMessage(message);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK && e.getEntity().getWorld() == PrisonEvo.SPAWN.getWorld()) {
            e.setCancelled(true);
        }
        if (e.getEntity().getType() != EntityType.PLAYER) {
            e.setCancelled(true);
            return;
        }
        Player p = (Player) e.getEntity();
        double protection = DamageHelper.getPlayerProtection(p);
        e.setDamage(e.getDamage() - protection);
        double finalDamage = Math.max(0D, e.getFinalDamage());
        if (p.getHealth() - finalDamage <= 0D) {
            e.setCancelled(true);
            p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            p.setFireTicks(0);
            p.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(p::removePotionEffect);
            Location loc = p.getLocation();
            PlayerUtil.dropItems(p);
            p.teleport(PrisonEvo.SPAWN);
            GamePlayer victim = PrisonEvo.getInstance().getAthenaManager().get(p.getName());
            victim.getDeathMessage().spawnHolograms(loc);
            Player damager = OnKillHelper.getDamager(p);
            if (damager == null) {
                victim.sendMessage("&cВы погибли.");
                return;
            }
            GamePlayer killer = PrisonEvo.getInstance().getAthenaManager().get(damager.getName());
            if (killer == null) {
                return;
            }
            Player handle = killer.getHandle();
            if (handle != null) {
                ItemStack hand = handle.getInventory().getItemInMainHand();
                if (hand != null && hand.getType() == Material.GOLD_SWORD) {
                    loc.getWorld().strikeLightningEffect(loc);
                    victim.sendMessage("&eСвет очистил вас..");
                }
            }
            long sum = (long) (0.01D * Math.min(victim.getBalance(), Level.getNextLevelCost(victim)));
            sum *= 0.9D;
            if (sum == 0) {
                victim.sendMessage("&cВы погибли от рук %s&c, но ничего не потеряли.", killer.asUser().getFullDisplayName());
                killer.sendMessage("&aВы убили %s&a, но ничего не получили.", victim.asUser().getFullDisplayName());
            } else {
                victim.changeBalance(-sum);
                killer.changeBalance(sum);
                String formatted = NumberUtil.formatMoney(sum);
                victim.sendMessage("&cВы погибли от рук %s &cи потеряли &e%s&c.", killer.asUser().getFullDisplayName(), formatted);
                killer.sendMessage("&aВы убили %s &aи получили &e%s&a.", victim.asUser().getFullDisplayName(), formatted);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();

        event.getDrops().removeIf(is -> {
            if (is == null) {
                return false;
            }
            if (is.getType() == Material.NETHER_STAR) {
                return true;
            }
            ItemMeta im = is.getItemMeta();
            List<String> lore = im.getLore();
            return lore == null || !lore.get(lore.size() - 1).equals(ItemUtils.SAFE_ITEM);
        });

        GamePlayer victim = PrisonEvo.getInstance().getAthenaManager().get(p.getName());
        victim.getDeathMessage().spawnHolograms(victim.getHandle().getLocation());
        Player damager = OnKillHelper.getDamager(p);
        if (damager == null) {
            victim.sendMessage("&cВы погибли.");
            return;
        }
        GamePlayer killer = PrisonEvo.getInstance().getAthenaManager().get(damager.getName());
        long sum = (long) (0.01D * Math.min(victim.getBalance(), Level.getNextLevelCost(victim)));
        sum *= 0.9D;
        if (sum == 0) {
            victim.sendMessage("&cВы погибли от рук %s&c, но ничего не потеряли.", killer.asUser().getFullDisplayName());
            killer.sendMessage("&aВы убили %s&a, но ничего не получили.", victim.asUser().getFullDisplayName());
        } else {
            victim.changeBalance(-sum);
            killer.changeBalance(sum);
            String formatted = NumberUtil.formatMoney(sum);
            victim.sendMessage("&cВы погибли от рук %s &cи потеряли &e%s&c.", killer.asUser().getFullDisplayName(), formatted);
            killer.sendMessage("&aВы убили %s &aи получили &e%s&a.", victim.asUser().getFullDisplayName(), formatted);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(PrisonEvo.getInstance(), p.spigot()::respawn, 1L);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(PrisonEvo.SPAWN);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDamageCreature(PlayerDamagePhantomEntityEvent e) {
        Player p = e.getDamager();
        GamePlayer gamePlayer = PrisonEvo.getInstance().getAthenaManager().get(p);
        Perks perks = gamePlayer.getPerks();
        int zero = Perks.Perk.BOSS_DAMAGE_I.ordinal();
        for (int ordinal = Perks.Perk.BOSS_DAMAGE_VI.ordinal(); ordinal >= zero; --ordinal) {
            Perks.Perk perk = Perks.Perk.values()[ordinal];
            if (perks.hasPerk(perk)) {
                e.setDamage(e.getDamage() + ordinal - zero + 1);
                break;
            }
        }

        int critChance = 0;
        float modifier = 1.0F;
        if (perks.hasPerk(Perks.Perk.BOSS_CRIT_V)) {
            critChance = 6;
            modifier = 2F;
        } else if (perks.hasPerk(Perks.Perk.BOSS_CRIT_IV)) {
            critChance = 5;
            modifier = 1.8F;
        } else if (perks.hasPerk(Perks.Perk.BOSS_CRIT_III)) {
            critChance = 4;
            modifier = 1.6F;
        } else if (perks.hasPerk(Perks.Perk.BOSS_CRIT_II)) {
            critChance = 3;
            modifier = 1.4F;
        } else if (perks.hasPerk(Perks.Perk.BOSS_CRIT_I)) {
            critChance = 2;
            modifier = 1.2F;
        }
        if (critChance != 0) {
            if (AlgoUtil.r(100) == critChance) {
                e.setDamage(e.getDamage() * modifier);
                e.getVictim().getAnimations().playAnimationCriticalHit();
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onFakeCreatureDamagePlayer(PhantomEntityDamagePlayerEvent e) {
        e.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, 0D);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        ItemStack is = e.getItemDrop().getItemStack();
        ItemMeta im = is.getItemMeta();
        if (im != null && im.hasLore()) {
            List<String> lore = im.getLore();
            if (lore.get(lore.size() - 1).equals(ItemUtils.SAFE_ITEM)) {
                e.setCancelled(true);
            }
        }
        if (is.getType() == Material.NETHER_STAR) {
            e.setCancelled(true);
            return;
        }
        if (is.getType() == Material.SHIELD) {
            e.getItemDrop().remove();
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) {
            event.getDrops().clear();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getType() == InventoryType.ENDER_CHEST) {
            Bukkit.broadcastMessage("клосед");
            PrisonEvo.getInstance().getAthenaManager().get(event.getPlayer().getName()).getEnderChest().saveAfterClose();
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onLeafDecay(LeavesDecayEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL && e.hasBlock() && e.getClickedBlock().getType() == Material.SOIL) {
            e.setCancelled(true);
        }
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = e.getItem();
            if (item == null) {
                return;
            }
            if (item.getType() == Material.ITEM_FRAME) {
                e.setCancelled(true);
                e.getPlayer().getInventory().setItemInMainHand(null);
            }
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Entity victim = e.getEntity();
        Entity damager = e.getDamager();
        if (victim instanceof Player && damager instanceof Player) {
            if (PvPManager.isProtected(victim)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
        e.setFoodLevel(20);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM)
            e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getSlotType() == InventoryType.SlotType.ARMOR || e.getView().getTopInventory().getName().startsWith("Рюкзак")) {
            e.setCancelled(true);
        }
        ItemStack item = e.getCurrentItem();
        if (item != null && item.getType() == Material.SHIELD) {
            Inventory inventory = e.getClickedInventory();
            if (inventory != null) {
                inventory.remove(Material.SHIELD);
            }
        }
    }

    private final Set<Material> disallowed = Sets.newHashSet(
            Material.WORKBENCH, Material.FURNACE, Material.BURNING_FURNACE, Material.CHEST, Material.NOTE_BLOCK,
            Material.DROPPER, Material.DISPENSER, Material.BREWING_STAND, Material.ANVIL, Material.HOPPER, Material.BED
    );

    @EventHandler
    public void onBed(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        Material type = e.getClickedBlock().getType();
        if (type == Material.HOPPER) {
            Player player = e.getPlayer();
            player.getInventory().setItemInMainHand(null);
            GamePlayer gamePlayer = PrisonEvo.getInstance().getAthenaManager().get(player);
            gamePlayer.sendMessage("&eВы успешно удалили предмет!");
            gamePlayer.sendMessage("&eЕсли вы удалили нужный предмет, то просто перезайдите.");
        }
        if (disallowed.contains(type)) {
            e.setCancelled(true);
            return;
        }
        if (type.name().contains("SHULKER")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractStar(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (item.getType() == Material.NETHER_STAR) {
            new MenuItemHandler(PrisonEvo.getInstance().getAthenaManager().get(event.getPlayer()));
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        event.setCancelled(true);
    }

}
