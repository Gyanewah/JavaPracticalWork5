package fr.isen.java2.db.daos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSourceFactory {

	private static final String DB_URL = "jdbc:sqlite:sqlite.db";

	private DataSourceFactory() {
		throw new IllegalStateException("This is a static class that should not be instantiated");
	}

	/**
	 * Provides a JDBC connection to the configured database.
	 * The JDBC driver is selected automatically using the connection URL.
	 *
	 * @return an open {@link Connection} to the database
	 * @throws SQLException if the connection cannot be created
	 */
	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL);
	}
}
