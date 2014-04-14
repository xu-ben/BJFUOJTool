import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 
 */

/**
 * @author Administrator
 *
 */
public final class DBAgent {
	
	private Connection connMySQL = null;

	private Statement stmtMySQL = null;
	
	public boolean connectRemoteMySQL() {
		String url = "jdbc:mysql://acm.bjfu.edu.cn:3306/acmhome";
		String user = "root";
		String pass = "BJFUacmTEAM320";
		return connectMySQL(url, user, pass);
	}

	public boolean connectRemoteMySQL2() {
		String url = "jdbc:mysql://bjfuacm.vicp.cc:3306/acmhome";
		String user = "root";
		String pass = "root";
		return connectMySQL(url, user, pass);
	}

	public boolean connectMySQL() {
		String url = "jdbc:mysql://localhost:3306/acmhome";
		String user = "bjfuacm";
		String pass = "acm320";
		return connectMySQL(url, user, pass);
	}
	
	public boolean connectMySQL(String url, String user, String pass) {
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
	 * @return 一个本类的实例
	 */
	public synchronized static DBAgent getInstance() {
		if(dba == null) {
			dba = new DBAgent();
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

	public ResultSet executeQuery(String sql) {
		try {
			return this.stmtMySQL.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
