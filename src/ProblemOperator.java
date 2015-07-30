import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */

/**
 * @author ben
 * 
 */
public final class ProblemOperator {

	/**
	 * 全局唯一实例
	 */
	private static ProblemOperator po = null;

	private DBAgent dba = null;

	private ProblemOperator() {
		dba = DBAgent.getInstance();
	}

	private ProblemOperator(String dburl, String dbuser, String dbpass) {
		dba = DBAgent.getInstance(dburl, dbuser, dbpass);
	}

	/**
	 * 取出系统中所有题目的编号
	 * 
	 * @return
	 */
	public int[] getAllProblemId() {
		ArrayList<Integer> ans = new ArrayList<Integer>();
		String sql = "select id from problem";
		try {
			ResultSet rs = null;
			PreparedStatement ps = dba.prepareStatement(sql);
			rs = ps.executeQuery();
			rs.beforeFirst();
			while (rs.next()) {
				ans.add(rs.getInt(1));
			}
			int[] ret = new int[ans.size()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = ans.get(i);
			}
			return ret;
		} catch (SQLException se) {
			se.printStackTrace();
			return null;
		}
	}

	private boolean setContentOfProblem(String field, int pid, String newtext) {
		String sql = String.format("update problem set %s = ? where id = ?",
				field);
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, newtext);
			ps.setInt(2, pid);
			ps.execute();
		} catch (SQLException se) {
			se.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean setDescpriptionOfProblem(int pid, String newtext) {
		return setContentOfProblem("description", pid, newtext);
	}

	public boolean setInputOfProblem(int pid, String newtext) {
		return setContentOfProblem("input", pid, newtext);
	}

	public boolean setOutputOfProblem(int pid, String newtext) {
		return setContentOfProblem("output", pid, newtext);
	}

	public boolean setHintOfProblem(int pid, String newtext) {
		return setContentOfProblem("hint", pid, newtext);
	}

	public boolean setSampleInputOfProblem(int pid, String newtext) {
		return setContentOfProblem("sample_input", pid, newtext);
	}

	public boolean setSampleOutputOfProblem(int pid, String newtext) {
		return setContentOfProblem("sample_output", pid, newtext);
	}

	/**
	 * 根据指定字段和id从数据库problem表中取出所需字符串
	 * 
	 * @param field
	 *            字段名
	 * @param pid
	 * @return
	 */
	private String getContentOfProblem(String field, int pid) {
		String ret = null;
		String sql = String
				.format("select %s from problem where id = ?", field);
		try {
			ResultSet rs = null;
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setInt(1, pid);
			rs = ps.executeQuery();
			rs.beforeFirst();
			if (rs.next()) {
				ret = rs.getString(1);
			}
		} catch (SQLException se) {
			se.printStackTrace();
		}
		return ret;
	}

	public String getDescpriptionOfProblem(int pid) {
		return getContentOfProblem("description", pid);
	}

	public String getInputOfProblem(int pid) {
		return getContentOfProblem("input", pid);
	}

	public String getOutputOfProblem(int pid) {
		return getContentOfProblem("output", pid);
	}

	public String getSampleInputOfProblem(int pid) {
		return getContentOfProblem("sample_input", pid);
	}

	public String getSampleOutputOfProblem(int pid) {
		return getContentOfProblem("sample_output", pid);
	}

	public String getHintOfProblem(int pid) {
		return getContentOfProblem("hint", pid);
	}

	public synchronized static ProblemOperator getInstance() {
		if (po == null) {
			po = new ProblemOperator();
		}
		return po;
	}

	public synchronized static ProblemOperator getInstance(String dburl,
			String dbuser, String dbpass) {
		if (po == null) {
			po = new ProblemOperator(dburl, dbuser, dbpass);
		}
		return po;
	}

	/**
	 * 批量处理oj题目中的图片路径，将绝对路径的图片改成相对路径
	 */
	private static void imageTreat1() {
		String url = "jdbc:mysql://211.71.149.133:3306/acmhome";
		String user = "bjfuacm";
		String pass = "acm320";

		ProblemOperator po = ProblemOperator.getInstance(url, user, pass);

		int[] pids = po.getAllProblemId();
		int changecnt = 0;
		final String[] fields = {"description", "input", "output", "hint"};
		
		Pattern p = Pattern
				.compile("(\")http://acm.bjfu.edu.cn(/acmhome/[^\"]+\")");
		for (int pid : pids) {
			// System.out.println(pid);
			boolean change = false;
			for (String field : fields) {
				Matcher m = p.matcher(po.getContentOfProblem(field, pid));
				if (m.find()) {
					change = true;
					po.setContentOfProblem(field, pid, m.replaceAll("$1$2"));
				}
			}
			if (change) {
				System.out.println("题目：" + pid + "被修改");
				changecnt++;
			}
		}
		System.out.println("共修改题数: " + changecnt);

	}

	/**
	 * 批量处理oj题目中的图片路径，将系统中/acmhome/judge/images改成了/acmhome/upload/image
	 */
	private static void imageTreat2() {
		String url = "jdbc:mysql://211.71.149.133:3306/acmhome";
		String user = "bjfuacm";
		String pass = "acm320";

		ProblemOperator po = ProblemOperator.getInstance(url, user, pass);

		int[] pids = po.getAllProblemId();
		int changecnt = 0;
		final String[] fields = {"description", "input", "output", "hint"};
		
		/**
		 * 将绝对路径改成相对路径
		 */
		Pattern p = Pattern
				.compile("(src=\"/acmhome/)judge/images(/[^\"]+\")");
		for (int pid : pids) {
			// System.out.println(pid);
			boolean change = false;
			for (String field : fields) {
				Matcher m = p.matcher(po.getContentOfProblem(field, pid));
				if (m.find()) {
					change = true;
					po.setContentOfProblem(field, pid, m.replaceAll("$1upload/image$2"));
				}
			}
			if (change) {
				System.out.println("题目：" + pid + "被修改");
				changecnt++;
			}
		}
		System.out.println("共修改题数: " + changecnt);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		imageTreat1();
		imageTreat2();
	}

}
