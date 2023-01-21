package jmp123.test;

import jmp123.decoder.AudioBuffer;
import jmp123.decoder.Synthesis;

/**
 * 测试多相合成滤波时间。
 */
public class Synt {
	private final static int FRAMES = 64917; // 假设测试的帧数为64917
	private Synthesis filter;
	private AudioBuffer audioBuf;
	private float[] samples;

	protected Synt() {
		audioBuf = new AudioBuffer(null, 4608);
		filter = new Synthesis(audioBuf, 2);
		samples = new float[32];
		for (int i = 0; i < 32; i++)
			samples[i] = (float) Math.sin(i + 1) * 32768;
	}

	protected void SynthesisCh(int ch) {
		for (int i = 0; i < 36; i++)
			filter.synthesisSubBand(samples, ch);
	}

	protected void output() {
		audioBuf.output();
	}

	/**
	 * 测试多相合成滤波时间。
	 */
	public static void main(String[] args) {
		Synt syn = new Synt();
		long t0 = System.nanoTime();

		for (int i = 0; i < FRAMES; i++) {
			syn.SynthesisCh(0);
			syn.SynthesisCh(1);

			syn.output();
		}

		long t1 = System.nanoTime() - t0;
		System.out.printf("\nelapsed time: %dns (%.9fs)\n", t1, t1 / 1e9);
	}

}
