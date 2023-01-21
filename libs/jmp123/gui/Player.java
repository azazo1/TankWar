/*
* Player.java -- jmp123 (JAVA MPEG-1/2/2.5 Layer I/II/III Player)
* Copyright (C) 2010
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
* If you would like to negotiate alternate licensing terms, you may do
* so by contacting the author: <http://jmp123.sourceforge.net/>
*/
package jmp123.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class Player extends JFrame {
	private File currentDirectory;
	//private JMenuBar theMenuBar;
	//private JMenu file, play;
	private JMenuItem miOpenTopStop;
	private JMenuItem miFileremove;
	private JMenuItem miPlayPause, miPlayNext, miPlayStop;
	private JCheckBoxMenuItem miViewSpectrum;
	protected JCheckBoxMenuItem miViewAlbumFrame;
	private PlayListThread playlistThread;
	private PlayList playlist;
	private JScrollPane scrollPanel;
	private JPanel contentPanel;
	private AudioGUI theAudioGUI;
	private ResourceBundle uiResBundle;
	private int width = 396, audioguiH = 140, audioguiDH = 2, listH = 160;
	private String preLAF;

	public Player() {
		super();

		try {
            uiResBundle = ResourceBundle.getBundle("jmp123.gui.resources.jgui");
        } catch (MissingResourceException e) {
            System.err.println(e.toString());
            System.exit(0);
        }

		this.initMenu();
		this.initGUI();

		addWindowStateListener(new java.awt.event.WindowStateListener(){
			@Override
			public void windowStateChanged(java.awt.event.WindowEvent e) {
				theAudioGUI.setVisible(getState() == JFrame.NORMAL && miViewSpectrum.getState());
			}
		});
	}

	protected void setLookAndFeel(String lafClassName) {
		// System.out.println(lafClassName);
		preLAF = lafClassName;
		try {
			UIManager.setLookAndFeel(lafClassName);
		} catch (Exception e) {
			preLAF = UIManager.getCrossPlatformLookAndFeelClassName();
		}
		SwingUtilities.updateComponentTreeUI(this);
		this.pack();
	}

	protected void setLookAndFeel() {
		setLookAndFeel(preLAF);
	}
	
	protected void loadDefaultM3U() {
		// 载入default.m3u
		playlist.openM3U("default.m3u");
		playlist.setSelectedIndex(0);
		startPlaylistThread();
	}

	private void startPlaylistThread() {
		if (playlist.getCount() == 0) {
			//miPlayPause.setEnabled(false);
			return;
		}

		if (playlistThread == null || playlistThread.isAlive() == false) {
			playlistThread = new PlayListThread(playlist, theAudioGUI);
			playlistThread.start();
		}

		miPlayPause.setEnabled(true);
		miPlayNext.setEnabled(true);
		miPlayStop.setEnabled(true);
	}

	private JMenuItem addMenuItem(JMenu jmOwner, String text, int keycode,
			int modifiers, final String method) {
		JMenuItem jmi = new JMenuItem(text, keycode);
		jmi.setAccelerator(KeyStroke.getKeyStroke(keycode, modifiers, true));
		jmOwner.add(jmi);

		jmi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					Player.this.getClass().getDeclaredMethod(method).invoke(Player.this);
				} catch (Exception e) {
					//e.printStackTrace();
					System.out.println(e.toString());
				}
			}
		});
		return jmi;
	}

	private JCheckBoxMenuItem addCheckMenuItem(JMenu jmOwner, String text,
			boolean selected, int keycode, final String method) {
		JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(text, selected);
		jmi.setMnemonic(keycode);
		jmi.setAccelerator(KeyStroke.getKeyStroke(keycode,
				ActionEvent.ALT_MASK, true));
		jmOwner.add(jmi);
		jmi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					Player.this.getClass().getDeclaredMethod(method).invoke(Player.this);
				} catch (Exception e) {
					//e.printStackTrace();
					System.out.println(e.toString());
				}
			}
		});
		return jmi;
	}

	private JMenu createLookAndFeelMenu(String text) {
		preLAF = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";

		JMenu lafMenu = new JMenu(text);
		javax.swing.ButtonGroup buttongroup = new javax.swing.ButtonGroup();
		UIManager.LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
		for (final UIManager.LookAndFeelInfo laf : lafInfo) {
			JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(
					laf.getName(), preLAF == laf.getClassName());

			lafItem.addActionListener(new ActionListener() {
				private String lafClassName = laf.getClassName();

				public void actionPerformed(ActionEvent e) {
					if (preLAF != lafClassName) {
						setLookAndFeel(lafClassName);
					}
				}

			});
			lafMenu.add(lafItem);
			buttongroup.add(lafItem);
		}

		return lafMenu;
	}

	private void initMenu() {
		// JVM默认语言环境
		//Locale.setDefault(new Locale("en","US")); // debug
		String strLocale = Locale.getDefault().toString();
		//System.out.println("[locale for JVM] " + strLocale);
		// JVM默认文件编码
		//System.out.println("[file.encoding] " + System.getProperty("file.encoding"));

        JMenuBar theMenuBar = new JMenuBar(); // 菜单栏
		// "文件"菜单项
		JMenu file;
		file = new JMenu(uiResBundle.getString("file"));
		file.setMnemonic('F');
		addMenuItem(file, uiResBundle.getString("file.open"), 'O',
				ActionEvent.ALT_MASK, "actionFileOpen");
		addMenuItem(file, uiResBundle.getString("file.openurl"), 'U',
				ActionEvent.ALT_MASK, "actionFileOpenUrl");
		file.addSeparator(); // ----分割线
		miFileremove = addMenuItem(file, uiResBundle.getString("file.remove"),
				KeyEvent.VK_DELETE, 0, "actionFileRemove");
		addMenuItem(file, uiResBundle.getString("file.savelist"), KeyEvent.VK_F2,
				0, "actionFileSave");
		file.addSeparator(); // ----分割线
		addMenuItem(file, uiResBundle.getString("file.exit"), 'X',
				ActionEvent.ALT_MASK, "actionFileExit");
		theMenuBar.add(file);

		// "播放"菜单项
		JMenu play;
		play = new JMenu(uiResBundle.getString("play"));
		play.setMnemonic('P');
		miPlayPause = addMenuItem(play, uiResBundle.getString("play.pause"), ' ',
				0, "actionPlayPause");
		miPlayNext = addMenuItem(play, uiResBundle.getString("play.next"), 'N',
				ActionEvent.ALT_MASK, "actionPlayNext");
		miPlayStop = addMenuItem(play, uiResBundle.getString("play.stop"), 'S',
				ActionEvent.ALT_MASK, "actionPlayStop");
		theMenuBar.add(play);

		// "视图"菜单
		JMenu view = new JMenu(uiResBundle.getString("view"));
		view.setMnemonic('V');
		miViewSpectrum = addCheckMenuItem(view, uiResBundle.getString("view.spectrum"),
				true, 'S', "actionViewSpectrum");
		if (strLocale.equals("zh_CN")) {
			// 简体中文语言环境特有的菜单项
			miViewAlbumFrame = addCheckMenuItem(view,
					uiResBundle.getString("view.floating"), false, 'W',
					"actionViewAlbumFrame");
			miViewAlbumFrame.setEnabled(false);
		}
		theMenuBar.add(view);
		view.add(createLookAndFeelMenu(uiResBundle.getString("view.laf")));

		// "帮助"菜单
		JMenu help = new JMenu(uiResBundle.getString("help"));
		help.setMnemonic('H');
		addMenuItem(help, uiResBundle.getString("help.about"), 'A',
				ActionEvent.ALT_MASK, "actionHelpAbout");
		theMenuBar.add(help);

		setJMenuBar(theMenuBar);

		miFileremove.setEnabled(true);
		if (miOpenTopStop != null)
			miOpenTopStop.setEnabled(false);
		miPlayPause.setEnabled(false);
		miPlayNext.setEnabled(false);
		miPlayStop.setEnabled(false);
	}

	private void initGUI() {
		// 容器
		contentPanel = new JPanel();
		contentPanel.setPreferredSize(new Dimension(width, audioguiH + listH + audioguiDH));

		// 播放列表
		playlist = new PlayList();

		// 频谱显示
		theAudioGUI = new AudioGUI(41000, this);
		theAudioGUI.setBounds(0, 0, width, audioguiH);

		playlist.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2) {
					playlistThread.startPlay(playlist.locationToIndex(e.getPoint()));
					startPlaylistThread();
				}
			}
		});

		// 播放列表(myJList)添加滚动条
		scrollPanel = new JScrollPane(playlist);
		scrollPanel.setBounds(0, audioguiH + audioguiDH, width, listH);
		scrollPanel.setViewportView(playlist);

		// 控件添加到容器
		contentPanel.setLayout(null);
		contentPanel.add(scrollPanel, null);
		contentPanel.add(theAudioGUI, null);
		setContentPane(contentPanel);
		//com.sun.awt.AWTUtilities.setWindowOpacity(this, 0.8f);
	}

	// 文件->打开
	protected void actionFileOpen() {
		JFileChooser jfc = new JFileChooser();
		jfc.setMultiSelectionEnabled(true);
		jfc.removeChoosableFileFilter(jfc.getChoosableFileFilters()[0]); 
		//jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		FileNameExtensionFilter filterMP3 = new FileNameExtensionFilter(
				uiResBundle.getString("file.open.filter.mp3"), "mp3");
		jfc.addChoosableFileFilter(filterMP3);
		
		jfc.addChoosableFileFilter(new FileNameExtensionFilter(
				uiResBundle.getString("file.open.filter.mpeg"), "dat", "vob"));
		
		FileNameExtensionFilter filterM3u = new FileNameExtensionFilter(
				uiResBundle.getString("file.open.filter.m3u"), "m3u");
		jfc.addChoosableFileFilter(filterM3u);
		
		jfc.setFileFilter(filterMP3);
		
		jfc.setCurrentDirectory(currentDirectory);
		
		int f = jfc.showOpenDialog(this);
		if (f == JFileChooser.APPROVE_OPTION) {
			File[] files = jfc.getSelectedFiles();
			int i;
			String strPath = jfc.getSelectedFile().getPath();
			if (jfc.getFileFilter().equals(filterM3u)) {
				playlist.openM3U(strPath);
			} else {
				for (i = 0; i < files.length; i++)
					playlist.append(files[i].getName(), files[i].getPath());
			}
		}
		currentDirectory = jfc.getCurrentDirectory();

		startPlaylistThread();
	}

	// 文件->打开URL
	protected void actionFileOpenUrl() {
		String strInput = JOptionPane.showInputDialog(this,
				uiResBundle.getString("file.openurl.message"),
				"http://jmp123.sf.net/m3u/top.m3u");
		if (strInput != null) {
			String str = strInput.toLowerCase();
			if (str.startsWith("http://") && str.endsWith(".mp3")) {
				playlist.append(strInput, strInput);
			} else if (str.startsWith("http://") && str.endsWith(".m3u"))
				playlist.openM3U(strInput);
			else {
				JOptionPane.showMessageDialog(this,
						uiResBundle.getString("file.openurl.errmsg"),
						"jmp123 - open URL", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			startPlaylistThread();
		}
	}

	// 文件->移除
	protected void actionFileRemove() {
		playlistThread.removeSelectedItem();
	}

	// 文件->保存列表
	protected void actionFileSave() {
		currentDirectory = playlist.saveM3U(currentDirectory,
				uiResBundle.getString("file.open.filter.m3u"),
				uiResBundle.getString("file.savelist.message"));
	}

	// 文件->退出
	protected void actionFileExit() {
		System.exit(0);
	}

	// 播放->暂停/继续
	protected void actionPlayPause() {
		playlistThread.pause();
	}

	// 播放->下一首
	protected void actionPlayNext() {
		playlistThread.playNext();
	}

	// 播放->停止
	protected void actionPlayStop() {
		miPlayPause.setEnabled(false);
		miPlayNext.setEnabled(false);
		miPlayStop.setEnabled(false);
		playlistThread.interrupt();
	}

	// 视图->频谱
	protected void actionViewSpectrum() {
		boolean isVisible = miViewSpectrum.isSelected();
		if(isVisible) {
			audioguiH = 140;
			audioguiDH = 2;
		} else {
			audioguiH = audioguiDH = 0;
		}

		if(theAudioGUI != null) {
			theAudioGUI.setVisible(isVisible);
		}
		
		contentPanel.setPreferredSize(new Dimension(width, audioguiH + listH + audioguiDH));
		theAudioGUI.setBounds(0, 0, width, audioguiH);
		scrollPanel.setBounds(0, audioguiH + audioguiDH, width, listH);
		pack();
	}
	
	// 视图->悬浮唱片集窗口
	protected void actionViewAlbumFrame() {
		if(theAudioGUI != null) {
			theAudioGUI.setAlbumVisible(miViewAlbumFrame.getState());
		}
	}

	// 帮助->关于
	protected void actionHelpAbout() {
		JOptionPane.showMessageDialog(this, uiResBundle.getString("help.about.dlgmsg"),
				"jmp123 - About", JOptionPane.INFORMATION_MESSAGE);
	}

	public static void main(String[] args) {
		//System.out.println("[user.dir] " + System.getProperty("user.dir"));
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				Player theplayer = new Player();
				theplayer.setLookAndFeel();
				theplayer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				theplayer.setResizable(false);
				theplayer.setTitle("JMP123");
				theplayer.setLocationRelativeTo(null); // 居中显示
				theplayer.setVisible(true);
				theplayer.loadDefaultM3U();
			}

		});
	}

}
