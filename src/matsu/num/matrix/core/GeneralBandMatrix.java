/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.12.11
 */
package matsu.num.matrix.core;

import java.util.Arrays;
import java.util.Objects;

import matsu.num.matrix.core.common.ArraysUtil;
import matsu.num.matrix.core.helper.value.BandDimensionPositionState;
import matsu.num.matrix.core.helper.value.MatrixRejectionConstant;
import matsu.num.matrix.core.validation.ElementsTooManyException;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;
import matsu.num.matrix.core.validation.MatrixStructureAcceptance;

/**
 * 一般的な (非対称な) 帯行列を扱う具象クラス.
 * 
 * <p>
 * このクラスのインスタンスはビルダ ({@link GeneralBandMatrix.Builder}) を用いて生成する.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 25.0
 */
public final class GeneralBandMatrix extends SkeletalAsymmetricMatrix<BandMatrix> implements BandMatrix {

    /*
     * 行列の各要素は対角成分, 狭義下三角成分, 狭義上三角成分に分けて, それぞれ1次元配列として扱う.
     * 次元を<i>n</i>, 下側帯幅を<i>b</i><sub>l</sub>,
     * 上側帯幅を<i>b</i><sub>u</sub>とすると,
     * 各配列の長さは<i>n</i>, <i>n</i><i>b</i><sub>l</sub>,
     * <i>n</i><i>b</i><sub>u</sub>である.
     * 
     * 例えば4*4行列で下側帯幅2, 上側帯幅4の場合:
     * 対角成分の配列を{@code d}, 狭義下三角成分の配列を{@code l}, 狭義上三角成分の配列を{@code u}とすると,
     * d.length = 4, l.length = 8, u.length = 16であり,
     * d[0] u[0] u[1] u[2] (u[3])}
     * l[0] d[1] u[4] u[5] (u[6] u[7])
     * l[1] l[2] d[2] u[8] (u[9] u[10] u[11])
     * ---- l[3] l[4] d[3] (u[12] u[13] u[14] u[15])
     * (--- ---- l[5] l[6])
     * (--- ---- ---- l[7])
     * と格納される.
     */
    private final BandMatrixDimension bandMatrixDimension;

    private final double[] diagonalEntry;
    private final double[] lowerEntry;
    private final double[] upperEntry;

    private double entryNormMax;

    /**
     * ビルダから呼ばれる.
     */
    private GeneralBandMatrix(Builder builder) {
        this.bandMatrixDimension = builder.bandMatrixDimension;
        this.diagonalEntry = builder.diagonalEntry;
        this.lowerEntry = builder.lowerEntry;
        this.upperEntry = builder.upperEntry;

        this.entryNormMax = this.calcEntryNormMax();
    }

    @Override
    public BandMatrixDimension bandMatrixDimension() {
        return this.bandMatrixDimension;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc }
     */
    @Override
    public double valueAt(final int row, final int column) {
        final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();
        final int thisUpperBandWidth = bandMatrixDimension.upperBandWidth();

        switch (BandDimensionPositionState.positionStateAt(row, column, this.bandMatrixDimension)) {
            case DIAGONAL:
                return diagonalEntry[row];
            case LOWER_BAND:
                return lowerEntry[column * thisLowerBandWidth + (row - column - 1)];
            case UPPER_BAND:
                return upperEntry[row * thisUpperBandWidth + (column - row - 1)];
            case OUT_OF_BAND:
                return 0;
            case OUT_OF_MATRIX:
                throw new IndexOutOfBoundsException(
                        String.format(
                                "行列内部でない:matrix:%s, (row, column)=(%s, %s)",
                                bandMatrixDimension.dimension(), row, column));
            default:
                throw new AssertionError("Bug: 列挙型に想定外の値");
        }
    }

