package com.credit.engine.infrastructure.persistence.repository.settlement;

import com.credit.engine.infrastructure.persistence.entity.settlement.SettlementEntity;
import com.credit.engine.infrastructure.persistence.repository.settlement.projections.SettlementExtractProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SettlementRepository extends JpaRepository<SettlementEntity, UUID> {

    /** Apoiada pelo índice idx_settlement_assignor criado na migration V6. */
    List<SettlementEntity> findByAssignorId(UUID assignorId);

    /**
     * Extrato de liquidação para consulta analítica.
     * (indices em valuation_date e assignor_id + valuation_date, ver migration V7),
     * evitando o overhead de hidratar entidades JPA completas (com a coleção de settlement_item) para uma listagem somente leitura.
     */
    @Query(value = """
        SELECT
            s.id AS id,
            s.assignor_id AS assignorId,
            a.name AS assignorName,
            a.document_number AS assignorDocumentNumber,
            s.settlement_date_time AS settlementDateTime,
            s.valuation_date AS valuationDate,
            s.target_currency_id AS targetCurrencyCode,
            s.total_face_value AS totalFaceValue,
            s.total_discount_amount AS totalDiscountAmount,
            s.total_net_amount AS totalNetAmount
        FROM settlement s
        JOIN assignor a ON a.id = s.assignor_id
        WHERE (:assignorId IS NULL OR s.assignor_id = :assignorId)
          AND (:currencyCode IS NULL OR s.target_currency_id = :currencyCode)
          AND (:valuationDateFrom IS NULL OR s.valuation_date >= :valuationDateFrom)
          AND (:valuationDateTo IS NULL OR s.valuation_date <= :valuationDateTo)
        ORDER BY s.valuation_date DESC, s.settlement_date_time DESC
    """,
    countQuery = """
        SELECT count(*)
        FROM settlement s
        WHERE (:assignorId IS NULL OR s.assignor_id = :assignorId)
          AND (:currencyCode IS NULL OR s.target_currency_id = :currencyCode)
          AND (:valuationDateFrom IS NULL OR s.valuation_date >= :valuationDateFrom)
          AND (:valuationDateTo IS NULL OR s.valuation_date <= :valuationDateTo)
    """, nativeQuery = true
    )
    Page<SettlementExtractProjection> findExtract(
            @Param("assignorId") UUID assignorId,
            @Param("currencyCode") String currencyCode,
            @Param("valuationDateFrom") LocalDate valuationDateFrom,
            @Param("valuationDateTo") LocalDate valuationDateTo,
            Pageable pageable
    );

}
