import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * 
 */

/**
 * @author Administrator
 * 
 */
public final class SolutionOperator {

	/**
	 * 全局唯一实例
	 */
	private static SolutionOperator so = null;

	private DBAgent dba = null; 

	private IOAgent ioa = IOAgent.getInstance();

	private final String[] RESULTS = { "Waiting", "Accepted",
			"Presenting Error", "Time Limit Exceed", "Memory Limit Exceed",
			"Wrong Answer", "Compile Error", "Runtime Error", "Judging",
			"System Error(Judge)", "Judge Delay", "Judge Error",
			"Output Limit Exceed", "Restrict Function", "System Error(File)",
			"保留", "RunTime Error(ARRAY_BOUNDS_EXCEEDED)" };

	private final String[] LANGUAGES = { "", "GCC", "", "", "G++", "Pascal",
			"Java" };

	private final String[] FILEEXTS = { "", "c", "", "", "cpp", "pas", "java" };

	private SolutionOperator(String xmlfilename) throws ParserConfigurationException, SAXException, IOException {
		dba = DBAgent.getInstance(xmlfilename);
	}

	private SolutionOperator(String dburl, String dbuser, String dbpass) {
		dba = DBAgent.getInstance(dburl, dbuser, dbpass);
	}

	public static synchronized SolutionOperator getInstance(String xmlfilename) {
		if (so == null) {
			try {
				so = new SolutionOperator(xmlfilename);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return so;
	}

	/**
	 * @param id
	 *            结果的id
	 * @return 结果字符串。当id无效时返回null
	 */
	public String getResultNameById(int id) {
		if (id >= 0 && id < 17) {
			return RESULTS[id];
		}
		return null;
	}

	/**
	 * @param name
	 *            结果描述字符串
	 * @return 结果的id, 当字符串无效时返回-1
	 */
	public int getResultIdByName(String name) {
		int len = RESULTS.length;
		for (int i = 0; i < len; i++) {
			if (name.equalsIgnoreCase(RESULTS[i])) {
				return i;
			}
		}
		return -1;
	}
	
	public String[] getResultNames() {
		return this.RESULTS;
	}

	/**
	 * 通过id取出代码
	 * 
	 * @param id
	 * @return
	 */
	public String getCodeBySolutionId(String id) {
		try {
			String sql = "select source_code from solution_code where solution_id = ?";
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			if (rs.next()) {
				String s = new String(rs.getBytes(1), "GBK");
				return s;
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (UnsupportedEncodingException ue) {
			ue.printStackTrace();
		}
		return null;
	}

	/**
	 * 将OJ上所有代码取出保存到文件中,所有文件均放到path目录下
	 */
	private boolean saveAllCodeToFile(String path) {
		String sql = "select problem_id, user_name, result, language, in_date, solution_id from solution";
		String name = null;
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			while (rs.next()) {
				String pid = rs.getString(1);
				String user_name = rs.getString(2);
				String result = getResultNameById(Integer.parseInt(rs
						.getString(3)));
				String ext = FILEEXTS[Integer.parseInt(rs.getString(4))];
				String date = rs.getString(5).replaceAll("[: .]", "-");
				name = String.format("%s_%s_%s_%s.%s", user_name, pid, result,
						date, ext);
				name = name.replaceAll(" ", "");
				String code = getCodeBySolutionId(rs.getString(6));
				ioa.setFileText(path + name, code);
			}
		} catch (SQLException se) {
			se.printStackTrace();
			return false;
		}
		return true;
	}
	
	public String getSolutionDetailById(String id) {
		String ret = new String();
		String code = getCodeBySolutionId(id);
		if (code == null) {
			return null;
		}
		String sql = "select problem_id, user_name, result, language from solution where solution_id = ?";
		ResultSet rs = null;
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, id);
			rs = ps.executeQuery();
			rs.beforeFirst();
			if (rs.next()) {
				String pid = rs.getString(1);
				String user_name = rs.getString(2);
				String result = getResultNameById(Integer.parseInt(rs
						.getString(3)));
				String lang = LANGUAGES[Integer.parseInt(rs.getString(4))];

				System.out.println(pid);
				System.out.println(user_name);
				System.out.println(result);
				System.out.println(lang);
				System.out.println(code);
			}
		} catch (SQLException se) {
			se.printStackTrace();
			return null;
		}
		return ret;
	}
	
	/**
	 * 获取一个Solution的提交时间
	 * @param solution_id
	 * @return
	 */
	public String getDateOfASolution(String solution_id) {
		String sql = "select in_date from solution where solution_id = ?";
		try {
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
			se.printStackTrace();
		}
		return null;
	}


	/**
	 * 通过(多个)Solution的详细信息取得其id(可能为多个)
	 * @param user_name
	 * @param problem_id
	 * @param result
	 * @param contest_id
	 * @return
	 */
	public ArrayList<String> getSolutionIdByDetail(String user_name, int problem_id, int result, int contest_id) {
		ArrayList<String> ids = new ArrayList<String>();
		String sql = "select solution_id from solution where result = ? and contest_id = ? and user_name = ? and problem_id = ?";
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setInt(1, result);
			ps.setInt(2, contest_id);
			ps.setString(3, user_name);
			ps.setInt(4, problem_id);

			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			String ans = null;
			while (rs.next()) {
				ans = rs.getString(1).trim();
				ids.add(ans);
			}
			return ids;
		} catch (SQLException se) {
			se.printStackTrace();
			return null;
		}
	}

	public boolean saveAllContestCodeToFile(String path, String contestid) {
		String sql = "select problem_id, user_name, result, language, in_date, solution_id from solution";
		String name = null;
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			while (rs.next()) {
				String pid = rs.getString(1);
				String user_name = rs.getString(2);
				String result = getResultNameById(Integer.parseInt(rs
						.getString(3)));
				String ext = FILEEXTS[Integer.parseInt(rs.getString(4))];
				String date = rs.getString(5).replaceAll("[: .]", "-");
				name = String.format("%s_%s_%s_%s.%s", user_name, pid, result,
						date, ext);
				name = name.replaceAll(" ", "");
				String code = getCodeBySolutionId(rs.getString(6));
				ioa.setFileText(path + name, code);
			}
		} catch (SQLException se) {
			se.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SolutionOperator so = SolutionOperator.getInstance("acm.db.xml");
		// so.getSolutionDetailById("4a4cfd8e3ff8d89301400096481a0007");
		// so.genFileNameById("4a4cfd8e3ff8d89301400096481a0007");
		so.saveAllCodeToFile("X:\\bjfuacmcode\\");
	}

}
