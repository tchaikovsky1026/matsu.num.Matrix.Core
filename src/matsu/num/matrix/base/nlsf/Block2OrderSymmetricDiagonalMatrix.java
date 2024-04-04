/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.4.4
 */
package matsu.num.matrix.base.nlsf;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.BandMatrix;
import matsu.num.matrix.base.BandMatrixDimension;
import matsu.num.matrix.base.Determinantable;
import matsu.num.matrix.base.EntryReadableMatrix;
import matsu.num.matrix.base.Invertible;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.VectorDimension;
import matsu.num.matrix.base.common.ArraysUtil;
import matsu.num.matrix.base.helper.matrix.SkeletalSymmetricInvertibleDeterminantableMatrix;
import matsu.num.matrix.base.helper.value.BandDimensionPositionState;
import matsu.num.matrix.base.helper.value.DeterminantValues;
import matsu.num.matrix.base.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * 1*1 あるいは 2*2の対称ブロック要素を持つ, ブロック対角行列とそれを係数に持つ連立方程式の解法を扱う.
 *
 * @author Matsuura Y.
 * @version 21.0
 */
interface Block2OrderSymmetricDiagonalMatrix
        extends BandMatrix, Symmetric,
        Invertible, Determinantable {

    @Override
    public abstract Block2OrderSymmetricDiagonalMatrix transpose();

    @Override
    public Optional<? extends Block2OrderSymmetricDiagonalMatrix> inverse();

    /**
     * <p>
     * ブロック対角行列のビルダ. <br>
     * スレッドセーフでない.
     * </p>
     */
    static final class Builder {

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
         * <p>
         * 対角要素:(<i>i</i>,<i>i</i>)要素を指定した値に置き換える.
         * </p>
         * 
         * <p>
         * 値が不正ならば, 正常値に修正される.
         * </p>
         *
         * @param index i, 対角成分index
         * @param value 置き換えた後の値
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (i,i)が行列内でない場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public void setDiagonal(final int index, double value) {
            if (Objects.isNull(this.diagonalEntry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!(0 <= index && index < this.bandMatrixDimension.dimension().rowAsIntValue())) {
                throw new IndexOutOfBoundsException(String.format("行列内部でない:(%s, %s)", index, index));
            }

            //値を修正する
            value = EntryReadableMatrix.modified(value);

            this.diagonalEntry[index] = value;
        }

        /**
         * <p>
         * 副対角要素:(<i>i</i> + 1,<i>i</i>)要素を指定した値に置き換える. <br>
         * 同時に(<i>i</i>,<i>i</i> + 1)の値も置き換わる.
         * </p>
         * 
         * <p>
         * 値が不正ならば, 正常値に修正される.
         * </p>
         *
         * @param index i, 副対角成分index
         * @param value 置き換えた後の値
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (i+1,i)が行列の帯領域内でない場合
         * @throws IllegalArgumentException valueを代入することでブロック対角行列でなくなる場合
         * @see EntryReadableMatrix#acceptValue(double)
         */
        public void setSubDiagonal(final int index, double value) {

            if (Objects.isNull(this.diagonalEntry)) {
                throw new IllegalStateException("すでにビルドされています");
            }
            if (!(0 <= index && index < this.bandMatrixDimension.dimension().rowAsIntValue() - 1)) {
                throw new IndexOutOfBoundsException(String.format("行列内部でない:(%s, %s)", index + 1, index));
            }
            //0以外が代入される場合にはその両側を確認する
            if (Double.compare(value, 0.0) != 0) {
                if (index >= 1 && Double.compare(this.subdiagonalEntry[index - 1], 0.0) != 0) {
                    throw new IllegalArgumentException(
                            String.format("この代入によりブロック対角行列でなくなる:(%s, %s)", index + 1, index));
                }
                if (index < this.bandMatrixDimension.dimension().rowAsIntValue() - 2
                        && Double.compare(this.subdiagonalEntry[index + 1], 0.0) != 0) {
                    throw new IllegalArgumentException(
                            String.format("この代入によりブロック対角行列でなくなる:(%s, %s)", index + 1, index));
                }
            }

            //値を修正する
            value = EntryReadableMatrix.modified(value);

            this.subdiagonalEntry[index] = value;
        }

        /**
         * 副対角要素:(<i>i</i> + 1,<i>i</i>)要素を0に置き換える. <br>
         * 同時に(<i>i</i>,<i>i</i> + 1)の値も置き換わる.
         *
         * @param index i, 副対角成分index
         * @throws IllegalStateException すでにビルドされている場合
         * @throws IndexOutOfBoundsException (i+1,i)が行列の帯領域内でない場合
         */
        public void setSubDiagonalToZero(final int index) {
            this.setSubDiagonal(index, 0.0);
        }

        /**
         * 対称ブロック行列をビルドする.
         *
         * @return 対称ブロック行列
         * @throws IllegalStateException すでにビルドされている場合
         */
        Block2OrderSymmetricDiagonalMatrix build() {
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
        static Builder zeroBuilder(final MatrixDimension matrixDimension) {
            return new Builder(matrixDimension);
        }

        private static final class Block2OrderSymmetricDiagonalMatrixImpl
                extends SkeletalSymmetricInvertibleDeterminantableMatrix<
                        Block2OrderSymmetricDiagonalMatrix, Block2OrderSymmetricDiagonalMatrix>
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
            private final InverstibleAndDeterminantStruct<Block2OrderSymmetricDiagonalMatrix> invAndDetOfInverse;

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
                    InverstibleAndDeterminantStruct<Block2OrderSymmetricDiagonalMatrix> invAndDetOfInverse) {
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
                                    "行列内部でない:matrix:%s, (row, column)=(%s, %s)",
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
            protected InverstibleAndDeterminantStruct<Block2OrderSymmetricDiagonalMatrix> createInvAndDetWrapper() {
                if (Objects.nonNull(this.invAndDetOfInverse)) {
                    return this.invAndDetOfInverse;
                }

                return new CreateInvAndDetWrapper(this).execute();
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

                private final Block2OrderSymmetricDiagonalMatrixImpl src;

                /*
                 * 配列は結果行列内に埋め込まれるので, このクラス内での値の書き換えに注意する.
                 */
                private boolean sign;
                private double[] inverseDiagEntry;
                private double[] inverseSubDiagEntry;

                //logAbsDetの計算結果保存
                private int shifting;
                private double absDetResidual;

                CreateInvAndDetWrapper(Block2OrderSymmetricDiagonalMatrixImpl src) {
                    super();
                    this.src = src;
                }

                /**
                 * 逆行列と行列式を生成する.
                 * 
                 * @return 逆行列と行列式
                 */
                public InverstibleAndDeterminantStruct<Block2OrderSymmetricDiagonalMatrix>
                        execute() {
                    double[] thisDiagonalEntry = this.src.diagonalEntry;
                    double[] thisSubdiagonalEntry = this.src.subdiagonalEntry;
                    final int dimension = thisDiagonalEntry.length;

                    this.sign = true;
                    this.inverseDiagEntry = new double[thisDiagonalEntry.length];
                    this.inverseSubDiagEntry = new double[thisSubdiagonalEntry.length];
                    this.shifting = 0;
                    this.absDetResidual = 1d;

                    //1*1か2*2かの状態を管理する
                    boolean state22 = false;

                    for (int i = 0; i < dimension; i++) {
                        if (i < dimension - 1 && thisSubdiagonalEntry[i] != 0.0) {
                            state22 = true;
                            continue;
                        }
                        if (!state22) {
                            State s = this.applyUnderState1(i, thisDiagonalEntry[i]);
                            if (s.equals(State.SINGULAR)) {
                                return new InverstibleAndDeterminantStruct<>();
                            }

                            continue;
                        }

                        State s = this.applyUnderState2(
                                i, thisDiagonalEntry[i - 1], thisDiagonalEntry[i], thisSubdiagonalEntry[i - 1]);

                        if (s.equals(State.SINGULAR)) {
                            return new InverstibleAndDeterminantStruct<>();
                        }

                        state22 = false;
                    }

                    double logAbsDeterminant = Math.log(absDetResidual)
                            + shifting * LOG_SHIFT_CONSTANT;

                    DeterminantValues thisDet = new DeterminantValues(
                            logAbsDeterminant,
                            sign ? 1 : -1);
                    //逆行列に埋め込まれるinverse: 逆行列の行列式とthisを埋め込む
                    InverstibleAndDeterminantStruct<Block2OrderSymmetricDiagonalMatrix> invWrapper =
                            new InverstibleAndDeterminantStruct<>(
                                    thisDet.createInverse(), this.src);

                    Block2OrderSymmetricDiagonalMatrix invMatrix = new Block2OrderSymmetricDiagonalMatrixImpl(
                            this.src.bandMatrixDimension, inverseDiagEntry, inverseSubDiagEntry, invWrapper);

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
                    this.determinantToField(abs_m_00, 1);

                    return State.REGULAR;
                }

                /**
                 * 2*2ブロックに対する処理.
                 * (i-1)と(i)を扱う.
                 * 
                 * @param i
                 * @param m_00
                 * @param m_01
                 * @param m_11
                 */
                private State applyUnderState2(int i, double m_00, double m_11, double m_01) {

                    double scale = this.calcScale(m_00, m_11, m_01);
                    double scaled_m_00 = m_00 / scale;
                    double scaled_m_11 = m_11 / scale;
                    double scaled_m_01 = m_01 / scale;

                    double scaledDet = scaled_m_00 * scaled_m_11 - scaled_m_01 * scaled_m_01;
                    if (Math.abs(scaledDet) < 1E-305) {
                        return State.SINGULAR;
                    }
                    final double invScaledDet = 1 / scaledDet;
                    final double im_01 = -(scaled_m_01 * invScaledDet) / scale;
                    final double im_00 = (scaled_m_11 * invScaledDet) / scale;
                    final double im_11 = (scaled_m_00 * invScaledDet) / scale;

                    if (!(EntryReadableMatrix.acceptValue(im_00)
                            && EntryReadableMatrix.acceptValue(im_01)
                            && EntryReadableMatrix.acceptValue(im_11))) {
                        return State.SINGULAR;
                    }
                    this.inverseDiagEntry[i - 1] = im_00;
                    this.inverseDiagEntry[i] = im_11;
                    this.inverseSubDiagEntry[i - 1] = im_01;
                    this.sign ^= scaledDet < 0;
                    double absScaleDet = Math.abs(scaledDet);

                    this.determinantToField(absScaleDet, 1);
                    this.determinantToField(scale, 2);

                    return State.REGULAR;
                }

                private double calcScale(double m_00, double m_11, double m_01) {
                    double absAC = Math.abs(m_00 * m_11);
                    double absBB = Math.abs(m_01 * m_01);
                    if (absAC > 1E+300 || absBB > 1E+300) {
                        return 1E+150;
                    }
                    if (absAC < 1E-300 && absBB < 1E-300) {
                        return 1E-150;
                    }
                    return 1d;
                }

                private void determinantToField(double value, int multiplicity) {

                    if (value > SHIFT_CONSTANT) {
                        if (value > SHIFT_CONSTANT_SQUARE) {
                            shifting += 2 * multiplicity;
                            value *= REVERSE_SHIFT_CONSTANT_SQUARE;
                        } else {
                            shifting += multiplicity;
                            value *= REVERSE_SHIFT_CONSTANT;
                        }
                    } else if (value < REVERSE_SHIFT_CONSTANT) {
                        if (value < REVERSE_SHIFT_CONSTANT_SQUARE) {
                            shifting -= 2 * multiplicity;
                            value *= SHIFT_CONSTANT_SQUARE;
                        } else {
                            shifting -= multiplicity;
                            value *= SHIFT_CONSTANT;
                        }
                    }

                    for (int c = 0; c < multiplicity; c++) {
                        this.absDetResidual *= value;
                        this.normalizeAbsDetResidual();
                    }
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
