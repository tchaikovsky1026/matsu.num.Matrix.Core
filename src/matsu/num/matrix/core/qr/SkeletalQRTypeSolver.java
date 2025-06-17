/*
 * Copyright © 2025 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2025.6.16
 */
package matsu.num.matrix.core.qr;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.PseudoRegularMatrixProcess;
import matsu.num.matrix.core.helper.value.MatrixRejectionConstant;
import matsu.num.matrix.core.lazy.ImmutableLazyCacheSupplier;
import matsu.num.matrix.core.nlsf.SolvingFactorizationExecutor;
import matsu.num.matrix.core.validation.MatrixStructureAcceptance;

/**
 * {@link QRTypeSolver} の骨格実装. <br>
 * 主に, 戻り値型の共変性に対する型パラメータと, {@link Object#toString()} の実装,
 * {@link QRTypeSolver#inverse()} のキャッシュを提供する.
 * 
 * @author Matsuura Y.
 * @param <TT> ターゲット行列の型パラメータ, {@link #target()} の戻り値型を具象クラスでバインドする.
 * @param <IT> 一般化逆行列の型パラメータ, {@link #inverse()} の戻り値型を具象クラスでバインドする.
 */
abstract class SkeletalQRTypeSolver<TT extends EntryReadableMatrix, IT extends Matrix>
        implements QRTypeSolver {

    //継承先のオーバーライドメソッドに依存するため, 遅延初期化される
    private final Supplier<? extends IT> invMarixSupplier;

    /**
     * 非公開の唯一のコンストラクタ.
     */
    SkeletalQRTypeSolver() {
        super();

        this.invMarixSupplier = ImmutableLazyCacheSupplier.of(
                () -> this.createInverse());
    }

    @Override
    public abstract TT target();

    @Override
    public final IT inverse() {
        return this.invMarixSupplier.get();
    }

    /**
     * ターゲット行列に関する, 一般化逆行列を計算する抽象メソッド. <br>
     * インスタンスが生成されてから一度だけ呼ばれる. <br>
     * 公開してはいけない.
     * 
     * @return 一般化逆行列
     */
    abstract IT createInverse();

    /**
     * このソルバーの名前を返す.
     * 
     * <p>
     * クラス定義に対して固定値であるべきである. <br>
     * デフォルトではシンプルなクラス名を返す.
     * </p>
     * 
     * <p>
     * このメソッドは, {@link #toString()} で返す文字列の作成を補助するために用意されており,
     * 外部から呼ばれることは想定されておらず,
     * 公開してはならない.
     * </p>
     * 
     * @return ソルバーの名前
     * @see #toString()
     */
    String solverName() {
        return this.getClass().getSimpleName();
    }

    /**
     * このクラスの文字列説明表現を提供する.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code %solverName[target: %matrix]}
     * </p>
     */
    @Override
    public String toString() {
        return "%s[target: %s]".formatted(
                this.solverName(), this.target());
    }

    /**
     * {@link SolvingFactorizationExecutor} の骨格実装.
     * 
     * <p>
     * このクラスでは,
     * {@link #accepts(Matrix)}, {@link #apply(Matrix)},
     * {@link #apply(Matrix, double)}
     * メソッドの適切な実装を提供する.
     * </p>
     * 
     * <p>
     * {@link #accepts(Matrix)} の実装では,
     * 正方行列でない場合は無条件にrejectされる. <br>
     * さらに, 正方行列に対して抽象メソッド {@link #acceptsConcretely(Matrix)}
     * により検証される.
     * </p>
     * 
     * <p>
     * {@link #apply(Matrix)}, {@link #apply(Matrix, double)} の実装では,
     * ({@code epsilon} の検証と) {@link #accepts(Matrix)} による行列の正当性の検証が行われ, <br>
     * 正当な行列に対して {@link #applyConcretely(Matrix, double)} によって行列分解が実行される.
     * </p>
     * 
     * @param <MT> 対応する行列の型パラメータ
     * @param <ST> 出力されるQR分解の型,
     *            {@link QRTypeSolver.Executor#apply(EntryReadableMatrix)}
     *            の戻り値型を具象クラスでバインドする.
     */
    static abstract class Executor<
            MT extends EntryReadableMatrix, ST extends QRTypeSolver>
            implements QRTypeSolver.Executor<MT> {

        /**
         * 唯一のコンストラクタ.
         */
        Executor() {
            super();
        }

        /**
         * 正方 &middot; 縦長行列に対して, 受け入れ可能かの詳細を判定する.
         * 
         * <p>
         * このメソッドは, {@link #accepts(EntryReadableMatrix)}
         * の具体的処理を実装するための抽象メソッドである. <br>
         * {@link #accepts(EntryReadableMatrix)} がコールされたときに引数が正方 &middot;
         * 縦長行列であるかどうかが検証され,
         * 正方 &middot; 縦長行列であることが確定した行列がこの抽象メソッドに渡される. <br>
         * このメソッドの実装では, 正方 &middot; 縦長行列に対して追加の条件を判定する.
         * </p>
         * 
         * <p>
         * このメソッドの公開, サブクラスからのコールはほとんど全ての場合に不適切である.
         * </p>
         * 
         * @param matrix 正方 &middot; 縦長行列であることが確定した行列(当然nullでない)
         * @return 判定結果
         */
        abstract MatrixStructureAcceptance acceptsConcretely(MT matrix);

        /**
         * 受け入れ可能であることが確定した行列に対して, 行列分解を実際に実行する.
         * 
         * <p>
         * このメソッドは, {@link #apply(EntryReadableMatrix, double)}
         * の具体的処理を実装するための抽象メソッドである. <br>
         * {@link #apply(EntryReadableMatrix, double)} がコールされたときに
         * {@code matrix} が
         * {@link #accepts(EntryReadableMatrix)} により検証, {@code epsilon} も検証され,
         * 不適切の場合は例外をスローする. <br>
         * 適切であることが確定した {@code matrix} 行列と
         * {@code epsilon} がこの抽象メソッドに渡される. <br>
         * このメソッドの実装では, 行列分解を実行し, 行列分解かもしくは空オプショナルを返す. <br>
         * このメソッドに渡される引数は正当であるため, 例外をスローしてはならない.
         * </p>
         * 
         * <p>
         * このメソッドの公開, サブクラスからのコールはほとんど全ての場合に不適切である.
         * </p>
         * 
         * @param matrix 受け入れ可能であることが確定した行列
         * @param epsilon 正常(0以上の有限数)であることが確定した相対epsilon
         * @return QR分解
         */
        abstract Optional<ST> applyConcretely(MT matrix, double epsilon);

        /**
         * @throws NullPointerException {@inheritDoc }
         */
        @Override
        public final MatrixStructureAcceptance accepts(MT matrix) {
            if (matrix.matrixDimension().isHorizontal()) {
                return MatrixRejectionConstant.REJECTED_BY_HORIZONTAL.get();
            }
            return this.acceptsConcretely(matrix);
        }

        /**
         * @throws IllegalArgumentException {@inheritDoc }
         * @throws NullPointerException {@inheritDoc }
         */
        @Override
        public final Optional<ST> apply(MT matrix, double epsilon) {
            if (!Double.isFinite(epsilon) || epsilon < 0) {
                throw new IllegalArgumentException(String.format("不正な値:epsilon=%s", epsilon));
            }

            MatrixStructureAcceptance acceptance = this.accepts(matrix);
            if (acceptance.isReject()) {
                throw acceptance.getException(matrix);
            }

            return this.applyConcretely(Objects.requireNonNull(matrix), epsilon);
        }

        @Override
        public final Optional<ST> apply(MT matrix) {
            return this.apply(matrix, PseudoRegularMatrixProcess.DEFAULT_EPSILON);
        }

        /**
         * このインスタンスの文字列表現を返す.
         */
        @Override
        public String toString() {
            Deque<Class<?>> enclosingClassLevels = new LinkedList<>();

            Class<?> currentLevel = this.getClass();
            while (Objects.nonNull(currentLevel)) {
                enclosingClassLevels.add(currentLevel);
                currentLevel = currentLevel.getEnclosingClass();
            }

            StringBuilder sb = new StringBuilder();
            for (Iterator<Class<?>> ite = enclosingClassLevels.descendingIterator();
                    ite.hasNext();) {
                Class<?> clazz = ite.next();
                sb.append(clazz.getSimpleName());
                if (ite.hasNext()) {
                    sb.append('.');
                }
            }

            return sb.toString();
        }
    }
}
