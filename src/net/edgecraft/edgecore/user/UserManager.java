package net.edgecraft.edgecore.user;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.edgecraft.edgecore.EdgeCore;
import net.edgecraft.edgecore.command.Level;
import net.edgecraft.edgecore.db.DatabaseHandler;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class UserManager {
	
	public final static String userTable = "edgecore_users";
	
	private static Map<Integer, User> users = new LinkedHashMap<>();
	private static List<String> bannedIPs = new ArrayList<>();
	
	private static int defaultLevel;
	
	private final DatabaseHandler db = EdgeCore.getDB();
	
	protected static final UserManager instance = new UserManager();
		
	protected UserManager() { /* ... */ }

	public static final UserManager getInstance() {
		return instance;
	}
	
	/**
	 * Registers a new user.
	 * (local + db)
	 * @param name
	 * @param ip
	 */
	public void registerUser(String name, String ip) {
		try {
			
			if (exists(name)) return;
			
			int id = generateID();
			
			PreparedStatement registerUser = db.prepareUpdate("INSERT INTO " + UserManager.userTable + " (id, name, ip, level, prefix, suffix, language, banned, banreason) VALUES (?, ?, ?, ? , ?, ?, DEFAULT, DEFAULT, DEFAULT);");
			registerUser.setInt(1, id);
			registerUser.setString(2, name);
			registerUser.setString(3, ip);
			registerUser.setInt(4, getDefaultLevel());
			registerUser.setString(5, "");
			registerUser.setString(6, "");
			registerUser.executeUpdate();
			
			synchronizeUser(id);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Deletes an existing user.
	 * (local + db)
	 * @param id
	 */
	public void deleteUser(int id) {
		try {
			
			PreparedStatement deleteUser = db.prepareUpdate("DELETE FROM " + UserManager.userTable + " WHERE id = '" + id + "';");
			deleteUser.executeUpdate();
			
			users.remove(id);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generates a possible user-id.
	 * @return
	 * @throws Exception
	 */
	public int generateID() throws Exception {
		if (amountOfUsers() <= 0) return 1;
		
		return greatestID() + 1;
	}
	
	/**
	 * Returns the greatest user-id.
	 * @return
	 * @throws Exception
	 */
	public int greatestID() throws Exception {
		List<Map<String, Object>> tempVar = db.getResults("SELECT COUNT(id) AS amount FROM " + UserManager.userTable);
		int tempID = Integer.parseInt(String.valueOf(tempVar.get(0).get("amount")));
		
		if (tempID <= 0) return 1;

		return tempID;
	}
	
	/**
	 * Returns the current amount of users.
	 * @return
	 */
	public int amountOfUsers() {
		return users.size();
	}
	
	/**
	 * Returns a list with all existing (online) users.
	 * @param language
	 * @return
	 */
	public String getUserList(String language) {
		
	    if (Bukkit.getOnlinePlayers().length <= 0) {
	    	return EdgeCore.getLang().getColoredMessage(language, "userlist").replace("[0]", "0").replace("[1]", Bukkit.getMaxPlayers() + "").replace("[2]", "");
	    }
	    
	    StringBuilder sb = new StringBuilder();
	    
	    for (User user : users.values()) {
	    	if (sb.length() > 0) {
	    		sb.append(ChatColor.RESET + ", ");
	    	}
	    	
	    	if ((user != null) && (Bukkit.getPlayerExact(user.getName()).isOnline())) {
	    		sb.append(user.getLevel().getColor() + user.getName());
	    	}
	    }
	    
	    return EdgeCore.getLang().getColoredMessage(language, "userlist").replace("[0]", Bukkit.getOnlinePlayers().length + "").replace("[1]", Bukkit.getMaxPlayers() + "").replace("[2]", sb.toString());
	}
	
	/**
	 * Checks whether the given id is already in use. 
	 * @param id
	 * @return true/false
	 */
	public boolean exists(int id) {
		return users.containsKey(id);
	}
	
	
	/**
	 * Checks whether the given name is already in use.
	 * @param name
	 * @return
	 */
	public boolean exists(String name) {
		return getUser(name) != null;
	}
	
	/**
	 * Returns the user registered with the given id.
	 * @param id
	 * @return
	 */
	public User getUser(int id) {
		return users.get(id);
	}
	
	/**
	 * Return the user registered with the given name.
	 * @param name
	 * @return
	 */
	public User getUser(String name) {
		
		int id = 0;
		
		for (Map.Entry<Integer, User> entry : users.entrySet()) {
			User searchFor = entry.getValue();
			
			if (searchFor.getName().equals(name)) {
				id = searchFor.getID();
				break;
			}
		}
		
		return users.get(id);
	}
	
	/**
	 * Returns the user registered with the given IP-Token.
	 * @param ip
	 * @return
	 */
	public User getUserByIP(String ip) {
		
		int id = 0;
		
		for (Map.Entry<Integer, User> entry : users.entrySet()) {
			User searchFor = entry.getValue();
			
			if (searchFor.getIP().equals(ip)) {
				id = searchFor.getID();
				break;
			}
		}
		
		return users.get(id);
	}
	
	/**
	 * Synchronizes all users.
	 */
	public void synchronizeUsers() {
		try {
			
			for (int i = 1; i <= greatestID(); i++) {
				synchronizeUser(i);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Synchronizes the user given through his id.
	 * ( db --> local )
	 * @param id
	 */
	public void synchronizeUser(int id) {
		try {
			
			List<Map<String, Object>> results = db.getResults("SELECT * FROM " + UserManager.userTable + " WHERE id = '" + id + "';");
			
			for (int i = 0; i < results.size(); i++) {
				
				User user = new User();
				
				for (Map.Entry<String, Object> entry : results.get(i).entrySet()) {
					
					if (entry.getKey().equals("id")) {
						user.setID(Integer.valueOf(entry.getValue().toString()));
						
					} else if(entry.getKey().equals("name")) {
						user.setName(entry.getValue().toString());
						
					} else if(entry.getKey().equals("ip")) {
						user.setIP(entry.getValue().toString());
						
					} else if(entry.getKey().equals("level")) {						
						user.setLevel( Level.getInstance(Integer.valueOf(entry.getValue().toString())) );
						
					} else if(entry.getKey().equals("prefix")) {
						user.setPrefix(entry.getValue().toString());
						
					} else if(entry.getKey().equals("suffix")) {
						user.setSuffix(entry.getValue().toString());
						
					} else if(entry.getKey().equals("language")) {
						user.setLanguage(entry.getValue().toString());
						
					} else if(entry.getKey().equals("banned")) {
						boolean b = ((Boolean) entry.getValue()).booleanValue();
						user.setBanStatus(b);
						
						if (user.isBanned()) bannedIPs.add(user.getIP());
						
					} else if(entry.getKey().equals("banreason")) {
						user.setBanReason(entry.getValue().toString());
					}
				}
				
				users.put(user.getID(), user);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the map containing all users.
	 * (local)
	 * @return
	 */
	public Map<Integer, User> getUsers() {
		return users;
	}

	/**
	 * Returns the list containing all banned IPs.
	 * (local)
	 * @return
	 */
	public static List<String> getBannedIPs() {
		return bannedIPs;
	}

	/**
	 * Returns the default level of an user.
	 * @return
	 */
	public int getDefaultLevel() {
		return defaultLevel;
	}
	
	/**
	 * Sets the default level of users.
	 * @param defaultLevel
	 */
	public static void setDefaultLevel( int defaultLevel ) {
	    Validate.isTrue(defaultLevel >= 0, "The value must be greater or equal than zero: %s", defaultLevel);
		UserManager.defaultLevel = defaultLevel;
	}
	
	public boolean exists( User u ) {
		return users.containsValue( u );
	}
	
	public void notify( User u, String msg ) {
		
		if( u == null || msg == null ) return;
		if( !exists(u) ) return;
		
		Player to = u.getPlayer();
		
		if( to == null ) return;
		
		to.sendMessage( ChatColor.BLUE + msg );
		
	}
	
	public void notifyAll( Level level, String msg ) {
		
		if( level == null || msg == null ) return;
		
		for( Map.Entry<Integer, User> entry : users.entrySet() ) {
			
			User cur = entry.getValue();
			Player p = cur.getPlayer();
			
			if( p == null ) return;
			
			if( Level.canUse( cur, level) ) {
				p.sendMessage( msg );
			}
			
		}
		
	}
	
}
