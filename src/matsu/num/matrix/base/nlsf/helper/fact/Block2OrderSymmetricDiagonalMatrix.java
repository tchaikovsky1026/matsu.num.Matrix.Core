/**
 * 2023.11.29
 */
package matsu.num.matrix.base.nlsf.helper.fact;

import java.util.Objects;
import java.util.Optional;

import matsu.num.commons.ArraysUtil;
import matsu.num.commons.Exponentiation;
import matsu.num.matrix.base.BandMatrix;
import matsu.num.matrix.base.BandMatrixDimension;
import matsu.num.matrix.base.Determinantable;
import matsu.num.matrix.base.EntryReadableMatrix;
import matsu.num.matrix.base.Inversion;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.VectorDimension;
import matsu.num.matrix.base.exception.MatrixFormatMismatchException;
import matsu.num.matrix.base.helper.matrix.SkeletalInvertibleDeterminantableMatrix;
import matsu.num.matrix.base.helper.value.BandDimensionPositionState;
import matsu.num.matrix.base.helper.value.DeterminantValues;
import matsu.num.matrix.base.lazy.InverseAndDeterminantStructure;

/**
 * 1*1 あるいは 2*2の対称ブロック要素を持つ, ブロック対角行列とそれを係数に持つ連立方程式の解法を扱う.
 *
 * @author Matsuura Y.
 * @version 17.0
 */
public interface Block2OrderSymmetricDiagonalMatrix extends BandMatrix, Symmetric, Inversion, Determinantable {

    @Override
    public abstract Block2OrderSymmetricDiagonalMatrix target();

    @Override
    public Optional<? extends Block2OrderSymmetricDiagonalMatrix> inverse();

    /**
     * {@link Block2OrderSymmetricDiagonalMatrix}のビルダ. スレッドセーフでない.
     */
    public static final class Builder {

        private BandMatrixDimension bandMatrixDimension;

        private double[] diagonalEntry;
        //下三角成分を保存、上三角にコピー
        private double[] subdiagonalEntry;

        /**
         * 与えられた行列次元(サイズ)の対称ブロック行列ビルダを生成する. <br>
         * 初期値は零行列.
         *
         * @param matrixDimension 行列次元(サイズ)
         * @throws MatrixFormatMismatchException 行列次元(サイズ)が正方行列でない場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        private Builder(final MatrixDimension matrixDimension) {
            if (!matrixDimension.isSquare()) {
                throw new MatrixFormatMismatchException(
                        String.format("正方形ではない行列サイズ:%s", matrixDimension));
            }
            this.bandMatrixDimension = BandMatrixDimension.symmetric(matrixDimension, 1);

            final int dimension = matrixDimension.rowAsIntValue();
            this.diagonalEntry = new double[dimension];
            this.subdiagonalEntry = new double[dimension];
        }

        /**
         * 対角要素:(<i>i</i>,<i>i</i>)要素を指定した値に置き換える.
         *
         * @param index i, 対角成分index
         * @param value 置き換えた後の値
         * @return this
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (i,i)が行列内でない場合
         * @throws IllegalArgumentException valueが不正な値の場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public Builder setDiagonal(final int index, final double value) {
            if (Objects.isNull(this.diagonalEntry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!EntryReadableMatrix.acceptValue(value)) {
                throw new IllegalArgumentException(String.format("不正な値:value=%.16G", value));
            }
            if (!(0 <= index && index < this.bandMatrixDimension.dimension().rowAsIntValue())) {
                throw new IndexOutOfBoundsException(String.format("行列内部でない:(%d, %d)", index, index));
            }
            this.diagonalEntry[index] = value;
            return this;
        }

        /**
         * 副対角要素:(<i>i</i> + 1,<i>i</i>)要素を指定した値に置き換える. <br>
         * 同時に(<i>i</i>,<i>i</i> + 1)の値も置き換わる.
         *
         * @param index i, 副対角成分index
         * @param value 置き換えた後の値
         * @return this
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (i+1,i)が行列の帯領域内でない場合
         * @throws IllegalArgumentException valueが不正な値(絶対値が大きすぎる, inf, NaN)の場合,
         *             valueを代入することでブロック対角行列でなくなる場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public Builder setSubDiagonal(final int index, final double value) {
            if (Objects.isNull(this.diagonalEntry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!EntryReadableMatrix.acceptValue(value)) {
                throw new IllegalArgumentException(String.format("不正な値:value=%.16G", value));
            }
            if (!(0 <= index && index < this.bandMatrixDimension.dimension().rowAsIntValue() - 1)) {
                throw new IndexOutOfBoundsException(String.format("行列内部でない:(%d, %d)", index + 1, index));
            }
            //0以外が代入される場合にはその両側を確認する
            if (Double.compare(value, 0.0) != 0) {
                if (index >= 1 && Double.compare(this.subdiagonalEntry[index - 1], 0.0) != 0) {
                    throw new IllegalArgumentException(
                            String.format("この代入によりブロック対角行列でなくなる:(%d, %d)", index + 1, index));
                }
                if (index < this.bandMatrixDimension.dimension().rowAsIntValue() - 2
                        && Double.compare(this.subdiagonalEntry[index + 1], 0.0) != 0) {
                    throw new IllegalArgumentException(
                            String.format("個の代入によりブロック対角行列でなくなる:(%d, %d)", index + 1, index));
                }
            }
            this.subdiagonalEntry[index] = value;

            return this;
        }

        /**
         * 副対角要素:(<i>i</i> + 1,<i>i</i>)要素を0に置き換える. <br>
         * 同時に(<i>i</i>,<i>i</i> + 1)の値も置き換わる.
         *
         * @param index i, 副対角成分index
         * @return this
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (i+1,i)が行列の帯領域内でない場合
         */
        public Builder setSubDiagonalToZero(final int index) {
            return this.setSubDiagonal(index, 0.0);
        }

