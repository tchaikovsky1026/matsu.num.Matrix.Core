/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.1.20
 */
package matsu.num.matrix.core;

import java.util.Arrays;
import java.util.Objects;

import matsu.num.matrix.core.common.ArraysUtil;
import matsu.num.matrix.core.helper.value.BandDimensionPositionState;
import matsu.num.matrix.core.helper.value.MatrixRejectionConstant;
import matsu.num.matrix.core.validation.ElementsTooManyException;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;
import matsu.num.matrix.core.validation.MatrixNotSymmetricException;
import matsu.num.matrix.core.validation.MatrixStructureAcceptance;

/**
 * 一般的な対称帯行列を扱う具象クラス.
 * 
 * <p>
 * このクラスのインスタンスはビルダ ({@link SymmetricBandMatrix.Builder}) を用いて生成する.
 * </p>
 * 
 * @author Matsuura Y.
 */
public final class SymmetricBandMatrix extends SkeletalSymmetricMatrix<SymmetricBandMatrix>
        implements BandMatrix, Symmetric {
    /*
     * 行列の各要素は対角成分, 副対角成分に分けて, それぞれ1次元配列として扱う.
     * 次元を<i>n</i>, 片側帯幅を<i>b</i>とすると, 各配列の長さは<i>n</i>, <i>n</i><i>b</i>である.
     * 
     * 例えば4*4行列で片側帯幅2の場合:
     * 対角成分の配列をd, 副対角成分の配列をbとすると,
     * d.length = 4, b.length = 8であり,
     * d[0] b[0] b[1] ----
     * b[0] d[1] b[2] b[3]
     * b[1] b[2] d[2] b[4] (b[5])
     * ---- b[3] b[4] d[3] (b[6] b[7])
     * (--- ---- b[5] b[6])
     * (--- ---- ---- b[7])
     * と格納される.
     */
    private final BandMatrixDimension bandMatrixDimension;

    private final double[] diagonalEntry;
    //下三角成分を保存、上三角にコピー
    private final double[] bandEntry;

    private final double entryNormMax;

    /**
     * ビルダから呼ばれる.
     */
    private SymmetricBandMatrix(final Builder builder) {
        this.bandMatrixDimension = builder.bandMatrixDimension;
        this.diagonalEntry = builder.diagonalEntry;
        this.bandEntry = builder.bandEntry;

        this.entryNormMax = this.calcEntryNormMax();
    }

    @Override
    public BandMatrixDimension bandMatrixDimension() {
        return this.bandMatrixDimension;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public double valueAt(final int row, final int column) {
        final int thisBandWidth = bandMatrixDimension.lowerBandWidth();

        switch (BandDimensionPositionState.positionStateAt(row, column, this.bandMatrixDimension)) {
            case DIAGONAL:
                return diagonalEntry[row];
            case LOWER_BAND:
                return bandEntry[column * thisBandWidth + (row - column - 1)];
            case UPPER_BAND:
                return bandEntry[row * thisBandWidth + (column - row - 1)];
            case OUT_OF_BAND:
                return 0;
            case OUT_OF_MATRIX:
                throw new IndexOutOfBoundsException(
                        String.format(
                                "out of matrix: matrix: %s, (row, column) = (%s, %s)",
                                bandMatrixDimension.dimension(), row, column));
            default:
                throw new AssertionError("Bug");
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
    protected SymmetricBandMatrix self() {
        return this;
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Vector operate(Vector operand) {
        final var vectorDimension = operand.vectorDimension();
        if (!bandMatrixDimension.dimension().rightOperable(vectorDimension)) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "undefined operation: matrix: %s, operand: %s",
                            bandMatrixDimension.dimension(), vectorDimension));
        }
        final int dimension = vectorDimension.intValue();
        final int thisBandWidth = bandMatrixDimension.lowerBandWidth();

        final double[] operandEntry = operand.entryAsArray();

        final double[] resultEntry = new double[dimension];

        final double[] thisDiagonalEntry = this.diagonalEntry;
        final double[] thisBandEntry = this.bandEntry;

        //対角成分
        for (int i = 0; i < dimension; i++) {
            resultEntry[i] += thisDiagonalEntry[i] * operandEntry[i];
        }
        //狭義下三角成分
        int in = -thisBandWidth;
        for (int i = 0; i < dimension; i++) {
            final double oe_i = operandEntry[i];
            in += thisBandWidth;
            for (int j = 0, l = Math.min(thisBandWidth, dimension - i - 1); j < l; j++) {
                resultEntry[i + j + 1] += thisBandEntry[in + j] * oe_i;
            }
        }
        //狭義上三角成分
        in = -thisBandWidth;
        for (int i = 0; i < dimension; i++) {
            double sumProduct = 0;
            int k, l;
            in += thisBandWidth;
            for (k = 0, l = Math.min(thisBandWidth, dimension - i - 1); k < l - 3; k += 4) {
                final double v0 = thisBandEntry[in + k] * operandEntry[i + k + 1];
                final double v1 = thisBandEntry[in + k + 1] * operandEntry[i + k + 2];
                final double v2 = thisBandEntry[in + k + 2] * operandEntry[i + k + 3];
                final double v3 = thisBandEntry[in + k + 3] * operandEntry[i + k + 4];
                sumProduct += (v0 + v1) + (v2 + v3);
            }
            for (; k < l; k++) {
                final double v0 = thisBandEntry[in + k] * operandEntry[i + k + 1];
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
        return Math.max(ArraysUtil.normMax(diagonalEntry), ArraysUtil.normMax(bandEntry));
    }

    /**
     * このインスタンスの文字列表現を返す.
     */
    @Override
    public String toString() {
        return String.format(
                "Matrix[band: %s, %s]",
                this.bandMatrixDimension(), EntryReadableMatrix.toSimplifiedEntryString(this));
    }

    /**
     * 対称帯行列を生成するビルダ. <br>
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
     * {@link SymmetricBandMatrix} をビルドする. <br>
     * {@link #build()} を実行したビルダは使用不能となる.
     * </p>
     * 
     * <p>
     * ビルダのコピーが必要な場合, {@link #copy()} をコールする. <br>
     * ただし, このコピーはビルド前しか実行できないことに注意.
     * </p>
     */
    public static final class Builder {

        private final BandMatrixDimension bandMatrixDimension;

        private double[] diagonalEntry;
        //下三角成分を保存、上三角にコピー
        private double[] bandEntry;

        /**
         * 与えられた帯行列構造の対称帯行列ビルダを生成する. <br>
         * 初期値は零行列.
         *
         * @param bandMatrixDimension 帯行列構造
         * @throws IllegalArgumentException (サブクラス)受け入れ拒否の場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final BandMatrixDimension bandMatrixDimension) {
            var acceptance = accepts(bandMatrixDimension);
            if (acceptance.isReject()) {
                throw acceptance.getException(bandMatrixDimension);
            }

            this.bandMatrixDimension = bandMatrixDimension;
            final int dimension = bandMatrixDimension.dimension().rowAsIntValue();
            final int bandWidth = bandMatrixDimension.lowerBandWidth();

            final int entrySize = dimension * bandWidth;
            diagonalEntry = new double[dimension];
            bandEntry = new double[entrySize];
        }

        /**
         * 与えられたソースから対称帯行列のビルダを作成する.
         *
         * @param src ソース
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final SymmetricBandMatrix src) {
            this.bandMatrixDimension = src.bandMatrixDimension;
            this.diagonalEntry = src.diagonalEntry.clone();
            this.bandEntry = src.bandEntry.clone();
        }

        /**
         * コピーコンストラクタ.
         */
        private Builder(final Builder src) {
            this.bandMatrixDimension = src.bandMatrixDimension;
            this.diagonalEntry = src.diagonalEntry.clone();
            this.bandEntry = src.bandEntry.clone();
        }

        /**
         * (<i>i</i>, <i>j</i>) 要素を指定した値に置き換える. <br>
         * 同時に (<i>j</i>, <i>i</i>) の値も置き換わる.
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

            final int thisBandWidth = bandMatrixDimension.lowerBandWidth();

            //値の修正
            value = EntryReadableMatrix.modified(value);

            switch (BandDimensionPositionState.positionStateAt(row, column, this.bandMatrixDimension)) {
                case DIAGONAL:
                    diagonalEntry[row] = value;
                    return;
                case LOWER_BAND:
                    bandEntry[column * thisBandWidth + (row - column - 1)] = value;
                    return;
                case UPPER_BAND:
                    bandEntry[row * thisBandWidth + (column - row - 1)] = value;
                    return;
                case OUT_OF_BAND:
                    throw new IndexOutOfBoundsException(
                            String.format(
                                    "out of band: matrix: %s, (row, column) = (%s, %s)",
                                    bandMatrixDimension, row, column));
                case OUT_OF_MATRIX:
                    throw new IndexOutOfBoundsException(
                            String.format(
                                    "out of matrix: matrix: %s, (row, column) = (%s, %s)",
                                    bandMatrixDimension.dimension(), row, column));
                default:
                    throw new AssertionError("Bug");
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
                throw new IllegalStateException("already built");
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
            this.bandEntry = null;
        }

        /**
         * 対称帯行列をビルドする.
         *
         * @return 対称帯行列, {@link Symmetric} が付与されている
         * @throws IllegalStateException すでにビルドされている場合
         */
        public SymmetricBandMatrix build() {
            this.throwISExIfCannotBeUsed();

            var out = new SymmetricBandMatrix(this);
            this.disable();

            return out;
        }

        /**
         * 与えた帯行列構造がビルダ生成 ({@link #zero(BandMatrixDimension)},
         * {@link #unit(BandMatrixDimension)} メソッドのコール)
         * に受け入れられるかを判定する.
         * 
         * @param bandMatrixDimension 帯行列構造
         * @return ビルダ生成が受け入れられるならACCEPT
         * @throws NullPointerException 引数がnullの場合
         */
        public static MatrixStructureAcceptance accepts(BandMatrixDimension bandMatrixDimension) {
            if (!bandMatrixDimension.isSymmetric()) {
                return MatrixRejectionConstant.REJECTED_BY_NOT_SYMMETRIC.get();
            }

            if (!bandMatrixDimension.isAccepedForBandMatrix()) {
                return MatrixRejectionConstant.REJECTED_BY_TOO_MANY_ELEMENTS.get();
            }

            return MatrixStructureAcceptance.ACCEPTED;
        }

        /**
         * 与えられた帯構造を持つ, 零行列で初期化された対称帯行列ビルダを生成する.
         *
         * @param bandMatrixDimension 帯行列構造
         * @return 零行列で初期化されたビルダ
         * @throws MatrixFormatMismatchException 帯行列構造が対称でない場合
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder zero(final BandMatrixDimension bandMatrixDimension) {
            return new Builder(bandMatrixDimension);
        }

        /**
         * 与えられた帯構造を持つ, 単位行列で初期化された対称帯行列ビルダを生成する.
         *
         * @param bandMatrixDimension 帯行列構造
         * @return 単位行列で初期化されたビルダ
         * @throws MatrixFormatMismatchException 帯行列構造が対称でない場合
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder unit(final BandMatrixDimension bandMatrixDimension) {
            Builder outBuilder = new Builder(bandMatrixDimension);
            Arrays.fill(outBuilder.diagonalEntry, 1d);

            return outBuilder;
        }

        /**
         * 与えられたインスタンスの成分で初期化されたビルダを作成する.
         *
         * @param src 元行列
         * @return 元行列と等価なビルダ
         * @throws MatrixNotSymmetricException 行列が対称行列でない場合
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         * @see Symmetric
         */
        public static Builder from(final BandMatrix src) {
            if (!(Objects.requireNonNull(src) instanceof Symmetric)) {
                throw new MatrixNotSymmetricException();
            }

            final var bandMatrixDimension = src.bandMatrixDimension();
            if (src instanceof SymmetricBandMatrix castedSrc) {
                return new Builder(castedSrc);
            }

            final int srcDimension = bandMatrixDimension.dimension().rowAsIntValue();
            final int srcBandWidth = bandMatrixDimension.lowerBandWidth();

            final var outBuilder = new Builder(bandMatrixDimension);

            //対角成分
            for (int i = 0; i < srcDimension; i++) {
                outBuilder.setValue(i, i, src.valueAt(i, i));
            }

            //下三角成分
            for (int i = 0; i < srcDimension; i++) {
                for (int j = 0, l = Math.min(srcBandWidth, srcDimension - i - 1); j < l; j++) {
                    int r = j + i + 1;
                    int c = i;

                    outBuilder.setValue(r, c, src.valueAt(r, c));
                }
            }

            return outBuilder;
        }
    }
}
