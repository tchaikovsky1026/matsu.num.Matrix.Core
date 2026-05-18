/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.5.17
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
     * この行列の転置行列を, 帯行列の形で返す.
     * 
     * @implSpec
     *               線形継承されたサブインターフェースでのみ, 型精密化を認める. <br>
     *               その他は, {@link Matrix#transpose()} に従う.
     * 
     * @return 転置行列
     */
    @Override
    public abstract BandMatrix transposeReadable();
}
