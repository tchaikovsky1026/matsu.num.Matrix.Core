/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.5.9
 */
package matsu.num.matrix.core;

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
}
