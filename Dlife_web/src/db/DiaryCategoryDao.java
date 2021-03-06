package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import system.Common;

public class DiaryCategoryDao {
	
	public int memberSK;
	Connection conn = null;
	PreparedStatement ps = null;	

	public DiaryCategoryDao() {
		super();
		Common.initDB();
	}
	
	public DiaryCategoryDao(int memberSK) {
		super();
		this.memberSK = memberSK;
		Common.initDB();
	}
	
	public DiaryCategoryDao close() {
		if(ps != null) {
			try {
				ps.close();
			} catch (SQLException e1) {
				System.out.println("DiaryCategoryDao ps close");
				e1.printStackTrace();
			}
		}
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				System.out.println("DiaryCategoryDao conn close");
				e.printStackTrace();
			}
		}
		return this;
	}

	public int insert(DiaryCategory diaryCategory) {
		int insertCount = 0;
		String sql = "insert into diary_category"
				+ "(member_sk, diary_sk , category_sk, top_category_sk, category_type,"
				+ " post_date)"
				+ " VALUES ("
				+ " ?,?,?,?,?,"
				+ " ?)";
		try {
			conn = DriverManager.getConnection(Common.DBURL, Common.DBACCOUNT,
					Common.DBPWD);
			ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, diaryCategory.getMember_sk());
			ps.setInt(2, diaryCategory.getDiary_sk());
			ps.setInt(3, diaryCategory.getCategory_sk());
			ps.setInt(4, diaryCategory.getTop_category_sk());
			ps.setString(5, diaryCategory.getCategory_type());
			ps.setString(6, Common.getNowDateTimeString());
			ps.executeUpdate();
			
			ResultSet tableKeys = ps.getGeneratedKeys();
			tableKeys.next();
			insertCount = tableKeys.getInt(1);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		close();
		return insertCount;
	}

	public int updateDiaryCategory(int diaryDetailSk, int top_category_sk, String category_type) {
		int rowCount = 0;
		String sql = " update diary_category"
				+ " set category_sk = ?, top_category_sk = ?, category_type = ?"
				+ " where diary_sk = ?"; 
				
		try {
			conn = DriverManager.getConnection(Common.DBURL, Common.DBACCOUNT, Common.DBPWD);
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, top_category_sk);
			ps.setInt(2, top_category_sk);
			ps.setString(3, category_type);
			ps.setInt(4, diaryDetailSk);
			rowCount = ps.executeUpdate();	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rowCount;
		
	}

	public boolean deleteDiaryCategoryDao(int dieayDetailSK) {
		String sql = "delete from diary_category"
				+ " where member_sk = ?"
				+ " and diary_sk = ?";
		try {
			conn = DriverManager.getConnection(Common.DBURL, Common.DBACCOUNT, Common.DBPWD);
			ps = conn.prepareStatement(sql);
			ps.setInt(1, memberSK);
			ps.setInt(2, dieayDetailSK);
			ps.executeUpdate();
			close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("deleteDiaryCategory fail memberSK:" + memberSK + " dieayDetailSK:" + dieayDetailSK);
			close();
			return false;
		}
	}


}
