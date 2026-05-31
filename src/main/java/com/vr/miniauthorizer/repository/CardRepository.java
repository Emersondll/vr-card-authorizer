package com.vr.miniauthorizer.repository;

import com.vr.miniauthorizer.document.Card;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * MongoDB repository for {@link Card} document persistence operations.
 *
 * <p>Extends {@link MongoRepository} to inherit standard CRUD operations.
 * The primary key is the card number ({@code String}).</p>
 *
 * <p>Spring Data MongoDB automatically implements all CRUD methods at runtime.
 * No additional implementation class is needed.</p>
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 * @see Card for document structure
 */
@Repository
public interface CardRepository extends MongoRepository<Card, String> {
}
