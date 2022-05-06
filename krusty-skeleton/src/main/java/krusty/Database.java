package krusty;

import spark.Request;
import spark.Response;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.protobuf.TextFormat.ParseException;

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


/* IS FUCKED */
	public String getPallets(Request req, Response res) throws SQLException, ParseException{
		return "{}"; 
	}

	public String reset(Request req, Response res) throws SQLException {
		String create = readCreate();
		String init = readInit();
	
		String delimiter = ";";

		Scanner scanner = new Scanner(create).useDelimiter(delimiter);
		Scanner scanner2 = new Scanner(init).useDelimiter(delimiter);

		Statement currentStatement = null;

		while (scanner.hasNext()) {
			String rawStatement = scanner.next() + delimiter;
			currentStatement = conn.createStatement();
			currentStatement.execute(rawStatement);
		}

		while (scanner2.hasNext()) {
			String rawStatement = scanner2.next() + delimiter;
			currentStatement = conn.createStatement();
			currentStatement.execute(rawStatement);
		}
		return "{\n\t\"status\": \"ok\"\n}";
	
	}
	
	private String readCreate() {
		try {
			String path = "src/main/resources/public/create-schema.sql";
			return new String(Files.readAllBytes(Paths.get(path)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	private String readInit() {
		try {
			String path = "src/main/resources/public/initial-data.sql";
			List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i<lines.size(); i++) {
				String liners = lines.get(i).toString();
				sb.append(liners);
			}
			return sb.toString();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public String createPallet(Request req, Response res) {
		String cookieName = req.queryParams("cookie");
		String time = LocalDate.now().toString();
		//For auto Increment
		int palletID = -1;
		String sql = "INSERT INTO K_pallets (production_date, cookies_cookieName) VALUES (?, ?)";
		if(cookie_exists(cookieName)) {
			try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
				ps.setString(1, time);
				ps.setString(2, cookieName);
				boolean result = ps.executeUpdate() != 0;
				ResultSet rs = ps.getGeneratedKeys();
				if(rs.next()) {
					palletID = rs.getInt(1);
				}
				ps.close();
				if(result) {
					//If it can execute update we need to subtract ingredients amount from storage
				subtract_ingredient(cookieName);	
				}

 
			} catch(SQLException e) {
				e.printStackTrace();
				return Jsonizer.anythingToJson("error", "status");
				}

				return "{\n\t\"status\": \"ok\" ," +
				"\n\t\"id\": " + palletID + "\n}";
		}
		return "{\n\t\"status\": \"unknown cookie\"\n}";
	}
	//Checks if cookie name exists in database
	private boolean cookie_exists(String cookieName) {
		String sql = "Select cookieName from K_cookies where cookieName = ?";

		try{
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, cookieName);
			ResultSet rs = ps.executeQuery();

			if(rs.next()) {
				ps.close();
				return true;
			}
			ps.close();

		} catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	//Subtracts amount needed for pallet from storage
	private void subtract_ingredient(String cookieName) {
		HashMap<String, Integer> ingredients = new HashMap<String, Integer>();

		try{
			conn.setAutoCommit(false);
			//Check how much of our ingredients are in storage
			String sql = "SELECT K_rawmaterials.name, K_rawmaterials.amount from K_rawmaterials"+
			" INNER JOIN K_recipes on K_recipes.rawmaterials_name = K_rawmaterials.name"+
			" WHERE K_recipes.cookies_cookieName = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, cookieName);
			ResultSet rs = ps.executeQuery();
			//Adds materials in sotorage
			while (rs.next()) {
				String ingredient = rs.getString("name"); // add ingredient and total in storage into map
				int amount = rs.getInt("amount");
				System.out.println(ingredient + " "+amount);
				ingredients.put(ingredient, amount); // Map with total amount of each ingredient
			}
			ps.close();
			//Selects amount needed for cooke
			sql = "SELECT rawmaterials_name, amount from K_recipes WHERE cookies_cookieName = ?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, cookieName);
			rs = ps.executeQuery();
			//materials - amount needed
			while(rs.next()) {
				String ingredient = rs.getString("rawmaterials_name");
				int amount = rs.getInt("amount");
				//Subtract amount to make cookies and update map
				int newAmount = ingredients.get(ingredient) - amount*54;
				System.out.println(ingredient + " "+newAmount);
				ingredients.put(ingredient, newAmount);
			}
			ps.close();
			//Update in SQL database
			for(Map.Entry<String, Integer> entry : ingredients.entrySet()) {
				sql = "UPDATE K_rawmaterials SET K_rawmaterials.amount  = ? WHERE K_rawmaterials.name = ?";
				ps = conn.prepareStatement(sql);
				ps.setInt(1, entry.getValue());
				ps.setString(2, entry.getKey());
				ps.executeUpdate();
				ps.close();
			}
			conn.commit();

		} catch(SQLException e) {
			try{
				conn.rollback();
			}catch(SQLException ex) {
				ex.printStackTrace();
			}
			e.printStackTrace();
		}
		try{
			conn.setAutoCommit(true);
		} catch(SQLException e) {
			e.printStackTrace();
		}

	}

}