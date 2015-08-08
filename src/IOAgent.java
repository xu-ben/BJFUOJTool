/*
 * 文件名：		IOAgent.java
 * 创建日期：	2013-3-29
 * 最近修改：	2013-3-29
 * 作者：		徐犇
 */

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * 负责输入输出的类。理论上，本程序中所有与磁盘的交互均应由此类作为中介完成。
 * 
 * @author ben
 * 
 */
public final class IOAgent {
	/**
	 * 全局唯一实例
	 */
	private static IOAgent ioagent = null;

	/**
	 * 文件BOM头的数值
	 */
	private final int BOM_VALUE = 65279;

	/**
	 * 构造函数私有
	 */
	private IOAgent() {
	}

	/**
	 * @return 一个本类的实例
	 */
	public static synchronized IOAgent getInstance() {
		if (ioagent == null) {
			ioagent = new IOAgent();
		}
		return ioagent;
	}

	/**
	 * 统一字符串的行结束符为'\n'
	 * 
	 * @param text
	 *            需要进行处理的字符串
	 * @return 经过处理的字符串
	 */
	private final String unifyLineSeparator(String text) {
		if (text == null || text.trim().equals("")) {
			return null;
		}
		/*
		 * 如果字符串中没有'\r'字符，则无需处理
		 */
		if (text.indexOf('\r') < 0) {
			return text;
		}
		/*
		 * 有"\r\n"出现，则认为其行结束符为"\r\n"
		 */
		if (text.indexOf("\r\n") >= 0) {
			return text.replaceAll("\r\n", "\n");
		}
		return text.replaceAll("\r", "\n");
	}

	/**
	 * 因为在本程序内部，行分隔符统一为'\n'， 这里取到系统的行分隔符并将'\n'替换为该分隔符
	 * 
	 * @param text
	 *            需要进行处理的文本内容
	 * @return 处理好的文本内容
	 */
	private final String varyLineSeparator(String text) {
		if (text == null || text.trim().equals("")) {
			return null;
		}
		String linesep = System.getProperty("line.separator");
		if (!linesep.equals("\n")) {
			text = text.replaceAll("\n", linesep);
		}
		return text;
	}

	/**
	 * 得到磁盘(文本)文件的内容，并将内容的行结束符统一为'\n'
	 * 
	 * @param filePath
	 *            给定的磁盘文件的文件路径
	 * @return 文件内容
	 */
	public final String getFileText(String filePath) {
		if (filePath == null || filePath.trim().equals("")) {
			return null;
		}
		File f = new File(filePath.trim());
		if (!f.exists()) {
			return null;
		}
		return this.getFileText(f);
	}

