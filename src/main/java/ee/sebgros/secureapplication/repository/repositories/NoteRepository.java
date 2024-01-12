package ee.sebgros.secureapplication.repository.repositories;

import ee.sebgros.secureapplication.repository.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
}
