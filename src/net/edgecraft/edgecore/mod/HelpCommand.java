package net.edgecraft.edgecore.mod;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.edgecraft.edgecore.EdgeCore;
import net.edgecraft.edgecore.command.AbstractCommand;
import net.edgecraft.edgecore.command.CommandsCollectionCommand;
import net.edgecraft.edgecore.command.Level;
import net.edgecraft.edgecore.user.User;

public class HelpCommand extends AbstractCommand {

	private static final HelpCommand instance = new HelpCommand();
	
	private HelpCommand() { super(); }
	
	public static final HelpCommand getInstance() {
		return instance;
	}
	
	@Override
	public String[] getNames() {
		return new String[]{ "help" };
	}

	@Override
	public Level getLevel() {
		return Level.USER;
	}

	@Override
	public boolean validArgsRange(String[] args) {
		return ( args.length == 2 );
	}

	@Override
	public void sendUsageImpl(CommandSender sender) {
		
		sender.sendMessage(EdgeCore.usageColor + "/help <command>");
	}

	@Override
	public boolean runImpl(Player player, User user, String[] args) {
		

		AbstractCommand cmd = commands.getCommand( args[1] );

		
		if( cmd instanceof CommandsCollectionCommand ) {
			cmd = ((CommandsCollectionCommand) cmd).getCommand( args[1] );
		}
		
		if( cmd == null ) {
			player.sendMessage( EdgeCore.errorColor + "Command " + args[1] + " not found!" );
			return false;
		}
		
		player.sendMessage( "Usage-Instructions of command " + args[1] + ":" );
		cmd.sendUsage( player );
		
		return true;
	}

	@Override
	public boolean sysAccess(CommandSender sender, String[] args) {
		return true;
	}
}
