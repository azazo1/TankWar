package jmp123.gui;

import java.awt.Graphics;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class PlayList extends JList {
	private Image bkImage;
	private DefaultListModel dataListModel;
	private int curIndex = -1; //当前正在播放的文件
	private int nextIndex; //下一播放的文件

	public PlayList() {
		super();
		bkImage = new ImageIcon(getClass().getResource("resources/image/bk2.jpg")).getImage();
		dataListModel = new DefaultListModel();
		setModel(dataListModel);
		int fontSize = getFont().getSize();
		setFixedCellHeight(3 * fontSize / 2);

		setCellRenderer(new PlayListCellRenderer());
		setOpaque(false); // 文字背景透明
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent e) {
				// 垂直滚动条自动滚动
				ensureIndexIsVisible(getSelectedIndex());
			}
		});
	}

	public void paint(Graphics g) {
		g.drawImage(bkImage, 0 - getX(), 0 - getY(), null); // 使背景位置固定
		super.paint(g);
	}

	public synchronized int getCount() {
		return dataListModel.getSize();
	}

	public synchronized void append(String title, String path) {
		dataListModel.addElement(new PlayListItem(title, path));
	}

	/**
	 * 获取指定的列表项。
	 * @param index 列表项的索引。
	 * @return 列表项。
	 */
	public synchronized PlayListItem getPlayListItem(int index) {
		return (PlayListItem) dataListModel.get(index);
	}

	/**
	 * 从列表中删除指定项。
	 * @param index 将要删除的列表项的索引。
	 */
	public synchronized void removeItem(int index) {
		if(index < 0 || index >= dataListModel.getSize())
			return;
		dataListModel.remove(index);
		if(index == curIndex)
			curIndex = -1;

		if(index >= dataListModel.getSize())
			index = 0;
		nextIndex = index;
		setSelectedIndex(index);
	}

	/**
	 * 清空列表。
	 */
	public synchronized void clear() {
		nextIndex = 0;
		curIndex = -1;
		dataListModel.clear();
	}

	/**
	 * 获取当前正在播放的文件的列表索引。
	 * @return 当前正在播放的文件的列表索引。
	 */
	public synchronized int getCurrentIndex() {
		return curIndex;
	}

	/**
	 * 获取下一个可用的列表索引。
	 * 
	 * @return 一个可用的列表索引。当列表中的文件全部不可用时返回值为-1。
	 */
	public synchronized int getNextIndex() {
		int i, count = dataListModel.getSize();
		if (nextIndex == -1)
			curIndex = (curIndex + 1 == count) ? 0 : curIndex + 1;
		else {
			curIndex = nextIndex;
			nextIndex = -1;
		}

		for (i = 0; i < count; i++) {
			PlayListItem item = (PlayListItem) dataListModel.get(curIndex);
			if (item.available()) {
				repaint();
				return curIndex;
			}
			curIndex = (curIndex + 1 == count) ? 0 : curIndex+1;
		}
		return -1;
	}

	/**
	 * 设置下一个即将被播放的文件。同时要中断当前播放的文件时调用此方法。
	 * @param i 下一个即将被播放的文件索引。
	 */
	public synchronized void setNextIndex(int i) {
		nextIndex = (i < 0 || i >= dataListModel.getSize()) ? 0 : i;
	}

	/**
	 * 播放当前文件时是否被用户调用 {@link #setNextIndex(int)} 方法中断。
	 * @return 返回<b>true</b>表示播放当前文件时是否被用户中断，否则返回<b>false</b>。
	 */
	public synchronized boolean isInterrupted() {
		return nextIndex != -1;
	}

	/**
	 * 从播放列表(*.m3u)文件添加到列表。如果文件读取成功，先清空列表再添加。
	 * @param name 播放列表文件名。
	 */
	public void openM3U(String name) {
		BufferedReader br = null;
		java.io.InputStream instream = null;
		int idx;
		StringBuilder info = new StringBuilder("[open M3U] ");
		info.append(name);
		try {
			// 打开以UTF-8格式编码的播放列表文件
			if (name.toLowerCase().startsWith("http://")) {
				URL url = new URL(name);
				HttpURLConnection huc = (HttpURLConnection) url.openConnection();
				huc.setConnectTimeout(5000);
				huc.setReadTimeout(10000);
				instream = huc.getInputStream();
			} else
				instream = new FileInputStream(name);
			br = new BufferedReader(new InputStreamReader(instream,"utf-8"));

			String path, title = br.readLine();
			// BOM: 0xfeff
			if(!"#EXTM3U".equals(title) && !"\ufeff#EXTM3U".equals(title)) {
				info.append("\nIllegal file format.");
				return;
			}
			clear();
			while ((title = br.readLine()) != null && (path = br.readLine()) != null) {
				if (!title.startsWith("#EXTINF")
						|| (idx = title.indexOf(',') + 1) == 0) {
					info.append("\nIllegal file format.");
					break;
				}
				this.append(title.substring(idx), path);
			}
			info.append("\n");
			info.append(getCount());
			info.append(" items");
		} catch (IOException e) {
			info.append("\nfalse: ");
			info.append(e.getMessage());
		} finally {
			try {
				if(instream != null)
					instream.close();
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
			System.out.println(info.toString());
		}
	}

	/**
	 * 保存播放列表(*.m3u)
	 * @param currentDirectory 当前目录。可以指定为null。
	 * @param description 播放列表文件类型简短描述。
	 * @param message 如果指定的文件已经存在，显示的错误对话诓上的描述信息。
	 * @return 当前目录。
	 */
	public synchronized File saveM3U(File currentDirectory, String description,
			String message) {
		if (getCount() == 0) {
			JOptionPane.showMessageDialog(this.getParent(),
					"The current playlist is empty.", "jmp123 - Save playlist",
					JOptionPane.INFORMATION_MESSAGE);
			return currentDirectory;
		}

		JFileChooser jfc = new JFileChooser();
		jfc.removeChoosableFileFilter(jfc.getChoosableFileFilters()[0]);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(description, "m3u");
		jfc.addChoosableFileFilter(filter);
		jfc.setCurrentDirectory(currentDirectory);
		if (jfc.showSaveDialog(this.getParent()) == JFileChooser.APPROVE_OPTION) {
			java.io.File selectedFile = jfc.getSelectedFile();
			String path = selectedFile.getAbsolutePath();
			if (!path.toLowerCase().endsWith(".m3u"))
				path += ".m3u";
			if (selectedFile.exists()
					&& JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(
							this.getParent(), message,
							"jmp123 - Save playlist", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE))
				return currentDirectory;
			try {
				StringBuilder content = new StringBuilder("#EXTM3U\r\n");
				int i, j = getCount();
				for (i = 0; i < j; i++) {
					PlayListItem item = (PlayListItem) dataListModel.get(i);
					//if(!item.available()) continue;

					// title
					content.append("#EXTINF:-1,");
					content.append(item.toString());
					content.append("\r\n");
					
					// path
					content.append(item.getPath());
					content.append("\r\n");
				}
				
				// 以UTF-8编码格式保存至.m3u文件
				FileOutputStream fos = new FileOutputStream(path);
				Writer fw = new java.io.OutputStreamWriter(fos, "UTF-8");
				fw.write(content.toString());
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			currentDirectory = jfc.getCurrentDirectory();
			System.out.println("[Save as M3U] file.encoding: UTF-8");
		}
		return currentDirectory;
	}

}
