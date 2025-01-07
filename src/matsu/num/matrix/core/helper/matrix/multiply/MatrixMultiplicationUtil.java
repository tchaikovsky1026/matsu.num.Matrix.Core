/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.1.7
 */
package matsu.num.matrix.core.helper.matrix.multiply;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.SkeletalAsymmetricMatrix;
import matsu.num.matrix.core.SkeletalSymmetricMatrix;
import matsu.num.matrix.core.Symmetric;
import matsu.num.matrix.core.UnitMatrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;
import matsu.num.matrix.core.validation.MatrixNotSymmetricException;

/**
 * 行列積に実行を扱う.
 * 
 * @author Matsuura Y.
 * @version 26.1
 */
public final class MatrixMultiplicationUtil {

    private MatrixMultiplicationUtil() {
        //インスタンス化不可
        throw new AssertionError();
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
    public static Matrix apply(Matrix first, Matrix... following) {
        return MultiplyingSeries.from(first, following);
    }

    /**
     * 行列の対称化二乗を返す. <br>
     * すなわち, 与えた行列Aに対して, AA<sup>T</sup>を返す. <br>
     * 戻り値には {@link Symmetric} が付与されている.
     * 
     * @param original 元の行列
     * @return 対称化二乗
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static Matrix symmetrizedSquare(Matrix original) {
        return MatrixMultiplicationUtil.symmetricMultiply(
                UnitMatrix.matrixOf(MatrixDimension.square(original.matrixDimension().columnAsIntValue())),
                original);
    }

    /**
     * 対称な行列積を返す. <br>
     * すなわち, 与えた行列L,Dに対して, LDL<sup>T</sup>を返す. <br>
     * 戻り値には {@link Symmetric} が付与されている. <br>
     * 与える行列Dには {@link Symmetric} が付与されていなければならない.
     * 
     * @param mid 行列D, 中央の行列
     * @param leftSide 行列L, 左サイドの行列
     * @return 対称な行列積
     * @throws MatrixNotSymmetricException 中央の行列 (D) が対称でない場合
     * @throws MatrixFormatMismatchException 行列のサイズが整合せずに行列積が定義できない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public static Matrix symmetricMultiply(Matrix mid, Matrix leftSide) {
        return new SymmetricMultipliedMatrix(mid, leftSide);
    }

    /**
     * 行列積を表現する行列.
     */
    private static final class MultiplyingSeries
            extends SkeletalAsymmetricMatrix<MultipliedMatrix> implements MultipliedMatrix {

        final Deque<Matrix> series;
        final MatrixDimension matrixDimension;

        /**
         * 唯一のコンストラクタ.
         */
        MultiplyingSeries(MatrixDimension matrixDimension, Deque<Matrix> series) {
            super();
            this.matrixDimension = matrixDimension;
            this.series = series;
        }

        @Override
        public final MatrixDimension matrixDimension() {
            return this.matrixDimension;
        }

        @Override
        public final Vector operate(Vector operand) {
            Vector result = Objects.requireNonNull(operand);
            for (Iterator<Matrix> ite = this.series.descendingIterator(); ite.hasNext();) {
                Matrix mx = ite.next();
                result = mx.operate(result);
            }
            return result;
        }

        @Override
        public final Vector operateTranspose(Vector operand) {
            Vector result = Objects.requireNonNull(operand);
            for (Iterator<Matrix> ite = this.series.iterator(); ite.hasNext();) {
                Matrix mx = ite.next();
                result = mx.operateTranspose(result);
            }
            return result;
        }

        /**
         * <p>
         * 行列積の転置は行列積で表現される. <br>
         * それを実現することを意図して,
         * {@link MultipliedMatrix} を返すように実装されなければならない.
         * </p>
         */
        @Override
        protected MultipliedMatrix createTranspose() {

            Deque<Matrix> transposedSeries = new LinkedList<>();
            for (Iterator<Matrix> ite = this.series.descendingIterator();
                    ite.hasNext();) {
                transposedSeries.add(ite.next().transpose());
            }
            return new TransposeAttachedMultipliedMatrix(
                    new MultiplyingSeries(this.matrixDimension.transpose(), transposedSeries),
                    this);
        }

        @Override
        public final Deque<Matrix> toSeries() {
            return new LinkedList<>(this.series);
        }

        /**
         * @throws MatrixFormatMismatchException 行列のサイズが整合せずに行列積が定義できない場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        static Matrix from(Matrix first, Matrix... following) {
            if (following.length == 0) {
                return Objects.requireNonNull(first);
            }

            Deque<Matrix> rawSeries = new LinkedList<>();
            rawSeries.add(Objects.requireNonNull(first));
            for (Matrix mx : following) {
                rawSeries.add(Objects.requireNonNull(mx));
            }

            return expand(rawSeries);
        }

        /**
         * 引数の行列たちを展開して行列積を作成する. <br>
         * 要素に行列積が含まれる場合は展開する.
         * nullチェックは行っていない.
         * おそらくサイズ2以上でなければならない.
         * 内部で新しくDequeが生成されるので, 呼び出しもとで複製する必要はない.
         * 
         * @throws MatrixFormatMismatchException 行列のサイズが整合せずに行列積が定義できない場合
         */
        static MultipliedMatrix expand(Deque<Matrix> rawSeries) {
            assert rawSeries.size() >= 2 : "サイズ2以上でない";

            requireFormatMatch(rawSeries)
                    .orElseThrow(
                            () -> new MatrixFormatMismatchException(
                                    "行列積が定義不可な組み合わせ"));

            MatrixDimension matrixDimension = MatrixDimension.rectangle(
                    rawSeries.getFirst().matrixDimension().rowAsIntValue(),
                    rawSeries.getLast().matrixDimension().columnAsIntValue());

            Deque<Matrix> series = new LinkedList<>();
            for (Matrix mx : rawSeries) {
                if (mx instanceof MultipliedMatrix castedMx) {
                    //要素Matrixが行列積を表しているなら展開する
                    series.addAll((castedMx).toSeries());
                    continue;
                }
                series.add(mx);
            }

            return new MultiplyingSeries(matrixDimension, series);
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
    }

    /**
     * 対称な行列積. <br>
     * LDL^T
     */
    private static final class SymmetricMultipliedMatrix
            extends SkeletalSymmetricMatrix<SymmetricMultipliedMatrix> implements MultipliedMatrix {

        private final MultipliedMatrix wrappedSeriesMatrix;

        /**
         * @throws MatrixNotSymmetricException 中央の行列が対称でない場合
         * @throws MatrixFormatMismatchException 行列のサイズが整合せずに行列積が定義できない場合
         * @throws NullPointerException 引数にnullが含まれる場合
         */
        SymmetricMultipliedMatrix(Matrix mid, Matrix leftSide) {
            if (!(Objects.requireNonNull(mid) instanceof Symmetric)) {
                throw new MatrixNotSymmetricException("中央の行列がSymmetricでない");
            }
            if (leftSide.matrixDimension().columnAsIntValue() != mid.matrixDimension().columnAsIntValue()) {
                throw new MatrixFormatMismatchException("行列積が定義できない");
            }

            Deque<Matrix> series = new LinkedList<>();
            series.add(leftSide);
            series.add(mid);
            series.add(leftSide.transpose());
            this.wrappedSeriesMatrix = MultiplyingSeries.expand(series);
        }

        @Override
        public MatrixDimension matrixDimension() {
            return this.wrappedSeriesMatrix.matrixDimension();
        }

        /**
         * 外部からの呼び出し不可.
         * 
         * @return -
         */
        @Override
        protected SymmetricMultipliedMatrix self() {
            return this;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public Vector operate(Vector operand) {
            return this.wrappedSeriesMatrix.operate(operand);
        }

        @Override
        public Deque<? extends Matrix> toSeries() {
            return this.wrappedSeriesMatrix.toSeries();
        }

        @Override
        public String toString() {
            return this.wrappedSeriesMatrix.toString();
        }
    }

    /**
     * 転置行列を直接紐づけるように {@link MultipliedMatrix} をラップする. <br>
     * オリジナルの transpose は呼ばれなくなる.
     */
    private static final class TransposeAttachedMultipliedMatrix implements MultipliedMatrix {

        private final MultipliedMatrix original;
        private final MultipliedMatrix transpose;

        /**
         * 唯一のコンストラクタ.
         * 引数の正当性は検査されていない.
         */
        TransposeAttachedMultipliedMatrix(
                MultipliedMatrix original, MultipliedMatrix transpose) {
            super();
            this.original = original;
            this.transpose = transpose;
        }

        @Override
        public MatrixDimension matrixDimension() {
            return this.original.matrixDimension();
        }

        @Override
        public Vector operate(Vector operand) {
            return this.original.operate(operand);
        }

        @Override
        public Vector operateTranspose(Vector operand) {
            return this.original.operateTranspose(operand);
        }

        @Override
        public MultipliedMatrix transpose() {
            return this.transpose;
        }

        @Override
        public Deque<? extends Matrix> toSeries() {
            return this.original.toSeries();
        }

        @Override
        public String toString() {
            return String.format(
                    "Matrix[dim:%s]",
                    this.matrixDimension());
        }
    }
}
