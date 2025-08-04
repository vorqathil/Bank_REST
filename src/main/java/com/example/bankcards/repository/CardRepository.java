package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card,Long> {
    boolean existsByCardNumber(String cardNumber);

    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findByValidityPeriodBeforeAndStatusNot(LocalDateTime date, Status status);

    Page<Card> findAllByUserAndCardNumberContainingIgnoreCase(User user, String cardNumber, Pageable pageable);

    Page<Card> findAllByUser(User user, Pageable pageable);
}
