package krusty;

import spark.Request;
import spark.Response;

import java.sql.*;
import java.util.*;

import static krusty.Jsonizer.toJson;

public class Database {
	/**
	 * Modify it to fit your environment and then use this string when connecting to your database!
	 */
	private static final String jdbcString = "jdbc:mysql://puccini.cs.lth.se:3306/hbg13";

	// For use with MySQL or PostgreSQL
	private static final String jdbcUsername = "hbg13";
	private static final String jdbcPassword = "pjd666zk";

	private Connection conn;

	public void connect() {
		try {
			conn = DriverManager.getConnection(jdbcString, jdbcUsername, jdbcPassword);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// TODO: Implement and change output in all methods below!

	public String getCustomers(Request req, Response res) {
		String sql = "SELECT name AS name, address FROM K_customers";
		String title = "customers";
		try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            return Jsonizer.toJson(rs, title);
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null; 
	}

	public String getRawMaterials(Request req, Response res) {
		String sql = "SELECT name, amount, unit FROM K_rawmaterials";
		String title = "raw-materials";
		try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            return Jsonizer.toJson(rs, title);
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null; 
	}

	public String getCookies(Request req, Response res) {
		String sql = "SELECT cookieName AS name FROM K_cookies";
		String title = "cookies";
		try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            return Jsonizer.toJson(rs, title);
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null; 
	}

	public String getRecipes(Request req, Response res) {
		String sql = "SELECT cookieName, rawmaterials_name, amount, unit FROM K_recipes";
		String title = "recipes";

		try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            return Jsonizer.toJson(rs, title);
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null; 
	}


	public String getPallets(Request req, Response res) {
		return "{\"pallets\":[]}";
	}

	public String reset(Request req, Response res) {
		return "{}";
	}

	public String createPallet(Request req, Response res) {
		//String cookie = req.queryParams("cookie");
		return "{}";
	}

}
