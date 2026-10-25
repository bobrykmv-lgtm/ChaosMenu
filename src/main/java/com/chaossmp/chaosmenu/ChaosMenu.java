```java
package com.chaossmp.chaosmenu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ChaosMenu extends JavaPlugin implements Listener {

    private final Set<UUID> awaitingHelp = new HashSet<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("ChaosMenu включён!");
    }

    @Override
    public void onDisable() {
        awaitingHelp.clear();
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Эта команда доступна только игрокам.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("menu")) {
            openMenu(player);
            return true;
        }

        return false;
    }

    private void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(
                null,
                27,
                ChatColor.AQUA.toString() + ChatColor.BOLD + "CHAOSSMP"
        );

        menu.setItem(10, createItem(
                Material.COMPASS,
                "§b§lИнформация о сервере",
                "§7Правила и механики ChaosSMP"
        ));

        menu.setItem(12, createItem(
                Material.BOOK,
                "§e§lПомощь администрации",
                "§7Нажми, чтобы отправить сообщение"
        ));

        menu.setItem(14, createItem(
                Material.NETHER_STAR,
                "§d§lМоя статистика",
                "§7Посмотреть информацию о хаосе"
        ));

        menu.setItem(16, createItem(
                Material.PAPER,
                "§a§lКоманды сервера",
                "§7Список полезных команд"
        ));

        menu.setItem(22, createItem(
                Material.BARRIER,
                "§c§lЗакрыть меню",
                "§7Закрыть это окно"
        ));

        player.openInventory(menu);
    }

    private ItemStack createItem(
            Material material,
            String name,
            String description
    ) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(description));
            item.setItemMeta(meta);
        }

        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!event.getView().getTitle().equals(
                ChatColor.AQUA.toString() + ChatColor.BOLD + "CHAOSSMP"
        )) {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() < 0 || event.getRawSlot() >= 27) {
            return;
        }

        switch (event.getRawSlot()) {
            case 10 -> showInformation(player);
            case 12 -> requestHelp(player);
            case 14 -> showStatistics(player);
            case 16 -> showCommands(player);
            case 22 -> player.closeInventory();
            default -> {
            }
        }
    }

    private void showInformation(Player player) {
        player.closeInventory();

        player.sendMessage("§8§m--------------------------------");
        player.sendMessage("§b§l            CHAOSSMP");
        player.sendMessage("§7Сервер выживания с собственными механиками.");
        player.sendMessage("");
        player.sendMessage("§c• §fБан за смерть: §e3 минуты");
        player.sendMessage("§c• §fЗа убийство: §a+1 хаос");
        player.sendMessage("§c• §fЗа смерть: §c-1 хаос");
        player.sendMessage("§c• §fМаксимум хаоса: §e25");
        player.sendMessage("§c• §fПовторное убийство той же жертвы");
        player.sendMessage("  §7в течение 5 минут не даёт хаос.");
        player.sendMessage("§c• §fДальние земли");
        player.sendMessage("§c• §fОрбитальные системы: §4Nuke §fи §cStab");
        player.sendMessage("");
        player.sendMessage("§7Команды: §f/chaos §7и §f/chaos top");
        player.sendMessage("§8§m--------------------------------");
    }

    private void showStatistics(Player player) {
        player.closeInventory();
        player.sendMessage("§b[ChaosSMP] §fТвой хаос: §e/chaos");
        player.sendMessage("§b[ChaosSMP] §fТаблица лидеров: §e/chaos top");
    }

    private void showCommands(Player player) {
        player.closeInventory();

        player.sendMessage("§8§m--------------------------------");
        player.sendMessage("§b§l      КОМАНДЫ CHAOSSMP");
        player.sendMessage("§e/menu §7— открыть меню");
        player.sendMessage("§e/chaos §7— посмотреть свой хаос");
        player.sendMessage("§e/chaos top §7— таблица лидеров");
        player.sendMessage("§8§m--------------------------------");
    }

    private void requestHelp(Player player) {
        player.closeInventory();
        awaitingHelp.add(player.getUniqueId());

        player.sendMessage("§b[ChaosSMP] §fНапиши сообщение для администрации в чат.");
        player.sendMessage("§7Для отмены введи: §cотмена");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!awaitingHelp.contains(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage().trim();

        Bukkit.getScheduler().runTask(this, () -> {
            if (!awaitingHelp.remove(player.getUniqueId())) {
                return;
            }

            if (message.equalsIgnoreCase("отмена")) {
                player.sendMessage("§cОтправка сообщения отменена.");
                return;
            }

            if (message.isBlank()) {
                player.sendMessage("§cСообщение не может быть пустым.");
                return;
            }

            if (message.length() > 200) {
                player.sendMessage("§cСообщение слишком длинное. Максимум 200 символов.");
                return;
            }

            String formatted = "§b[Обращение] §f"
                    + player.getName() + "§7: §f" + message;

            int recipients = 0;

            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission("chaosmenu.admin")) {
                    admin.sendMessage(formatted);
                    recipients++;
                }
            }

            if (recipients == 0) {
                player.sendMessage(
                        "§eСейчас нет администраторов онлайн. Обращение не доставлено."
                );
            } else {
                player.sendMessage("§aОбращение отправлено администрации.");
            }
        });
    }
}
```
