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
 * Signature matrix (符号行列) を表す.
 * 
 * <p>
 * Signature matrix とは, 対角成分が1または-1の対角行列である. <br>
 * よって, 対称行列かつ直交行列である. <br>
 * 対角成分の-1の個数が奇数のときは行列式は-1であり,
 * 対角成分の-1の個数が偶数のときは行列式は1である. <br>
 * 対角成分の-1の個数の偶奇は {@link #isEven()} により判定可能である.
 * </p>
 * 
 * <p>
 * 対角成分を直接指定、あるいは逐次的に符号を反転しての符号行列の生成には,
 * ビルダ ({@link SignatureMatrix.Builder}) を用いて行う.
 * </p>
 * 
 * @implSpec
 *               このインターフェースは主に, 戻り値型を公開するために用意されており,
 *               モジュール外での実装は想定されていない. <br>
 *               モジュール外で実装する場合, 互換性が失われる場合がある.
 * 
 * @author Matsuura Y.
 */
public interface SignatureMatrix
        extends DiagonalMatrix, OrthogonalMatrix {

    /**
     * @implSpec
     *               {@link OrthogonalMatrix#transpose()} に従う.
     */
    @Override
    public abstract SignatureMatrix transpose();

    /**
     * @implSpec
     *               {@link OrthogonalMatrix#inverse()} に従う.
     */
    @Override
    public abstract Optional<? extends SignatureMatrix> inverse();

    /**
     * Signature matrix の対角成分に並ぶ-1の個数の偶奇を取得する.
     *
     * @return 偶数個のときtrue
     */
    public abstract boolean isEven();

    /**
     * {@link SignatureMatrix} を生成するためのビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
     * 
     * <p>
     * このビルダインスタンスを得るには,
     * {@link #unit(MatrixDimension)}
     * をコールする.
     * </p>
     * 
     * <p>
     * ビルド準備ができたビルダに対して {@link #build()} をコールすることで
     * {@link SignatureMatrix} をビルドする. <br>
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
         * 符号行列の対角成分 (<i>i</i>, <i>i</i>) を1に書き換える.
         * 
         * @param index <i>i</i>, 対角成分のindex
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (<i>i</i>, <i>i</i>) が行列内部でない場合
         */
        public abstract void setPositiveAt(int index);

        /**
         * 符号行列の対角成分 (<i>i</i>, <i>i</i>) を-1に書き換える.
         * 
         * @param index <i>i</i>, 対角成分のindex
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (<i>i</i>, <i>i</i>) が行列内部でない場合
         */
        public abstract void setNegativeAt(int index);

        /**
         * 符号行列の対角成分 (<i>i</i>, <i>i</i>) を反転させる.
         * 
         * @param index <i>i</i>, 対角成分のindex
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (<i>i</i>, <i>i</i>) が行列内部でない場合
         */
        public abstract void reverseAt(int index);

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
         * 符号行列をビルドする.
         *
         * @return 符号行列
         * @throws IllegalStateException すでにビルドされている場合
         */
        public abstract SignatureMatrix build();

        /**
         * 与えられた次元(サイズ)の符号行列ビルダを生成する. <br>
         * 初期値は単位行列.
         *
         * @param matrixDimension 行列サイズ
         * @return 新しいビルダ
         * @throws MatrixFormatMismatchException 行列サイズが正方形でない場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder unit(MatrixDimension matrixDimension) {
            return SignatureMatrixImpl.Builder.unitImpl(matrixDimension);
        }
    }
}
