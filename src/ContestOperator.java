import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
	
	private IOAgent ioa = IOAgent.getInstance();
	
	private ContestOperator() {
		dba = DBAgent.getInstance();
		so = SolutionOperator.getInstance();
	}
	
	private ContestOperator(String dburl, String dbuser, String dbpass) {
		dba = DBAgent.getInstance(dburl, dbuser, dbpass);
		so = SolutionOperator.getInstance(dburl, dbuser, dbpass);
	}
	
	public static synchronized ContestOperator getInstance() {
		if(co == null) {
			co = new ContestOperator();
		}
		return co;
	}

	public static synchronized ContestOperator getInstance(String dburl, String dbuser, String dbpass) {
		if(co == null) {
			co = new ContestOperator(dburl, dbuser, dbpass);
		}
		return co;
	}
	
	/**
	 * 根据比赛号得到题目序号
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
	 * @param id 比赛id
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
	public final void getACCodeOfAContestToDir(Integer id, String rootdir, String[] namelist) {
		ArrayList<Integer> problemlist = getProblemidsOfAContest(id);
		for (int pid : problemlist) {
			String dir = rootdir;
			if (problemlist.size() > 1) {
				dir += "\\" + pid;
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
					String path = formatFilePath(dir, time, res, user, pid);
					ioa.setFileText(path, code);
				}
			}
		}
	}
	
	/**
	 * 将一场比赛的所有代码按要求导出到指定目录下
	 * @param cid 比赛(在数据库中)的序号
	 * @param rootdir 指定根目录
	 */
	public final void getCodesToFilesFromAContest(Integer cid, String rootdir) {
		ArrayList<String> namelist = co.getNameListOfAContest(cid);
		String[] ret = new String[namelist.size()]; 
		getCodesToFilesFromAContest(cid, rootdir, namelist.toArray(ret));
	}
	
	/**
	 * 将一场比赛中<b>指定用户</b>的所有代码按要求导出到指定目录下
	 * @param cid 比赛(在数据库中)的序号
	 * @param rootdir 指定根目录
	 * @param namelist 给定的用户名列表
	 */
	public final void getCodesToFilesFromAContest(Integer cid, String rootdir, String[] namelist) {
		String[] RESULTS = so.getResultNames();
		ArrayList<Integer> problemlist = getProblemidsOfAContest(cid);
		for (String user : namelist) {
			String dir = rootdir + "\\" + user;
			File f = new File(dir);
			if (!f.exists()) {
				f.mkdir();
			}
			dir += "\\";

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
						String path = formatFilePath(dir, time, res, user, pid);
						ioa.setFileText(path, code);
					}
				}
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
	 * 根据给定的信息，得到一个唯一的文件(全)路径
	 * 
	 * @param dir
	 * @param pid
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
		sb.append(pid);
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
	 * @param args
	 */
	public static void main(String[] args) {
		String url = "jdbc:mysql://211.71.149.133:3306/acmhome";
		String user = "bjfuacm";
		String pass = "acm320";
		ContestOperator co = ContestOperator.getInstance(url, user, pass);
		int[] contestid = {64, 65, 66, 68, 69, 71, 74};
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

}
