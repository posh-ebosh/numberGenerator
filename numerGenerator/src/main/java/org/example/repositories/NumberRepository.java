package org.example.repositories;

import org.example.models.Number;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NumberRepository extends JpaRepository<Number, Long> {
    Number findFirstByOrderByIdDesc();
    Boolean existsByFullNumber(@Param("f") String fullNumber);
}
