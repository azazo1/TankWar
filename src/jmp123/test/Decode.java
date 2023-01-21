package jmp123.test;

import jmp123.PlayBack;
import jmp123.decoder.Header;

import java.io.IOException;

/**
 * 测试解码速度，也是解码器调用的一个示例。
 */
public class Decode {
	/**
	 * 控制台命令行测试解码时间，只解码不产生音频输出。
	 * @param args 指定的源文件，本地磁盘文件。
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Please specify a valid filename.");
			return;
		}

		PlayBack player = new PlayBack(null);
		//PlayBack player = new PlayBack(new Audio());

		try {
			if (player.open(args[0], null)) {
				long t0 = System.nanoTime();

				System.out.println("decoding...");
				player.start(false);

				long t1 = System.nanoTime() - t0;
				Header header = player.getHeader();
				long fr = header.getFrames();
				System.out.printf("\n       input: %d bytes, %d frames", header.getTackLength(), fr);
				System.out.printf("\n       ouput: %d bytes (%.2fM)", fr * 4608, fr * 4608f / 0x100000);
				System.out.printf("\nelapsed time: %dns (%.9fs, %.2f fps)\n\n", t1,
						t1 / 1e9, header.getFrames() / (t1 / 1e9));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
