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

/**
 * 一般的な矩形 (長方形) の零行列を表現する.
 * 
 * <p>
 * このインターフェースを実装した具象クラスのインスタンスは,
 * {@link ZeroMatrix#matrixOf(MatrixDimension)} メソッドにより得られる.
 * </p>
 * 
 * <p>
 * 正方な零行列の場合, それは対角行列である. <br>
 * 正方な零行列であることが確信できる場合,
 * {@link DiagonalMatrix} のサブタイプでもある {@link SquareZeroMatrix} を使用し,
 * {@link SquareZeroMatrix} 型で扱うべきである.
 * </p>
 * 
 * @implSpec
 *               このインターフェースは主に, 戻り値型を公開するために用意されており,
 *               モジュール外での実装は想定されていない. <br>
 *               モジュール外で実装する場合, 互換性が失われる場合がある.
 * 
 * @author Matsuura Y.
 */
public interface ZeroMatrix extends EntryReadableMatrix {

    /**
     * @implSpec
     *               {@link Matrix#transpose()} に従う.
     */
    @Override
    public abstract ZeroMatrix transpose();

    /**
     * 与えられた次元 (サイズ) の零行列を返す.
     *
     * @param matrixDimension 行列サイズ
     * @return 零行列
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static ZeroMatrix matrixOf(final MatrixDimension matrixDimension) {
        return new ZeroMatrixImpl(matrixDimension);
    }
}
