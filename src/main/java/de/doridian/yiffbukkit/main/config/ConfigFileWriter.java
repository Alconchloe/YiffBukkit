package de.doridian.yiffbukkit.main.config;

import de.doridian.yiffbukkitsplit.YiffBukkit;

import java.io.FileWriter;
import java.io.IOException;

public class ConfigFileWriter extends FileWriter {
	public ConfigFileWriter(String file) throws IOException {
		super(YiffBukkit.instance.getDataFolder() + "/" + file);
	}
}
