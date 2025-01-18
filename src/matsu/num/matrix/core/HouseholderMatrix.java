/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.1.18
 */
package matsu.num.matrix.core;

import matsu.num.matrix.core.helper.matrix.householder.HouseholderMatrixFactory;
import matsu.num.matrix.core.helper.matrix.householder.HouseholderMatrixSealed;

/**
 * Householder 行列を扱う.
 * 
 * <p>
 * Householder 行列とは, 鏡映変換の法線ベクトル <b>u</b> (大きさ1)について, <br>
 * H = I - 2<b>u</b><b>u</b><sup>T</sup> <br>
 * で得られる直交行列 H である. <br>
 * H は対称行列であり, 固有値は (-1, 1, 1, ... ), det H = -1 である.
 * </p>
 * 
 * <p>
 * このクラスのインスタンスは,
 * {@link HouseholderMatrix#from(Vector)} メソッドにより得られる.
 * </p>
 * 
 * @author Matsuura Y.
 */
public sealed interface HouseholderMatrix
        extends OrthogonalMatrix, Determinantable, Symmetric
        permits HouseholderMatrixSealed {

    /**
     * @return {@code -1d}
     */
    @Override
    public default double determinant() {
        return -1d;
    }

    /**
     * @return {@code 0d}
     */
    @Override
    public default double logAbsDeterminant() {
        return 0d;
    }

    /**
     * @return {@code -1}
     */
    @Override
    public default int signOfDeterminant() {
        return -1;
    }

    /**
     * 鏡映変換の法線ベクトル (以下, 鏡映ベクトルと表記)
     * を指定して, Householder 行列を構築する.
     * 
     * <p>
     * 内部で鏡映ベクトルの規格化を行うため,
     * 与える鏡映ベクトルは大きさが1である必要はない. <br>
     * ただし, 大きさが厳密に0,
     * すなわち全成分が厳密に {@code 0d}, {@code -0d} であってはならず, その場合は例外がスローされる.
     * </p>
     * 
     * @param reflection 鏡映ベクトル
     * @return 鏡映ベクトルに対応した Householder 行列
     * @throws IllegalArgumentException 鏡映ベクトルのノルムが0の場合
     * @throws NullPointerException 引数に null が含まれる場合
     */
    public static HouseholderMatrix from(Vector reflection) {
        return HouseholderMatrixFactory.createFrom(reflection);
    }
}
