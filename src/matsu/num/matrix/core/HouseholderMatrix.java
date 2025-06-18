/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.6.14
 */
package matsu.num.matrix.core;

import java.util.Optional;

import matsu.num.matrix.core.helper.matrix.householder.HouseholderMatrixFactory;
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
 * 法線ベクトルを直接指定する形でのインスタンスは,
 * {@link HouseholderMatrix#from(Vector)} メソッドにより得られる. <br>
 * <b>x</b>, <b>y</b> を指定して <b>x</b> を <b>y</b> に移すような Householder 行列を得るには,
 * {@link #from(Vector, Vector)} メソッドを使用する.
 * </p>
 * 
 * <p>
 * {@link #from(Vector)}, {@link #from(Vector, Vector)} メソッドにおいては,
 * 大きさが厳密に0のベクトルは生成のソースとして与えることができない. <br>
 * その判定を助ける {@link #accepts(Vector)} メソッドを提供している.
 * </p>
 * 
 * @implSpec
 *               このインターフェースは主に, 戻り値型を公開するために用意されており,
 *               モジュール外での実装は想定されていない. <br>
 *               モジュール外で実装する場合, 互換性が失われる場合がある.
 * 
 * @author Matsuura Y.
 */
public interface HouseholderMatrix
        extends OrthogonalMatrix, Determinantable, Symmetric {

    /**
     * @implSpec
     *               {@link OrthogonalMatrix#transpose()} に従う.
     */
    @Override
    public abstract HouseholderMatrix transpose();

    /**
     * @implSpec
     *               {@link OrthogonalMatrix#inverse()} に従う.
     */
    @Override
    public abstract Optional<? extends HouseholderMatrix> inverse();

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
     * <p>
     * 引数が Householder 行列の生成に使用できるかを判定する. <br>
     * 使用できるための条件は, 次をすべて満たすことである.
     * </p>
     * 
     * <ul>
     * <li>ベクトルのノルム (大きさ) が厳密に正である. <br>
     * すなわち, 成分のいずれかが {@code 0d}, {@code -0d} でない.
     * </li>
     * </ul>
     * 
     * @param vector 判定対象
     * @return 使用できる場合は true
     * @throws NullPointerException 引数がnullの場合
     */
    public static boolean accepts(Vector vector) {
        return HouseholderMatrixFactory.accepts(vector);
    }

    /**
     * 鏡映変換の法線ベクトルを指定して, Householder 行列を構築する.
     * (以下, このベクトルを鏡映ベクトルと表記する.)
     * 
     * <p>
     * 与えたベクトルが受け入れ可能かどうかは {@link #accepts(Vector)}
     * によって判定される. <br>
     * 大きさが1である必要はない.
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
     * 与えられた source を target の定数倍に移すような Householder 行列を得る.
     * 
     * <p>
     * source を <b>x</b>, target を <b>y</b> とする. <br>
     * <b>x</b>, <b>y</b> が1次元である場合を除き,
     * 返される Householder 行列 H には,
     * <b>y</b> = <i>c</i>H<b>x</b> を満たす <i>c</i> が正数となるようなものが選ばれる.
     * </p>
     * 
     * <p>
     * 与える2個のベクトルの次元は等しくなければならない. <br>
     * それぞれのベクトルが受け入れ可能かどうかは {@link #accepts(Vector)}
     * によって判定される. <br>
     * 大きさが1である必要はない.
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
