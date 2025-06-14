/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.6.14
 */
package matsu.num.matrix.core;

import matsu.num.matrix.core.helper.matrix.transpose.TranspositionBandUtil;

/**
 * 正方形の帯行列を扱う.
 * 
 *
 * @implSpec
 *               <p>
 *               {@link Matrix}, {@link EntryReadableMatrix} の規約に従う.
 *               </p>
 * 
 *               <p>
 *               {@link Symmetric} インターフェースが付与される場合, 必ず対称帯構造でなければならない. <br>
 *               すなわち,
 *               {@code this.bandMatrixDimension().isSymmetric()}
 *               は {@code true} でなければならない.
 *               </p>
 *
 * @author Matsuura Y.
 */
public interface BandMatrix extends EntryReadableMatrix {

    /**
     * 行列の帯行列構造を取得する.
     *
     * @return 行列の帯行列構造
     */
    public BandMatrixDimension bandMatrixDimension();

    @Override
    public default MatrixDimension matrixDimension() {
        return this.bandMatrixDimension().dimension();
    }

    /**
     * @implSpec
     *               {@link Matrix#transpose()} に従う.
     */
    @Override
    public abstract BandMatrix transpose();

    /**
     * 与えられた帯行列の転置行列を生成する.
     * 
     * <p>
     * 引数 {@code original}, 戻り値 {@code returnValue} について,
     * {@code returnValue.transpose() == original} が {@code true} である.
     * <br>
     * {@code original} に {@link Symmetric} が付与されている場合,
     * {@code returnValue == original} が {@code true} である.
     * </p>
     * 
     * <p>
     * <u>
     * <i>
     * このメソッドは {@link #transpose()} や
     * {@link SkeletalAsymmetricMatrix#createTranspose()}
     * の実装を補助するために用意されている. <br>
     * {@link Matrix} およびそのサブタイプのインスタンスの転置行列を得る場合は,
     * このメソッドではなく, インスタンスメソッドである {@link #transpose()} を呼ばなければならない.
     * </i>
     * </u>
     * </p>
     *
     * @param original 元の行列
     * @return 転置行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static BandMatrix createTransposedOf(BandMatrix original) {
        return TranspositionBandUtil.apply(original);
    }
}
