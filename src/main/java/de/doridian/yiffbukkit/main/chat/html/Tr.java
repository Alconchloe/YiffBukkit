/**
 * This file is part of YiffBukkit.
 *
 * YiffBukkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * YiffBukkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with YiffBukkit.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.doridian.yiffbukkit.main.chat.html;

import net.minecraft.server.v1_7_R3.ChatBaseComponent;
import net.minecraft.server.v1_7_R3.ChatMessage;
import net.minecraft.server.v1_7_R3.ChatModifier;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.List;

@XmlRootElement
public class Tr extends Element {
	@XmlAttribute(required = true)
	private String key;

	@Override
	protected void modifyStyle(ChatModifier style) {
		// TODO: get rid of this
	}

	@Override
	public List<ChatBaseComponent> getNmsComponents(ChatModifier style, boolean condenseElements, Object[] params) throws JAXBException {
		final List<ChatBaseComponent> components = super.getNmsComponents(style, true, params);

		final ChatBaseComponent translateComponent = new ChatMessage(key, components.toArray());
		translateComponent.setChatModifier(style);
		return Arrays.asList(translateComponent);
	}
}
