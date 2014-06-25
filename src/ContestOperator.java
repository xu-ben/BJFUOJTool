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
	
	private DBAgent dba = DBAgent.getInstance();
	
	private SolutionOperator so = SolutionOperator.getInstance();
	
	private ContestOperator() {
	}
	
	public static synchronized ContestOperator getInstance() {
		if(co == null) {
			co = new ContestOperator();
		}
		return co;
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
	

	public boolean saveAllContestCodeToFile(String path, String contestid) {
		String ret = new String();
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
			return null;
		}
		return ret;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		so.saveAllContestCodeToFile("X:\\cpp\\", "40");
	}

}
