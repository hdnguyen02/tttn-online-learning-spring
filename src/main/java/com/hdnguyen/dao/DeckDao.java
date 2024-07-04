package com.hdnguyen.dao;

import com.hdnguyen.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeckDao extends JpaRepository<Deck, Long>, PagingAndSortingRepository<Deck, Long> {

    List<Deck> findByUserEmail(String email);
    Optional<Deck> findFirstByIdAndUserEmail(Long idDeck, String emailUser);

    // viết 1 hàm chỉ lấy ra với public = true

    @Query("SELECT d FROM Deck d WHERE d.isPublic = true")
    List<Deck> getGlobal();

    @Query("SELECT d FROM Deck d WHERE d.user.email = ?1 AND CONCAT(d.name, ' ', d.description) LIKE %?2%")
    List<Deck> search(String emailUser, String searchTerm);


    @Query("SELECT d FROM Deck d WHERE CONCAT(d.name, ' ', d.description) LIKE %?1%")
    List<Deck> searchGlobal(String searchTerm);


}