	/**
	 * 得到磁盘(文本)文件的内容，并将内容的行结束符统一为'\n'
	 * 
	 * @param f
	 *            给定的磁盘文件
	 * @return 文件内容
	 */
	public String getFileText(File f) {
		if (f == null || !f.exists() || !f.isFile() || !f.canRead()) {
			return null;
		}
		int len = (int) f.length();
		if (len <= 0) {
			return null;
		}
		/*
		 * 得到该文件的编码格式字符串
		 */
		String type = getCodeType(f);
		BufferedReader br = null;
		try {
			FileInputStream fis = new FileInputStream(f);
			/*
			 * 指定读取文件时以type的编码格式读取
			 */
			InputStreamReader isr = new InputStreamReader(fis, type);
			br = new BufferedReader(isr);
			char[] content = new char[len];
			int textLen = br.read(content);
			int offset = 0;
			/*
			 * 去掉BOM头无效字符
			 */
			if (BOM_VALUE == (int) content[0]) {
				offset = 1;
			}
			String ret = String.valueOf(content, offset, textLen - offset);
			br.close();
			return unifyLineSeparator(ret);
		} catch (IOException ioe) {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	/**
	 * 判断文件f的字符编码
	 * 
	 * @param f
	 *            需要进行分析的文件
	 * @return 文件f的字符编码名称
	 */
	private String getCodeType(File f) {
		final byte _ef = (byte) 0xef;
		final byte _bb = (byte) 0xbb;
		final byte _bf = (byte) 0xbf;
		final byte _fe = (byte) 0xfe;
		final byte _ff = (byte) 0xff;
		byte[] bom = new byte[10];
		int cn = -1;
		try {
			FileInputStream is = new FileInputStream(f);
			cn = is.read(bom);
			is.close();
		} catch (Exception ex) {
		}
		if (cn >= 3 && bom[0] == _ef && bom[1] == _bb && bom[2] == _bf) {
			return "UTF-8";
		} else if (cn >= 2 && bom[0] == _ff && bom[1] == _fe) {
			return "Unicode";
		} else if (cn >= 2 && bom[0] == _fe && bom[1] == _ff) {
			// Unicode big endian
			return "Unicode";
		} else {
			// 初步认为是文件无BOM头，返回当前操作系统的默认文件编码
			return System.getProperty("file.encoding");
		}
		// String os = System.getProperty("os.name").toLowerCase();
		// if (os.indexOf("win") >= 0) {// windows
		// } else if (os.indexOf("mac") >= 0) {// mac
		// } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {//
		// linux或unix
		// }else {
		// }
	}

	/**
	 * 写磁盘文件方法
	 * 
	 * @param f
	 *            要向其中写入内容的文件，此文件必须已经存在，并且具有相应的写入权限
	 * @param text
	 *            要写入的文本内容
	 * @param append
	 *            是否采用追加的写入方式(否则为覆盖的写入方式)
	 * @param charset
	 *            文件采用的字符编码
	 * @return 操作是否成功
	 */
	public boolean setFileText(File f, String text, boolean append,
			String charset) {
		if (f == null || !f.exists() || !f.isFile() || !f.canWrite()) {
			return false;
		}
		if (text == null || text.trim().equals("")) {
			return false;
		}
		try {
			FileOutputStream fos = new FileOutputStream(f, append);
			OutputStreamWriter osw = new OutputStreamWriter(fos, charset);
			BufferedWriter bw = new BufferedWriter(osw);
			if (!append) {
				// 写入文件BOM头
				bw.write(BOM_VALUE);
			}
			bw.write(text);
			bw.flush();
			bw.close();
		} catch (IOException ioe) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * 写磁盘文件方法
	 * 
	 * @param filepath
	 *            要向其中写入内容的文件的文件路径
	 * @param text
	 *            要写入的文本内容
	 * @param append
	 *            是否采用追加的写入方式(否则为覆盖的写入方式)
	 * @param charset
	 *            文件采用的字符编码
	 * @return 操作是否成功
	 */
	public boolean setFileText(String filepath, String text, boolean append,
			String charset) {
		if (filepath == null || filepath.trim().equals("")) {
			return false;
		}
		File f = new File(filepath.trim());
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println(filepath);
			}
		}
		return setFileText(f, text, append, charset);
	}

	/**
	 * 
	 * 采用默认方式(采用系统默认字符编码和行结束符，写入BOM文件头，替换原有文件)写磁盘文件的方法
	 * 
	 * @param filepath
	 *            要向其中写入内容的文件的文件路径
	 * @param text
	 *            要写入的文本内容
	 * @return 操作是否成功
	 */
	public boolean setFileText(String filepath, String text) {
		String charset = System.getProperty("file.encoding");
		text = varyLineSeparator(text);
		return this.setFileText(filepath, text, false, charset);
	}

	/**
	 * 从Jar包中读取图片的函数
	 * 
	 * @param path
	 *            文件路径
	 * @param cls
	 *            调用类
	 * @return Image对象
	 */
	public Image getImageFromJar(String path, Class<?> cls) {
		Image image = null;
		InputStream is = cls.getResourceAsStream(path);
		BufferedInputStream bis = new BufferedInputStream(is);
		if (is == null) {
			return null;
		}

		// 存储读到的数据
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			// 临时缓冲区
			byte buf[] = new byte[1024];
			int len = 0;
			while ((len = bis.read(buf)) > 0) {
				baos.write(buf, 0, len);
			}

			// 生成图片对象
			image = Toolkit.getDefaultToolkit().createImage(baos.toByteArray());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return image;
	}

	/**
	 * 从Jar包中读取文本文件
	 * 
	 * @param path
	 *            文件的相对路径
	 * @param cls
	 *            调用类
	 * @return 读出的文本
	 */
	public String getTextFromJar(String path, Class<?> cls) {
		URL url = cls.getResource(path);
		if (url == null) {
			return null;
		}
		File file = null;
		try {
			file = new File(url.toURI());
			return getFileText(file);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

}
