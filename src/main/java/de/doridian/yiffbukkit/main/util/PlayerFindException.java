package de.doridian.yiffbukkit.main.util;

import de.doridian.yiffbukkitsplit.YiffBukkitCommandException;

public class PlayerFindException extends YiffBukkitCommandException {
	private static final long serialVersionUID = 1L;

	public PlayerFindException(String message, Throwable cause) {
		super(message, cause);
	}

	public PlayerFindException(String message) {
		super(message);
	}

	public PlayerFindException(Throwable cause) {
		super(cause);
	}

}
