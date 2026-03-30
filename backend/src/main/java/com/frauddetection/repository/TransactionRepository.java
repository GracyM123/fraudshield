package com.frauddetection.repository;

import com.frauddetection.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findTop50ByOrderByCreatedAtDesc();

    List<Transaction> findByFlaggedTrueOrderByCreatedAtDesc();

    long countByFlaggedTrue();

    long countByCreatedAtAfter(Instant since);

    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.accountId = :accountId AND t.createdAt > :since")
    Double findAvgAmountByAccountSince(@Param("accountId") String accountId, @Param("since") Instant since);

    @Query("SELECT STDDEV(t.amount) FROM Transaction t WHERE t.accountId = :accountId AND t.createdAt > :since")
    Double findStdDevAmountByAccountSince(@Param("accountId") String accountId, @Param("since") Instant since);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.accountId = :accountId AND t.createdAt > :since")
    long countByAccountSince(@Param("accountId") String accountId, @Param("since") Instant since);

    @Query("SELECT AVG(t.amount) FROM Transaction t")
    Double findGlobalAvgAmount();

    @Query("SELECT STDDEV(t.amount) FROM Transaction t")
    Double findGlobalStdDevAmount();

    @Query("SELECT t.amount FROM Transaction t WHERE t.accountId = :accountId ORDER BY t.createdAt DESC")
    List<BigDecimal> findAmountsByAccount(@Param("accountId") String accountId);

    @Query(value = """
        SELECT DATE_TRUNC('hour', created_at) as hour,
               COUNT(*) as total,
               COUNT(CASE WHEN is_flagged THEN 1 END) as flagged,
               AVG(amount) as avg_amount
        FROM transactions
        WHERE created_at > NOW() - INTERVAL '24 hours'
        GROUP BY DATE_TRUNC('hour', created_at)
        ORDER BY hour
        """, nativeQuery = true)
    List<Object[]> findHourlyStats();
}
