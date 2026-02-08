package fr.isen.java2.db.daos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.isen.java2.db.entities.Genre;
import java.util.Optional;

public class GenreDaoTestCase {

	private final GenreDao genreDao = new GenreDao();

	@BeforeEach
	public void initDatabase() throws Exception {
		try (Connection connection = DataSourceFactory.getConnection()){
                    try (Statement stmt = connection.createStatement()){
                        stmt.executeUpdate(
                                        "CREATE TABLE IF NOT EXISTS genre (idgenre INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , name VARCHAR(50) NOT NULL);");
                        stmt.executeUpdate("DELETE FROM genre");
                        stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='genre'");
                        stmt.executeUpdate("INSERT INTO genre(idgenre,name) VALUES (1,'Drama')");
                        stmt.executeUpdate("INSERT INTO genre(idgenre,name) VALUES (2,'Comedy')");
                        stmt.executeUpdate("INSERT INTO genre(idgenre,name) VALUES (3,'Thriller')");
                    }
                }
	}

	@Test
	public void shouldListGenres() {
		// WHEN
		List<Genre> genres = genreDao.listGenres();
		// THEN
		assertThat(genres).hasSize(3);
		assertThat(genres).extracting("id", "name").containsOnly(tuple(1, "Drama"), tuple(2, "Comedy"),
				tuple(3, "Thriller"));
	}
	
        
        
        /**
            * Integration tests for {@link GenreDao}.
            *
            * The shouldGetGenreByName and shouldNotGetUnknownGenre() tests validate the transition from returning {@code null}
            * to returning {@link java.util.Optional} in the {@code getGenre} method.
            * This change makes the absence of a Genre explicit and prevents
            * NullPointerException risks in client code.
            *
            * The test cases verify when a genre is successfully found  and the expected
            * behavior when a Genre is not found.
            */
	@Test
	public void shouldGetGenreByName() {
		// WHEN
		Optional<Genre> genre = genreDao.getGenre("Comedy");
		// THEN
		assertThat(genre).isPresent();
                assertThat(genre.get().getId()).isEqualTo(2);
                assertThat(genre.get().getName()).isEqualTo("Comedy");
	}
	
	@Test
	public void shouldNotGetUnknownGenre() {
		// WHEN
		Optional<Genre> genre = genreDao.getGenre("Unknown");
		// THEN
		assertThat(genre).isEmpty();
	}
	
	@Test
	public void shouldAddGenre() throws Exception {
		// WHEN 
		genreDao.addGenre("Western");
		// THEN
		try (Connection connection = DataSourceFactory.getConnection()){
                    try (Statement statement = connection.createStatement()){
                        try (ResultSet resultSet = statement.executeQuery("SELECT * FROM genre WHERE name='Western'")){
                            assertThat(resultSet.next()).isTrue();
                            assertThat(resultSet.getInt("idgenre")).isNotNull();
                            assertThat(resultSet.getString("name")).isEqualTo("Western");
                            assertThat(resultSet.next()).isFalse();
                        }
                    }
                }
	}
}
