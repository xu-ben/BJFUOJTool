/*
 * 文件名：		DBAgent.java
 * 创建日期：	2014-6-26
 * 最近修改：	2014-6-26
 * 作者：		徐犇
 */

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author Administrator
 *
 */
public final class DBAgent {

	private Connection connMySQL = null;

	private Statement stmtMySQL = null;

	private boolean connectMySQL(String url, String user, String pass) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connMySQL = DriverManager.getConnection(url, user, pass);
			stmtMySQL = connMySQL.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 全局唯一实例
	 */
	private static DBAgent dba = null;

	private DBAgent() {
	}

	/**
	 * 返回一个本类的实例，采用默认账号密码连接北林acm数据库
	 * 
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 *
	 *
	 */
	
	/**
	 * 返回一个本类的实例，采用指定的xml文件名，从xml文件中读取数据配置数据库
	 * @param xmlfilename xml文件名
	 * @return 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public synchronized static DBAgent getInstance(String xmlfilename)
			throws ParserConfigurationException, SAXException, IOException {
		if (dba != null) {
			return dba;
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		File xmlfile = new File(xmlfilename);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(xmlfile);
		Node urln = doc.getElementsByTagName("url").item(0);
		String url = urln.getFirstChild().getNodeValue();
		Node usern = doc.getElementsByTagName("username").item(0);
		String user = usern.getFirstChild().getNodeValue();
		Node passn = doc.getElementsByTagName("password").item(0);
		String pass = passn.getFirstChild().getNodeValue();
		return getInstance(url, user, pass);
	}

	/**
	 * 返回一个本类的实例，采用指定的账号密码连接指定url的数据库
	 * 
	 * @param url
	 * @param user
	 * @param pass
	 */
	public synchronized static DBAgent getInstance(String url, String user,
			String pass) {
		if (dba == null) {
			dba = new DBAgent();
			if (!dba.connectMySQL(url, user, pass)) {// 连接失败
				dba = null;
			}
		}
		return dba;
	}

	public PreparedStatement prepareStatement(String sql) {
		try {
			return this.connMySQL.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 执行sql语句，返回查询结果集
	 * 
	 * @param sql
	 *            sql语句字符串
	 * @return 查询结果集
	 */
	public ResultSet executeQuery(String sql) {
		try {
			return this.stmtMySQL.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 执行sql语句
	 * 
	 * @param sql
	 * @return 执行操作是否成功
	 */
	public boolean execute(String sql) {
		try {
			return this.stmtMySQL.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

}
