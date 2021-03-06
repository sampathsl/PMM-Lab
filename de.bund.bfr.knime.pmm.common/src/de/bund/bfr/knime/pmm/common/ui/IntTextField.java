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
package de.bund.bfr.knime.pmm.common.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class IntTextField extends JTextField implements DocumentListener {

	private static final long serialVersionUID = 1L;

	private int minValue;
	private int maxValue;
	private boolean optional;

	private boolean isValueValid;
	private Integer value;

	private List<TextListener> listeners;

	public IntTextField() {
		this(Integer.MIN_VALUE, Integer.MAX_VALUE, false);
	}

	public IntTextField(boolean optional) {
		this(Integer.MIN_VALUE, Integer.MAX_VALUE, optional);
	}

	public IntTextField(int minValue, int maxValue) {
		this(minValue, maxValue, false);
	}

	public IntTextField(int minValue, int maxValue, boolean optional) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.optional = optional;
		getDocument().addDocumentListener(this);
		listeners = new ArrayList<>();
		textChanged();
	}

	public void addTextListener(TextListener listener) {
		listeners.add(listener);
	}

	public void removeTextListener(TextListener listener) {
		listeners.remove(listener);
	}

	public boolean isValueValid() {
		return isValueValid;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		if (value != null) {
			setText(value.toString());
		} else {
			setText("");
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		textChanged();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		textChanged();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		textChanged();
	}

	@Override
	public Color getForeground() {
		if (!isValueValid && isEnabled()) {
			return Color.RED;
		} else {
			return super.getForeground();
		}
	}

	@Override
	public Color getBackground() {
		if (!isValueValid && isEnabled() && getDocument() != null
				&& getText().trim().isEmpty()) {
			return Color.RED;
		} else {
			return super.getBackground();
		}
	}

	private void textChanged() {
		if (getText().trim().isEmpty()) {
			value = null;

			if (optional) {
				isValueValid = true;
			} else {
				isValueValid = false;
			}
		} else {
			try {
				value = Integer.parseInt(getText());

				if (value >= minValue && value <= maxValue) {
					isValueValid = true;
				} else {
					isValueValid = false;
				}
			} catch (NumberFormatException e) {
				value = null;
				isValueValid = false;
			}
		}

		for (TextListener listener : listeners) {
			listener.textChanged(this);
		}
	}

}