    /**
     * -
     * 
     * <p>
     * (外部からの呼び出し不可)
     * </p>
     * 
     * @return -
     */
    @Override
    protected BandMatrix createTranspose() {
        return BandMatrix.createTransposedOf(this);
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     */
    @Override
    public Vector operate(Vector operand) {
        final var vectorDimension = operand.vectorDimension();
        if (!bandMatrixDimension.dimension().rightOperable(vectorDimension)) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "右から演算不可:matrix:%s, operand:%s",
                            bandMatrixDimension.dimension(), vectorDimension));
        }

        final int dimension = vectorDimension.intValue();
        final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();
        final int thisUpperBandWidth = bandMatrixDimension.upperBandWidth();

        final double[] operandEntry = operand.entry();
        final double[] resultEntry = new double[dimension];

        final double[] thisDiagonalEntry = this.diagonalEntry;
        final double[] thisLowerEntry = this.lowerEntry;
        final double[] thisUpperEntry = this.upperEntry;

        //対角成分
        for (int i = 0; i < dimension; i++) {
            resultEntry[i] += thisDiagonalEntry[i] * operandEntry[i];
        }
        //狭義下三角成分
        int in = -thisLowerBandWidth;
        for (int i = 0; i < dimension; i++) {
            final double oe_i = operandEntry[i];
            in += thisLowerBandWidth;
            for (int j = 0, l = Math.min(thisLowerBandWidth, dimension - i - 1); j < l; j++) {
                resultEntry[i + j + 1] += thisLowerEntry[in + j] * oe_i;
            }
        }
        //狭義上三角成分
        in = -thisUpperBandWidth;
        for (int i = 0; i < dimension; i++) {
            double sumProduct = 0;
            int k, l;
            in += thisUpperBandWidth;
            for (k = 0, l = Math.min(thisUpperBandWidth, dimension - i - 1); k < l - 3; k += 4) {
                final double v0 = thisUpperEntry[in + k] * operandEntry[i + k + 1];
                final double v1 = thisUpperEntry[in + k + 1] * operandEntry[i + k + 2];
                final double v2 = thisUpperEntry[in + k + 2] * operandEntry[i + k + 3];
                final double v3 = thisUpperEntry[in + k + 3] * operandEntry[i + k + 4];
                sumProduct += (v0 + v1) + (v2 + v3);
            }
            for (; k < l; k++) {
                final double v0 = thisUpperEntry[in + k] * operandEntry[i + k + 1];
                sumProduct += v0;
            }
            resultEntry[i] += sumProduct;
        }

        var builder = Vector.Builder.zeroBuilder(vectorDimension);
        builder.setEntryValue(resultEntry);
        return builder.build();
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     */
    @Override
    public Vector operateTranspose(Vector operand) {
        final var vectorDimension = operand.vectorDimension();
        if (!bandMatrixDimension.dimension().leftOperable(vectorDimension)) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "左から演算不可:matrix:%s, operand:%s",
                            bandMatrixDimension.dimension(), vectorDimension));
        }

        final int dimension = vectorDimension.intValue();
        final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();
        final int thisUpperBandWidth = bandMatrixDimension.upperBandWidth();

        final double[] operandEntry = operand.entry();
        final double[] resultEntry = new double[dimension];

        final double[] thisDiagonalEntry = this.diagonalEntry;
        final double[] thisLowerEntry = this.lowerEntry;
        final double[] thisUpperEntry = this.upperEntry;

        //対角成分
        for (int i = 0; i < dimension; i++) {
            resultEntry[i] += thisDiagonalEntry[i] * operandEntry[i];
        }
        //狭義上三角成分
        int in = -thisUpperBandWidth;
        for (int i = 0; i < dimension; i++) {
            final double oe_i = operandEntry[i];
            in += thisUpperBandWidth;
            for (int j = 0, l = Math.min(thisUpperBandWidth, dimension - i - 1); j < l; j++) {
                resultEntry[i + j + 1] += thisUpperEntry[in + j] * oe_i;
            }
        }
        //狭義下三角成分
        in = -thisLowerBandWidth;
        for (int i = 0; i < dimension; i++) {
            double sumProduct = 0;
            int k, l;
            in += thisLowerBandWidth;
            for (k = 0, l = Math.min(thisLowerBandWidth, dimension - i - 1); k < l - 3; k += 4) {
                final int in_p_k = in + k;
                final int i_p_k = i + k;
                final double v0 = thisLowerEntry[in_p_k] * operandEntry[i_p_k + 1];
                final double v1 = thisLowerEntry[in_p_k + 1] * operandEntry[i_p_k + 2];
                final double v2 = thisLowerEntry[in_p_k + 2] * operandEntry[i_p_k + 3];
                final double v3 = thisLowerEntry[in_p_k + 3] * operandEntry[i_p_k + 4];
                sumProduct += (v0 + v1) + (v2 + v3);
            }
            for (; k < l; k++) {
                final double v0 = thisLowerEntry[in + k] * operandEntry[i + k + 1];
                sumProduct += v0;
            }
            resultEntry[i] += sumProduct;
        }

        var builder = Vector.Builder.zeroBuilder(vectorDimension);
        builder.setEntryValue(resultEntry);
        return builder.build();
    }

    @Override
    public double entryNormMax() {
        return this.entryNormMax;
    }

    /**
     * 成分ごとのノルムの最大ノルムを計算する. <br>
     * 一度だけ呼ばれる.
     * 
     * @return ノルム
     */
    private double calcEntryNormMax() {
        return Math.max(
                ArraysUtil.normMax(diagonalEntry),
                Math.max(ArraysUtil.normMax(lowerEntry), ArraysUtil.normMax(upperEntry)));
    }

    /**
     * このインスタンスの文字列表現を返す.
     */
    @Override
    public String toString() {
        return String.format(
                "Matrix[band:%s, %s]",
                this.bandMatrixDimension(), EntryReadableMatrix.toSimplifiedEntryString(this));
    }

    /**
     * 正方形の帯行列を生成するビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
     * 
     * <p>
     * このビルダインスタンスを得るには,
     * {@link #zero(BandMatrixDimension)},
     * {@link #unit(BandMatrixDimension)}
     * をコールする. <br>
     * ビルダの生成時に有効要素数が大きすぎる場合は例外がスローされる. <br>
     * そのルールは {@link BandMatrixDimension#isAccepedForBandMatrix()}
     * に従う.
     * </p>
     * 
     * <p>
     * ビルド準備ができたビルダに対して {@link #build()} をコールすることで
     * {@link GeneralBandMatrix} をビルドする. <br>
     * {@link #build()} を実行したビルダは使用不能となる.
     * </p>
     * 
     * <p>
     * ビルダのコピーが必要な場合, {@link #copy()} をコールする. <br>
     * ただし, このコピーはビルド前しか実行できないことに注意.
     * </p>
     *
     */
    public static final class Builder {
        private final BandMatrixDimension bandMatrixDimension;

        private double[] diagonalEntry;
        private double[] lowerEntry;
        private double[] upperEntry;

        /**
         * 与えられた帯行列構造の帯行列ビルダを生成する. <br>
         * 初期値は零行列.
         *
         * @param bandMatrixDimension 帯行列構造
         * @throws IllegalArgumentException (サブクラス)受け入れ拒否の場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final BandMatrixDimension bandMatrixDimension) {
            this.bandMatrixDimension = Objects.requireNonNull(bandMatrixDimension);
            final int dimension = bandMatrixDimension.dimension().rowAsIntValue();
            final int lowerBandWidth = bandMatrixDimension.lowerBandWidth();
            final int upperBandWidth = bandMatrixDimension.upperBandWidth();

            var acceptance = accepts(bandMatrixDimension);
            if (acceptance.isReject()) {
                throw acceptance.getException(bandMatrixDimension);
            }

            diagonalEntry = new double[dimension];
            lowerEntry = new double[lowerBandWidth * dimension];
            upperEntry = new double[upperBandWidth * dimension];
        }

        /**
         * 与えられたソースから正方帯行列のビルダを作成する.
         *
         * @param src ソース
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final GeneralBandMatrix src) {
            this.bandMatrixDimension = src.bandMatrixDimension;
            this.diagonalEntry = src.diagonalEntry.clone();
            this.lowerEntry = src.lowerEntry.clone();
            this.upperEntry = src.upperEntry.clone();
        }

        /**
         * コピーコンストラクタ.
         */
        private Builder(final Builder src) {
            this.bandMatrixDimension = src.bandMatrixDimension;
            this.diagonalEntry = src.diagonalEntry.clone();
            this.lowerEntry = src.lowerEntry.clone();
            this.upperEntry = src.upperEntry.clone();
        }

        /**
         * (<i>i</i>, <i>j</i>) 要素を指定した値に置き換える.
         * 
         * <p>
         * 値が不正ならば, 正常値に修正される.
         * </p>
         *
         * @param row <i>i</i>, 行index
         * @param column <i>j</i>, 列index
         * @param value 置き換えた後の値
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (<i>i</i>, <i>j</i>) が行列の帯領域内でない場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public void setValue(final int row, final int column, double value) {
            this.throwISExIfCannotBeUsed();

            final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();
            final int thisUpperBandWidth = bandMatrixDimension.upperBandWidth();

            //値を修正する
            value = EntryReadableMatrix.modified(value);

            switch (BandDimensionPositionState.positionStateAt(row, column, this.bandMatrixDimension)) {
                case DIAGONAL:
                    diagonalEntry[row] = value;
                    return;
                case LOWER_BAND:
                    lowerEntry[column * thisLowerBandWidth + (row - column - 1)] = value;
                    return;
                case UPPER_BAND:
                    upperEntry[row * thisUpperBandWidth + (column - row - 1)] = value;
                    return;
                case OUT_OF_BAND:
                    throw new IndexOutOfBoundsException(
                            String.format(
                                    "帯の外側:matrix:%s, (row, column)=(%s, %s)",
                                    bandMatrixDimension, row, column));
                case OUT_OF_MATRIX:
                    throw new IndexOutOfBoundsException(
                            String.format(
                                    "行列内部でない:matrix:%s, (row, column)=(%s, %s)",
                                    bandMatrixDimension.dimension(), row, column));
                default:
                    throw new AssertionError("Bug: 列挙型に想定外の値");
            }
        }

        /**
         * このビルダが使用可能か (ビルド前かどうか) を判定する.
         * 
         * @return 使用可能なら {@code true}
         */
        public boolean canBeUsed() {
            return Objects.nonNull(this.diagonalEntry);
        }

        /**
         * ビルド前かを判定し, ビルド後なら例外をスロー.
         */
        private void throwISExIfCannotBeUsed() {
            if (!this.canBeUsed()) {
                throw new IllegalStateException("すでにビルドされています");
            }
        }

        /**
         * このビルダのコピーを生成して返す.
         * 
         * @return このビルダのコピー
         * @throws IllegalStateException すでにビルドされている場合
         */
        public Builder copy() {
            this.throwISExIfCannotBeUsed();

            return new Builder(this);
        }

        /**
         * ビルダを使用不能にする.
         */
        private void disable() {
            this.diagonalEntry = null;
            this.lowerEntry = null;
            this.upperEntry = null;
        }

        /**
         * 正方帯行列をビルドする.
         *
         * @return 正方帯行列
         * @throws IllegalStateException すでにビルドされている場合
         */
        public GeneralBandMatrix build() {
            this.throwISExIfCannotBeUsed();

            var out = new GeneralBandMatrix(this);
            this.disable();

            return out;
        }

        /**
         * 与えた帯行列構造がビルダ生成に受け入れられるかを判定する.
         * 
         * @param bandMatrixDimension 帯行列構造
         * @return ビルダ生成が受け入れられるならACCEPT
         * @throws NullPointerException 引数がnullの場合
         */
        public static MatrixStructureAcceptance accepts(BandMatrixDimension bandMatrixDimension) {
            if (!bandMatrixDimension.isAccepedForBandMatrix()) {
                return MatrixRejectionConstant.REJECTED_BY_TOO_MANY_ELEMENTS.get();
            }

            return MatrixStructureAcceptance.ACCEPTED;
        }

        /**
         * 与えられた帯構造を持つ, 零行列で初期化された正方帯行列ビルダを生成する.
         *
         * @param bandMatrixDimension 帯行列構造
         * @return 零行列で初期化されたビルダ
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder zero(final BandMatrixDimension bandMatrixDimension) {
            return new Builder(bandMatrixDimension);
        }

        /**
         * 与えられた帯構造を持つ, 単位行列で初期化された正方帯行列ビルダを生成する.
         *
         * @param bandMatrixDimension 帯行列構造
         * @return 単位行列で初期化されたビルダ
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder unit(final BandMatrixDimension bandMatrixDimension) {
            var outBuilder = new Builder(bandMatrixDimension);
            Arrays.fill(outBuilder.diagonalEntry, 1d);
            return outBuilder;
        }

        /**
         * 与えられたインスタンスの成分で初期化されたビルダを作成する.
         *
         * @param src 元行列
         * @return 元行列と等価なビルダ
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder from(final BandMatrix src) {
            final var bandMatrixDimension = src.bandMatrixDimension();
            if (src instanceof GeneralBandMatrix) {
                return new Builder((GeneralBandMatrix) src);
            }

            final int srcDimension = bandMatrixDimension.dimension().rowAsIntValue();
            final int srcLowerBandWidth = bandMatrixDimension.lowerBandWidth();
            final int srcUpperBandWidth = bandMatrixDimension.upperBandWidth();

            final var outBuilder = new Builder(bandMatrixDimension);

            //対角成分
            for (int i = 0; i < srcDimension; i++) {
                outBuilder.setValue(i, i, src.valueAt(i, i));
            }

            //下三角成分
            for (int i = 0; i < srcDimension; i++) {
                for (int j = 0, l = Math.min(srcLowerBandWidth, srcDimension - i - 1); j < l; j++) {
                    int r = j + i + 1;
                    int c = i;

                    outBuilder.setValue(r, c, src.valueAt(r, c));
                }
            }
            //上三角成分
            for (int i = 0; i < srcDimension; i++) {
                for (int j = 0, l = Math.min(srcUpperBandWidth, srcDimension - i - 1); j < l; j++) {
                    int r = i;
                    int c = j + i + 1;

                    outBuilder.setValue(r, c, src.valueAt(r, c));
                }
            }

            return outBuilder;
        }
    }
}
