/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.6.26
 */
package matsu.num.matrix.core;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.core.common.ArraysUtil;
import matsu.num.matrix.core.helper.value.BandDimensionPositionState;
import matsu.num.matrix.core.helper.value.MatrixRejectionConstant;
import matsu.num.matrix.core.helper.value.MatrixValidationSupport;
import matsu.num.matrix.core.validation.ElementsTooManyException;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;
import matsu.num.matrix.core.validation.MatrixStructureAcceptance;

/**
 * 単位下三角の帯行列を扱う具象クラス.
 * 
 * <p>
 * このクラスのインスタンスはビルダ ({@link LowerUnitriangularBandMatrix.Builder}) を用いて生成する.
 * </p>
 * 
 * @author Matsuura Y.
 */
public final class LowerUnitriangularBandMatrix
        extends SkeletalAsymmetricMatrix<BandMatrix> implements LowerUnitriangular, BandMatrix {

    /*
     * 行列の各要素は, 内部では狭義下三角成分を1次元配列として扱う.
     * 次元を<i>n</i>, 下側帯幅を<i>b</i><sub>l</sub>とすると,
     * 配列の長さは<i>n</i><i>b</i><sub>l</sub>である.
     * 
     * 例えば4*4行列で下側帯幅2の場合:
     * 副対角成分の配列を{@code b}とすると{@code b.length = 8}であり, 以下のように格納する。
     * 1.0 --- --- ---
     * [0] 1.0 --- ---
     * [1] [2] 1.0 ---
     * --- [3] [4] 1.0
     * (-- --- [5] [6])
     * (-- --- --- [7])
     */
    private final BandMatrixDimension bandMatrixDimension;

    private final double[] lowerEntry;

    private final Optional<Matrix> inverse;

    /**
     * ビルダから呼ばれる.
     */
    private LowerUnitriangularBandMatrix(final Builder builder) {
        this.bandMatrixDimension = builder.bandMatrixDimension;
        this.lowerEntry = builder.lowerEntry;

        this.inverse = Optional.of(this.createInverse());
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

        MatrixValidationSupport.validateIndexInMatrix(bandMatrixDimension.dimension(), row, column);

        switch (BandDimensionPositionState.positionStateAt(row, column, this.bandMatrixDimension)) {
            case DIAGONAL:
                return 1;
            case LOWER_BAND:
                return lowerEntry[column * thisLowerBandWidth + (row - column - 1)];
            case OUT_OF_BAND:
                return 0;
            // OUT_OF_MATRIXは検証済み
            //$CASES-OMITTED$
            default:
                throw new AssertionError("Bug: unreachable");
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

        MatrixValidationSupport.validateOperate(bandMatrixDimension.dimension(), vectorDimension);

        final int dimension = vectorDimension.intValue();
        final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();

        final double[] resultEntry;
        final double[] thisLowerEntry = this.lowerEntry;

        //対角成分の影響: 全て1なのでコピー
        resultEntry = operand.entryAsArray();
        //狭義下三角成分
        int in = dimension * thisLowerBandWidth;
        for (int i = dimension - 1; i >= 0; i--) {
            final double oe_i = operand.valueAt(i);
            in -= thisLowerBandWidth;
            for (int j = Math.min(thisLowerBandWidth, dimension - i - 1) - 1; j >= 0; j--) {
                resultEntry[i + j + 1] += thisLowerEntry[in + j] * oe_i;
            }
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

        MatrixValidationSupport.validateOperateTranspose(bandMatrixDimension.dimension(), vectorDimension);

        final int dimension = vectorDimension.intValue();
        final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();

        final double[] operandEntry = operand.entryAsArray();
        final double[] resultEntry;
        final double[] thisLowerEntry = this.lowerEntry;

        //対角成分の影響: 全て1なのでコピー
        resultEntry = operand.entryAsArray();
        //狭義下三角成分
        int in = -thisLowerBandWidth;
        for (int i = 0; i < dimension; i++) {
            in += thisLowerBandWidth;

            /*
             * 主要ループで4成分の計算を同時に行う.
             * 影響する変数を分けることで, 並列実行できる可能性がある.
             */
            double v0 = 0d;
            double v1 = 0d;
            double v2 = 0d;
            double v3 = 0d;

            int k, l;
            for (k = 0, l = Math.min(thisLowerBandWidth, dimension - i - 1); k < l - 3; k += 4) {
                v0 += thisLowerEntry[in + k] * operandEntry[i + k + 1];
                v1 += thisLowerEntry[in + k + 1] * operandEntry[i + k + 2];
                v2 += thisLowerEntry[in + k + 2] * operandEntry[i + k + 3];
                v3 += thisLowerEntry[in + k + 3] * operandEntry[i + k + 4];
            }
            for (; k < l; k++) {
                v0 += thisLowerEntry[in + k] * operandEntry[i + k + 1];
            }
            resultEntry[i] += (v0 + v1) + (v2 + v3);
        }

        var builder = Vector.Builder.zeroBuilder(vectorDimension);
        builder.setEntryValue(resultEntry);
        return builder.build();
    }

    @Override
    public double entryNormMax() {
        return Math.max(ArraysUtil.normMax(lowerEntry), 1);
    }

    @Override
    public double determinant() {
        return 1.0;
    }

    @Override
    public double logAbsDeterminant() {
        return 0.0;
    }

    @Override
    public int signOfDeterminant() {
        return 1;
    }

    @Override
    public Optional<Matrix> inverse() {
        return this.inverse;
    }

    @Override
    public String toString() {
        return "Matrix[band: %s, %s, lower, unitriangular]"
                .formatted(
                        this.bandMatrixDimension(),
                        EntryReadableMatrix.toSimplifiedEntryString(this));
    }

    /**
     * この下三角行列の逆行列を作成する.
     * 
     * @return 逆行列
     */
    private Matrix createInverse() {
        return new SkeletalAsymmetricMatrix<Matrix>() {

            @Override
            public MatrixDimension matrixDimension() {
                return bandMatrixDimension.dimension();
            }

            /**
             * -
             * 
             * <p>
             * (外部からの呼び出し不可)
             * </p>
             * 
             * @return -
             * 
             */
            @Override
            protected Matrix createTranspose() {
                return Matrix.createTransposedOf(this);
            }

            @Override
            public Vector operate(Vector operand) {
                final var vectorDimension = operand.vectorDimension();

                MatrixValidationSupport.validateOperateTranspose(
                        bandMatrixDimension.dimension(), vectorDimension);

                final int dimension = vectorDimension.intValue();
                final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();

                final double[] resultEntry = operand.entryAsArray();
                final double[] thisLowerEntry = lowerEntry;

                int in = -thisLowerBandWidth;
                for (int i = 0; i < dimension; i++) {
                    final double re_i = resultEntry[i];
                    in += thisLowerBandWidth;
                    for (int j = 0, l = Math.min(thisLowerBandWidth, dimension - i - 1); j < l; j++) {
                        resultEntry[i + j + 1] -= thisLowerEntry[in + j] * re_i;
                    }
                }

                var builder = Vector.Builder.zeroBuilder(vectorDimension);
                builder.setEntryValue(resultEntry);
                return builder.build();
            }

            @Override
            public Vector operateTranspose(Vector operand) {
                final var vectorDimension = operand.vectorDimension();

                MatrixValidationSupport.validateOperate(
                        bandMatrixDimension.dimension(), vectorDimension);

                final int dimension = vectorDimension.intValue();
                final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();

                final double[] resultEntry = operand.entryAsArray();
                final double[] thisLowerEntry = lowerEntry;

                int in = dimension * thisLowerBandWidth;
                for (int i = dimension - 1; i >= 0; i--) {
                    in -= thisLowerBandWidth;

                    /*
                     * 主要ループで4成分の計算を同時に行う.
                     * 影響する変数を分けることで, 並列実行できる可能性がある.
                     */
                    double v0 = 0d;
                    double v1 = 0d;
                    double v2 = 0d;
                    double v3 = 0d;

                    int k, l;
                    for (l = Math.min(thisLowerBandWidth, dimension - i - 1), k = l - 1; k >= 3; k -= 4) {
                        v0 += thisLowerEntry[in + k] * resultEntry[i + k + 1];
                        v1 += thisLowerEntry[in + k + 1] * resultEntry[i + k + 2];
                        v2 += thisLowerEntry[in + k + 2] * resultEntry[i + k + 3];
                        v3 += thisLowerEntry[in + k + 3] * resultEntry[i + k + 4];
                    }
                    for (; k >= 0; k--) {
                        v0 += thisLowerEntry[in + k] * resultEntry[i + k + 1];
                    }
                    resultEntry[i] -= (v0 + v1) + (v2 + v3);
                }

                var builder = Vector.Builder.zeroBuilder(vectorDimension);
                builder.setEntryValue(resultEntry);
                return builder.build();
            }
        };

    }

    /**
     * 単位下三角の帯行列を作成するビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
     * 
     * <p>
     * このビルダインスタンスを得るには,
     * {@link #unit(BandMatrixDimension)}
     * をコールする. <br>
     * ビルダの生成時に有効要素数が大きすぎる場合は例外がスローされる. <br>
     * そのルールは {@link BandMatrixDimension#isAccepedForBandMatrix()}
     * に従う.
     * </p>
     * 
     * <p>
     * ビルド準備ができたビルダに対して {@link #build()} をコールすることで
     * {@link LowerUnitriangularBandMatrix} をビルドする. <br>
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
        private double[] lowerEntry;

        /**
         * 与えられた帯行列構造の単位下三角帯行列ビルダを生成する. <br>
         * 初期値は単位行列.
         *
         * @param bandMatrixDimension 下三角である帯行列構造
         * @throws IllegalArgumentException (サブクラス)受け入れ拒否の場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final BandMatrixDimension bandMatrixDimension) {
            var acceptance = accepts(bandMatrixDimension);
            if (acceptance.isReject()) {
                throw acceptance.getException(bandMatrixDimension);
            }

            this.bandMatrixDimension = bandMatrixDimension;

            final int entrySize =
                    bandMatrixDimension.dimension().rowAsIntValue()
                            * bandMatrixDimension.lowerBandWidth();
            this.lowerEntry = new double[entrySize];
        }

        /**
         * コピーコンストラクタ.
         */
        private Builder(final Builder src) {
            this.bandMatrixDimension = src.bandMatrixDimension;
            this.lowerEntry = src.lowerEntry.clone();
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
         * @throws IndexOutOfBoundsException (<i>i</i>, <i>j</i>)
         *             が行列の狭義下側帯領域内でない場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public void setValue(final int row, final int column, double value) {
            this.throwISExIfCannotBeUsed();

            final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();

            MatrixValidationSupport.validateIndexInMatrixAndBand(bandMatrixDimension, row, column);

            //値を修正する
            value = EntryReadableMatrix.modified(value);

            switch (BandDimensionPositionState.positionStateAt(row, column, this.bandMatrixDimension)) {
                case DIAGONAL:
                    throw new IndexOutOfBoundsException(
                            "diagonal cannot be substituted: matrix: %s, (row, column) = (%s, %s)"
                                    .formatted(bandMatrixDimension, row, column));
                case LOWER_BAND:
                    lowerEntry[column * thisLowerBandWidth + (row - column - 1)] = value;
                    return;
                //OUT_OF_BAND, OUT_OF_MATRIXは検証済み
                //$CASES-OMITTED$
                default:
                    throw new AssertionError("Bug: unreachable");
            }
        }

        /**
         * このビルダが使用可能か (ビルド前かどうか) を判定する.
         * 
         * @return 使用可能なら {@code true}
         */
        public boolean canBeUsed() {
            return Objects.nonNull(this.lowerEntry);
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
            this.lowerEntry = null;
        }

        /**
         * 単位下三角行列をビルドする.
         * 
         * @return 単位下三角行列
         * @throws IllegalStateException すでにビルドされている場合
         */
        public LowerUnitriangularBandMatrix build() {
            this.throwISExIfCannotBeUsed();

            var out = new LowerUnitriangularBandMatrix(this);
            this.disable();

            return out;
        }

        /**
         * 与えた帯行列構造がビルダ生成 ({@link #unit(BandMatrixDimension)} メソッドのコール)
         * に受け入れられるかを判定する.
         * 
         * @param bandMatrixDimension 帯行列構造
         * @return ビルダ生成が受け入れられるならACCEPT
         * @throws NullPointerException 引数がnullの場合
         */
        public static MatrixStructureAcceptance accepts(BandMatrixDimension bandMatrixDimension) {
            if (bandMatrixDimension.upperBandWidth() > 0) {
                return MatrixRejectionConstant.REJECTED_BY_NOT_LOWER_TRIANGULAR.get();
            }

            if (!bandMatrixDimension.isAccepedForBandMatrix()) {
                return MatrixRejectionConstant.REJECTED_BY_TOO_MANY_ELEMENTS.get();
            }

            return MatrixStructureAcceptance.ACCEPTED;
        }

        /**
         * 与えられた帯行列構造を持つ, 単位行列で初期化された単位下三角帯行列ビルダを生成する.
         *
         * @param bandMatrixDimension 下三角である帯行列構造
         * @return 単位行列で初期化されたビルダ
         * @throws MatrixFormatMismatchException 帯行列構造が下三角構造でない,
         *             {@link BandMatrixDimension#upperBandWidth} &gt;
         *             0である場合
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder unit(final BandMatrixDimension bandMatrixDimension) {
            return new Builder(bandMatrixDimension);
        }
    }
}
