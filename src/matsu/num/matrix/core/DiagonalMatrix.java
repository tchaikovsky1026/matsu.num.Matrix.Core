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
import java.util.Optional;

import matsu.num.matrix.core.common.ArraysUtil;
import matsu.num.matrix.core.helper.matrix.SkeletalSymmetricInvertibleDeterminantableMatrix;
import matsu.num.matrix.core.helper.value.BandDimensionPositionState;
import matsu.num.matrix.core.helper.value.DeterminantValues;
import matsu.num.matrix.core.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.core.sealed.DiagonalMatrixSealed;
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
 * @author Matsuura Y.
 * @see <a href="https://en.wikipedia.org/wiki/Diagonal_matrix">
 *          Diagonal matrix</a>
 */
public sealed interface DiagonalMatrix
        extends BandMatrix, Symmetric, Invertible, Determinantable
        permits DiagonalMatrixSealed, SignatureMatrix, SquareZeroMatrix {

    @Override
    public abstract Optional<? extends DiagonalMatrix> inverse();

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
     */
    public static final class Builder {

        private final BandMatrixDimension bandMatrixDimension;
        private double[] diagonalEntry;

        /**
         * 内部から呼ばれる.
         * 
         * <p>
         * 配列は防御的コピーされていない.
         * </p>
         */
        private Builder(BandMatrixDimension bandMatrixDimension, double[] diagonalEntry) {
            this.bandMatrixDimension = bandMatrixDimension;
            this.diagonalEntry = diagonalEntry;
        }

        /**
         * コピーコンストラクタ.
         */
        private Builder(Builder src) {
            this(src.bandMatrixDimension, src.diagonalEntry.clone());
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
        public void setValue(final int index, double value) {
            this.throwISExIfCannotBeUsed();

            var matrixDimension = this.bandMatrixDimension.dimension();
            if (!matrixDimension.isValidRowIndex(index)) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "行列外:matrix:%s, (i, i)=(%s, %s)",
                                matrixDimension, index, index));
            }

            //値を修正する
            value = EntryReadableMatrix.modified(value);

            this.diagonalEntry[index] = value;
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
        }

        /**
         * 対角行列をビルドする.
         *
         * @return 対角行列
         * @throws IllegalStateException すでにビルドされている場合
         */
        public DiagonalMatrix build() {
            this.throwISExIfCannotBeUsed();

            var out = new DiagonalMatrixImpl(this);
            this.disable();

            return out;
        }

        /**
         * 与えられた次元(サイズ)を持つ, 零行列で初期化された対角行列ビルダを生成する.
         *
         * @param matrixDimension 行列サイズ
         * @return 零行列で初期化されたビルダ
         * @throws MatrixFormatMismatchException 行列サイズが正方形でない場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder zeroBuilder(final MatrixDimension matrixDimension) {
            return new Builder(
                    BandMatrixDimension.symmetric(matrixDimension, 0),
                    new double[matrixDimension.rowAsIntValue()]);
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

            var bandMatrixDimension = BandMatrixDimension.symmetric(matrixDimension, 0);
            double[] diagonalEntry = new double[matrixDimension.rowAsIntValue()];
            Arrays.fill(diagonalEntry, 1d);

            return new Builder(bandMatrixDimension, diagonalEntry);
        }

        /**
         * 対角行列の実装.
         */
        private static final class DiagonalMatrixImpl
                extends SkeletalSymmetricInvertibleDeterminantableMatrix<
                        DiagonalMatrixImpl, DiagonalMatrix>
                implements DiagonalMatrixSealed {

            private final BandMatrixDimension bandMatrixDimension;
            private final double[] diagonalEntry;

            private final double entryNormMax;

            /**
             * ビルダから呼ばれる.
             */
            DiagonalMatrixImpl(final Builder builder) {
                this(builder.bandMatrixDimension, builder.diagonalEntry);
            }

            /**
             * 内部から呼ばれる.
             */
            private DiagonalMatrixImpl(final BandMatrixDimension bandMatrixDimension, double[] diagonalEntry) {
                this.bandMatrixDimension = bandMatrixDimension;
                this.diagonalEntry = diagonalEntry;

                this.entryNormMax = this.calcEntryNormMax();
            }

            @Override
            public BandMatrixDimension bandMatrixDimension() {
                return this.bandMatrixDimension;
            }

            @Override
            public double valueAt(final int row, final int column) {
                switch (BandDimensionPositionState.positionStateAt(row, column, this.bandMatrixDimension)) {
                    case DIAGONAL:
                        return this.diagonalEntry[row];
                    case LOWER_BAND:
                        throw new AssertionError("Bug: 到達不能");
                    case UPPER_BAND:
                        throw new AssertionError("Bug: 到達不能");
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
             * @return -
             * @deprecated (外部からの呼び出し不可)
             */
            @Deprecated
            @Override
            protected DiagonalMatrixImpl self() {
                return this;
            }

            @Override
            public Vector operate(Vector operand) {

                final var vectorDimension = operand.vectorDimension();
                final int dimension = vectorDimension.intValue();
                if (!this.bandMatrixDimension.dimension().rightOperable(vectorDimension)) {
                    throw new MatrixFormatMismatchException(
                            String.format(
                                    "右から演算不可:matrix:%s, operand:%s",
                                    this.bandMatrixDimension.dimension(), vectorDimension));
                }

                final double[] operandEntry = operand.entry();
                final double[] resultEntry = new double[dimension];
                final double[] thisDiagonalEntry = this.diagonalEntry;
                for (int i = 0; i < dimension; i++) {
                    resultEntry[i] = thisDiagonalEntry[i] * operandEntry[i];
                }

                var builder = Vector.Builder.zeroBuilder(vectorDimension);
                builder.setEntryValue(resultEntry);
                return builder.build();
            }

            /**
             * -
             * 
             * @return -
             * @deprecated (外部からの呼び出し不可)
             */
            @Deprecated
            @Override
            protected InverstibleAndDeterminantStruct<DiagonalMatrix> createInvAndDetWrapper() {
                return new CreateInvAndDetWrapper().execute();
            }

            @Override
            public double entryNormMax() {
                return this.entryNormMax;
            }

            private double calcEntryNormMax() {
                return Double.valueOf(ArraysUtil.normMax(this.diagonalEntry));
            }

            @Override
            public String toString() {
                return String.format(
                        "Matrix[band:%s, %s, diagonal]",
                        this.bandMatrixDimension(), EntryReadableMatrix.toSimplifiedEntryString(this));
            }

            /**
             * {@link Block2OrderSymmetricDiagonalMatrixImpl} の内部で使う,
             * 行列式と逆行列の計算を支援する仕組み. <br>
             * インスタンスに対して {@link #execute()} は一度しか読んではいけない (使いまわしてはいけない.
             */
            private final class CreateInvAndDetWrapper {

                //logAbsDetの計算で使う定数
                private static final double SHIFT_CONSTANT = 1E150;
                private static final double SHIFT_CONSTANT_SQUARE = SHIFT_CONSTANT * SHIFT_CONSTANT;
                private static final double REVERSE_SHIFT_CONSTANT = 1 / SHIFT_CONSTANT;
                private static final double REVERSE_SHIFT_CONSTANT_SQUARE = 1 / SHIFT_CONSTANT_SQUARE;
                private static final double LOG_SHIFT_CONSTANT = Math.log(SHIFT_CONSTANT);

                /*
                 * 配列は結果行列内に埋め込まれるので, このクラス内での値の書き換えに注意する.
                 */
                private boolean sign;
                private double[] inverseDiagEntry;

                //logAbsDetの計算結果保存
                private int shifting;
                private double absDetResidual;

                CreateInvAndDetWrapper() {
                    super();
                }

                /**
                 * 逆行列と行列式を生成する.
                 * 
                 * @return 逆行列と行列式
                 */
                InverstibleAndDeterminantStruct<DiagonalMatrix> execute() {

                    double[] thisDiagonalEntry = DiagonalMatrixImpl.this.diagonalEntry;
                    final int dimension = thisDiagonalEntry.length;

                    this.sign = true;
                    this.inverseDiagEntry = new double[thisDiagonalEntry.length];
                    this.shifting = 0;
                    this.absDetResidual = 1d;

                    for (int i = 0; i < dimension; i++) {
                        State s = this.applyUnderState1(i, thisDiagonalEntry[i]);
                        if (s.equals(State.SINGULAR)) {
                            return new InverstibleAndDeterminantStruct<>();
                        }
                    }

                    double logAbsDeterminant = Math.log(absDetResidual)
                            + shifting * LOG_SHIFT_CONSTANT;

                    var thisDet = new DeterminantValues(
                            logAbsDeterminant,
                            sign ? 1 : -1);

                    var invMatrix = new InverseAndDeterminantAttachedDiagonalMatrixImpl(
                            new DiagonalMatrixImpl(
                                    DiagonalMatrixImpl.this.bandMatrixDimension, inverseDiagEntry),
                            thisDet.createInverse(), DiagonalMatrixImpl.this);

                    return new InverstibleAndDeterminantStruct<>(thisDet, invMatrix);
                }

                private State applyUnderState1(int i, double m_00) {

                    double im_00 = 1 / m_00;
                    if (!EntryReadableMatrix.acceptValue(im_00)) {
                        return State.SINGULAR;
                    }
                    this.inverseDiagEntry[i] = im_00;

                    this.sign ^= m_00 < 0;

                    double abs_m_00 = Math.abs(m_00);
                    this.determinantToField(abs_m_00);

                    return State.REGULAR;
                }

                private void determinantToField(double value) {

                    if (value > SHIFT_CONSTANT) {
                        if (value > SHIFT_CONSTANT_SQUARE) {
                            shifting += 2;
                            value *= REVERSE_SHIFT_CONSTANT_SQUARE;
                        } else {
                            shifting++;
                            value *= REVERSE_SHIFT_CONSTANT;
                        }
                    } else if (value < REVERSE_SHIFT_CONSTANT) {
                        if (value < REVERSE_SHIFT_CONSTANT_SQUARE) {
                            shifting -= 2;
                            value *= SHIFT_CONSTANT_SQUARE;
                        } else {
                            shifting--;
                            value *= SHIFT_CONSTANT;
                        }
                    }

                    this.absDetResidual *= value;
                    this.normalizeAbsDetResidual();
                }

                private void normalizeAbsDetResidual() {
                    if (absDetResidual > SHIFT_CONSTANT) {
                        if (absDetResidual > SHIFT_CONSTANT_SQUARE) {
                            shifting += 2;
                            absDetResidual *= REVERSE_SHIFT_CONSTANT_SQUARE;
                        } else {
                            shifting++;
                            absDetResidual *= REVERSE_SHIFT_CONSTANT;
                        }
                    } else if (absDetResidual < REVERSE_SHIFT_CONSTANT) {
                        if (absDetResidual < REVERSE_SHIFT_CONSTANT_SQUARE) {
                            shifting -= 2;
                            absDetResidual *= SHIFT_CONSTANT_SQUARE;
                        } else {
                            shifting--;
                            absDetResidual *= SHIFT_CONSTANT;
                        }
                    }
                }

                private static enum State {
                    REGULAR, SINGULAR;
                }
            }
        }

        /**
         * 行列式と逆行列を直接結びつけた対角行列. <br>
         * originalをラップし, 行列式と逆行列関連のメソッドを隠ぺいする.
         */
        private static final class InverseAndDeterminantAttachedDiagonalMatrixImpl
                extends SkeletalSymmetricMatrix<InverseAndDeterminantAttachedDiagonalMatrixImpl>
                implements DiagonalMatrixSealed {

            private final DiagonalMatrix original;
            private final DeterminantValues determinantValues;
            private final Optional<DiagonalMatrix> opInverse;

            /**
             * 唯一のコンストラクタ.
             * 引数の正当性はチェックしていない.
             */
            InverseAndDeterminantAttachedDiagonalMatrixImpl(DiagonalMatrix original,
                    DeterminantValues determinantValues, DiagonalMatrix inverse) {
                super();
                this.original = original;
                this.determinantValues = determinantValues;
                this.opInverse = Optional.of(inverse);
            }

            @Override
            public BandMatrixDimension bandMatrixDimension() {
                return this.original.bandMatrixDimension();
            }

            @Override
            public double valueAt(int row, int column) {
                return this.original.valueAt(row, column);
            }

            @Override
            public double entryNormMax() {
                return this.original.entryNormMax();
            }

            @Override
            public Vector operate(Vector operand) {
                return this.original.operate(operand);
            }

            @Override
            public double determinant() {
                return this.determinantValues.determinant();
            }

            @Override
            public double logAbsDeterminant() {
                return this.determinantValues.logAbsDeterminant();
            }

            @Override
            public int signOfDeterminant() {
                return this.determinantValues.sign();
            }

            @Override
            public Optional<? extends DiagonalMatrix> inverse() {
                return this.opInverse;
            }

            /**
             * -
             * 
             * @return -
             * @deprecated (外部からの呼び出し不可)
             */
            @Deprecated
            @Override
            protected InverseAndDeterminantAttachedDiagonalMatrixImpl self() {
                return this;
            }

            @Override
            public String toString() {
                return this.original.toString();
            }
        }
    }
}
