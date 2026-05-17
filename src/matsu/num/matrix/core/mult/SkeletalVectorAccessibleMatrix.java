/*
 * Copyright © 2026 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.5.14
 */
package matsu.num.matrix.core.mult;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.Vector;

/**
 * 行ベクトル, 列ベクトルを取得可能な行列を表現するための骨格クラス.
 * 
 * <p>
 * <u><i>
 * パッケージ外には非公開としたい.
 * </i></u>
 * </p>
 * 
 * @author Matsuura Y.
 */
abstract class SkeletalVectorAccessibleMatrix implements EntryReadableMatrix {

    /**
     * 与えられた行番号の行ベクトルを取得する.
     * 
     * @param index index
     * @return rowVector
     * @throws IndexOutOfBoundsException indexが範囲外
     */
    public abstract Vector rowVectorAt(int index);

    /**
     * 与えられた列番号の列ベクトルを取得する.
     * 
     * @param index index
     * @return columnVector
     * @throws IndexOutOfBoundsException indexが範囲外
     */
    public abstract Vector columnVectorAt(int index);

    @Override
    @SuppressWarnings("removal")
    public abstract EntryReadableMatrix transpose();

    @Override
    public final EntryReadableMatrix transposeReadable() {
        return transpose();
    }

    /**
     * このインスタンスの文字列説明表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code Matrix[dim:%dimension, vector-accessible, %entry]}
     * </p>
     * 
     * @implSpec
     *               継承先においてオーバーライドを許可する. <br>
     *               {@code Matrix["param":%param]} や
     *               {@code Matrix["param"=%param]} の形が適切であると思われる.
     */
    @Override
    public String toString() {
        return "Matrix[dim: %s, vector-accessible , %s]"
                .formatted(
                        this.matrixDimension(),
                        EntryReadableMatrix.toSimplifiedEntryString(this));
    }
}
