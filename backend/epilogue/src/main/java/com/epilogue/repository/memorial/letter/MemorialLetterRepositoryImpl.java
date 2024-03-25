package com.epilogue.repository.memorial.letter;

import com.epilogue.domain.memorial.MemorialLetter;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MemorialLetterRepositoryImpl implements MemorialLetterRepositoryCustom {

    @Autowired
    EntityManager entityManager;

    @Override
    public List<MemorialLetter> findAllByUserSeq(int userSeq) {
        return entityManager.createQuery("SELECT ml FROM MemorialLetter ml JOIN FETCH ml.memorial.user.userSeq = :userSeq", MemorialLetter.class)
                .setParameter("userSeq", userSeq)
                .getResultList();
    }
}
