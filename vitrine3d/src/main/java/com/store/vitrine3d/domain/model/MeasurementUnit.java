package com.store.vitrine3d.domain.model;

import com.store.vitrine3d.rest.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Unidades de medida dos materiais de consumo. Cada unidade pertence a uma dimensão e
 * carrega seu fator em relação à unidade base daquela dimensão (grama, mililitro, unidade).
 *
 * O estoque é guardado na unidade do próprio material (stockUnit), não na unidade base:
 * um material medido em KG guarda "3.5" no banco, e não "3500". A conversão só acontece
 * quando a entrada ou saída vem em unidade diferente da do material.
 */
public enum MeasurementUnit {

    UN(Dimension.COUNT,  BigDecimal.ONE),
    G (Dimension.MASS,   BigDecimal.ONE),
    KG(Dimension.MASS,   BigDecimal.valueOf(1000)),
    ML(Dimension.VOLUME, BigDecimal.ONE),
    L (Dimension.VOLUME, BigDecimal.valueOf(1000));

    /** Casas decimais do estoque — 1 g em KG vira 0.001, então 3 é o mínimo viável. */
    private static final int SCALE = 3;

    public enum Dimension { COUNT, MASS, VOLUME }

    private final Dimension dimension;
    private final BigDecimal factorToBase;

    MeasurementUnit(Dimension dimension, BigDecimal factorToBase) {
        this.dimension = dimension;
        this.factorToBase = factorToBase;
    }

    public Dimension getDimension() {
        return dimension;
    }

    /**
     * Converte uma quantidade desta unidade para a unidade alvo.
     * Unidades de dimensões diferentes não se convertem (não existe kg de litro).
     */
    public BigDecimal convertTo(BigDecimal quantity, MeasurementUnit target) {
        if (dimension != target.dimension) {
            throw new BusinessRuleException("INCOMPATIBLE_UNIT",
                    "Não é possível converter " + this + " (" + dimension + ") para "
                            + target + " (" + target.dimension + ")");
        }
        if (this == target) {
            return quantity.setScale(SCALE, RoundingMode.HALF_UP);
        }
        return quantity.multiply(factorToBase)
                .divide(target.factorToBase, SCALE, RoundingMode.HALF_UP);
    }
}
