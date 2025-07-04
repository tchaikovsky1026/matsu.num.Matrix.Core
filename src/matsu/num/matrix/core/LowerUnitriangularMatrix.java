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
import matsu.num.matrix.core.common.CalcUtil;
import matsu.num.matrix.core.helper.value.MatrixRejectionConstant;
import matsu.num.matrix.core.helper.value.MatrixValidationSupport;
import matsu.num.matrix.core.validation.ElementsTooManyException;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;
import matsu.num.matrix.core.validation.MatrixStructureAcceptance;

/**
 * 単位下三角の密行列を扱う具象クラス.
 * 
 * <p>
 * このクラスのインスタンスはビルダ ({@link LowerUnitriangularMatrix.Builder}) を用いて生成する.
 * </p>
 * 
 * @author Matsuura Y.
 */
public final class LowerUnitriangularMatrix
        extends SkeletalAsymmetricMatrix<EntryReadableMatrix> implements LowerUnitriangular {

    /*
     * 行列の各要素は, 内部では1次元配列として,
     * 1.0
     * [0] 1.0
     * [1] [2] 1.0
     * [3] [4] [5] 1.0
     * [6] [7] [8] [9] 1.0
     * の形で狭義下三角成分を保持し, 対角成分, 狭義上三角成分は省略する.
     */
    private final MatrixDimension matrixDimension;

    private final double[] lowerEntry;

    private final Optional<Matrix> inverse;

    /**
     * ビルダから呼ばれる.
     */
    private LowerUnitriangularMatrix(final Builder builder) {
        this.matrixDimension = builder.matrixDimension;
        this.lowerEntry = builder.lowerEntry;

        this.inverse = Optional.of(this.createInverse());
    }

    @Override
    public MatrixDimension matrixDimension() {
        return this.matrixDimension;
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

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc }
     */
    @Override
    public double valueAt(final int row, final int column) {

        MatrixValidationSupport.validateIndexInMatrix(matrixDimension, row, column);

        if (row == column) {
            return 1;
        } else if (column < row) {
            return lowerEntry[column + CalcUtil.sumOf1To(row - 1)];
        } else {
            return 0;
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
    protected EntryReadableMatrix createTranspose() {
        return EntryReadableMatrix.createTransposedOf(this);
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     */
    @Override
    public Vector operate(Vector operand) {
        final var vectorDimension = operand.vectorDimension();

        MatrixValidationSupport.validateOperate(matrixDimension, vectorDimension);

        final int dimension = vectorDimension.intValue();

        final double[] operandEntry = operand.entryAsArray();
        final double[] thisLowerEntry = this.lowerEntry;

        //対角成分の影響: 全て1なのでコピー
        final double[] resultEntry = operand.entryAsArray();
        //狭義下三角成分
        int in = CalcUtil.sumOf1To(dimension - 1);
        for (int i = dimension - 1; i >= 0; i--) {
            in -= i;

            /*
             * 主要ループで4成分の計算を同時に行う.
             * 影響する変数を分けることで, 並列実行できる可能性がある.
             */
            double v0 = 0d;
            double v1 = 0d;
            double v2 = 0d;
            double v3 = 0d;

            int j;
            for (j = i - 1; j >= 3; j -= 4) {
                v0 += thisLowerEntry[in + j] * operandEntry[j];
                v1 += thisLowerEntry[in + j - 1] * operandEntry[j - 1];
                v2 += thisLowerEntry[in + j - 2] * operandEntry[j - 2];
                v3 += thisLowerEntry[in + j - 3] * operandEntry[j - 3];
            }
            for (; j >= 0; j--) {
                v0 += thisLowerEntry[in + j] * operandEntry[j];
            }
            resultEntry[i] += (v0 + v1) + (v2 + v3);
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

        MatrixValidationSupport.validateOperateTranspose(matrixDimension, vectorDimension);

        final int dimension = vectorDimension.intValue();

        final double[] thisLowerEntry = this.lowerEntry;

        //対角成分の影響: 全て1なのでコピー
        final double[] resultEntry = operand.entryAsArray();
        //狭義下三角成分
        int in = 1;
        for (int i = 0; i < dimension; i++) {
            final double oe_i = operand.valueAt(i);
            in += i - 1;
            for (int j = 0; j < i; j++) {
                resultEntry[j] += thisLowerEntry[in + j] * oe_i;
            }
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
    public Optional<Matrix> inverse() {
        return this.inverse;
    }

    @Override
    public String toString() {
        return String.format(
                "Matrix[dim: %s, %s, lower, unitriangular]",
                this.matrixDimension(), EntryReadableMatrix.toSimplifiedEntryString(this));
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
                return matrixDimension;
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
            protected Matrix createTranspose() {
                return Matrix.createTransposedOf(this);
            }

            @Override
            public Vector operate(Vector operand) {
                final var vectorDimension = operand.vectorDimension();

                MatrixValidationSupport.validateOperateTranspose(matrixDimension, vectorDimension);

                final int dimension = vectorDimension.intValue();
                final double[] thisLowerEntry = lowerEntry;

                final double[] resultEntry = operand.entryAsArray();

                int in = 1;
                for (int i = 0; i < dimension; i++) {
                    in += i - 1;

                    /*
                     * 主要ループで4成分の計算を同時に行う.
                     * 影響する変数を分けることで, 並列実行できる可能性がある.
                     */
                    double v0 = 0d;
                    double v1 = 0d;
                    double v2 = 0d;
                    double v3 = 0d;

                    int j;
                    for (j = 0; j < i - 3; j += 4) {
                        v0 += thisLowerEntry[in + j] * resultEntry[j];
                        v1 += thisLowerEntry[in + j + 1] * resultEntry[j + 1];
                        v2 += thisLowerEntry[in + j + 2] * resultEntry[j + 2];
                        v3 += thisLowerEntry[in + j + 3] * resultEntry[j + 3];
                    }
                    for (; j < i; j++) {
                        v0 += thisLowerEntry[in + j] * resultEntry[j];
                    }
                    resultEntry[i] -= (v0 + v1) + (v2 + v3);
                }

                var builder = Vector.Builder.zeroBuilder(vectorDimension);
                builder.setEntryValue(resultEntry);
                return builder.build();
            }

            @Override
            public Vector operateTranspose(Vector operand) {
                final var vectorDimension = operand.vectorDimension();

                MatrixValidationSupport.validateOperate(matrixDimension, vectorDimension);

                final int dimension = vectorDimension.intValue();
                final double[] thisLowerEntry = lowerEntry;

                final double[] resultEntry = operand.entryAsArray();

                int in = CalcUtil.sumOf1To(dimension - 1);
                for (int i = dimension - 1; i >= 0; i--) {
                    in -= i;
                    final double re_i = resultEntry[i];
                    for (int j = i - 1; j >= 0; j--) {
                        resultEntry[j] -= thisLowerEntry[in + j] * re_i;
                    }
                }

                var builder = Vector.Builder.zeroBuilder(vectorDimension);
                builder.setEntryValue(resultEntry);
                return builder.build();
            }
        };
    }

    /**
     * 単位下三角の密行列を作成するビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
     * 
     * <p>
     * このビルダインスタンスを得るには,
     * {@link #unit(MatrixDimension)}
     * をコールする. <br>
     * ビルダの生成時に有効要素数が大きすぎる場合は例外がスローされる. <br>
     * そのルールは {@link MatrixDimension#isAccepedForDenseMatrix()}
     * に従う.
     * </p>
     * 
     * <p>
     * ビルド準備ができたビルダに対して {@link #build()} をコールすることで
     * {@link LowerUnitriangularMatrix} をビルドする. <br>
     * {@link #build()} を実行したビルダは使用不能となる.
     * </p>
     * 
     * <p>
     * ビルダのコピーが必要な場合, {@link #copy()} をコールする. <br>
     * ただし, このコピーはビルド前しか実行できないことに注意.
     * </p>
     */
    public static final class Builder {

        private final MatrixDimension matrixDimension;

        private double[] lowerEntry;

        /**
         * 与えられた次元(サイズ)の単位下三角行列ビルダを生成する. <br>
         * 初期値は単位行列.
         *
         * @param matrixDimension 行列サイズ
         * @throws IllegalArgumentException (サブクラス)受け入れ拒否の場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final MatrixDimension matrixDimension) {
            var acceptance = accepts(matrixDimension);
            if (acceptance.isReject()) {
                throw acceptance.getException(matrixDimension);
            }

            this.matrixDimension = matrixDimension;

            final int dimension = matrixDimension.rowAsIntValue();
            final int entrySize = CalcUtil.sumOf1To(dimension - 1);

            this.lowerEntry = new double[entrySize];
        }

        /**
         * コピーコンストラクタ.
         */
        private Builder(final Builder src) {
            this.matrixDimension = src.matrixDimension;
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
         * @throws IndexOutOfBoundsException (<i>i</i>, <i>j</i>)
         *             が行列の狭義下三角成分でない場合
         * @throws IllegalStateException すでにビルドされている場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public void setValue(final int row, final int column, double value) {
            this.throwISExIfCannotBeUsed();

            MatrixValidationSupport.validateIndexInMatrix(matrixDimension, row, column);

            //値を修正する
            value = EntryReadableMatrix.modified(value);

            if (column < row) {
                lowerEntry[column + CalcUtil.sumOf1To(row - 1)] = value;
            } else {
                throw new IndexOutOfBoundsException(
                        "out of lower triangular: matrix: %s, (row, column) = (%s, %s)"
                                .formatted(matrixDimension, row, column));
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
        public LowerUnitriangularMatrix build() {
            this.throwISExIfCannotBeUsed();

            var out = new LowerUnitriangularMatrix(this);
            this.disable();

            return out;
        }

        /**
         * この行列サイズがビルダ生成 ({@link #unit(MatrixDimension)} メソッドのコール)
         * に受け入れられるかを判定する.
         * 
         * @param matrixDimension 行列サイズ
         * @return ビルダ生成が受け入れられるならACCEPT
         * @throws NullPointerException 引数がnullの場合
         */
        public static MatrixStructureAcceptance accepts(MatrixDimension matrixDimension) {
            if (!matrixDimension.isSquare()) {
                return MatrixRejectionConstant.REJECTED_BY_NOT_SQUARE.get();
            }

            if (!matrixDimension.isAccepedForDenseMatrix()) {
                return MatrixRejectionConstant.REJECTED_BY_TOO_MANY_ELEMENTS.get();
            }

            return MatrixStructureAcceptance.ACCEPTED;
        }

        /**
         * 与えられた次元(サイズ)を持つ, 単位行列で初期化された単位下三角行列ビルダを生成する.
         *
         * @param matrixDimension 行列サイズ
         * @return 単位行列で初期化されたビルダ
         * @throws MatrixFormatMismatchException 行列サイズが正方サイズでない場合
         * @throws ElementsTooManyException 行列の有効要素数が大きすぎる場合(クラス説明文)
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder unit(final MatrixDimension matrixDimension) {
            return new Builder(matrixDimension);
        }
    }
}
