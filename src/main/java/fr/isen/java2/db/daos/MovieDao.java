package fr.isen.java2.db.daos;

import fr.isen.java2.db.entities.Genre;
import java.sql.Date;
import java.util.List;
import fr.isen.java2.db.entities.Movie;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

public class MovieDao {
    
        
	/**
	 * Retrieves all movies stored in the database.
	 *
	 * This method performs a SQL JOIN between the movie and genre tables
	 * in order to fully populate Movie objects with their associated Genre.
	 *
	 * Database columns that can accept null (such as release_date) are converted
	 * safely to Java types. 
	 *
	 * @return a list of all movies found in the database
	 */

	public List<Movie> listMovies() {
                List<Movie> listOfMovies = new ArrayList<>();
                try (Connection connection = DataSourceFactory.getConnection()) {
                    try (Statement statement = connection.createStatement()) {
                        try (ResultSet results = statement.executeQuery("SELECT * FROM movie JOIN genre ON movie.genre_id = genre.idgenre")) {
                            while (results.next()) {
                                Date sqlDate = results.getDate("release_date");
                                LocalDate releaseDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                                
                                Genre genre = new Genre(
                                results.getInt("idgenre"),
                                results.getString("name"));
            
                                
                                Movie movie = new Movie(results.getInt("idmovie"),
                                                        results.getString("title"),
                                                        releaseDate,
                                                        genre,
                                                        results.getInt("duration"),                                                                                                                
                                                        results.getString("director"),
                                                        results.getString("summary"));
                                listOfMovies.add(movie);
                            }
                        }
                    }
                    return listOfMovies;
                } catch (SQLException e) {
                    // Manage Exception
                    throw new RuntimeException("oups", e); 
                }
            }
        
        /**
	 * Retrieves all movies belonging to a given genre.
	 *
	 * This method uses a prepared statement to safely filter movies
	 * by genre name. The SQL JOIN ensures that Genre data is always
	 * available when constructing Movie objects.
	 *
	 * @param genreName the name of the genre used as filter
	 * @return a list of movies associated with the given genre
	 */

	public List<Movie> listMoviesByGenre(String genreName) {
		List<Movie> listOfMoviesByGenre = new ArrayList<>();
                try (Connection connection = DataSourceFactory.getConnection()) {
                    try (PreparedStatement statement = connection.prepareStatement(
                                "SELECT * FROM movie JOIN genre ON movie.genre_id = genre.idgenre WHERE genre.name = ?")) {
                        statement.setString(1, genreName);
                        try (ResultSet results = statement.executeQuery()) {
                            while (results.next()) {
                                Date sqlDate = results.getDate("release_date");
                                LocalDate releaseDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                                
                                Genre genre = new Genre(
                                results.getInt("idgenre"),
                                results.getString("name"));
            
                                
                                Movie movie = new Movie(results.getInt("idmovie"),
                                                        results.getString("title"),
                                                        releaseDate,
                                                        genre,
                                                        results.getInt("duration"),                                                                                                                
                                                        results.getString("director"),
                                                        results.getString("summary"));
                                listOfMoviesByGenre.add(movie);
                            }
                        }
                    }
                    return listOfMoviesByGenre;
                } catch (SQLException e) {
                    // Manage Exception
                    throw new RuntimeException("Failed to retrieve movies for genre: " + genreName, e); 
                }
	}
        
        
        /**
	 * Inserts a new movie into the database.
	 *
	 * This method handles fields that accept null explicitly before executing
	 * the SQL INSERT statement. Doing so avoids inserting invalid values
	 * and ensures consistency with the database schema.
	 *
	 * Once the movie is inserted, the generated identifier is retrieved
	 * and a new Movie instance is returned. 
	 *
	 * @param movie the Movie to be persisted
	 * @return a new Movie instance containing the generated identifier
	 */

	public Movie addMovie(Movie movie) {
		try (Connection connection = DataSourceFactory.getConnection()) {
                String sqlQuery = "INSERT INTO movie(title,release_date,genre_id,duration,director,summary) VALUES(?,?,?,?,?,?)";
                try (PreparedStatement statement = connection.prepareStatement(
                                sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, movie.getTitle());
                    
                    if (movie.getReleaseDate() != null) {
				statement.setDate(2, java.sql.Date.valueOf(movie.getReleaseDate()));
			} else {
				statement.setNull(2, java.sql.Types.DATE);
			}
                    
                    statement.setInt(3, movie.getGenre().getId());
                    
                    if (movie.getDuration() != null){
                        statement.setInt(4, movie.getDuration());
                    }  else {
                        statement.setNull(4, java.sql.Types.INTEGER);
                    }
                    
                    statement.setString(5, movie.getDirector());
                    
                    statement.setString(6, movie.getSummary());
                    
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
				if (keys.next()) {
					Integer id = keys.getInt(1);

					// return a NEW Movie with generated id
					return new Movie(
						id,
						movie.getTitle(),
						movie.getReleaseDate(),
						movie.getGenre(),
						movie.getDuration(),
						movie.getDirector(),
						movie.getSummary()
					);
				}
			}
		}

                } catch (SQLException e) {
                        throw new RuntimeException("Failed to insert movie into the database", e);
                }

                throw new RuntimeException("Movie insertion failed");
        }
}

