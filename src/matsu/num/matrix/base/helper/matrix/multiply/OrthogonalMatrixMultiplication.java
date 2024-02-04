/**
 * 2024.1.19
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
import matsu.num.matrix.base.OrthogonalMatrix;
import matsu.num.matrix.base.SkeletalOrthogonalMatrix;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * 直交行列の行列積を扱う.
 * 
 * @author Matsuura Y.
 * @version 19.0
 */
public final class OrthogonalMatrixMultiplication {

    private static final OrthogonalMatrixMultiplication INSTANCE = new OrthogonalMatrixMultiplication();

    private OrthogonalMatrixMultiplication() {
        if (Objects.nonNull(INSTANCE)) {
            throw new AssertionError();
        }
    }

    /**
     * 1個以上の直交行列に対し, それらの行列積を返す.
     * 
     * @param first 行列積の左端の行列
     * @param following firstに続く行列, 左から順番
     * @return 直交行列の行列積
     * @throws MatrixFormatMismatchException 行列のサイズが整合せずに行列積が定義できない場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    public OrthogonalMatrix apply(OrthogonalMatrix first, OrthogonalMatrix... following) {
        if (following.length == 0) {
            return Objects.requireNonNull(first);
        }
        return new MultiplyingSeries(first, following);
    }

    /**
     * 直交行列の行列積を行うインスタンスを返す.
     * 
     * @return インスタンス
     */
    public static OrthogonalMatrixMultiplication instance() {
        return INSTANCE;
    }

    /**
     * 直交行列の行列積を表現する行列.
     */
    private static final class MultiplyingSeries
            extends SkeletalOrthogonalMatrix
            implements MultipliedOrthogonalMatrix {

        private final Deque<OrthogonalMatrix> series;
        private final MatrixDimension matrixDimension;

        private final OrthogonalMatrix transpose;

        MultiplyingSeries(OrthogonalMatrix first, OrthogonalMatrix... following) {
            this.transpose = null;

            Deque<OrthogonalMatrix> rawSeries = new LinkedList<>();
            rawSeries.add(Objects.requireNonNull(first));
            for (OrthogonalMatrix mx : following) {
                rawSeries.add(Objects.requireNonNull(mx));
            }

            this.series = expand(
                    requireFormatMatch(rawSeries)
                            .orElseThrow(() -> new MatrixFormatMismatchException("行列積が定義不可な組み合わせ")));

            this.matrixDimension = this.series.getFirst().matrixDimension();
        }

        /**
         * このクラス内部から呼ばれる. <br>
         * transposeを表現するためのコンストラクタ.
         * 
         */
        private MultiplyingSeries(MatrixDimension matrixDimension,
                Deque<OrthogonalMatrix> series, OrthogonalMatrix transpose) {
            super();
            this.series = series;
            //直交行列は正方行列なので, 行列次元をそのまま得る
            this.matrixDimension = matrixDimension;
            this.transpose = Objects.requireNonNull(transpose);
        }

        /**
         * 引数の行列たちからDequeを作成する. <br>
         * 要素に行列積が含まれる場合は展開する.
         * 
         * @param first 行列積の左端の行列
         * @param following firstに続く行列, 左から順番
         * @return 行列積を表す一連の行列のDeque
         */
        private static Deque<OrthogonalMatrix> expand(Collection<? extends OrthogonalMatrix> rawSeries) {
            Deque<OrthogonalMatrix> series = new LinkedList<>();
            for (OrthogonalMatrix mx : rawSeries) {
                if (mx instanceof MultipliedOrthogonalMatrix) {
                    //要素Matrixが行列積を表しているなら展開する
                    series.addAll(((MultipliedOrthogonalMatrix) mx).toSeries());
                    continue;
                }
                series.add(Objects.requireNonNull(mx));

            }
            return series;
        }

        /**
         * サイズの整合性を検証する.
         * 
         * @param series 行列積を表す一連の行列
         * @return seriesと等価なオプショナル, 整合しない場合は空
         */
        private static Optional<Deque<OrthogonalMatrix>> requireFormatMatch(Deque<OrthogonalMatrix> series) {

            //サイズの整合性の検証
            //直交行列は正方行列なので,行列サイズが一致することを求める
            OrthogonalMatrix former = null;
            for (Iterator<OrthogonalMatrix> ite = series.iterator(); ite.hasNext();) {
                OrthogonalMatrix latter = ite.next();
                if (Objects.nonNull(former)) {
                    if (!(former.matrixDimension().equals(latter.matrixDimension()))) {
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
            for (Iterator<OrthogonalMatrix> ite = this.series.descendingIterator(); ite.hasNext();) {
                Matrix mx = ite.next();
                result = mx.operate(result);
            }
            return result;
        }

        @Override
        public Vector operateTranspose(Vector operand) {
            Vector result = Objects.requireNonNull(operand);
            for (Iterator<OrthogonalMatrix> ite = this.series.iterator(); ite.hasNext();) {
                Matrix mx = ite.next();
                result = mx.operateTranspose(result);
            }
            return result;
        }

        @Override
        public Deque<OrthogonalMatrix> toSeries() {
            return new LinkedList<>(this.series);
        }

        @Override
        protected OrthogonalMatrix createInverse() {
            if (Objects.nonNull(this.transpose)) {
                return this.transpose;
            }

            Deque<OrthogonalMatrix> transposedSeries = new LinkedList<>();
            for (Iterator<OrthogonalMatrix> ite = this.series.descendingIterator(); ite.hasNext();) {
                transposedSeries.add(ite.next().transpose());
            }
            return new MultiplyingSeries(this.matrixDimension.transpose(), transposedSeries, this);
        }

        @Override
        public String toString() {
            return OrthogonalMatrix.toString(this);
        }
    }
}
