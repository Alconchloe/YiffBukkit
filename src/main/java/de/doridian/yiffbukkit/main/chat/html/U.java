package de.doridian.yiffbukkit.main.chat.html;

import net.minecraft.server.v1_7_R1.ChatModifier;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class U extends Element {
	@Override
	protected void modifyStyle(ChatModifier style) {
		style.setUnderline(true);
	}
}
