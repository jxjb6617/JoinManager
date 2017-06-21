package com.fornow_blog.joinmanager;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class JoinManager extends JavaPlugin {

	private FileConfiguration cfg = getConfig();

	@Override
	public void onEnable() {
		// TODO 自動生成されたメソッド・スタブ
		super.onEnable();
		getLogger().info("JoinManagerが有効になったよ！");

		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();

		Team ops = board.registerNewTeam("OP");
		ops.setPrefix("OP - ");

		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			if (player.isOp()) {
				ops.addPlayer(player);
			}
		}
	}

	@Override
	public void onDisable() {
		// TODO 自動生成されたメソッド・スタブ
		super.onDisable();
		getLogger().info("JoinManagerが無効になったよ！");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		return jmCmdCheck(sender, cmd, args);
	}

	private boolean jmCmdCheck(CommandSender sender, Command cmd, String[] args) {

		//コマンド処理軍
		if (cmd.getName().equalsIgnoreCase("jm")) {
			if (args[0].equalsIgnoreCase("help")) {
				sendHelpMessage(sender);
			} else if (cmd.getName().equalsIgnoreCase("open")) {
				jmServerOpen(sender);
			} else if (cmd.getName().equalsIgnoreCase("close")) {
				jmServerClose(sender);
			} else if (cmd.getName().equalsIgnoreCase("limit")) {
				jmServerLimitSet(sender, args[1]);
			} else if (cmd.getName().equalsIgnoreCase("add")) {
				jmServerPlayerAdd(sender, args[1]);
			} else if (cmd.getName().equalsIgnoreCase("remove")) {
				jmServerPlayerRemove(sender, args[1]);
			} else {
				sendHelpMessage(sender);
			}
		}
		return true;

	}

	private void sendHelpMessage(CommandSender sender) {
		String[] message = { ChatColor.RED + "/jm help : このヘルプを表示します", ChatColor.RED + "/jm open : サーバーを開放します",
				ChatColor.RED + "/jm close : サーバーを閉鎖します", ChatColor.RED + "/jm limit <Number> : サーバーの上限人数を設定します",
				ChatColor.RED + "/jm add <PlayerName> : サーバーに接続可能なプレイヤーを追加します",
				ChatColor.RED + "/jm remove <PlayerName> : サーバーに追加可能なプレイヤーを削除します" };
		sender.sendMessage(message);
	}

	private void jmServerOpen(CommandSender sender) {
		cfg.set("open", true);
		saveConfig();
		sender.sendMessage(ChatColor.GREEN + "サーバーを開放しました");
	}

	private void jmServerClose(CommandSender sender) {
		cfg.set("open", false);
		saveConfig();
		sender.sendMessage(ChatColor.RED + "サーバーを閉鎖します");
		for (Player kickplayer : Bukkit.getOnlinePlayers()) {
			if (!(kickplayer.isOp())) {
				kickplayer.kickPlayer(ChatColor.RED + "サーバーを閉鎖しました。\nサーバーへのご参加ありがとうございました。");
			}
		}
		sender.sendMessage(ChatColor.GREEN + "サーバーを閉鎖しました");
	}

	private void jmServerLimitSet(CommandSender sender, String string) {
		int limitplayers = 0;

		try {
			limitplayers = Integer.parseInt(string);
		} catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "上限は数値で指定してください");
		}

		if (limitplayers < 0) {
			sender.sendMessage(ChatColor.RED + "上限はマイナスの値を設定しないでください");
		} else if (limitplayers > sender.getServer().getMaxPlayers()) {
			sender.sendMessage(ChatColor.RED + "上限はこのサーバーの上限接続人数を超えないでください");
		}

		cfg.set("limit", limitplayers);
		saveConfig();
		sender.sendMessage(ChatColor.GREEN + "プレイヤー上限を" + limitplayers + "人に設定しました");
	}

	private void jmServerPlayerAdd(CommandSender sender, String string) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(string));

		cfg.set("players.", player.getName());
		saveConfig();
		sender.sendMessage(ChatColor.GREEN + player.getName() + "を追加しました");

	}

	private void jmServerPlayerRemove(CommandSender sender, String string) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(string));

		cfg.set("players", player.getName());
		saveConfig();
		sender.sendMessage(ChatColor.GREEN + player.getName() + "を削除しました");

	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();

		List<Player> players = player.getWorld().getPlayers();
		if (players.size() > getConfig().getInt("playerlimit")) {
			player.kickPlayer(ChatColor.RED + "サーバー解放まで参加できません");
		}

		if (cfg.getBoolean("open") == true || player.isOp() == true) {
			// 鯖が開いている、もしくはOPだったら
			e.setJoinMessage(ChatColor.GOLD + player.getName() + " が参加しました");
		} else if (cfg.getBoolean("open") == false) {
			// 鯖が閉じてたら
			player.kickPlayer(ChatColor.RED + "サーバー開放まで参加できません");
			e.setJoinMessage("");

		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();

		e.setQuitMessage(ChatColor.YELLOW + player.getName() + " が退出しました");
	}

}
