/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package org.hsh.bfr.db.gui.actions;

import java.awt.Cursor;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import org.hsh.bfr.db.Backup;
import org.hsh.bfr.db.BackupMyDBI;
import org.hsh.bfr.db.DBKernel;

/**
 * @author Armin
 *
 */
public class BackupAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BackupAction(String name, Icon icon, String toolTip) {
    putValue(Action.NAME, name);
    putValue(Action.SHORT_DESCRIPTION, toolTip);
    putValue(Action.SMALL_ICON, icon);
  }    

  public void actionPerformed(ActionEvent e) {
	  try {
		  if (DBKernel.mainFrame != null) DBKernel.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		  if (DBKernel.myDBi != null && DBKernel.myDBi.getConn() != null) BackupMyDBI.dbBackup();
		  else Backup.dbBackup();
	  }
	  finally {
		  if (DBKernel.mainFrame != null) DBKernel.mainFrame.setCursor(Cursor.getDefaultCursor());
	  }
  }
}
