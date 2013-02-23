/*
 * Copyright 1997-2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui.menu;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import madkit.action.GlobalAction;
import madkit.kernel.Madkit;
import madkit.kernel.MadkitClassLoader;

/**
 * This class builds a {@link JMenu} containing all the 
 * MDK configuration files found on the class path.
 * Each item will launch a separate instance of MaDKit
 * using the corresponding configuration files
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.2
 * @version 0.9
 * 
 */
public class LaunchMDKConfigurations extends ClassPathSensitiveMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3650744981788324553L;

	/**
	 * Builds a new menu.
	 * @param title the title to use
	 */
	public LaunchMDKConfigurations(final String title) {
		super(title);
		setMnemonic(KeyEvent.VK_C);
		update();
	}

	@Override
	public void update() {
		removeAll();
		for (final String string : MadkitClassLoader.getMDKFiles()) {
			JMenuItem name = new JMenuItem(GlobalAction.LAUNCH_MDK_CONFIG);
			name.setActionCommand(string);
			Properties config = new Properties();
			try(final InputStream resourceAsStream = Madkit.class.getResourceAsStream(File.separatorChar+string)) {
				config.load(resourceAsStream);
		} catch (IOException e) {//FIXME
		}
			name.setText(string+" "+config);
			add(name);
		}
		setVisible(getItemCount() != 0);
	}

}
