package de.doridian.yiffbukkit.main.chat.html;

import net.minecraft.server.v1_7_R2.ChatModifier;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class B extends Element {
	@Override
	protected void modifyStyle(ChatModifier style) {
		style.setBold(true);
	}
}
