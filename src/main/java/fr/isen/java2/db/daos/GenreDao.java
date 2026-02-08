package fr.isen.java2.db.daos;

import java.util.List;
import java.util.Optional;

import fr.isen.java2.db.entities.Genre;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class GenreDao {
        
	public List<Genre> listGenres() {
            List<Genre> listOfGenres = new ArrayList<>();
            try (Connection connection = DataSourceFactory.getConnection()) {
                try (Statement statement = connection.createStatement()) {
                    try (ResultSet results = statement.executeQuery("SELECT * FROM genre")) {
                        while (results.next()) {
                            Genre genre = new Genre(results.getInt("idgenre"),
                                                    results.getString("name")
                            );
                            listOfGenres.add(genre);
                        }
                    }
                }
                return listOfGenres;
            } catch (SQLException e) {
                // Manage Exception
        throw new RuntimeException("oups", e); 
    }
	}
        
        
      /**
        * This method previously returned null when a genre was not found.
        * Returning null can easily lead to NullPointerException issues
        * when the caller forgets to perform a null check.
        *
        * The method now returns an Optional to explicitly represent
        * the absence of a value and force the caller to handle this case
        * in a safe and explicit way.
        * @param name
        * @return 
        */


	public Optional<Genre> getGenre(String name) {
                try (Connection connection = DataSourceFactory.getConnection()) {
                    try (PreparedStatement statement = connection.prepareStatement(
                                "SELECT * FROM genre WHERE name = ?")) {
                        statement.setString(1, name);
                        try (ResultSet results = statement.executeQuery()) {
                            if (results.next()) {
                                Genre genre = new Genre(results.getInt("idgenre"),
                                                        results.getString("name")
                                );
                                return Optional.of(genre);
                        }
                    }
                }   
            }       catch (SQLException e) {
                        // Manage Exception
                        throw new RuntimeException("Error while fetching genre", e); 
                }
            return Optional.empty();
	}

	public void addGenre(String name) {
		try (Connection connection = DataSourceFactory.getConnection()) {
                String sqlQuery = "INSERT INTO genre(name) VALUES(?)";
                try (PreparedStatement statement = connection.prepareStatement(
                                sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, name);
                    statement.executeUpdate();
                }
            }catch (SQLException e) {
                // Manage Exception
                throw new RuntimeException("oups", e); 
            }
	}
}
