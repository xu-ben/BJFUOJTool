import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * 
 */

/**
 * @author Administrator
 *
 */
public final class UserOperator {

	/**
	 * 数据库操作代理对象
	 */
	private DBAgent dba = null;
	
	/**
	 * 全局唯一实例
	 */
	private static UserOperator uo = null;

	private UserOperator() {
		dba = DBAgent.getInstance();
	}
	
	private UserOperator(String dburl, String dbuser, String dbpass) {
		dba = DBAgent.getInstance(dburl, dbuser, dbpass);
	}
	
	public static synchronized UserOperator getInstance() {
		if(uo == null) {
			uo = new UserOperator();
		}
		return uo;
	}

	public static synchronized UserOperator getInstance(String dburl, String dbuser, String dbpass) {
		if(uo == null) {
			uo = new UserOperator(dburl, dbuser, dbpass);
		}
		return uo;
	}
	
	/**
	 * 有一次在数据库中将用户的昵称误删，写了这个函数恢复
	 */
	@Deprecated
	private void restoreNickname() {
		Scanner cin = new Scanner(System.in);
		String username = null, nickname = null;
		while (cin.hasNextLine()) {
			username = cin.nextLine();
			nickname = cin.nextLine();
			username = username.trim();
			nickname = nickname.trim();
			if (username == null || nickname == null) {
				break;
			}
			if (!nickname.equals("null")) {
				updateNickname(username, nickname);
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args == null || args.length != 1) {
			System.out.println("parameter error!");
			return ;
		}
		String username = args[0];
		UserOperator uo = UserOperator.getInstance();
//		uo.getNicknameOfUser();
//		uo.getPassword("root");
		boolean flag = uo.setPasswordToOne(username);
		if(flag) {
			System.out.println("Success!");
		} else {
			System.out.println("Error!");
		}
	}

	public String getNicknameOfUser() {
		String s = null;
		try {
			String sql = "select user_name, nickname from users";
			ResultSet rs = dba.executeQuery(sql);
			rs.beforeFirst();
			while (rs.next()) {
				s = rs.getString(2);
				if (s == null || s.trim().equals("")) {
					s = "null";
				}
				System.out.printf("%s\t\t%s\n", rs.getString(1), s.trim());
			}
		} catch (SQLException se) {
			se.printStackTrace();
			return null;
		}
		return s;
	}
	
	public boolean setPasswordToOne(String username) {
		String sql = "update users set password=\'96E79218965EB72C92A549DD5A330112\' where user_name = ?";
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, username);
			ps.execute();
		} catch (SQLException se) {
			se.printStackTrace();
			return false;
		}
		return true;
	}
	
	public String getPassword(String username) {
		String sql = "select user_name, password from users where user_name = ?";
		String s = null;
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			while (rs.next()) {
				s = rs.getString(2);
				if (s == null || s.trim().equals("")) {
					s = "null";
				}
				System.out.printf("%s\n", s.trim());
			}
		} catch (SQLException se) {
			se.printStackTrace();
			return null;
		}
		return s;
	}

	public boolean updateNickname(String username, String nickname) {
		String sql = new String("update users set nickname = ? where user_name = ?");
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, nickname);
			ps.setString(2, username);
			ps.executeUpdate(sql);
		} catch (SQLException se) {
			se.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 利用用户名提取用户登陆IP地址
	 */
	public String getUserip(String username) {
		String userip = null;
		try {
			String sql = new String(
					"select ip from login_log where user_name = ? and time between '2010-11-04 17:00:00.0' and '2010-11-04 22:00:00.0'");
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			if (rs.next()) {
				return rs.getString(1);
			}
		} catch (Exception se) {
			se.printStackTrace();
		}
		return userip;
	}
	
}
