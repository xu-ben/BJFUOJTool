import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
	
	private DBAgent dba = DBAgent.getInstance();

	private ProblemOperator() {
	}
	
	public String getSampleOutputOfProblem(String pid) {
		String ret = null;
		String sql = "select sample_output from problem where id = ?";
		ResultSet rs = null;
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ps.setString(1, pid);
			rs = ps.executeQuery();
			rs.beforeFirst();
			if (rs.next()) {
				ret = rs.getString(1);
			}
		} catch (SQLException se) {
			se.printStackTrace();
			return null;
		}
		return ret;
	}
	
	//public boolean 
	
	public synchronized static ProblemOperator getInstance() {
		if(po == null) {
			po = new ProblemOperator();
		}
		return po;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ProblemOperator po = ProblemOperator.getInstance();
		System.out.println(po.getSampleOutputOfProblem("1216"));
	}

}
