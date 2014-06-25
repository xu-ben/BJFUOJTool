import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
	private DBAgent dba = DBAgent.getInstance();
	
	/**
	 * 全局唯一实例
	 */
	private static UserOperator uo = null;

	private UserOperator() {
	}
	
	public static synchronized UserOperator getInstance() {
		if(uo == null) {
			uo = new UserOperator();
		}
		return uo;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		Scanner cin = new Scanner(System.in);
//		String username = null, nickname = null;
//		while (cin.hasNextLine()) {
//			username = cin.nextLine();
//			nickname = cin.nextLine();
//			username = username.trim();
//			nickname = nickname.trim();
//			if (username == null || nickname == null) {
//				break;
//			}
//			if (!nickname.equals("null")) {
//				updateNickname(username, nickname);
//			}
//		}
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
			System.out.println("Eorror!");
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
	
}
