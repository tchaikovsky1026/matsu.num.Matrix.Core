/**
 * 2024.1.16
 */
package matsu.num.matrix.base;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.common.ArraysUtil;
import matsu.num.matrix.base.exception.MatrixFormatMismatchException;
import matsu.num.matrix.base.helper.matrix.SkeletalSymmetricInvertibleDeterminantableMatrix;
import matsu.num.matrix.base.helper.value.BandDimensionPositionState;
import matsu.num.matrix.base.helper.value.DeterminantValues;
import matsu.num.matrix.base.helper.value.InverseAndDeterminantStruct;

/**
 * 対角行列を扱う.
 *
 * @author Matsuura Y.
 * @version 18.3
 */
public interface DiagonalMatrix extends BandMatrix, Symmetric,
        Inversion, Determinantable {

    @Override
    public abstract Optional<? extends DiagonalMatrix> inverse();

    @Override
    public abstract DiagonalMatrix target();

    @Override
    public abstract DiagonalMatrix transpose();

    /**
     * {@link DiagonalMatrix}のビルダ. <br>
     * このビルダはミュータブルであり, スレッドセーフでない.
     */
    public static final class Builder {

        private BandMatrixDimension bandMatrixDimension;
        private double[] diagonalEntry;

        /**
         * 与えられた次元(サイズ)の対角行列ビルダを生成する. <br>
         * 初期値は零行列.
         *
         * @param matrixDimension 行列サイズ
         * @throws MatrixFormatMismatchException 行列サイズが正方形でない場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final MatrixDimension matrixDimension) {
            this.bandMatrixDimension = BandMatrixDimension.symmetric(matrixDimension, 0);
            this.diagonalEntry = new double[matrixDimension.rowAsIntValue()];
        }

        /**
         * (<i>i</i>, <i>i</i>) 要素を指定した値に置き換える.
         *
         * @param index i, 行, 列index
         * @param value 置き換えた後の値
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (i, i) が対角成分でない場合
         * @throws IllegalArgumentException valueが不正な値の場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public void setValue(final int index, final double value) {
            if (Objects.isNull(this.diagonalEntry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            MatrixDimension matrixDimension = this.bandMatrixDimension.dimension();
            if (!matrixDimension.isValidRowIndex(index)) {
                throw new IndexOutOfBoundsException(
                        String.format(
                                "行列外:matrix:%s, (i,i)=(%d,%d)",
                                matrixDimension, index, index));
            }
            if (!EntryReadableMatrix.acceptValue(value)) {
                throw new IllegalArgumentException(String.format("不正な値:value=%.16G", value));
            }
            this.diagonalEntry[index] = value;
        }

        /**
         * 対角行列をビルドする.
         *
         * @return 対角行列
         * @throws IllegalStateException すでにビルドされている場合
         */
        public DiagonalMatrix build() {
            if (Objects.isNull(this.diagonalEntry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            DiagonalMatrix out = new DiagonalMatrixImpl(this.bandMatrixDimension, this.diagonalEntry);
            this.diagonalEntry = null;

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
            return new Builder(matrixDimension);
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
            Builder out = new Builder(matrixDimension);
            for (int i = 0, len = matrixDimension.rowAsIntValue(); i < len; i++) {
                out.setValue(i, 1.0);
            }
            return out;
        }

        private static final class DiagonalMatrixImpl
                extends SkeletalSymmetricInvertibleDeterminantableMatrix<DiagonalMatrix, DiagonalMatrix>
                implements DiagonalMatrix {

            private final BandMatrixDimension bandMatrixDimension;
            private final double[] diagonalEntry;

            private final double entryNormMax;

            /**
             * thisの逆行列と行列式を表す. <br>
             * すでに計算されている場合について埋め込まれる. <br>
             * もし計算されていない状態(ビルダから生成された場合)はnull.
             */
            private final InverseAndDeterminantStruct<DiagonalMatrix> invAndDetOfInverse;

            /**
             * ビルダから呼ばれる.
             */
            private DiagonalMatrixImpl(final BandMatrixDimension bandMatrixDimension, double[] diagonalEntry) {
                this.bandMatrixDimension = bandMatrixDimension;
                this.diagonalEntry = diagonalEntry;

                this.entryNormMax = this.calcEntryNormMax();

                this.invAndDetOfInverse = null;
            }

            /**
             * 内部から呼ばれる. <br>
             * 生成される行列に対し, 逆行列を直接紐づける.
             */
            private DiagonalMatrixImpl(final BandMatrixDimension bandMatrixDimension, double[] diagonalEntry,
                    InverseAndDeterminantStruct<DiagonalMatrix> invAndDetOfInverse) {
                this.bandMatrixDimension = bandMatrixDimension;
                this.diagonalEntry = diagonalEntry;

                this.entryNormMax = this.calcEntryNormMax();

                this.invAndDetOfInverse = Objects.requireNonNull(invAndDetOfInverse);
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
                                    "行列内部でない:matrix:%s, (row, column)=(%d, %d)",
                                    bandMatrixDimension.dimension(), row, column));
                default:
                    throw new AssertionError("Bug: 列挙型に想定外の値");
                }
            }

            @Override
            public Vector operate(Vector operand) {

                final VectorDimension vectorDimension = operand.vectorDimension();
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

                Vector.Builder builder = Vector.Builder.zeroBuilder(vectorDimension);
                builder.setEntryValue(resultEntry);
                return builder.build();
            }

            @Override
            public Vector operateTranspose(Vector operand) {
                return this.operate(operand);
            }

            @Override
            protected InverseAndDeterminantStruct<DiagonalMatrix> createInvAndDetWrapper() {
                if (Objects.nonNull(this.invAndDetOfInverse)) {
                    return this.invAndDetOfInverse;
                }
                return new CreateInvAndDetWrapper(this).execute();
            }

            @Override
            public double entryNormMax() {
                return this.entryNormMax;
            }

            private double calcEntryNormMax() {
                return Double.valueOf(ArraysUtil.normMax(this.diagonalEntry));
            }

            /**
             * このオブジェクトの文字列説明表現を返す.
             * 
             * <p>
             * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
             * おそらくは次のような表現であろう. <br>
             * {@code @hashCode[dimension: %dimension, entry: %entry, diagonal]}
             * </p>
             * 
             * @return 説明表現
             */
            @Override
            public String toString() {
                return BandMatrix.toString(this, "diagonal");
            }

            /**
             * {@linkplain Block2OrderSymmetricDiagonalMatrixImpl} の内部で使う,
             * 行列式と逆行列の計算を支援する仕組み.
             */
            private static final class CreateInvAndDetWrapper {

                //logAbsDetの計算で使う定数
                private static final double SHIFT_CONSTANT = 1E150;
                private static final double SHIFT_CONSTANT_SQUARE = SHIFT_CONSTANT * SHIFT_CONSTANT;
                private static final double REVERSE_SHIFT_CONSTANT = 1 / SHIFT_CONSTANT;
                private static final double REVERSE_SHIFT_CONSTANT_SQUARE = 1 / SHIFT_CONSTANT_SQUARE;
                private static final double LOG_SHIFT_CONSTANT = Math.log(SHIFT_CONSTANT);

                private final DiagonalMatrixImpl src;

                /*
                 * 配列は結果行列内に埋め込まれるので, このクラス内での値の書き換えに注意する.
                 */
                private boolean sign;
                private double[] inverseDiagEntry;

                //logAbsDetの計算結果保存
                private int shifting;
                private double absDetResidual;

                CreateInvAndDetWrapper(DiagonalMatrixImpl src) {
                    super();
                    this.src = src;
                }

                /**
                 * 逆行列と行列式を生成する.
                 * 
                 * @return 逆行列と行列式
                 */
                public InverseAndDeterminantStruct<DiagonalMatrix>
                        execute() {
                    double[] thisDiagonalEntry = this.src.diagonalEntry;
                    final int dimension = thisDiagonalEntry.length;

                    this.sign = true;
                    this.inverseDiagEntry = new double[thisDiagonalEntry.length];
                    this.shifting = 0;
                    this.absDetResidual = 1d;

                    for (int i = 0; i < dimension; i++) {
                        State s = this.applyUnderState1(i, thisDiagonalEntry[i]);
                        if (s.equals(State.SINGULAR)) {
                            return new InverseAndDeterminantStruct<>();
                        }
                    }

                    double logAbsDeterminant = Math.log(absDetResidual)
                            + shifting * LOG_SHIFT_CONSTANT;

                    DeterminantValues thisDet = new DeterminantValues(
                            logAbsDeterminant,
                            sign ? 1 : -1);
                    //逆行列に埋め込まれるinverse: 逆行列の行列式とthisを埋め込む
                    InverseAndDeterminantStruct<DiagonalMatrix> invWrapper =
                            new InverseAndDeterminantStruct<>(
                                    thisDet.createInverse(), this.src);

                    DiagonalMatrix invMatrix = new DiagonalMatrixImpl(
                            this.src.bandMatrixDimension, inverseDiagEntry, invWrapper);

                    return new InverseAndDeterminantStruct<>(thisDet, invMatrix);
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
    }

}