        /**
         * 対称ブロック行列をビルドする.
         *
         * @return 対称ブロック行列
         * @throws IllegalStateException すでにビルドされている場合
         */
        public Block2OrderSymmetricDiagonalMatrix build() {
            if (Objects.isNull(this.diagonalEntry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            Block2OrderSymmetricDiagonalMatrix out = new Block2OrderSymmetricDiagonalMatrixImpl(
                    bandMatrixDimension, diagonalEntry, subdiagonalEntry);
            this.diagonalEntry = null;
            this.subdiagonalEntry = null;
            return out;
        }

        /**
         * 与えられた行列次元(サイズ)を持つ, 零行列で初期化された対称ブロック行列ビルダを生成する.
         *
         * @param matrixDimension 行列次元(サイズ)
         * @return 零行列で初期化されたビルダ
         * @throws MatrixFormatMismatchException 行列次元(サイズ)が正方行列でない場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        public static Builder zeroBuilder(final MatrixDimension matrixDimension) {
            return new Builder(matrixDimension);
        }

        private static final class Block2OrderSymmetricDiagonalMatrixImpl
                extends SkeletalInvertibleDeterminantableMatrix<Block2OrderSymmetricDiagonalMatrix>
                implements Block2OrderSymmetricDiagonalMatrix {

            /*
             * 内部では対称三重対角行列として, すなわち, 行列の各要素は対角成分, 副対角成分に分けて, それぞれ1次元配列として扱う.
             * 次元を<i>n</i>とすると, 各配列の長さは<i>n</i>, <i>n</i>である.
             * 
             * 例えば4*4行列の場合:
             * 対角成分の配列をd, 副対角成分の配列をsとすると,
             * d.length = 4, {b.length = 4であり,
             * d[0] s[0] ---- ----
             * s[0] d[1] s[1] ----
             * ---- s[1] d[2] s[2]
             * ---- ---- s[2] d[3] (s[3])
             * (--- ---- ---- s[3])
             * と格納される.
             */
            private final BandMatrixDimension bandMatrixDimension;

            private final double[] diagonalEntry;
            //下三角成分を保存、上三角にコピー
            private final double[] subdiagonalEntry;

            private final double entryNormMax;

            //thisの逆行列と行列式を表す. 
            //すでに計算されている場合について埋め込まれる
            //もし計算されていない状態(ビルダから生成された場合)はnull
            private final InverseAndDeterminantStructure<Block2OrderSymmetricDiagonalMatrix> invAndDetOfInverse;

            /**
             * ビルダから呼ばれる
             */
            private Block2OrderSymmetricDiagonalMatrixImpl(
                    BandMatrixDimension bandMatrixDimension, double[] diagonalEntry, double[] subDiagonalEntry) {
                this.bandMatrixDimension = bandMatrixDimension;
                this.diagonalEntry = diagonalEntry;
                this.subdiagonalEntry = subDiagonalEntry;

                this.entryNormMax = this.calcEntryNormMax();

                this.invAndDetOfInverse = null;
            }

            /**
             * 内部から呼ばれる. <br>
             * 生成される行列に対し, 逆行列を直接紐づける.
             */
            private Block2OrderSymmetricDiagonalMatrixImpl(
                    BandMatrixDimension bandMatrixDimension, double[] diagonalEntry, double[] subDiagonalEntry,
                    InverseAndDeterminantStructure<Block2OrderSymmetricDiagonalMatrix> invAndDetOfInverse) {
                this.bandMatrixDimension = bandMatrixDimension;
                this.diagonalEntry = diagonalEntry;
                this.subdiagonalEntry = subDiagonalEntry;

                this.entryNormMax = this.calcEntryNormMax();

                this.invAndDetOfInverse = Objects.requireNonNull(invAndDetOfInverse);
            }

            @Override
            public BandMatrixDimension bandMatrixDimension() {
                return this.bandMatrixDimension;
            }

            @Override
            public Block2OrderSymmetricDiagonalMatrix target() {
                return this;
            }

            /**
             * @throws IndexOutOfBoundsException {@inheritDoc }
             */
            @Override
            public double valueAt(final int row, final int column) {

                switch (BandDimensionPositionState.positionStateAt(row, column, bandMatrixDimension)) {
                case DIAGONAL:
                    return diagonalEntry[row];
                case LOWER_BAND:
                    return subdiagonalEntry[row - 1];
                case UPPER_BAND:
                    return subdiagonalEntry[column - 1];
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
            public double entryNormMax() {
                return this.entryNormMax;
            }

            private double calcEntryNormMax() {
                return Math.max(ArraysUtil.normMax(this.diagonalEntry), ArraysUtil.normMax(this.subdiagonalEntry));
            }

            /**
             * @throws MatrixFormatMismatchException {@inheritDoc }
             * @throws IllegalArgumentException {@inheritDoc }
             * @throws NullPointerException {@inheritDoc }
             */
            @Override
            public Vector operate(Vector operand) {
                final VectorDimension vectorDimension = operand.vectorDimension();
                if (!bandMatrixDimension.dimension().rightOperable(vectorDimension)) {
                    throw new MatrixFormatMismatchException(
                            String.format(
                                    "右から演算不可:matrix:%s, operand:%s",
                                    bandMatrixDimension.dimension(), vectorDimension));
                }

                final int dimension = vectorDimension.intValue();

                final double[] operandEntry = operand.entryAsArray();
                final double[] resultEntry = new double[dimension];

                final double[] thisDiagonalEntry = this.diagonalEntry;
                final double[] thisSubdiagonalEntry = this.subdiagonalEntry;

                //対角成分
                for (int i = 0; i < dimension; i++) {
                    resultEntry[i] += thisDiagonalEntry[i] * operandEntry[i];
                }
                //狭義下三角成分
                for (int i = 0; i < dimension - 1; i++) {
                    resultEntry[i + 1] += thisSubdiagonalEntry[i] * operandEntry[i];
                }
                //狭義上三角成分
                for (int i = 0; i < dimension - 1; i++) {
                    resultEntry[i] += thisSubdiagonalEntry[i] * operandEntry[i + 1];
                }

                Vector.Builder builder = Vector.Builder.zeroBuilder(vectorDimension);
                builder.setEntryValue(resultEntry);
                return builder.build();
            }

            /**
             * @throws MatrixFormatMismatchException {@inheritDoc }
             * @throws IllegalArgumentException {@inheritDoc }
             * @throws NullPointerException {@inheritDoc }
             */
            @Override
            public Vector operateTranspose(Vector operand) {
                return this.operate(operand);
            }

            /**
             * このオブジェクトの文字列説明表現を返す.
             * 
             * <p>
             * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
             * おそらくは次のような表現であろう. <br>
             * {@literal @hashCode[dimension: %dimension, entry: %entry, block2Order]}
             * </p>
             * 
             * @return 説明表現
             */
            @Override
            public String toString() {
                return EntryReadableMatrix.toString(this, "block2Order");
            }

            @Override
            protected InverseAndDeterminantStructure<Block2OrderSymmetricDiagonalMatrix> createInvAndDetWrapper() {
                if (Objects.nonNull(this.invAndDetOfInverse)) {
                    return this.invAndDetOfInverse;
                }

                //ブロック対角行列の逆行列を生成
                double[] thisDiagonalEntry = this.diagonalEntry;
                double[] thisSubdiagonalEntry = this.subdiagonalEntry;
                final int dimension = thisDiagonalEntry.length;

                //1*1か2*2かの状態を管理する
                boolean state22 = false;

                /* 逆行列, 行列式のlogAbs, 行列式の符号を同時に計算する */
                double logAbsDeterminant = 0d;
                boolean sign = true;
                double[] inverseDiagEntry = new double[thisDiagonalEntry.length];
                double[] inverseSubDiagEntry = new double[thisSubdiagonalEntry.length];

                for (int i = 0; i < dimension; i++) {
                    if (i < dimension - 1 && thisSubdiagonalEntry[i] != 0.0) {
                        state22 = true;
                        continue;
                    }
                    if (!state22) {
                        double m_00 = thisDiagonalEntry[i];
                        double im_00 = 1 / m_00;
                        if (!EntryReadableMatrix.acceptValue(im_00)) {
                            return new InverseAndDeterminantStructure<>();
                        }
                        inverseDiagEntry[i] = im_00;

                        sign ^= m_00 < 0;
                        logAbsDeterminant += Exponentiation.log(Math.abs(m_00));

                        continue;
                    }
                    final double m_01 = thisSubdiagonalEntry[i - 1];
                    final double m_00 = thisDiagonalEntry[i - 1];
                    final double m_11 = thisDiagonalEntry[i];

                    double[] inv = this.blockInverseAndDeterminantValue(m_00, m_11, m_01);
                    if (Objects.isNull(inv)) {
                        return new InverseAndDeterminantStructure<>();
                    }

                    inverseDiagEntry[i - 1] = inv[0];
                    inverseDiagEntry[i] = inv[1];
                    inverseSubDiagEntry[i - 1] = inv[2];
                    logAbsDeterminant += inv[3];
                    sign ^= inv[4] < 0;

                    state22 = false;
                }

                DeterminantValues thisDet = new DeterminantValues(
                        logAbsDeterminant,
                        sign ? 1 : -1);
                //逆行列に埋め込まれるinverse: 逆行列の行列式とthisを埋め込む
                InverseAndDeterminantStructure<Block2OrderSymmetricDiagonalMatrix> invWrapper =
                        new InverseAndDeterminantStructure<>(
                                thisDet.createInverse(), this);

                Block2OrderSymmetricDiagonalMatrix invMatrix = new Block2OrderSymmetricDiagonalMatrixImpl(
                        this.bandMatrixDimension, inverseDiagEntry, inverseSubDiagEntry, invWrapper);

                return new InverseAndDeterminantStructure<>(thisDet, invMatrix);
            }

            /**
             * 与えられたブロック要素(2*2)に対して, 逆行列要素と行列式に関する要素を返す. <br>
             * その形式は, {@code double}配列であり, <br>
             * {@code [im_00, im_11, im_01, logAbsDet, signOfDet]} <br>
             * である. <br>
             * {@code signOfDet}は{@code 1.0}または{@code -1.0}である. <br>
             * ただし, 特異の場合のみ, {@code null}を返す.
             * 
             * @param m_00 00成分
             * @param m_11 11成分
             * @param m_01 01成分=10成分
             * @return 値を有する配列
             */
            private double[] blockInverseAndDeterminantValue(double m_00, double m_11, double m_01) {

                double scale = Math.max(Math.abs(m_00), Math.abs(m_11));
                scale = Math.max(scale, Math.abs(m_01));
                if (scale == 0.0) {
                    return null;
                }
                m_00 /= scale;
                m_11 /= scale;
                m_01 /= scale;

                double det = m_00 * m_11 - m_01 * m_01;
                if (Math.abs(det) < 1E-100) {
                    return null;
                }
                final double invDet = 1 / det;

                final double im_01 = -(m_01 * invDet) / scale;
                final double im_00 = (m_11 * invDet) / scale;
                final double im_11 = (m_00 * invDet) / scale;
                if (!(EntryReadableMatrix.acceptValue(im_00)
                        && EntryReadableMatrix.acceptValue(im_01)
                        && EntryReadableMatrix.acceptValue(im_11))) {
                    return null;
                }
                double[] out = {
                        im_00,
                        im_11,
                        im_01,
                        Exponentiation.log(Math.abs(det)) + 2 * Exponentiation.log(scale),
                        det > 0d ? 1.0 : -1.0
                };

                return out;
            }
        }

    }

}
