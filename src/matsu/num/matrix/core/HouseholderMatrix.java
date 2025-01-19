/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.1.19
 */
package matsu.num.matrix.core;

import matsu.num.matrix.core.helper.matrix.householder.HouseholderMatrixFactory;
import matsu.num.matrix.core.sealed.HouseholderMatrixSealed;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

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
 * 
 * <p>
 * Householder 行列の生成方法において,
 * 大きさが厳密に0のベクトルは生成のソースとして与えることができない. <br>
 * その判定を助ける {@link #accepts(Vector)} メソッドを提供している.
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
     * 引数がHouseholder行列の生成に使用できるかを判定する.
     * 
     * @param vector 判定対象
     * @return 使用できる場合は true
     * @throws NullPointerException 引数がnullの場合
     */
    public static boolean accepts(Vector vector) {
        return HouseholderMatrixFactory.accepts(vector);
    }

    /**
     * 鏡映変換の法線ベクトル (以下, 鏡映ベクトルと表記)
     * を指定して, Householder 行列を構築する.
     * 
     * <p>
     * 与えたベクトルが受け入れ可能かどうかは {@link #accepts(Vector)}
     * によって判定される. <br>
     * 内部で鏡映ベクトルの規格化を行うため,
     * 与える鏡映ベクトルは大きさが1である必要はない.
     * </p>
     * 
     * @param reflection 鏡映ベクトル
     * @return 鏡映ベクトルに対応した Householder 行列
     * @throws IllegalArgumentException ベクトルが accept されない場合
     * @throws NullPointerException 引数に null が含まれる場合
     */
    public static HouseholderMatrix from(Vector reflection) {
        return HouseholderMatrixFactory.createFrom(reflection);
    }

    /**
     * 与えられた source を target の定数倍に移すような Householder 変換行列を得る.
     * 
     * <p>
     * 与えたベクトルが受け入れ可能かどうかは {@link #accepts(Vector)}
     * によって判定される. <br>
     * 内部で鏡映ベクトルの規格化を行うため,
     * 与える鏡映ベクトルは大きさが1である必要はない.
     * </p>
     * 
     * @param source source
     * @param target target
     * @return source を target に移す Householder 行列
     * @throws MatrixFormatMismatchException 引数の次元が整合しない場合
     * @throws IllegalArgumentException それぞれのベクトルが accept されない場合
     * @throws NullPointerException 引数に null が含まれる場合
     */
    public static HouseholderMatrix from(Vector source, Vector target) {
        return HouseholderMatrixFactory.createFrom(source, target);
    }
}
