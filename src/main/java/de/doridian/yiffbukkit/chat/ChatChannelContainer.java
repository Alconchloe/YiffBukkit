package de.doridian.yiffbukkit.chat;

import java.io.Serializable;
import java.util.HashMap;

public class ChatChannelContainer implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//maps PLAYER to CHATCHANNEL
	public HashMap<String, ChatChannel> activeChannel = new HashMap<String, ChatChannel>();
	//maps STRING (name) to CHATCHANNEL
	public HashMap<String, ChatChannel> channels = new HashMap<String, ChatChannel>();
}
