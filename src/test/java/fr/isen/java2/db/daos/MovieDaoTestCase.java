package fr.isen.java2.db.daos;

import fr.isen.java2.db.entities.Genre;
import fr.isen.java2.db.entities.Movie;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class MovieDaoTestCase {

	private final MovieDao movieDao = new MovieDao();

	/**
	 * Initializes the database before each test.
	 *
	 * This method creates the required tables if they do not exist,
	 * clears existing data to ensure test isolation, resets auto-increment
	 * counters, and inserts a controlled data used by the test cases.
         * @throws java.lang.Exception
	 */
	@BeforeEach
	public void initDb() throws Exception {
		try (Connection connection = DataSourceFactory.getConnection()) {
			try (Statement stmt = connection.createStatement()) {
				stmt.executeUpdate(
						"CREATE TABLE IF NOT EXISTS genre (idgenre INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , name VARCHAR(50) NOT NULL);");
				stmt.executeUpdate("DELETE FROM movie");
				stmt.executeUpdate("DELETE FROM genre");
				stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='movie'");
				stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='genre'");
				stmt.executeUpdate("INSERT INTO genre(idgenre,name) VALUES (1,'Drama')");
				stmt.executeUpdate("INSERT INTO genre(idgenre,name) VALUES (2,'Comedy')");
				stmt.executeUpdate(
						"INSERT INTO movie(idmovie,title, release_date, genre_id, duration, director, summary) "
								+ "VALUES (1, 'Title 1', '2015-11-26 12:00:00.000', 1, 120, 'director 1', 'summary of the first movie')");
				stmt.executeUpdate(
						"INSERT INTO movie(idmovie,title, release_date, genre_id, duration, director, summary) "
								+ "VALUES (2, 'My Title 2', '2015-11-14 12:00:00.000', 2, 114, 'director 2', 'summary of the second movie')");
				stmt.executeUpdate(
						"INSERT INTO movie(idmovie,title, release_date, genre_id, duration, director, summary) "
								+ "VALUES (3, 'Third title', '2015-12-12 12:00:00.000', 2, 176, 'director 3', 'summary of the third movie')");
			}
		}
	}

	/**
	 * Verifies that all movies are correctly retrieved from the database.
	 *
	 * This test validates the SQL JOIN between movie and genre tables and
	 * ensures that Movie objects are fully populated with their associated
	 * Genre data.
	 */
	@Test
	public void shouldListMovies() {
		// WHEN
		List<Movie> movies = movieDao.listMovies();

		// THEN
		assertThat(movies).hasSize(3);

		assertThat(movies).extracting(
				"id",
				"title",
				"genre.name",
				"duration",
				"director"
		).containsOnly(
				tuple(1, "Title 1", "Drama", 120, "director 1"),
				tuple(2, "My Title 2", "Comedy", 114, "director 2"),
				tuple(3, "Third title", "Comedy", 176, "director 3")
		);
	}

	/**
	 * Verifies that movies can be filtered by genre name.
	 *
	 * This test ensures that the DAO correctly applies a WHERE clause
	 * on the genre name and that all returned movies belong to the
	 * requested genre.
	 */
	@Test
	public void shouldListMoviesByGenre() {
		// WHEN
		List<Movie> movies = movieDao.listMoviesByGenre("Comedy");

		// THEN
		assertThat(movies).hasSize(2);

		assertThat(movies).extracting("title")
				.containsOnly("My Title 2", "Third title");

		assertThat(movies)
				.allMatch(movie -> movie.getGenre().getName().equals("Comedy"));
	}

	/**
	 * Verifies that a new movie can be inserted into the database.
	 *
	 * This test checks that:
	 * - the movie is persisted correctly,
	 * - a database-generated identifier is assigned,
	 * - the returned Movie instance contains this generated identifier,
	 * - and the database state matches the returned object.
         * 
         * @throws java.lang.Exception
	 */
	@Test
	public void shouldAddMovie() throws Exception {

		// GIVEN
		Genre drama = new Genre(1, "Drama");

		Movie movieToAdd = new Movie(
				null,
				"New Movie",
				LocalDate.of(2020, 1, 15),
				drama,
				95,
				"New Director",
				"A brand new movie"
		);

		// WHEN
		Movie addedMovie = movieDao.addMovie(movieToAdd);

		// THEN
		assertThat(addedMovie.getId()).isNotNull();
		assertThat(addedMovie.getTitle()).isEqualTo("New Movie");
		assertThat(addedMovie.getGenre().getName()).isEqualTo("Drama");

		// AND verify database state
		try (Connection connection = DataSourceFactory.getConnection()) {
			try (Statement statement = connection.createStatement()) {
				try (ResultSet rs = statement.executeQuery(
						"SELECT * FROM movie WHERE title='New Movie'"
				)) {

					assertThat(rs.next()).isTrue();
					assertThat(rs.getInt("idmovie")).isEqualTo(addedMovie.getId());
					assertThat(rs.getString("director")).isEqualTo("New Director");
					assertThat(rs.next()).isFalse();
				}
			}
		}
	}
}
