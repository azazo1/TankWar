package jmp123;

import java.io.IOException;

/**
 * 控制台命令行播放器。这是一个解码器调用示例。
 * <p>源码及文档下载：<a href="http://jmp123.sf.net/">http://jmp123.sf.net/</a>
 */
public class Play {
	/**
	 * 控制台命令行播放器。
	 * 
	 * @param args
	 *            指定的源文件。
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Please specify a valid filename.");
			return;
		}

		PlayBack player = new PlayBack(new jmp123.output.Audio());
		//PlayBack player = new PlayBack(null);

		for (String name : args) {
			System.out.println(name);
			try {
				if (player.open(name, null)) { // null:可以不指定歌曲标题
					player.getID3Tag().printTag();
					player.getHeader().printVBRTag();
					player.getHeader().printHeaderInfo();
					player.start(true); // true:在控制台打印播放进度
					player.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}