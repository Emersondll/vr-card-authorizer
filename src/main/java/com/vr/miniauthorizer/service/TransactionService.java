package com.vr.miniauthorizer.service;

import com.vr.miniauthorizer.exception.BalanceException;
import com.vr.miniauthorizer.exception.CardException;
import com.vr.miniauthorizer.exception.PasswordException;
import com.vr.miniauthorizer.model.TransactionModel;

/**
 * Service contract for card debit transaction processing.
 *
 * <p>Defines the authorization flow for debit transactions against pre-paid cards.
 * Implementations validate card existence, PIN correctness, and sufficient balance
 * before committing the debit.</p>
 *
 * <p>All implementations of this interface are expected to be Spring-managed
 * beans annotated with {@code @Service}.</p>
 *
 * @author Emerson Lima
 * @version 1.0
 * @since 1.0.0
 * @see com.vr.miniauthorizer.service.impl.TransactionServiceImpl for the default implementation
 */
public interface TransactionService {

    /**
     * Authorizes and executes a debit transaction against a pre-paid card.
     *
     * <p>Authorization rules applied in order:</p>
     * <ol>
     *   <li>Card must exist in the repository</li>
     *   <li>Provided password must match the stored hash</li>
     *   <li>Card balance must be greater than or equal to the transaction amount</li>
     * </ol>
     *
     * <p>If all rules pass, the card balance is decremented atomically within a
     * {@code @Transactional} boundary.</p>
     *
     * @param transactionModel the transaction details: card number, plain-text password,
     *                         and transaction amount; must be non-null
     * @throws CardException.CardNotFoundException if no card with the given number exists
     *                                             (returns {@code "CARTAO_INEXISTENTE"})
     * @throws PasswordException                   if the provided password does not match
     *                                             the stored hash (returns {@code "SENHA_INVALIDA"})
     * @throws BalanceException                    if the card balance is insufficient
     *                                             (returns {@code "SALDO_INSUFICIENTE"})
     */
    void performTransaction(TransactionModel transactionModel);
}
