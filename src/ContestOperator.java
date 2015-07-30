import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author ben
 * 
 */
public final class ContestOperator {

	/**
	 * 全局唯一实例
	 */
	private static ContestOperator co = null;

	private DBAgent dba = null;

	private SolutionOperator so = null;
	
	private UserOperator uo = null;

	private IOAgent ioa = IOAgent.getInstance();

	private ContestOperator() {
		dba = DBAgent.getInstance();
		so = SolutionOperator.getInstance();
		uo = UserOperator.getInstance();
	}

	private ContestOperator(String dburl, String dbuser, String dbpass) {
		dba = DBAgent.getInstance(dburl, dbuser, dbpass);
		so = SolutionOperator.getInstance(dburl, dbuser, dbpass);
		uo = UserOperator.getInstance(dburl, dbuser, dbpass);
	}

	public static synchronized ContestOperator getInstance() {
		if (co == null) {
			co = new ContestOperator();
		}
		return co;
	}

	public static synchronized ContestOperator getInstance(String dburl,
			String dbuser, String dbpass) {
		if (co == null) {
			co = new ContestOperator(dburl, dbuser, dbpass);
		}
		return co;
	}

	/**
	 * 根据比赛号得到题目序号
	 * 
	 * @param cid
	 * @return
	 */
	public ArrayList<Integer> getProblemidsOfAContest(Integer cid) {
		String sql = "select problem_id from contest_problem where contest_id = ? order by problem_id";
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setInt(1, cid);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			ArrayList<Integer> list = new ArrayList<Integer>();
			while (rs.next()) {
				list.add(rs.getInt(1));
			}
			return list;
		} catch (Exception se) {
			se.printStackTrace();
			return null;
		}
	}

	/**
	 * 根据竞赛号得到用户名序列(无重复)
	 * 
	 * @param id
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
			se.printStackTrace();
			return null;
		}
		return namelist;
	}

	/**
	 * 将一次竞赛中所有用户Accepted的代码按用户分文件存放在指定的目录下
	 * 
	 * @param id
	 *            比赛id
	 */
	public final void getACCodeOfAContestToDir(Integer id, String rootdir) {
		ArrayList<String> namelist = co.getNameListOfAContest(id);
		String[] tmp = new String[namelist.size()];
		getACCodeOfAContestToDir(id, rootdir, namelist.toArray(tmp));
	}

	/**
	 * 将一次竞赛中<b>指定用户</b>Accepted的代码按用户分文件存放在指定的目录下
	 * 
	 * @param id
	 */
	public final void getACCodeOfAContestToDir(Integer id, String rootdir,
			String[] namelist) {
		ArrayList<Integer> problemlist = getProblemidsOfAContest(id);
		int I = 0;
		for (int pid : problemlist) {
			String dir = rootdir;
			if (problemlist.size() > 1) {
				dir += "\\" + (char)('A' + I);
			}
			File f = new File(dir);
			if (!f.exists()) {
				f.mkdir();
			}
			dir += "\\";
			for (String user : namelist) {
				ArrayList<String> ids;
				ids = so.getSolutionIdByDetail(user, pid, 1, id);
				if (ids == null || ids.size() == 0) {
					continue;
				}
				for (String solution_id : ids) {
					String code = so.getCodeBySolutionId(solution_id);
					String date = so.getDateOfASolution(solution_id);
					String time = getMyTime(date);
					String res = "Accepted";
					String path = formatFilePath(dir, time, res, user, I);
					ioa.setFileText(path, code);
				}
			}
			I++;
		}
	}

	/**
	 * 将一场比赛的所有代码按要求导出到指定目录下
	 * 
	 * @param cid
	 *            比赛(在数据库中)的序号
	 * @param rootdir
	 *            指定根目录
	 */
	public final void getCodesToFilesFromAContest(Integer cid, String rootdir) {
		ArrayList<String> namelist = co.getNameListOfAContest(cid);
		String[] ret = new String[namelist.size()];
		getCodesToFilesFromAContest(cid, rootdir, namelist.toArray(ret));
	}

	/**
	 * 将一场比赛中<b>指定用户</b>的所有代码按要求导出到指定目录下
	 * 
	 * @param cid
	 *            比赛(在数据库中)的序号
	 * @param rootdir
	 *            指定根目录
	 * @param namelist
	 *            给定的用户名列表
	 */
	public final void getCodesToFilesFromAContest(Integer cid, String rootdir,
			String[] namelist) {
		String[] RESULTS = so.getResultNames();
		ArrayList<Integer> problemlist = getProblemidsOfAContest(cid);
		for (String user : namelist) {
			String dir = rootdir + "\\" + user;
			File f = new File(dir);
			if (!f.exists()) {
				f.mkdir();
			}
			dir += "\\";

			int I = 0;
			for (int pid : problemlist) {
				for (int i = 0; i < RESULTS.length; i++) {
					ArrayList<String> sids;
					sids = so.getSolutionIdByDetail(user, pid, i, cid);
					if (sids == null || sids.size() == 0) {
						continue;
					}
					for (String solution_id : sids) {
						String code = so.getCodeBySolutionId(solution_id);
						String date = so.getDateOfASolution(solution_id);
						String time = getMyTime(date);
						String res = RESULTS[i];
						String path = formatFilePath(dir, time, res, user, I);
						ioa.setFileText(path, code);
					}
				}
				I++;
			}
		}
	}

	/**
	 * 将从数据库中取出的日期时间文本处理成所需格式的时间文本
	 * 
	 * @param date
	 * @return
	 */
	private String getMyTime(String date) {
		int index = date.indexOf(' ');
		if (index == -1) {
			return null;
		}
		date = date.substring(index + 1);
		date = date.replaceAll(":", "-");
		return date.trim();
	}

	/**
	 * 根据比赛id获取比赛开始及结束时间
	 * 
	 * @param id
	 * @return
	 */
	public Timestamp[] getStartAndEndTimeofContest(int id) {
		Timestamp[] ret = new Timestamp[2];
		String sql = "select start_time, end_time from contest where contest_id = "
				+ id;
		try {
			ResultSet rs = dba.executeQuery(sql);
			rs.beforeFirst();
			if (rs.next()) {
				ret[0] = rs.getTimestamp(1);
				ret[1] = rs.getTimestamp(2);
			}
		} catch (SQLException se) {
			se.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * 获取指定时间区间的所有登录情况，输出到指定输出流(如System.out标准输出)中
	 * @param username 用户名
	 * @param start 开始时间
	 * @param end 结束时间
	 * @param out 输出流
	 */
	public void getLoginDetail(String username, Timestamp start, Timestamp end, PrintStream out) {
		String sql = "select ip, time from login_log where user_name = ? and time between ? and ?";
		out.print(username);
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, username);
			ps.setTimestamp(2, start);
			ps.setTimestamp(3, end);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			while (rs.next()) {
				out.print(", " + rs.getTime(2));
				out.print(", " + rs.getString(1));
			}
		} catch (Exception se) {
			se.printStackTrace();
		}
		out.println();
	}

	/**
	 * 根据给定的信息，得到一个唯一的文件(全)路径
	 * 
	 * @param dir
	 * @param pid 从0开始编号的题目序号
	 * @param subtime
	 * @param result
	 * @param user_name
	 * @return
	 */
	private String formatFilePath(String dir, String subtime, String result,
			String user_name, int pid) {
		StringBuilder sb = new StringBuilder();
		sb.append(dir);
		sb.append(user_name);
		sb.append("_");
		sb.append((char)(pid + 'A'));
		sb.append("_");
		sb.append(subtime);
		sb.append("_");
		sb.append(result);
		sb.append(".cpp");
		return sb.toString();
	}

	private static void mkdir(String dir) {
		File f = new File(dir);
		if (!f.exists()) {
			f.mkdir();
		}
	}

	/**
	 * 筛掉不符合要求的但参加了比赛的用户名
	 * @param namelist
	 * @return
	 */
	private static String[] treamNameList(ArrayList<String> namelist) {
		int size = namelist.size();
		for (int i = size - 1; i >= 0; i--) {
			String name = namelist.get(i);
			if (!name.matches("\\d+")) {
				namelist.remove(i);
			}
		}
		size = namelist.size();
		String[] ret = new String[size];
		return namelist.toArray(ret);
	}

	/**
	 * 导出考试数据,包括学生提交的所有代码，ac代码以及考试期间用户登录情况
	 * @param rootdir 存储数据文件的根目录
	 * @param cid 比赛的数据库id
	 * @throws FileNotFoundException 
	 */
	public void exportExamData(String rootdir, Integer cid) throws FileNotFoundException {
		mkdir(rootdir);
		String alldir = rootdir + "\\all";
		mkdir(alldir);
		String[] namelist = treamNameList(co.getNameListOfAContest(cid));
		getCodesToFilesFromAContest(cid, alldir, namelist);
		String acdir = rootdir + "\\ac";
		mkdir(acdir);
		getACCodeOfAContestToDir(cid, acdir, namelist);
		
		Timestamp[] contestTime = getStartAndEndTimeofContest(cid);
		PrintStream login_ip = new PrintStream(rootdir + "\\login_ip.csv");
		PrintStream login_detail = new PrintStream(rootdir + "\\login_detail.csv");
		for (String name : namelist) {
			getLoginDetail(name, contestTime[0], contestTime[1], login_detail);
			
			login_ip.print(name);
			String[] ips = uo.getLoginIPs(name, contestTime[0], contestTime[1]);
			for (String ip : ips) {
				login_ip.print(", " + ip);
			}
			login_ip.println();
		}
		login_ip.close();
		login_detail.close();
	}
	
	/**
	 * 导出实验数据,包括学生提交的所有代码，ac代码等
	 * @param rootdir 存储数据文件的根目录
	 * @param cids 比赛的数据库id
	 */
	public void exportExperimentData(String rootdir, int[] cids) {
		mkdir(rootdir);
		for (int i = 0; i < cids.length; i++) {
			String cdir = rootdir + "\\实验" + (i + 1);
			mkdir(cdir);
			int cid = cids[i];
			String alldir = cdir + "\\all";
			mkdir(alldir);

			String[] namelist = treamNameList(co.getNameListOfAContest(cid));
			co.getCodesToFilesFromAContest(cid, alldir, namelist);

			String acdir = cdir + "\\ac";
			mkdir(acdir);
			co.getACCodeOfAContestToDir(cid, acdir, namelist);
		}
		
	}

	private static void pro_experiment2015() {
		String url = "jdbc:mysql://211.71.149.133:3306/acmhome";
		String user = "bjfuacm";
		String pass = "acm320";
		ContestOperator co = ContestOperator.getInstance(url, user, pass);
		int[] contestid = { 64, 65, 66, 68, 69, 71, 74 };
		String rootdir = "F:\\Experiment";
		mkdir(rootdir);
		for (int i = 1; i <= 7; i++) {
			String cdir = rootdir + "\\实验" + i;
			mkdir(cdir);
			int cid = contestid[i - 1];
			String alldir = cdir + "\\all";
			mkdir(alldir);

			String[] namelist = treamNameList(co.getNameListOfAContest(cid));
			co.getCodesToFilesFromAContest(cid, alldir, namelist);

			String acdir = cdir + "\\ac";
			mkdir(acdir);
			co.getACCodeOfAContestToDir(cid, acdir, namelist);
		}

	}
	
	private static void cpp_xyy_2015() {
//		String url = "jdbc:mysql://211.71.149.166:3306/acmhome";
//		String user = "ben";
//		String pass = "110423";
//		String cdir = "F:\\Cpp_XYY";
//		ContestOperator co = ContestOperator.getInstance(url, user, pass);
//		try {
//			co.exportExamData(cdir + "\\exam", 3);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		String url = "jdbc:mysql://211.71.149.133:3306/acmhome";
		String user = "bjfuacm";
		String pass = "acm320";
		String cdir = "F:\\C语言复习";
		ContestOperator co = ContestOperator.getInstance(url, user, pass);
		int[] cids = {63, 67, 34, 70, 73, 76};
		co.exportExperimentData(cdir, cids);
	}

	private static void cpp_exam_wsr_2015() {
//		String url = "jdbc:mysql://211.71.149.166:3306/acmhome";
//		String user = "ben";
//		String pass = "110423";
//		String cdir = "F:\\Exam";
//		ContestOperator co = ContestOperator.getInstance(url, user, pass);
//		try {
//			co.exportExamData(cdir, 2);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		String url = "jdbc:mysql://211.71.149.133:3306/acmhome";
		String user = "bjfuacm";
		String pass = "acm320";
		String cdir = "F:\\C++上机实验";
		ContestOperator co = ContestOperator.getInstance(url, user, pass);
		int[] cids = {75, 77, 78};
		co.exportExperimentData(cdir, cids);
	}

	private static void pro_exam2015() {
		String url = "jdbc:mysql://211.71.149.166:3306/acmhome";
		String user = "ben";
		String pass = "110423";
		ContestOperator co = ContestOperator.getInstance(url, user, pass);
		UserOperator uo = UserOperator.getInstance(url, user, pass);
		int cid = 2;
		String[] namelist = treamNameList(co.getNameListOfAContest(cid));
		Timestamp[] contestTime = co.getStartAndEndTimeofContest(cid);
		for (String name : namelist) {
			System.out.print("学号：" + name);
			System.out.print("\t登录过的ip：");
			String[] ips = uo.getLoginIPs(name, contestTime[0], contestTime[1]);
			for (String ip : ips) {
				System.out.print(" " + ip);
			}
			System.out.println();

		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		cpp_exam2015_detail();
//		cpp_xyy_2015();
		cpp_exam_wsr_2015();
	}
}
