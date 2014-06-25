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
public final class ForumOperator {
	
	private DBAgent dba = DBAgent.getInstance();
	
	/**
	 * 全局唯一实例
	 */
	private static ForumOperator fo = null;
	
	private ForumOperator() {
	}

	public static synchronized ForumOperator getInstance() {
		if(fo == null) {
			fo = new ForumOperator();
		}
		return fo;
	}

	public void work() {
		dba.execute("set names utf8");
		String sql = "select user_name, topic from forum where top=1 order by in_date desc";
		try {
			PreparedStatement ps = dba.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			int i = 0;
			while(rs.next()) {
				if(++i % 3 != 0) {
					continue;
				}
				byte[] tmp = rs.getBytes(2);
				for(int j = 0; j < tmp.length; j++) {
					System.out.printf("%x ", tmp[j]);
				}
//				String topic = rs.getString(2);
//				IOAgent ioa = IOAgent.getInstance();
//				ioa.setFileText("I:\\tmp.txt", topic);
//				byte[] tmp = topic.getBytes("ISO-8859-1");
//				byte[] tmp = topic.getBytes("UTF-8");
//				System.out.println(new String(tmp, "GBK"));
//				byte[] tmp = rs.getString(2).getBytes("ISO-8859-1");
//				System.out.println(new String(tmp, "gbk"));
//				String tmps = "ACM爱好者协会";
//				System.out.println(tmps.getBytes("ISO-8859-1"));
//				System.out.println(new String(tmp, "ISO-8859-1"));
				
//				String topic = new String(rs.getBytes(2), "GBK");
//				System.out.println(topic);
//				byte[] temp_t = temp_p.getBytes("ISO-8859-1");
//				String temp = new String(temp_t, "GBK");

			}
		} catch (SQLException e) {
			e.printStackTrace();
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ForumOperator fo = ForumOperator.getInstance();
		fo.work();
	}

}
