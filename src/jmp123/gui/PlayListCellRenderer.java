package jmp123.gui;

import javax.swing.*;
import java.awt.*;

/**
 * 个性化显示播放列表项
 */
@SuppressWarnings("serial")
class PlayListCellRenderer extends JLabel implements ListCellRenderer {
	private Font selFont;
	private Font plainFont;
	private Color selectionColor, foregroundColor, curColor;

	public PlayListCellRenderer() {
		super();
		String fontName = getFont().getName();
		int fontSize = getFont().getSize();
		selFont = new Font(fontName, Font.BOLD, 5 * fontSize / 4);
		plainFont = new Font(fontName, Font.PLAIN, fontSize);
		selectionColor = Color.white;
		foregroundColor = Color.black;
		curColor = Color.blue;
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		PlayListItem item = (PlayListItem) value;
		int curIndex = ((PlayList) list).getCurrentIndex();

		// 设置单元格： 序号+标题+艺术家
		StringBuilder sbuf = new StringBuilder();
		sbuf.append(index + 1);
		sbuf.append(". ");
		sbuf.append(item.toString());

		setText(sbuf.toString());

		if (index == curIndex) {
			setForeground(curColor);
			setFont(selFont);
		} else if (isSelected) {
			setForeground(selectionColor);
			setFont(selFont);
		} else if (item.available() == false) {
			setForeground(Color.lightGray);
			setFont(plainFont);
		} else {
			setForeground(foregroundColor);
			setFont(plainFont);
		}

		return this;
	}
}
