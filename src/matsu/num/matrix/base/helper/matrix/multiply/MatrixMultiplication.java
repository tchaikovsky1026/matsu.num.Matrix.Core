/**
 * 2023.8.21
 */
package matsu.num.matrix.base.helper.matrix.multiply;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.SkeletalMatrix;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.UnitMatrix;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.exception.MatrixFormatMismatchException;
import matsu.num.matrix.base.exception.MatrixNotSymmetricException;

/**
 * 行列積を扱う.
 * 
 * @author Matsuura Y.
 * @version 15.1
 */
public final class MatrixMultiplication {

    private static final MatrixMultiplication INSTANCE = new MatrixMultiplication();

    private MatrixMultiplication() {
        if (Objects.nonNull(INSTANCE)) {
            throw new AssertionError();
        }
    }

    /**
     * 1個以上の行列に対し, それらの行列積を返す.
     * 
     * @param first 行列積の左端の行列
     * @param following firstに続く行列, 左から順番
     * @return 行列積
     * @throws MatrixFormatMismatchException 行列のサイズが整合せずに行列積が定義できない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public Matrix apply(Matrix first, Matrix... following) {
        if (following.length == 0) {
            return Objects.requireNonNull(first);
        }
        return new MultiplyingSeries(first, following);
    }

    /**
     * 行列の対称化二乗を返す. <br>
     * すなわち, 与えた行列Aに対して, AA<sup>T</sup>を返す. <br>
     * 戻り値には{@link Symmetric}が付与されている.
     * 
     * @param original 元の行列
     * @return 対称化二乗
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public Matrix symmetrizedSquare(Matrix original) {
        return MatrixMultiplication.instance().symmetricMultiply(
                UnitMatrix.matrixOf(MatrixDimension.square(original.matrixDimension().columnAsIntValue())),
                original);
    }

    /**
     * 対称な行列積を返す. <br>
     * すなわち, 与えた行列L,Dに対して, LDL<sup>T</sup>を返す. <br>
     * 戻り値には{@link Symmetric}が付与されている. <br>
     * 与える行列Dには{@link Symmetric}が付与されていなければならない.
     * 
     * @param mid 行列D, 中央の行列
     * @param leftSide 行列L, 左サイドの行列
     * @return 対称な行列積
     * @throws MatrixNotSymmetricException 中央の行列が対称でない場合
     * @throws MatrixFormatMismatchException 行列のサイズが整合せずに行列積が定義できない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public Matrix symmetricMultiply(Matrix mid, Matrix leftSide) {
        return new SymmetricMultipliedMatrix(mid, leftSide);
    }

    /**
     * 行列積を行うインスタンスを返す.
     * 
     * @return インスタンス
     */
    public static MatrixMultiplication instance() {
        return INSTANCE;
    }

    /**
     * 行列積を表現する行列.
     */
    private static final class MultiplyingSeries extends SkeletalMatrix implements MultipliedMatrix {

        private final Deque<Matrix> series;
        private final MatrixDimension matrixDimension;

        private final Matrix transpose;

        public MultiplyingSeries(Matrix first, Matrix... following) {
            this.transpose = null;

            Deque<Matrix> rawSeries = new LinkedList<>();
            rawSeries.add(Objects.requireNonNull(first));
            for (Matrix mx : following) {
                rawSeries.add(Objects.requireNonNull(mx));
            }

            this.series = expand(
                    requireFormatMatch(rawSeries)
                            .orElseThrow(() -> new MatrixFormatMismatchException("行列積が定義不可な組み合わせ")));

            this.matrixDimension = MatrixDimension.rectangle(
                    this.series.getFirst().matrixDimension().rowAsIntValue(),
                    this.series.getLast().matrixDimension().columnAsIntValue());
        }

        /**
         * このクラス内部から呼ばれる. <br>
         * transposeを表現するためのコンストラクタ. 
         * 
         * @param matrixDimension
         * @param series
         * @param transpose
         */
        private MultiplyingSeries(MatrixDimension matrixDimension, Deque<Matrix> series, Matrix transpose) {
            this.matrixDimension = matrixDimension;
            this.series = series;
            this.transpose = Objects.requireNonNull(transpose);
        }

        /**
         * 引数の行列たちからDequeを作成する. <br>
         * 要素に行列積が含まれる場合は展開する.
         * 
         * @return 行列積を表す一連の行列のDeque
         */
        private static Deque<Matrix> expand(Collection<? extends Matrix> rawSeries) {
            Deque<Matrix> series = new LinkedList<>();
            for (Matrix mx : rawSeries) {
                if (mx instanceof MultipliedMatrix) {
                    //要素Matrixが行列積を表しているなら展開する
                    series.addAll(((MultipliedMatrix) mx).toSeries());
                    continue;
                }
                series.add(mx);
            }
            return series;
        }

