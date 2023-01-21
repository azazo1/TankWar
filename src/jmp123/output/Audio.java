/*
 * Audio.java -- 音频输出
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
 * so by contacting the author: <http://jmp123.sf.net/>
 */
package jmp123.output;

import jmp123.decoder.Header;
import jmp123.decoder.IAudio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * 将解码得到的PCM数据写入音频设备（播放）。
 * 
 */
public class Audio implements IAudio {
	private SourceDataLine dateline;

	@Override
	public boolean open(Header h, String artist) {
		AudioFormat af = new AudioFormat(h.getSamplingRate(), 16,
				h.getChannels(), true, false);
		try {
			dateline = (SourceDataLine) AudioSystem.getSourceDataLine(af);
			dateline.open(af, 8 * h.getPcmSize());
			// dateline.open(af);
		} catch (LineUnavailableException e) {
			System.err.println("初始化音频输出失败。");
			return false;
		}
		
		dateline.start();
		return true;
	}

	@Override
	public int write(byte[] b, int size) {
		return dateline.write(b, 0, size);
	}

	public void start(boolean b) {
		if (dateline == null)
			return;
		if (b)
			dateline.start();
		else
			dateline.stop();
	}

	@Override
	public void drain() {
		if (dateline != null)
			dateline.drain();
	}

	@Override
	public void close() {
		if (dateline != null) {
			dateline.stop();
			dateline.close();
		}
	}

	@Override
	public void refreshMessage(String msg) {
		System.out.print(msg);
	}

}