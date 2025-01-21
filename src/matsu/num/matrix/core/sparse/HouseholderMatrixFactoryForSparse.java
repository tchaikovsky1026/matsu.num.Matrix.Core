/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.1.21
 */
package matsu.num.matrix.core.sparse;

import matsu.num.matrix.core.HouseholderMatrix;

/**
 * <p>
 * スパースなベクトル ({@link SparseVector}) から Householder 行列を生成するユーティリティクラス.
 * </p>
 * 
 * @author Matsuura Y.
 */
public final class HouseholderMatrixFactoryForSparse {

    private HouseholderMatrixFactoryForSparse() {
        //インスタンス化不可
        throw new AssertionError();
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
    public static boolean accepts(SparseVector vector) {
        return HouseholderMatrixWithSparseVector.accepts(vector);
    }

    /**
     * <p>
     * 鏡映変換の法線ベクトルを指定して, Householder 行列を構築する.
     * (以下, このベクトルを鏡映ベクトルと表記する.)
     * </p>
     * 
     * <p>
     * 与えたベクトルが受け入れ可能かどうかは {@link #accepts(SparseVector)}
     * によって判定される. <br>
     * 大きさが1である必要はない.
     * </p>
     * 
     * @param reflection 鏡映ベクトル
     * @return 鏡映ベクトルに対応した Householder 行列
     * @throws IllegalArgumentException ベクトルが accept されない場合
     * @throws NullPointerException 引数に null が含まれる場合
     */
    public static HouseholderMatrix from(SparseVector reflection) {
        return new HouseholderMatrixWithSparseVector(reflection);
    }
}
