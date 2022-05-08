package krusty;

import spark.Request;
import spark.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

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
		String sql = "SELECT cookies_cookieName, rawmaterials_name, amount, unit FROM K_recipes";
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

	public String getPallets(Request req, Response res) throws SQLException {
		String sql = "SELECT palletId AS id, cookies_cookieName AS cookie, production_date, address AS customer, blocked FROM K_pallets";
		String title = "pallets";
		StringBuilder sb = new StringBuilder();
	
		sb.append(sql);
	
		List<String> filerList = Arrays.asList("from", "to", "cookie", "blocked");
		HashMap<String, String> map = new HashMap<String, String>();

		for (String filter : filerList) {
			if (req.queryParams(filter) != null) {
				map.put(filter, req.queryParams(filter));
			}
		}

		if (map.size() > 0) {
			sb.append(" WHERE");
		}
	
		int size = 1;

		for (Map.Entry<String, String> entry : map.entrySet()) {
			switch (entry.getKey()) {
				case "from":
					sb.append(" production_date >= ?");
					break;
				case "to":
					sb.append(" production_date <= ?");
					break;
				case "blocked":
					sb.append(" blocked = ?");
					break;
				case "cookie":
					sb.append(" cookies_cookieName = ?");
					break;
				default:
					break;
			}
			if (map.size() > size) {
				size++;
				sb.append(" AND");
			}
		}
	
		PreparedStatement stmt = conn.prepareStatement(sb.toString());
	
		int i = 1;
	
		for (Map.Entry<String, String> entry : map.entrySet()) {
			switch (entry.getKey()) {
				case "from":
					stmt.setDate(i, Date.valueOf(req.queryParams("from")));
					break;
				case "to":
					stmt.setDate(i, Date.valueOf(req.queryParams("to")));
					break;
				case "blocked":
					stmt.setString(i, req.queryParams("blocked"));
					break;
				case "cookie":
					stmt.setString(i, req.queryParams("cookie"));
					break;
				default:
					break;
			}
			i++;
		}
	
		ResultSet rs = stmt.executeQuery();
	
		String result = Jsonizer.toJson(rs, title);
	
		return result;
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

		int palletID = -1;
		String sqlPallet = "INSERT INTO K_pallets (production_date, cookies_cookieName, blocked, address) VALUES (?, ?, 'no', ?)";
		String sqlOrder = "INSERT INTO K_orders (pallets_PalletID, customers_name) VALUES (?, ?)";
		String sqlOrderSpec = "INSERT INTO K_order_spec (amount, cookies_cookieName, orders_orderID) VALUES (1, ?, ?)";

		List<String> customerList = Arrays.asList("Småbröd AB", "Kaffebröd AB", "Bjudkakor AB", "Kalaskakor AB", "Partykakor AB", "Gästkakor AB", "Skånekakor AB", "Finkakor AB");
		Random r = new Random();
		
		int randomitem = r.nextInt(8);
		String randomCustomer = customerList.get(randomitem);


		if(cookie_exists(cookieName)) {
			try  {
				String addressquery = "SELECT address FROM K_customers WHERE name = ?";
				PreparedStatement psAq = conn.prepareStatement(addressquery);
				psAq.setString(1, randomCustomer);
				ResultSet rsAq = psAq.executeQuery();
				String addressCustomer = "test";
				if (rsAq.next()) {
					addressCustomer = rsAq.getString("address");
				}
				

				PreparedStatement ps = conn.prepareStatement(sqlPallet, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, time);
				ps.setString(2, cookieName);
				ps.setString(3, addressCustomer);
				boolean result = ps.executeUpdate() != 0;
				ResultSet rs = ps.getGeneratedKeys();

				if(rs.next()) {
					palletID = rs.getInt(1);
				}
				
				if(result) {
				subtract_ingredient(cookieName);	
				}
				
				PreparedStatement ps1 = conn.prepareStatement(sqlOrder);
				ps1.setInt(1, palletID);
				ps1.setString(2, randomCustomer);
				ps1.executeUpdate();

				PreparedStatement ps2 = conn.prepareStatement(sqlOrderSpec);
				ps2.setString(1, cookieName);
				ps2.setInt(2, palletID);
				ps2.executeUpdate();
 
			} catch(SQLException e) {
				e.printStackTrace();
				return Jsonizer.anythingToJson("error", "status");
				}

				return "{\n\t\"status\": \"ok\" ," +
				"\n\t\"id\": " + palletID + "\n}";
		}
		return "{\n\t\"status\": \"unknown cookie\"\n}";
	}

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

	private void subtract_ingredient(String cookieName) {
		HashMap<String, Integer> ingredients = new HashMap<String, Integer>();

		try{
			conn.setAutoCommit(false);

			String sql = "SELECT K_rawmaterials.name, K_rawmaterials.amount from K_rawmaterials"+
			" INNER JOIN K_recipes on K_recipes.rawmaterials_name = K_rawmaterials.name"+
			" WHERE K_recipes.cookies_cookieName = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, cookieName);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String ingredient = rs.getString("name"); 
				int amount = rs.getInt("amount");
				ingredients.put(ingredient, amount); 
			}
			ps.close();

			sql = "SELECT rawmaterials_name, amount from K_recipes WHERE cookies_cookieName = ?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, cookieName);
			rs = ps.executeQuery();
			while(rs.next()) {
				String ingredient = rs.getString("rawmaterials_name");
				int amount = rs.getInt("amount");
				int newAmount = ingredients.get(ingredient) - amount*54;
				ingredients.put(ingredient, newAmount);
			}
			ps.close();
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