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
 * 置換行列を表現する.
 * 
 * <p>
 * 置換行列は,
 * 各行 &middot; 各列にちょうど1個だけ1を持ち,
 * それ以外が全て0であるような正方行列であり,
 * 直交行列である. <br>
 * 置換行列の転置行列もまた置換行列である. <br>
 * 置換行列を縦ベクトルに左から作用させると,
 * 成分を並び変えた縦ベクトルが得られる.
 * </p>
 * 
 * <p>
 * 置換行列の行列式は1または-1である. <br>
 * 偶置換のときに1であり, 奇置換のときに-1となる. <br>
 * 偶奇は {@link #isEven()} により判定可能である.
 * </p>
 * 
 * <p>
 * このインターフェースの実装クラスのインスタンスは,
 * ビルダ ({@link PermutationMatrix.Builder}) を用いて生成する.
 * </p>
 * 
 * @implSpec
 *               このインターフェースは主に, 戻り値型を公開するために用意されており,
 *               モジュール外での実装は想定されていない. <br>
 *               モジュール外で実装する場合, 互換性が失われる場合がある.
 *
 * @author Matsuura Y.
 * @see <a href="https://en.wikipedia.org/wiki/Permutation_matrix">
 *          Permutation matrix</a>
 */
public interface PermutationMatrix
        extends EntryReadableMatrix, OrthogonalMatrix, Determinantable {

    /**
     * 置換の偶奇を取得する.
     *
     * @return 偶置換のときtrue
     */
    public abstract boolean isEven();

    /**
     * @implSpec
     *               {@link OrthogonalMatrix#transpose()} に従う.
     */
    @Override
    public abstract PermutationMatrix transpose();

    /**
     * @implSpec
     *               {@link OrthogonalMatrix#inverse()} に従う.
     */
    @Override
    public abstract Optional<? extends PermutationMatrix> inverse();

    /**
     * 置換行列の実装を提供するビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
     * 
     * <p>
     * このビルダインスタンスを得るには,
     * {@link #unitBuilder(MatrixDimension)}
     * をコールする.
     * </p>
     * 
     * <p>
     * ビルド準備ができたビルダに対して {@link #build()} をコールすることで
     * {@link PermutationMatrix} をビルドする. <br>
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

        /**
         * 外部に公開されないコンストラクタ.
         */
        Builder() {
            super();
        }

        /**
         * 第 <i>i</i> 行と第 <i>j</i> 行を交換した行列を返す.
         *
         * @param row1 i, 行index1
         * @param row2 j, 行index2
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException i, jが行列の内部でない場合
         */
        public abstract void swapRows(final int row1, final int row2);

        /**
         * 第 <i>i</i> 列と第 <i>j</i> 列を交換した行列を返す.
         *
         * @param column1 i, 列index1
         * @param column2 j, 列index2
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException i, jが行列の内部でない場合
         */
        public abstract void swapColumns(final int column1, final int column2);

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
         * 置換行列をビルドする.
         *
         * @return 置換行列
         * @throws IllegalStateException すでにビルドされている場合
         */
        public abstract PermutationMatrix build();

        /**
         * 与えられた次元(サイズ)を持つ, 単位行列で初期化された置換行列ビルダを生成する.
         *
         * @param matrixDimension 行列サイズ
         * @return 単位行列で初期化されたビルダ
         * @throws MatrixFormatMismatchException 行列サイズが正方形でない場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder unitBuilder(final MatrixDimension matrixDimension) {
            return PermutationMatrixImpl.Builder.unitBuilderImpl(matrixDimension);
        }
    }
}
