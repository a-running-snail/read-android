package com.jingdong.app.reader.io;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.jingdong.app.reader.client.OnDownloadListener;
import com.jingdong.app.reader.client.RequestEntry;
import com.jingdong.app.reader.io.IOUtil.ProgressListener;
import com.jingdong.app.reader.util.HttpGroup.StopController;
import com.jingdong.app.reader.util.MZLog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class IOUtil {

	private static final String LOG_TAG = "IOUtilities";

	private static final int IO_BUFFER_SIZE = 4 * 1024;
	private static int bufferSize = 10240;
	private static int callback_interval_time = 1000;

	public static void deleteFile(File file) {
		try {
			if (file.isDirectory())
				for (File child : file.listFiles())
					deleteFile(child);

			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Copy the content of the input stream into the output stream, using a
	 * temporary byte array buffer whose size is defined by
	 * {@link #IO_BUFFER_SIZE}.
	 * 
	 * @param in
	 *            The input stream to copy from.
	 * @param out
	 *            The output stream to copy to.
	 * 
	 * @throws java.io.IOException
	 *             If any error occurs during the copy.
	 */
	public static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] b = new byte[IO_BUFFER_SIZE];
		int read;
		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
	}

	public static void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				Log.e(LOG_TAG, "Could not close stream", e);
			}
		}
	}

	public static String readIt(InputStream in) throws IOException {
		if (in == null) {
			return "";
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder out = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			out.append(line);
		}
		reader.close();
		return out.toString();
	}

	public static List<File> listFiles(File path, String extension) {
		ArrayList<File> files = new ArrayList<File>();
		scan(path, extension, files);
		return files;
	}

	private static void scan(File path, String extension, List<File> files) {
		File[] fileArray = path.listFiles();
		if (fileArray == null) {
			return;
		}
		for (File f : path.listFiles()) {
			if (f.isFile()) {
				if (f.getAbsolutePath().endsWith(extension)) {
					files.add(f);
				}
			} else {
				scan(f, extension, files);
			}
		}
	}

	private static final int MAX_DECODE_PICTURE_SIZE = 1920 * 1440;

	public static Bitmap extractThumbNail(final String path, final int width,
			final int height, final boolean crop) {
		BitmapFactory.Options options = new BitmapFactory.Options();

		try {
			options.inJustDecodeBounds = true;
			Bitmap tmp = BitmapFactory.decodeFile(path, options);
			if (tmp != null) {
				tmp.recycle();
				tmp = null;
			}

			final double beY = options.outHeight * 1.0 / height;
			final double beX = options.outWidth * 1.0 / width;
			options.inSampleSize = (int) (crop ? (beY > beX ? beX : beY)
					: (beY < beX ? beX : beY));
			if (options.inSampleSize <= 1) {
				options.inSampleSize = 1;
			}

			// NOTE: out of memory error
			while (options.outHeight * options.outWidth / options.inSampleSize > MAX_DECODE_PICTURE_SIZE) {
				options.inSampleSize++;
			}

			int newHeight = height;
			int newWidth = width;
			if (crop) {
				if (beY > beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			} else {
				if (beY < beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			}

			options.inJustDecodeBounds = false;

			Bitmap bm = BitmapFactory.decodeFile(path, options);
			if (bm == null) {
				return null;
			}

			final Bitmap scale = Bitmap.createScaledBitmap(bm, newWidth,
					newHeight, true);
			if (scale != null) {
				bm.recycle();
				bm = scale;
			}

			if (crop) {
				final Bitmap cropped = Bitmap.createBitmap(bm,
						(bm.getWidth() - width) >> 1,
						(bm.getHeight() - height) >> 1, width, height);
				if (cropped == null) {
					return bm;
				}

				bm.recycle();
				bm = cropped;
			}
			return bm;

		} catch (final OutOfMemoryError e) {
			options = null;
		}

		return null;
	}

	/**
	 * 读取为 string
	 */
	public static String readAsString(InputStream is, String encode)
			throws Exception {
		return readAsString(is, encode, null);
	}

	
	
	
	/**
	 * 读取为 string
	 */
	public static String readAsString(InputStream is, String encode,
			ProgressListener progressListener) throws Exception {
		try {
			byte[] data = readAsBytes(is, progressListener);
			return new String(data, encode);
		} catch (UnsupportedEncodingException e) {
			MZLog.d("HttpRequest", e.getMessage());
			return null;
		}
	}

	
	/**
	 * 读取为 byte[]
	 */
	public static byte[] readAsBytes(InputStream is,
			ProgressListener progressListener) throws Exception {

		byte[] data = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			byte[] buf = new byte[bufferSize];
			int len = 0;
			int progress = 0;
			while ((len = is.read(buf)) != -1) {
				os.write(buf, 0, len);
				progress += len;
				if (null != progressListener) {
					progressListener.notify(len, progress);
				}
			}
			data = os.toByteArray();
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (null != os) {
					os.close();
				}
			} catch (Exception e) {
			}
		}
		return data;

	}

	/*
	 * /** 读取为 file
	 */
	/*
	 * public static void readAsFile(InputStream is, FileOutputStream os,
	 * ProgressListener progressListener, StopController stopController) throws
	 * Exception {
	 * 
	 * try { byte[] buf = new byte[bufferSize]; int len = 0; int progress = 0;
	 * while ((len = is.read(buf)) != -1 && !stopController.isStop()) {
	 * os.write(buf, 0, len); progress += len; if (null != progressListener) {
	 * progressListener.notify(len, progress); } } } catch (Exception e) { throw
	 * e; } finally { try { if (null != os) { os.close(); } } catch (Exception
	 * e) { } }
	 * 
	 * }
	 * 
	 * /** 读取为 file
	 */
	/*
	 * public static void readAsFile(InputStream is, File file, ProgressListener
	 * progressListener,long start, StopController stopController) throws
	 * Exception { RandomAccessFile savedFile = null; try { savedFile = new
	 * RandomAccessFile(file, "rwd"); savedFile.seek(start); byte[] buf = new
	 * byte[bufferSize]; int len = 0; long progress = start; while ((len =
	 * is.read(buf)) != -1 && !stopController.isStop()) { savedFile.write(buf,
	 * 0, len); progress += len; if (null != progressListener) {
	 * progressListener.notify(len, progress); } } } catch (Exception e) { throw
	 * e; } finally { try { if (null != savedFile) { savedFile.close(); } }
	 * catch (Exception e) { } } }
	 * 
	 * /** 读取为 file
	 */
	public static void readAsFile(InputStream is, File file,OnDownloadListener progressListener, RequestEntry requestEntry,final long length) throws Exception {
		System.out.println("DDDDDDDD===IOUtil=====readAsFile=====1111======");		
		RandomAccessFile savedFile = null;
		try {
			savedFile = new RandomAccessFile(file, "rwd");
			savedFile.seek(requestEntry.start);
			byte[] buf = new byte[bufferSize];
			int len = 0;
			long progress = requestEntry.start;
			long lastTime = 0;
			while ((len = is.read(buf)) != -1 && !requestEntry.isStop()) {
				System.out.println("DDDDDDDD===IOUtil=====readAsFile=====2222=====len=" + len);					
				savedFile.write(buf, 0, len);
				progress += len;
				System.out.println("DDDDDDDD===IOUtil=====readAsFile=====3333=====progress=" + progress);
				if (null != progressListener ) {
					//更新进度
					progressListener.onprogress(progress, requestEntry.start + length);
				}
			}
			progressListener.onprogress(progress, requestEntry.start + length);
		} catch (Exception e) {
			// e.printStackTrace();
			throw (e);
		} finally {
			try {
				if (null != savedFile) {
					savedFile.close();
					is.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public interface ProgressListener {
		/**
		 * @author lijingzuo
		 * 
		 *         Time: 2011-3-22 下午02:26:16
		 * 
		 *         Name:
		 * 
		 *         Description:
		 * 
		 * @param incremental
		 *            增量
		 * @param cumulant
		 *            累计量
		 * 
		 */
		void notify(int incremental, long cumulant);
	}

	/**
	 * 读取为 file
	 */
	public static void readAsFile(InputStream is, File file,
			ProgressListener progressListener,long start, StopController stopController)
			throws Exception {
		RandomAccessFile savedFile = null;
		try {
			savedFile = new RandomAccessFile(file, "rwd");
			savedFile.seek(start);
			byte[] buf = new byte[bufferSize];
			int len = 0;
			long progress = start;
			while ((len = is.read(buf)) != -1 && !stopController.isStop()) {
				savedFile.write(buf, 0, len);
				progress += len;
				if (null != progressListener) {
					progressListener.notify(len, progress);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (null != savedFile) {
					savedFile.close();
				}
			} catch (Exception e) {
			}
		}
	}
	


}
