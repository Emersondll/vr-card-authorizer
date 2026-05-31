package resources.fixtures;

import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vr.miniauthorizer.document.Card;
import com.vr.miniauthorizer.model.TransactionModel;

/**
 * Shared test data factory for mini-authorizer unit and integration tests.
 *
 * <p>Centralizes test constants and fixture creation methods to ensure consistency
 * across all test classes and avoid duplication of magic values.</p>
 *
 * <p>This class cannot be instantiated — all members are static.</p>
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 */
public final class TestFixture {

    /** Card number used across all test scenarios. */
    public static final String CARD_NUMBER = "1234567890";

    /** Plain-text card password used in test requests. */
    public static final String CARD_PASSWORD = "41feds4316sd*$@dfs!";

    /** SHA-256 Base64 hash of {@link #CARD_PASSWORD} — pre-computed for verification tests. */
    public static final String CARD_PASSWORD_HASH = "MEGfTPMhBPRFStXbZHAUT0jFoAWrsA35l/XHgqfPU3E=";

    /** Standard initial card balance (matches {@code values.standardValue} in application.yml). */
    public static final String CARD_AMOUNT = "500.00";

    /** Amount exceeding the standard balance — used to trigger {@code BalanceException}. */
    public static final String CARD_AMOUNT_HIGH = "500000.00";

    private TestFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Creates a {@link Card} test fixture with the standard card number, password, and balance.
     *
     * @return a fully initialized {@link Card} document for test scenarios
     */
    public static Card createCard() {
        Card card = new Card();
        card.setPassword(CARD_PASSWORD);
        card.setCardNumber(CARD_NUMBER);
        card.setAmount(new BigDecimal(CARD_AMOUNT));
        return card;
    }

    /**
     * Serializes a {@link TransactionModel} to a JSON string using Jackson.
     *
     * @param transaction the transaction model to serialize, must be non-null
     * @return JSON string representation of the transaction
     * @throws JsonProcessingException if serialization fails
     */
    public static String writeJson(final TransactionModel transaction) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(transaction);
    }
}
