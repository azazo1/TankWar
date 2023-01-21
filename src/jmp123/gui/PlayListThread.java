package jmp123.gui;

import jmp123.PlayBack;
import jmp123.decoder.IAudio;

import java.io.IOException;

public class PlayListThread extends Thread {
	private volatile boolean interrupted;
	private PlayBack playback;
	private PlayList playlist;

	public PlayListThread(PlayList playlist, IAudio audio) {
		this.playlist = playlist;
		playback = new PlayBack(audio);
		setName("playlist_thread");
	}

	public synchronized void pause() {
		playback.pause();
	}

	public synchronized void playNext() {
		playlist.setNextIndex(playlist.getCurrentIndex() + 1);
		playback.stop();
	}

	// 双击后播放列表某一条目后被调用
	public synchronized void startPlay(int idx) {
		playlist.setNextIndex(idx);
		playback.stop();
	}

	/**
	 * 终止此播放线程。
	 */
	public synchronized void interrupt() {
		interrupted = true;
		super.interrupt();
		playback.stop();
	}

	public PlayList getPlayList() {
		return playlist;
	}

	public void removeSelectedItem() {
		playlist.removeItem(playlist.getSelectedIndex());
	}

	@Override
	public void run() {
		Runtime rt = Runtime.getRuntime();
		String filename;
		int curIndex;
		float freeMemory, totalMemory; // VM

		while (!interrupted) {
			if((curIndex = playlist.getNextIndex()) == -1)
				break;

			freeMemory = (float) rt.freeMemory();
			totalMemory = (float) rt.totalMemory();
			System.out.printf("\nMemory used: %dK [allocated %dK]\n",
					(int) ((totalMemory - freeMemory) / 1024),
					(int) (totalMemory / 1024));

			playlist.setSelectedIndex(curIndex);
			PlayListItem item = playlist.getPlayListItem(curIndex);
			filename = item.getPath();
			System.out.println(item.toString());//##
			System.out.println(filename);//##

			try {
				if (playback.open(filename, item.toString())) {
					playback.getID3Tag().printTag();
					playback.getHeader().printHeaderInfo();
					playback.start(false);
				} else
					item.enable(playlist.isInterrupted());
			} catch (IOException e) {
				// 如果打开网络文件时被用户中断，会抛出异常。
				System.out.println(e.toString());
				item.enable(playlist.isInterrupted());
			} finally {
				playback.close();
			}
		}
		//System.out.println("jmp123.gui.PlayListThread.run() ret.");
	}

}


