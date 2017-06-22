package com.fornow_blog.joinmanager;

import java.util.LinkedList;
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
	private boolean serverClosing = false;

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

		saveDefaultConfig();
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

		String subc = args.length == 0 ? "" : args[1];
		String cmds = cmd.getName();
		//コマンド処理軍
		if (cmds.equalsIgnoreCase("jm")) {
			if (args[0].equalsIgnoreCase("help")) {
				sendHelpMessage(sender);
			} else if (cmds.equalsIgnoreCase("open")) {
				jmServerOpen(sender);
			} else if (cmds.equalsIgnoreCase("close")) {
				jmServerClose(sender);
			} else if (cmds.equalsIgnoreCase("limit")) {
				return jmServerLimitSet(sender, subc); //ここだけbooleanをreturn
			} else if (cmds.equalsIgnoreCase("add")) {
				jmServerPlayerAdd(sender, subc);
			} else if (cmds.equalsIgnoreCase("remove")) {
				jmServerPlayerRemove(sender, subc);
			} else if (cmds.equalsIgnoreCase("allow")) {
				jmServerRensenAllow(sender);
			} else if (cmds.equalsIgnoreCase("deny")) {
				jmServerRensenDeny(sender);
			} else if (cmds.equalsIgnoreCase("save")) {
				jmServerRensenDataSave(sender);
			} else {
				sendHelpMessage(sender);
			}
		}
		return true;

	}

	private void sendHelpMessage(CommandSender sender) {
		String[] message = {
				ChatColor.RED + "/jm help : このヘルプを表示します",
				ChatColor.RED + "/jm open : サーバーを開放します",
				ChatColor.RED + "/jm close : サーバーを閉鎖します",
				ChatColor.RED + "/jm limit <Number> : サーバーの上限人数を設定します",
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
		serverClosing = true;
		for (Player kickplayer : Bukkit.getOnlinePlayers()) {
			if (!(kickplayer.isOp())) {
				kickplayer.kickPlayer(ChatColor.RED + "サーバーを閉鎖しました。\nサーバーへのご参加ありがとうございました。");
			}
		}
		serverClosing = false;
		sender.sendMessage(ChatColor.GREEN + "サーバーを閉鎖しました");
	}

	private boolean jmServerLimitSet(CommandSender sender, String limitPlayersNum) {
		int limitplayers = 0;

		try {
			limitplayers = Integer.parseInt(limitPlayersNum);
		} catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "上限は数値で指定してください");
			return false;
		}

		if (limitplayers < 0) {
			sender.sendMessage(ChatColor.RED + "上限はマイナスの値を設定しないでください");
			return false;
		} else if (limitplayers > sender.getServer().getMaxPlayers()) {
			sender.sendMessage(ChatColor.RED + "上限はこのサーバーの上限接続人数を超えないでください");
			return false;
		}

		cfg.set("limit", limitplayers);
		saveConfig();
		sender.sendMessage(ChatColor.GREEN + "プレイヤー上限を" + limitplayers + "人に設定しました");
		return true;
	}

	private void jmServerPlayerAdd(CommandSender sender, String playerName) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerName));

		cfg.set("players.", player.getName());
		saveConfig();
		sender.sendMessage(ChatColor.GREEN + player.getName() + "を追加しました");

	}

	private void jmServerPlayerRemove(CommandSender sender, String playerName) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerName));

		cfg.set("players", player.getName());
		saveConfig();
		sender.sendMessage(ChatColor.GREEN + player.getName() + "を削除しました");

	}

	private void jmServerRensenAllow(CommandSender sender) {
		cfg.set("rensen", true);
		saveConfig();
		sender.sendMessage(ChatColor.GREEN + "連戦を許可しました。");
	}

	private void jmServerRensenDeny(CommandSender sender){
		cfg.set("rensen", false);
		saveConfig();
		sender.sendMessage(ChatColor.GREEN + "連戦を禁止しました。");
	}

	private void jmServerRensenDataSave(CommandSender sender){
		List<Player> playerlist = new LinkedList<>();
		cfg.set("rensenPlayer", playerlist);

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!(player.isOp())) {
				playerlist.add(player);
			}
		}
		cfg.set("rensenPlayer", playerlist);
		sender.sendMessage(ChatColor.GREEN + "連戦情報を追加しました。");

	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();

		if (player.getWorld().getPlayers().size() > cfg.getInt("limit")) {
			player.kickPlayer(ChatColor.RED + "サーバーの人数が上限に達しました。");
		}

		if (cfg.getBoolean("open") || player.isOp()) {
			// 鯖が開いている、もしくはOPだったら
			e.setJoinMessage(ChatColor.GOLD + player.getName() + " が参加しました");
		}else{
			// 鯖が閉じてたら
			player.kickPlayer(ChatColor.RED + "サーバー開放まで参加できません");
			e.setJoinMessage("");

		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		if (serverClosing) {
			e.setQuitMessage("");
		}else{
			e.setQuitMessage(ChatColor.YELLOW + player.getName() + " が退出しました");
		}
	}

}
