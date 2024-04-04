/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.4.4
 */
package matsu.num.matrix.base;

/**
 * 正則な(もしくはそれに準ずる)行列の処理に関する基本定数に関する.
 * 
 * @author Matsuura Y.
 * @version 21.0
 */
public final class PseudoRegularMatrixProcess {

    /**
     * 行列の正則性を判定する相対epsilonのデフォルト値.
     */
    public static final double DEFAULT_EPSILON = 1E-15;

    private PseudoRegularMatrixProcess() {
        throw new AssertionError();
    }

}
