package org.example.tests;

import org.example.domain.Media;
import org.example.persistence.MediaRepository;
import org.junit.jupiter.api.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MediaRepositoryTest {

    @Test
    @Order(1)
    public void testSaveMedia() throws Exception {
        Media m = new Media();
        m.title = "TestMovie";
        m.description = "Desc";
        m.mediaType = "movie";
        m.releaseYear = 2020;
        m.ageRestriction = 12;

        MediaRepository.save(m);

        ArrayList<Media> list = MediaRepository.findAll();
        assertTrue(list.stream().anyMatch(x -> x.title.equals("TestMovie")));
    }

    @Test
    @Order(2)
    public void testFindAll() throws Exception {
        ArrayList<Media> list = MediaRepository.findAll();
        assertNotNull(list);
        assertTrue(list.size() > 0);
    }



    @Order(3)
    public void testDeleteMedia() throws Exception {
        MediaRepository.delete(1);

        Media deleted = MediaRepository.findById(1);

        assertNull(deleted);
    }
}
