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

	public static void main_new(String[] args) {
		Test test = new Test();
		// for (int i = 10; i < 19; i++) {
		// getACCodeOfAContestToDir(i);
		// System.gc();
		// }
		// getCodeToFilesFromAContest(24);
		test.getACCodeOfAContestToDir(23);
	}

	/**
	 * 将一次竞赛中所有用户Accepted的代码按用户分文件存放在指定的目录下
	 * 
	 * @param id
	 */
	private void getACCodeOfAContestToDir(int id) {
		ArrayList<String> namelist = getNameListOfAContest(id);
		String root = "H:\\tt";
		String dir;
		int k;

		Integer[] problemlist = getProblemList(id);
		String solution_id;
		ArrayList<String> ids;

		for (String name : namelist) {
			for (int i = 0; i < problemlist.length; i++) {
				dir = new String(root);
				if (problemlist.length == 1) {

				} else if (i == 0) {
					dir += "\\A题";
				} else if (i == 1) {
					dir += "\\B题";
				} else {
					dir += "\\C题";
				}
				File f = new File(dir);
				if (!f.exists()) {
					f.mkdir();
				}

				dir += "\\";

				k = 1;
				ids = getSolutionId(name, problemlist[i], k, id);
				if (ids == null || ids.size() == 0) {
					continue;
				}
				for (int j = 0; j < ids.size(); j++) {
					solution_id = ids.get(j);
					String code = getACodeBySolutionId(solution_id);
					String date = getDateOfASolution(solution_id);
					setCodeFile(dir, problemlist[i], date, "Accepted", code,
							name);
				}

			}

			// solution_id = getFailedSolutionId(namelist[i], problemlist[j],
			// id);
		}
	}

	private void getCodeToFilesFromAContest(int id) {

		String[] RESULTS = { "waiting", "Accepted", "presenting error",
				"Time Limit Exceed", "Memory Limit Exceed", "Wrong Answer",
				"Compile Error", "Runtime Error", "Judging",
				"System Error(Judge)", "Judge Delay", "Judge Error",
				"Output Limit Exceed", "Restrict Function",
				"System Error(File)", "保留",
				"RunTime Error(ARRAY_BOUNDS_EXCEEDED)" };

		String[] namelist = getNameListOfAContest(id);
		String root = "H:\\临时文件夹\\tt\\问题求解实验所有提交代码\\实验3\\";
		String dir;

		Integer[] problemlist = getProblemList(id);
		String solution_id;
		ArrayList<String> ids;

		for (int i = 0; i < namelist.length; i++) {
			dir = new String(root + namelist[i]);
			File f = new File(dir);
			if (!f.exists()) {
				f.mkdir();
			}

			dir += "\\";

			for (int j = 0; j < problemlist.length; j++) {
				for (int k = 0; k < RESULTS.length; k++) {
					ids = getSolutionId(namelist[i], problemlist[j], k, id);
					if (ids == null || ids.size() == 0) {
						continue;
					}
					for (int ii = 0; ii < ids.size(); ii++) {
						solution_id = ids.get(ii);
						String code = getACodeBySolutionId(solution_id);
						String date = getDateOfASolution(solution_id);
						setCodeFile(dir, problemlist[j], date, RESULTS[k],
								code, namelist[i]);
					}
				}

			}

			// solution_id = getFailedSolutionId(namelist[i], problemlist[j],
			// id);
		}
	}

	public String getDateOfASolution(String solution_id) {
		try {
			String sql = new String(
					"select in_date from solution where solution_id = ?");
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, solution_id);

			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			String ans = null;
			if (rs.next()) {
				ans = rs.getString(1);
				return ans;
			}
		} catch (SQLException se) {
			System.out.println("main sql" + se.toString());
			return null;
		}
		return null;
	}

	private ArrayList<String> getSolutionId(String name, int pid, int k, int id) {
		ArrayList<String> ids = new ArrayList<String>();
		String sql = "select solution_id from solution where result = ? and contest_id = ? and user_name = ? and problem_id = ?";
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setInt(1, k);
			ps.setInt(2, id);
			ps.setString(3, name);
			ps.setInt(4, pid);

			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			String ans = null;
			while (rs.next()) {
				ans = rs.getString(1).trim();
				ids.add(ans);
			}
			return ids;
		} catch (SQLException se) {
			System.out.println("main sql" + se.toString());
			return null;
		}
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
			System.out.println(se.toString());
		}
		return str;
	}

	public String getACodeBySolutionId(String solution_id) {
		try {
			String sql = "select source_code from solution_code where solution_id = ?";
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, solution_id);
			ResultSet rs = ps.executeQuery(sql);
			rs.beforeFirst();
			if (rs.next()) {
				String s;
				try {
					s = new String(rs.getBytes(1), "GBK");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					return null;
				}
				return s;
			}
		} catch (SQLException se) {
			System.out.println("main sql" + se.toString());
			return null;
		}
		return null;
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

		String[] namelist = getNameListOfAContest(id);
		for (int i = 0; i < namelist.length; i++) {
			System.out.println(namelist[i] + "登陆IP" + getUserip(namelist[i]));
		}
		System.exit(0);
		// if (null == namelist) {
		// return "出错!!!";
		// }
		Integer[] problemlist = getProblemList(id);
		for (int i = 0; i < namelist.length; i++) {
			if (namelist[i].equals("090824412")) {
				continue;
			}
			if (namelist[i].equals("090824429")) {
				continue;
			}
			s += "\r\n----------------------------------------------------------------------\r\n";
			s += "用户:\t\t" + namelist[i] + "\r\n";
			String loginip = getUserip(namelist[i]);
			s += "登陆IP:\t\t" + loginip + "\r\n";
			int acnum = getAcNum(id.toString(), namelist[i]);
			s += "解决了 " + acnum + " 道题!\r\n\r\n";
			// 登录的IP

			// System.out.println(namelist[i]);
			for (int j = 0; j < problemlist.length; j++) {
				String solution_id = getAcSolutionId(namelist[i],
						problemlist[j], id);
				if (null != solution_id) {
					s += "\r\n解决的第" + (j + 1) + "道题的代码为:\r\n";
					s += getACodeBySolutionId(solution_id) + "\r\n";
					continue;
				}
				solution_id = getFailedSolutionId(namelist[i], problemlist[j],
						id);
				if (null != solution_id) {
					s += "\r\n第" + (j + 1) + "道题未成功解决!\r\n最后一次提交的错误代码为:\r\n";
					s += getACodeBySolutionId(solution_id) + "\r\n";
					continue;
				}
				s += "\r\n未曾提交解决第" + (j + 1) + "道题的代码!\r\n";
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
			System.out.println(se.toString());
		}
		return userip;
	}

	public Integer[] getProblemList(Integer id) {
		try {
			String sql = new String(
					"select problem_id from contest_problem where contest_id = ? order by problem_id");
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			ArrayList<Integer> list = new ArrayList<Integer>();
			while (rs.next()) {
				list.add(rs.getInt(1));
			}
			int len = list.size();
			Integer[] ans = new Integer[len];
			for (int i = 0; i < len; i++) {
				ans[i] = list.get(i);
			}
			return ans;
		} catch (Exception se) {
			System.out.println(se.toString());
			return null;
		}
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
			System.out.println(se.toString());
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
			System.out.println("main sql" + se.toString());
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
		try {
			String sql = new String(
					"select solution_id from solution where result = 1 and contest_id = ? and user_name = ? and problem_id = ?");
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
			System.out.println("main sql" + se.toString());
			return null;
		}
		return null;
	}

	/**
	 * 根据竞赛号得到用户名序列(无重复)
	 * 
	 * @param i
	 * @return
	 */
	public ArrayList<String> getNameListOfAContest(Integer id) {
		ArrayList<String> namelist = new ArrayList<String>();
		String sql = "select user_id from contest_status where contest_id = ? order by user_id";
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, id.toString());
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			while (rs.next()) {
				String name = rs.getString(1).trim();
				namelist.add(name);
			}
		} catch (SQLException se) {
			System.err.println("main sql" + se.toString());
			return null;
		}
		return namelist;
	}

	public String getMyTime(String date) {
		int index = date.indexOf(' ');
		if (index == -1) {
			return null;
		}
		date = date.substring(index + 1);
		date = date.replaceAll(":", "-");
		// System.out.println(date);
		// System.exit(0);
		return date.trim();
	}

	/**
	 * 将代码(按文件)存到磁盘中
	 * 
	 * @param filePath
	 * @param text
	 * @return
	 */
	public boolean setCodeFile(String dir, int problemid, String date,
			String result, String code, String username) {
		String filePath = new String(dir + username + "_");
		switch (problemid) {
		case 1095:
			filePath += "A题_";
			break;
		case 1096:
			filePath += "B题_";
			break;
		case 1076:
			filePath += "C题_";
			break;
		}
		filePath += getMyTime(date);
		filePath += "_" + result + ".cpp";
		ioa.setFileText(filePath, code);
		return true;
	}

}
