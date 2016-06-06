import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

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

	private UserOperator(String xmlfilename) throws ParserConfigurationException, SAXException, IOException {
		dba = DBAgent.getInstance(xmlfilename);
	}

	public static synchronized UserOperator getInstance(String xmlfilename) {
		if (uo == null) {
			try {
				uo = new UserOperator(xmlfilename);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return uo;
	}

	/**
	 * 有一次在数据库中将用户的昵称误删，写了这个函数恢复
	 */
	@SuppressWarnings("unused")
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
	 * @param str
	 * @return
	 * @Date: 2013-9-6
	 * @Author: lulei
	 * @Description: 32位小写MD5
	 */
	public static String parseStrToMd5L32(String str) {
		String reStr = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] bytes = md5.digest(str.getBytes());
			StringBuffer stringBuffer = new StringBuffer();
			for (byte b : bytes) {
				int bt = b & 0xff;
				if (bt < 16) {
					stringBuffer.append(0);
				}
				stringBuffer.append(Integer.toHexString(bt));
			}
			reStr = stringBuffer.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return reStr;
	}

	/**
	 * @param str
	 * @return
	 * @Date: 2013-9-6
	 * @Author: lulei
	 * @Description: 32位大写MD5
	 */
	public static String parseStrToMd5U32(String str) {
		String reStr = parseStrToMd5L32(str);
		if (reStr != null) {
			reStr = reStr.toUpperCase();
		}
		return reStr;
	}

	@SuppressWarnings("unused")
	private static void setPasswordforCppExam2015() {
		String str = "ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";// 字符集
		final int bitnum = 8;
		char[] arr = new char[bitnum];
		UserOperator uo = UserOperator.getInstance("166.db.xml");
		for (int i = 0; i < 64; i++) {
			for (int j = 0; j < bitnum; j++) {
				int t = (int) (Math.random() * str.length());
				arr[j] = str.charAt(t);
			}
			String p = new String(arr);
			String u = String.format("exam%04d", i);
			uo.setPassword(u, p);
			System.out.printf("%s, %s\n", u, p);
		}
	}

	@SuppressWarnings("unused")
	private static void setPasswordforProblemSolveExam2016() {
		String str = "ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";// 字符集
		final int bitnum = 8;
		char[] arr = new char[bitnum];
		UserOperator uo = UserOperator.getInstance("161.db.xml");
		for (int i = 0; i < 120; i++) {
			for (int j = 0; j < bitnum; j++) {
				int t = (int) (Math.random() * str.length());
				arr[j] = str.charAt(t);
			}
			String p = new String(arr);
			String u = String.format("bjfu%04d", i);
			uo.setPassword(u, p);
			System.out.printf("%s, %s\n", u, p);
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

	/**
	 * 将用户username的密码设成'111111'
	 * 
	 * @param username
	 * @return
	 */
	public boolean setPasswordToOne(String username) {
		return setPassword(username, "111111");
	}

	/**
	 * 将用户username的密码设成password
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean setPassword(String username, String password) {
		password = parseStrToMd5U32(password);
		String sql = "update users set password = ? where user_name = ?";
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, password);
			ps.setString(2, username);
			ps.execute();
		} catch (SQLException se) {
			se.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * u
	 * @param username
	 * @param solved
	 * @return
	 */
	public boolean setSolved(String username, int solved) {
		String sql = "update users set solved = ? where user_name = ?";
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setInt(1, solved);
			ps.setString(2, username);
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
		String sql = new String(
				"update users set nickname = ? where user_name = ?");
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
	 * 取到当前系统中所有的用户名
	 * @return
	 */
	public String[] getAllUsers() {
		String sql = "select user_name from users";
		ArrayList<String> ans = new ArrayList<String>();
		PreparedStatement ps = dba.prepareStatement(sql);
		try {
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			while (rs.next()) {
				ans.add(rs.getString(1));
			}
			if (ans.size() <= 0) {
				return null;
			}
			String[] users = new String[ans.size()];
			return ans.toArray(users);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 计算某用户在oj上的过题数。这个计算方法较准确
	 * @param username
	 * @return
	 */
	public int countSolvedProblemsOfUser(String username) {
		//select distinct problem_id from solution where user_name='bingfeng' and result=1 and problem_id < 10000 order by problem_id;		
		String sql = "select count(distinct problem_id) from solution where user_name=? and result=1 and problem_id < 10000";
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return -1;
	}
	
	public int getSolvedProblemsOfUser(String username) {
		String sql = "select solved from users where user_name=?";
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return -1;		
	}

	/**
	 * 获取指定用户在指定时间段登录本系统的ip地址列表
	 * 
	 * @param username
	 *            用户名
	 * @param start
	 *            开始时间
	 * @param end
	 *            结束时间
	 * @return ip地址数组
	 */
	public String[] getLoginIPs(String username, Timestamp start, Timestamp end) {
		String sql = "select ip from login_log where user_name = ? and time between ? and ?";
		HashSet<String> ans = new HashSet<String>();
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, username);
			ps.setTimestamp(2, start);
			ps.setTimestamp(3, end);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			while (rs.next()) {
				ans.add(rs.getString(1));
			}
			if (ans.size() <= 0) {
				return null;
			}
			String[] userip = new String[ans.size()];
			return ans.toArray(userip);

		} catch (Exception se) {
			se.printStackTrace();
			return null;
		}
	}
	
	public static void recoverySolvedNum() {
		ContestOperator co = ContestOperator.getInstance("acm.db.xml");
//		String[] ret = uo.getAllUsers();
//		for (String u : ret) {
//			int cs = uo.countSolvedProblemsOfUser(u);
//			int gs = uo.getSolvedProblemsOfUser(u);
//			if (cs != gs) {
//				System.out.printf("%s, %d, %d\n", u, cs, gs);
//				System.out.println(u);
//			}
//		}
		String[] usernames = {"140824209","bingfeng","songjs","test","xiaomaigua"};
		for (String u: usernames) {
			int cs = uo.countSolvedProblemsOfUser(u);
			int gs = uo.getSolvedProblemsOfUser(u);
			System.out.printf("%s, %d, %d\n", u, cs, gs);
//			uo.setSolved(u, cs);
			
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		recoverySolvedNum();
//		UserOperator uo = UserOperator.getInstance("acm.db.xml");
//		String[] ret = uo.getAllUsers();
//		for (String u : ret) {
//			int cs = uo.countSolvedProblemsOfUser(u);
//			System.out.printf("%s, %d\n", u, cs);
//		}
		setPasswordforProblemSolveExam2016();
	}


}
