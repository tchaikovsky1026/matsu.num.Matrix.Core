/**
 * 2023.8.21
 */
package matsu.num.matrix.base;

import java.util.Objects;
import java.util.Optional;

import matsu.num.commons.ArraysUtil;
import matsu.num.commons.Exponentiation;
import matsu.num.matrix.base.exception.MatrixFormatMismatchException;
import matsu.num.matrix.base.helper.matrix.SkeletalInvertibleDeterminantableMatrix;
import matsu.num.matrix.base.helper.value.BandDimensionPositionState;
import matsu.num.matrix.base.helper.value.DeterminantValues;
import matsu.num.matrix.base.lazy.InverseAndDeterminantStructure;

/**
 * 対角行列を扱う.
 *
 * @author Matsuura Y.
 * @version 15.1
 */
public interface DiagonalMatrix extends BandMatrix, Symmetric, Inversion, Determinantable {

    @Override
    public DiagonalMatrix target();

    @Override
    public Optional<? extends DiagonalMatrix> inverse();

    /**
     * {@link DiagonalMatrix}のビルダ. スレッドセーフでない.
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
         * (<i>i</i>,<i>i</i>)要素を指定した値に置き換える.
         *
         * @param index i, 行, 列index
         * @param value 置き換えた後の値
         * @return this
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (i,i)が対角成分でない場合
         * @throws IllegalArgumentException valueが不正な値の場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public Builder setValue(final int index, final double value) {
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
            return this;
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

        private static final class DiagonalMatrixImpl extends SkeletalInvertibleDeterminantableMatrix<DiagonalMatrix>
                implements DiagonalMatrix {

            private final BandMatrixDimension bandMatrixDimension;
            private final double[] diagonalEntry;

            private final double entryNormMax;

            //thisの逆行列と行列式を表す. 
            //すでに計算されている場合について埋め込まれる
            //もし計算されていない状態(ビルダから生成された場合)はnull
            private final InverseAndDeterminantStructure<DiagonalMatrix> invAndDetOfInverse;

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
                    InverseAndDeterminantStructure<DiagonalMatrix> invAndDetOfInverse) {
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
            public DiagonalMatrix target() {
                return this;
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
                return Vector.Builder.zeroBuilder(vectorDimension).setEntryValue(resultEntry).build();
            }

            @Override
            public Vector operateTranspose(Vector operand) {
                return this.operate(operand);
            }

            @Override
            protected InverseAndDeterminantStructure<DiagonalMatrix> createInvAndDetWrapper() {
                if (Objects.nonNull(this.invAndDetOfInverse)) {
                    return this.invAndDetOfInverse;
                }

                //対角成分の逆数を持つ対角行列成分
                double[] inverseDiagEntry = new double[this.diagonalEntry.length];

                //行列式のsign
                boolean sign = true;

                //logAbsDetの計算で使う定数
                final double shiftConstant = 1E100;
                final double reverseShiftConstant = 1 / shiftConstant;

                //logAbsDet
                double logDetResidual = 0d;
                int shiftCount = 0;
                double absDetResidual = 1.0;

                for (int i = 0, len_i = inverseDiagEntry.length; i < len_i; i++) {
                    double m_00 = this.diagonalEntry[i];
                    double abs_m_00 = Math.abs(m_00);
                    double im_00 = 1.0 / m_00;
                    if (!Double.isFinite(im_00)) {
                        return new InverseAndDeterminantStructure<>();
                    }
                    //逆行列の成分を反映
                    inverseDiagEntry[i] = im_00;
                    //行列式の符号を反映
                    sign ^= m_00 < 0;

                    //行列式の対数の反映
                    //極端な値は直接に
                    if (abs_m_00 > shiftConstant || abs_m_00 < reverseShiftConstant) {
                        logDetResidual += Exponentiation.log(abs_m_00);
                        continue;
                    }
                    //穏やかな値は対数を取らず蓄積する
                    absDetResidual *= abs_m_00;
                    if (absDetResidual > shiftConstant) {
                        absDetResidual *= reverseShiftConstant;
                        shiftCount++;
                    }
                    if (absDetResidual < reverseShiftConstant) {
                        absDetResidual *= shiftConstant;
                        shiftCount--;
                    }
                }

                double logAbsDet = logDetResidual + Exponentiation.log(absDetResidual)
                        + shiftCount * Exponentiation.log(shiftConstant);

                DeterminantValues thisDet = new DeterminantValues(
                        logAbsDet,
                        sign ? 1 : -1);

                //逆行列に埋め込まれるinverse: 逆行列の行列式とthisを埋め込む
                InverseAndDeterminantStructure<DiagonalMatrix> invWrapper =
                        new InverseAndDeterminantStructure<>(thisDet.createInverse(), this);

                DiagonalMatrixImpl invMatrix =
                        new DiagonalMatrixImpl(this.bandMatrixDimension, inverseDiagEntry, invWrapper);

                return new InverseAndDeterminantStructure<>(thisDet, invMatrix);
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
                return EntryReadableMatrix.toString(this, "diagonal");
            }
        }
    }

}
