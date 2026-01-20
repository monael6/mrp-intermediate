package org.example.tests;

import org.example.db.Database;
import org.example.domain.Media;
import org.example.domain.Rating;
import org.example.domain.User;
import org.example.persistence.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepositoryTests {

    @BeforeAll
    public static void setup() {
        Database.init();
    }

    private void clearDb() throws Exception {
        try (Connection conn = Database.connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(
                    "TRUNCATE TABLE users, media, ratings, favorites, rating_likes, media_genres RESTART IDENTITY CASCADE");
        }
    }

    private void updateUserGenre(int userId, String genre) throws Exception {
        try (Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement("UPDATE users SET favorite_genre = ? WHERE id = ?")) {
            stmt.setString(1, genre);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    @Test
    @Order(1)
    public void testCleanup() throws Exception {
        clearDb();
        assertTrue(true);
    }

    // ==========================================
    // USER TESTS
    // ==========================================

    @Test
    @Order(2)
    public void testUserRegister() throws Exception {
        User u = new User();
        u.username = "testuser";
        u.password = "secret";

        UserRepository.save(u); // returns void

        User saved = UserRepository.findByUsername("testuser");
        assertNotNull(saved);
        assertTrue(saved.id > 0);
    }

    @Test
    @Order(3)
    public void testFindUserByUsername() throws Exception {
        // Relies on user created in Order(2)
        User u = UserRepository.findByUsername("testuser");
        assertNotNull(u);
        assertEquals("testuser", u.username);
    }

    @Test
    @Order(4)
    public void testUserRegisterDuplicate() {
        User u = new User();
        u.username = "testuser"; // Same as above
        u.password = "123";

        assertThrows(Exception.class, () -> {
            UserRepository.save(u);
        });
    }

    // ==========================================
    // MEDIA TESTS
    // ==========================================

    @Test
    @Order(5)
    public void testCreateMedia() throws Exception {
        User creator = UserRepository.findByUsername("testuser");

        Media m = new Media();
        m.title = "Inception";
        m.description = "Dreams";
        m.mediaType = "movie";
        m.releaseYear = 2010;
        m.ageRestriction = 12;
        m.creatorId = creator.id;

        int id = MediaRepository.save(m);
        assertTrue(id > 0);
    }

    @Test
    @Order(6)
    public void testMediaWithGenres() throws Exception {
        User creator = UserRepository.findByUsername("testuser");

        Media m = new Media();
        m.title = "The Witcher";
        m.mediaType = "series";
        m.releaseYear = 2019;
        m.ageRestriction = 18;
        m.creatorId = creator.id;
        m.genres = new ArrayList<>();
        m.genres.add("Fantasy");
        m.genres.add("Action");

        int id = MediaRepository.save(m);
        Media fetched = MediaRepository.findById(id);

        assertNotNull(fetched);
        assertEquals(2, fetched.genres.size());
        assertTrue(fetched.genres.contains("Fantasy"));
    }

    @Test
    @Order(7)
    public void testUpdateMedia() throws Exception {
        ArrayList<Media> all = MediaRepository.findAll();
        Media m = null;
        // Find Inception explicitly to avoid changing Witcher
        for (Media media : all) {
            if ("Inception".equals(media.title)) {
                m = media;
                break;
            }
        }
        // Fallback if Inception not found (should not happen if Order 5 ran)
        if (m == null && !all.isEmpty()) {
            m = all.get(0);
        }
        assertNotNull(m, "No media found to update");

        m.title = "Updated Title";
        MediaRepository.update(m);

        Media fetched = MediaRepository.findById(m.id);
        assertEquals("Updated Title", fetched.title);
    }

    @Test
    @Order(8)
    public void testSearchMediaByTitle() throws Exception {
        ArrayList<Media> results = MediaRepository.search("Witcher", null, null, null, null, null, null);
        assertTrue(results.size() > 0);
        assertTrue(results.get(0).title.contains("Witcher"));
    }

    @Test
    @Order(9)
    public void testSearchMediaByGenre() throws Exception {
        ArrayList<Media> results = MediaRepository.search(null, "Fantasy", null, null, null, null, null);
        assertTrue(results.size() > 0);
    }

    @Test
    @Order(10)
    public void testDeleteMedia() throws Exception {
        // Create dummy to delete
        User creator = UserRepository.findByUsername("testuser");
        Media m = new Media();
        m.title = "ToDelete";
        m.mediaType = "movie";
        m.creatorId = creator.id;
        int id = MediaRepository.save(m);

        MediaRepository.delete(id);
        assertNull(MediaRepository.findById(id));
    }

    // ==========================================
    // RATING TESTS
    // ==========================================

    @Test
    @Order(11)
    public void testAddRating() throws Exception {
        User u = UserRepository.findByUsername("testuser");
        ArrayList<Media> media = MediaRepository.findAll();
        Media m = media.get(0); // Any media

        RatingRepository.upsert(m.id, u.id, 5, "Awesome");

        // Verify existence
        ArrayList<Rating> ratings = RatingRepository.findByMediaIdPublic(m.id);

        boolean found = ratings.stream().anyMatch(r -> r.userId == u.id && r.stars == 5);
        assertTrue(found, "Rating should be found via media ID");
    }

    @Test
    @Order(12)
    public void testScoreCalculation() throws Exception {
        User u2 = new User();
        u2.username = "critic";
        u2.password = "pw";
        UserRepository.save(u2);
        u2 = UserRepository.findByUsername("critic"); // get ID

        ArrayList<Media> media = MediaRepository.findAll();
        Media m = media.get(0);

        // Add second rating
        RatingRepository.upsert(m.id, u2.id, 1, "Bad");

        // Refetch media to check score
        Media refreshed = MediaRepository.findById(m.id);
        assertEquals(3.0, refreshed.score, 0.01);
    }

    @Test
    @Order(13)
    public void testUpdateRating() throws Exception {
        User u = UserRepository.findByUsername("testuser");
        ArrayList<Media> media = MediaRepository.findAll();
        Media m = media.get(0);

        // Find rating id first
        int ratingId = -1;
        for (Rating r : RatingRepository.findByMediaIdPublic(m.id)) {
            if (r.userId == u.id)
                ratingId = r.id;
        }
        assertTrue(ratingId > 0);

        RatingRepository.updateById(ratingId, u.id, 4, "Updated Comment");

        // Verify
        boolean found = false;
        for (Rating r : RatingRepository.findByMediaIdPublic(m.id)) {
            if (r.id == ratingId && r.stars == 4)
                found = true;
        }
        assertTrue(found);
    }

    @Test
    @Order(14)
    public void testListUserRatings() throws Exception {
        User u = UserRepository.findByUsername("testuser");
        // UserRatingsRepository exists and has listUserRatings
        var list = UserRatingsRepository.listUserRatings(u.id);
        assertTrue(list.size() > 0);
    }

    // ==========================================
    // FAVORITE TESTS
    // ==========================================

    @Test
    @Order(15)
    public void testAddFavorite() throws Exception {
        User u = UserRepository.findByUsername("testuser");
        ArrayList<Media> media = MediaRepository.findAll();
        Media m = media.get(0);

        FavoriteRepository.add(m.id, u.id);

        // Verify via list
        ArrayList<Media> favs = FavoriteRepository.listFavorites(u.id);
        assertTrue(favs.stream().anyMatch(fx -> fx.id == m.id));
    }

    @Test
    @Order(16)
    public void testRemoveFavorite() throws Exception {
        User u = UserRepository.findByUsername("testuser");
        ArrayList<Media> media = MediaRepository.findAll();
        Media m = media.get(0);

        FavoriteRepository.remove(m.id, u.id);

        ArrayList<Media> favs = FavoriteRepository.listFavorites(u.id);
        assertFalse(favs.stream().anyMatch(fx -> fx.id == m.id));
    }

    // ==========================================
    // LIKE TESTS
    // ==========================================

    @Test
    @Order(17)
    public void testLikeRating() throws Exception {
        User u = UserRepository.findByUsername("critic"); // User 2
        User rater = UserRepository.findByUsername("testuser"); // User 1
        ArrayList<Media> media = MediaRepository.findAll();
        Media m = media.get(0);

        int ratingId = -1;
        for (Rating r : RatingRepository.findByMediaIdPublic(m.id)) {
            if (r.userId == rater.id)
                ratingId = r.id;
        }
        assertTrue(ratingId > 0);

        LikeRepository.like(ratingId, u.id);

        // Assert no error (void return)
        assertTrue(true);
    }

    @Test
    @Order(18)
    public void testUnlikeRating() throws Exception {
        User u = UserRepository.findByUsername("critic");
        User rater = UserRepository.findByUsername("testuser");
        ArrayList<Media> media = MediaRepository.findAll();
        Media m = media.get(0);

        int ratingId = -1;
        for (Rating r : RatingRepository.findByMediaIdPublic(m.id)) {
            if (r.userId == rater.id)
                ratingId = r.id;
        }

        LikeRepository.unlike(ratingId, u.id);
        assertTrue(true);
    }

    // ==========================================
    // RECOMMENDATION TESTS
    // ==========================================

    @Test
    @Order(19)
    public void testRecommendationByGenre() throws Exception {
        User u = UserRepository.findByUsername("testuser");

        // Manually set genre
        updateUserGenre(u.id, "Action");

        // Ensure we have 'Action' media
        // (Created in test 6: The Witcher -> Action)

        ArrayList<Media> recs = RecommendationRepository.byGenre(u.id, 10);
        assertTrue(recs.size() > 0);
        assertTrue(recs.stream().anyMatch(x -> x.genres.contains("Action")));
    }

    @Test
    @Order(20)
    public void testRecommendationByContent() throws Exception {
        User u = UserRepository.findByUsername("testuser");
        // This relies on having a "Top 1" rating.
        // testuser rated media[0] (Inception?) with 4 stars (updated in test 13).

        ArrayList<Media> recs = RecommendationRepository.byContent(u.id, 10);
        assertNotNull(recs);
    }
}
