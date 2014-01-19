package net.edgecraft.edgecraft.command;

import net.edgecraft.edgecraft.EdgeCraft;
import net.edgecraft.edgecraft.api.EdgeCraftPlugin;
import net.edgecraft.edgecraft.user.User;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermissionCommand extends AbstractCommand {
	
	public PermissionCommand(){ super.instance = new PermissionCommand(); }
	
	public final PermissionCommand getInstance() {
		return (PermissionCommand)super.instance;
	}
	
	@Override
	public boolean runImpl(Player player, User user, String[] args) throws NumberFormatException, Exception {
		
		
		switch( args.length ){
		
		case 1:
			return false;
		case 2:
			if( args[1].equalsIgnoreCase( "listranks" ) ) {
				listranks( player );
				return true;
			}
			break;
		case 3:
			if( args[1].equalsIgnoreCase( "getlevel" ) ) {
				return getrank( player, args[2] );
			} else {
				return false;
			}
		case 4:
			if( args[1].equalsIgnoreCase("setlevel") ){
				return setrank( player, args[2], args[3] );
			}
			return false;
		default:
			return false;
		
		}
		
		return true;
	}
	
	
private boolean setrank(CommandSender sender, String name, String level) throws NumberFormatException, Exception {
		
		User u = EdgeCraftPlugin.userAPI().getUser( name );
		
		u.updateLevel( Level.getInstance(Integer.valueOf(level)) );
		sender.sendMessage( ChatColor.GREEN + " Changed!" );
		
		return true;
	}

	private boolean getrank( CommandSender sender, String name ) {
		
		sender.sendMessage(ChatColor.GREEN + "Rank: " + ChatColor.GRAY + EdgeCraftPlugin.userAPI().getUser(name).getLevel());
		
		return true;
	}

	private void listranks( CommandSender sender ) {
		
		sender.sendMessage(ChatColor.GREEN + "Rankings:");
		sender.sendMessage(ChatColor.GRAY + "Admin - 15");
		sender.sendMessage(ChatColor.GRAY + "Team - 10");
		sender.sendMessage(ChatColor.GRAY + "Architekt - 5");
		sender.sendMessage(ChatColor.GRAY + "User - 1");
		sender.sendMessage(ChatColor.GRAY + "Gast - 0");
	}
	
	@Override
	public String getName() {
		return "permission";
	}


	@Override
	public void sendUsage( CommandSender sender ) {
		
		if( !(sender instanceof Player) || Level.canUse( EdgeCraft.getUsers().getUser(((Player)sender).getName()), Level.ADMIN) ) {
			sender.sendMessage( EdgeCraft.usageColor + "/permission <setlevel|getlevel|listranks> [player] [level]");
		}
		
	}

	@Override
	public boolean validArgsRange(String[] args) {
		if( args.length < 2 || args.length > 4 ) return false;
		
		return true;
	}
	
	@Override
	public Level getLevel() {
		return Level.ADMIN;
	}

	@Override
	public boolean sysAccess( CommandSender sender, String[] args) {
		listranks(sender);
		return true;
	}
}