/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.5.15
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
     * 
     * @deprecated
     *                 この共変戻り値となった {@code transpose()} は使用すべきでない. <br>
     *                 {@code Matrix.transpose()} か
     *                 {@link #transposeReadable()}
     *                 を使用すべき.
     * 
     *                 <p>
     *                 <i><u>
     *                 version 29 以降に削除され, {@code Matrix.transpose()}
     *                 が呼ばれるようになる.
     *                 </u></i>
     *                 </p>
     */
    @Deprecated(forRemoval = true, since = "28.9")
    @Override
    @SuppressWarnings("removal")
    public abstract BandMatrix transpose();

    /**
     * この行列の転置行列を, 帯行列の形で返す.
     * 
     * @implSpec
     *               線形継承されたサブインターフェースでのみ, 型精密化を認める. <br>
     *               その他は, {@link Matrix#transpose()} に従う.
     * 
     *               <p>
     *               <i><u>
     *               version 29 以降デフォルトメソッドが削除されるので,
     *               実装側は必ずオーバーライドすること.
     *               </u></i>
     *               </p>
     * 
     * @return 転置行列
     */
    @Override
    public default BandMatrix transposeReadable() {
        return transpose();
    }
}
