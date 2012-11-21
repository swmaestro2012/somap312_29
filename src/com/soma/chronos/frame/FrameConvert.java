package com.soma.chronos.frame;


public final class FrameConvert {
	public static int[] decodeYUV420SP(byte[] yuv420sp, int width, int height) {
		final int frameSize = width * height;
		int rgb[] = new int[width * height];
		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}
				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);
				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
						| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);

			}
		}
		return rgb;
	}

	public static byte[] encodeYUV420SP_original(int[] rgba, int width,
			int height) {
		byte[] yuv420sp = new byte[width * height];
		final int frameSize = width * height;

		int[] U, V;
		U = new int[frameSize];
		V = new int[frameSize];

		int r, g, b, y, u, v;
		for (int j = 0; j < height; j++) {
			int index = width * j;
			for (int i = 0; i < width; i++) {
				r = (rgba[index] & 0xff000000) >> 24;
				g = (rgba[index] & 0xff0000) >> 16;
				b = (rgba[index] & 0xff00) >> 8;

				// rgb to yuv
				y = (66 * r + 129 * g + 25 * b + 128) >> 8 + 16;
				u = (-38 * r - 74 * g + 112 * b + 128) >> 8 + 128;
				v = (112 * r - 94 * g - 18 * b + 128) >> 8 + 128;

				// clip y
				yuv420sp[index++] = (byte) ((y < 0) ? 0 : ((y > 255) ? 255 : y));
				U[index] = u;
				V[index++] = v;
			}
		}

		return yuv420sp;
	}

}