        /**
         * サイズの整合性を検証する. 
         * 
         * @param series 行列積を表す一連の行列
         * @return seriesと等価なオプショナル, 整合しない場合は空
         */
        private static Optional<Deque<Matrix>> requireFormatMatch(Deque<Matrix> series) {

            //サイズの整合性の検証
            Matrix former = null;
            for (Iterator<Matrix> ite = series.iterator(); ite.hasNext();) {
                Matrix latter = ite.next();
                if (Objects.nonNull(former)) {
                    if (former.matrixDimension().columnAsIntValue() != latter.matrixDimension().rowAsIntValue()) {
                        return Optional.empty();
                    }
                }
                former = latter;
            }
            return Optional.of(series);
        }

        @Override
        public MatrixDimension matrixDimension() {
            return this.matrixDimension;
        }

        @Override
        public Vector operate(Vector operand) {
            Vector result = Objects.requireNonNull(operand);
            for (Iterator<Matrix> ite = this.series.descendingIterator(); ite.hasNext();) {
                Matrix mx = ite.next();
                result = mx.operate(result);
            }
            return result;
        }

        @Override
        public Vector operateTranspose(Vector operand) {
            Vector result = Objects.requireNonNull(operand);
            for (Iterator<Matrix> ite = this.series.iterator(); ite.hasNext();) {
                Matrix mx = ite.next();
                result = mx.operateTranspose(result);
            }
            return result;
        }

        @Override
        protected Matrix createTranspose() {
            if (Objects.nonNull(this.transpose)) {
                return this.transpose;
            }

            Deque<Matrix> transposedSeries = new LinkedList<>();
            for (Iterator<Matrix> ite = this.series.descendingIterator(); ite.hasNext();) {
                transposedSeries.add(ite.next().transpose());
            }
            return new MultiplyingSeries(this.matrixDimension.transpose(), transposedSeries, this);
        }

        @Override
        public Deque<Matrix> toSeries() {
            return new LinkedList<>(this.series);
        }

        @Override
        public String toString() {
            return MultipliedMatrix.toString(this);
        }

    }

    /**
     * 対称な行列積. <br>
     * LDL^T
     */
    private static final class SymmetricMultipliedMatrix extends SkeletalMatrix implements MultipliedMatrix, Symmetric {

        private final MatrixDimension matrixDimension;
        private final Matrix mxD;
        private final Matrix mxL;

        private final Deque<Matrix> series;

        /**
         * 
         * @param mid mxD
         * @param leftSide mxL 
         * @throws MatrixNotSymmetricException 中央の行列が対称でない場合
         * @throws MatrixFormatMismatchException 行列のサイズが整合せずに行列積が定義できない場合
         * @throws NullPointerException
         */
        public SymmetricMultipliedMatrix(Matrix mid, Matrix leftSide) {
            this.mxD = Objects.requireNonNull(mid);
            this.mxL = Objects.requireNonNull(leftSide);
            if (!(this.mxD instanceof Symmetric)) {
                throw new MatrixNotSymmetricException("中央の行列がSymmetricでない");
            }
            if (this.mxL.matrixDimension().columnAsIntValue() != this.mxD.matrixDimension().columnAsIntValue()) {
                throw new MatrixFormatMismatchException("行列積が定義できない");
            }
            this.matrixDimension = MatrixDimension.square(this.mxL.matrixDimension().rowAsIntValue());

            Deque<Matrix> rawSeries = new LinkedList<>();
            rawSeries.add(leftSide);
            rawSeries.add(mid);
            rawSeries.add(leftSide.transpose());
            this.series = expand(rawSeries);
        }

        /**
         * 引数の行列たちからDequeを作成する. <br>
         * 要素に行列積が含まれる場合は展開する.
         * 
         * @param first 行列積の左端の行列
         * @param following firstに続く行列, 左から順番
         * @return 行列積を表す一連の行列のDeque
         */
        private static Deque<Matrix> expand(Collection<? extends Matrix> rawSeries) {
            Deque<Matrix> series = new LinkedList<>();
            for (Matrix mx : rawSeries) {
                if (mx instanceof MultipliedMatrix) {
                    //要素Matrixが行列積を表しているなら展開する
                    series.addAll(((MultipliedMatrix) mx).toSeries());
                    continue;
                }
                series.add(mx);
            }
            return series;
        }

        @Override
        public MatrixDimension matrixDimension() {
            return this.matrixDimension;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public Vector operate(Vector operand) {
            Vector result = Objects.requireNonNull(operand);
            for (Iterator<Matrix> ite = this.series.descendingIterator(); ite.hasNext();) {
                Matrix mx = ite.next();
                result = mx.operate(result);
            }
            return result;
        }

        @Override
        public Vector operateTranspose(Vector operand) {
            return this.operate(operand);
        }

        @Override
        public Deque<? extends Matrix> toSeries() {
            return new LinkedList<>(this.series);
        }

        @Override
        public String toString() {
            return MultipliedMatrix.toString(this, "symmetric");
        }
    }
}
