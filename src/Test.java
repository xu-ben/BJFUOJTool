import java.io.*;
import java.sql.*;
import java.util.*;

public class Test {
	/**
	 * 文件读写代理对象
	 */
	private IOAgent ioa = IOAgent.getInstance();

	/**
	 * 数据库操作代理对象
	 */
	private DBAgent dba = DBAgent.getInstance();

	private SolutionOperator so = SolutionOperator.getInstance();

	private ContestOperator co = ContestOperator.getInstance();

	public static void main(String[] args) {
		Test test = new Test();
		Integer id = 14;
		/*
		 * String strusername = ""; String str = new String(""); str +=
		 * "*****************************************************************\r\n"
		 * ; str += "*******************Java程序设计实验" + (id - 9) +
		 * "代码存档*******************\r\n"; str +=
		 * "*****************************************************************\r\n\r\n"
		 * ; strusername = "090824210"; str += getErrorUser(strusername); str +=
		 * "\r\n"; str +=
		 * "\r\n*****************************************************************\r\n"
		 * ; str += "*******************Java程序设计实验" + (id - 9) +
		 * "代码存档*******************\r\n"; str +=
		 * "*****************************************************************\r\n"
		 * ; DbOperation.setFileText("G:\\t\\Java程序设计实验" + (id - 9) +
		 * "开小号存档.doc", str);
		 */

		String s = test.getCodeFromAContest(id);
		System.out.println(s);
		// test.fio.setFileText("G:\\t\\Java程序设计实验" + (id - 9) + "代码存档.doc", s);

	}

	/**
	 * 利用用户名提取用户信息
	 */

	public String getErrorUser(String username) {
		String str = "用户:\t\t" + username + "\r\n";
		try {
			String sql = new String(
					"select ip,time from login_log where user_name = ? and time between '2010-10-28 17:00:00.0' and '2010-10-28 22:00:00.0'");
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			while (rs.next()) {
				str = str + "登陆IP：\t\t" + rs.getString(1) + "\r\n";
				str = str + "登陆时间：\t\t" + rs.getString(2) + "\r\n";
			}
		} catch (Exception se) {
			se.printStackTrace();
		}
		return str;
	}

	/**
	 * 根据指定竞赛号获得代码存档
	 * 
	 * @param id
	 * @return
	 */
	public String getCodeFromAContest(Integer id) {
		String s = new String("");
		s += "*****************************************************************\r\n";
		s += "*******************Java程序设计实验" + (id - 9)
				+ "代码存档*******************\r\n";
		s += "*****************************************************************\r\n\r\n";

		ArrayList<String> namelist = co.getNameListOfAContest(id);
		System.exit(0);
		// if (null == namelist) {
		// return "出错!!!";
		// }
		ArrayList<Integer> problemlist = co.getProblemidsOfAContest(id);
		for (String name : namelist) {
			if (name.equals("090824412")) {
				continue;
			}
			if (name.equals("090824429")) {
				continue;
			}
			s += "\r\n----------------------------------------------------------------------\r\n";
			s += "用户:\t\t" + name + "\r\n";
			// String loginip = getUserip(name);
			// s += "登陆IP:\t\t" + loginip + "\r\n";
			int acnum = getAcNum(id.toString(), name);
			s += "解决了 " + acnum + " 道题!\r\n\r\n";
			// 登录的IP

			// System.out.println(name);
			int j = 0;
			for (int pid : problemlist) {
				String solution_id = getAcSolutionId(name, pid, id);
				if (null != solution_id) {
					s += "\r\n解决的第" + (j + 1) + "道题的代码为:\r\n";
					s += so.getCodeBySolutionId(solution_id) + "\r\n";
					continue;
				}
				solution_id = getFailedSolutionId(name, pid, id);
				if (null != solution_id) {
					s += "\r\n第" + (j + 1) + "道题未成功解决!\r\n最后一次提交的错误代码为:\r\n";
					s += so.getCodeBySolutionId(solution_id) + "\r\n";
					continue;
				}
				s += "\r\n未曾提交解决第" + (j + 1) + "道题的代码!\r\n";
				j++;
			}
			s += "\r\n----------------------------------------------------------------------\r\n";
		}
		// System.out.println(ss);
		// ;= getCodeFromAContest(4);
		s += "\r\n*****************************************************************\r\n";
		s += "*******************Java程序设计实验" + (id - 9)
				+ "代码存档*******************\r\n";
		s += "*****************************************************************\r\n";
		// select * from solution where contest_id = '4' and time != '-1'
		return s;
	}


	/**
	 * 特定的用户在一场比赛中AC的题数
	 * 
	 * @param contest_id
	 * @param user_id
	 * @return
	 */
	public int getAcNum(String contest_id, String user_id) {
		try {
			String sql = new String(
					"select accepts from contest_status where contest_id = ? and user_id = ?");
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, contest_id);
			ps.setString(2, user_id);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (Exception se) {
			se.printStackTrace();
			return 0;
		}
		return 0;
	}

	public String getFailedSolutionId(String user_name, Integer problem_id,
			Integer contest_id) {
		try {
			String sql = new String(
					"select solution_id from solution where contest_id = ? and user_name = ? and problem_id = ? order by in_date desc");
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setInt(1, contest_id);
			ps.setString(2, user_name);
			ps.setInt(3, problem_id);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			if (rs.next()) {
				return rs.getString(1).trim();
			}
		} catch (SQLException se) {
			se.printStackTrace();
			return null;
		}
		return null;
	}

	/**
	 * 根据给定的用户名和题号和竞赛号，得到正确解决方案号
	 * 
	 * @param user_name
	 * @param problem_id
	 * @param contest_id
	 * @return
	 */
	public String getAcSolutionId(String user_name, Integer problem_id,
			Integer contest_id) {
		String sql = "select solution_id from solution where result = 1 and contest_id = ? and user_name = ? and problem_id = ?";
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setInt(1, contest_id);
			ps.setString(2, user_name);
			ps.setInt(3, problem_id);

			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			String ans = null;
			if (rs.next()) {
				ans = rs.getString(1).trim();
				return ans;
			}
		} catch (SQLException se) {
			se.printStackTrace();
			return null;
		}
		return null;
	}

}
