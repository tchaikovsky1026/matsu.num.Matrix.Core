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

import java.util.Optional;

import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * 対角行列を表現する.
 * 
 * <p>
 * 対角行列は,
 * 対角成分 (行index = 列index であるような成分)
 * のみが0でないような正方行列である.
 * </p>
 * 
 * <p>
 * この対角行列インターフェースは,
 * 帯幅0であるような帯行列 ({@link BandMatrix})
 * で表現され,
 * 対角行列特有の性質
 * ({@link Symmetric}, {@link Invertible}, {@link Determinantable})
 * が追加されている.
 * </p>
 * 
 * <p>
 * 対角成分を直接指定しての対角行列の生成は,
 * ビルダ ({@link matsu.num.matrix.core.DiagonalMatrix.Builder}) を経由して行う.
 * </p>
 * 
 * @implSpec
 *               このインターフェースは主に, 戻り値型を公開するために用意されており,
 *               モジュール外での実装は想定されていない. <br>
 *               モジュール外で実装する場合, 互換性が失われる場合がある.
 *
 * @author Matsuura Y.
 * @see <a href="https://en.wikipedia.org/wiki/Diagonal_matrix">
 *          Diagonal matrix</a>
 */
public interface DiagonalMatrix
        extends BandMatrix, Symmetric, Invertible, Determinantable {

    /**
     * @implSpec
     *               {@link Invertible#inverse()} に従う.
     */
    @Override
    public abstract Optional<? extends DiagonalMatrix> inverse();

    /**
     * @implSpec
     *               {@link Matrix#transpose()} に従う.
     */
    @Override
    public abstract DiagonalMatrix transpose();

    /**
     * 対角行列の実装を提供するビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
     * 
     * <p>
     * このビルダインスタンスを得るには,
     * {@link #zeroBuilder(MatrixDimension)},
     * {@link #unitBuilder(MatrixDimension)}
     * をコールする.
     * </p>
     * 
     * <p>
     * ビルド準備ができたビルダに対して {@link #build()} をコールすることで
     * {@link DiagonalMatrix} をビルドする. <br>
     * {@link #build()} を実行したビルダは使用不能となる.
     * </p>
     * 
     * <p>
     * ビルダのコピーが必要な場合, {@link #copy()} をコールする. <br>
     * ただし, このコピーはビルド前しか実行できないことに注意.
     * </p>
     * 
     * <p>
     * <u>
     * <i>
     * この抽象クラスは実装クラスを隠ぺいするためのものであり,
     * 外部で実装することはできない.
     * </i>
     * </u>
     * </p>
     */
    public static abstract class Builder {

        /*
         * インターフェースとして定義されているが,
         * 具象クラスは DiagonalMatrixImpl.Builderのみである.
         * staticメソッドの実装も, 具象クラス側に転送されている.
         */

        /**
         * パッケージ外に非公開のコンストラクタ.
         */
        Builder() {
            super();
        }

        /**
         * <p>
         * (<i>i</i>, <i>i</i>) 要素を指定した値に置き換える.
         * </p>
         * 
         * <p>
         * 値が不正ならば, 正常値に修正される.
         * </p>
         *
         * @param index <i>i</i>, 行, 列index
         * @param value 置き換えた後の値
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (<i>i</i>, <i>i</i>) が対角成分でない場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public abstract void setValue(final int index, double value);

        /**
         * このビルダが使用可能か (ビルド前かどうか) を判定する.
         * 
         * @return 使用可能なら {@code true}
         */
        public abstract boolean canBeUsed();

        /**
         * このビルダのコピーを生成して返す.
         * 
         * @return このビルダのコピー
         * @throws IllegalStateException すでにビルドされている場合
         */
        public abstract Builder copy();

        /**
         * 対角行列をビルドする.
         *
         * @return 対角行列
         * @throws IllegalStateException すでにビルドされている場合
         */
        public abstract DiagonalMatrix build();

        /**
         * 与えられた次元(サイズ)を持つ, 零行列で初期化された対角行列ビルダを生成する.
         *
         * @param matrixDimension 行列サイズ
         * @return 零行列で初期化されたビルダ
         * @throws MatrixFormatMismatchException 行列サイズが正方形でない場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder zeroBuilder(final MatrixDimension matrixDimension) {
            return DiagonalMatrixImpl.Builder.zeroBuilderImpl(matrixDimension);
        }

        /**
         * 与えられた次元(サイズ)を持つ, 単位行列で初期化された対角行列ビルダを生成する.
         *
         * @param matrixDimension 行列サイズ
         * @return 単位行列で初期化されたビルダ
         * @throws MatrixFormatMismatchException 行列サイズが正方形でない場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder unitBuilder(final MatrixDimension matrixDimension) {
            return DiagonalMatrixImpl.Builder.unitBuilderImpl(matrixDimension);
        }
    }
}
