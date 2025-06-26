/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2025.6.26
 */
package matsu.num.matrix.core.nlsf;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.PseudoRegularMatrixProcess;
import matsu.num.matrix.core.helper.value.MatrixRejectionConstant;
import matsu.num.matrix.core.validation.MatrixStructureAcceptance;

/**
 * {@link LUTypeSolver} の骨格実装.
 * 
 * <p>
 * このクラスでは,
 * {@link #inverse()}, {@link #signOfDeterminant()},
 * {@link #determinant()}, {@link #logAbsDeterminant()}
 * メソッドの適切な実装を提供する. <br>
 * これらの戻り値は {@link #createInverseDeterminantStruct()}
 * メソッドにより一度だけ計算, キャッシュされ,
 * 以降はそのキャッシュを戻す.
 * </p>
 * 
 * 
 * <hr>
 * 
 * <h2>使用上の注意</h2>
 * 
 * <p>
 * このクラスはインターフェースの骨格実装を提供するためのものであり,
 * 型として扱うべきではない. <br>
 * 具体的に, 次のような取り扱いは強く非推奨である.
 * </p>
 * 
 * <ul>
 * <li>このクラスを変数宣言の型として使う.</li>
 * <li>{@code instanceof} 演算子により, このクラスのサブタイプかを判定する.</li>
 * <li>インスタンスをこのクラスにキャストして使用する.</li>
 * </ul>
 * 
 * <p>
 * この骨格実装クラスの継承関係は積極的には維持されず,
 * このモジュールや関連モジュールの具象クラスが将来的にこのクラスのサブタイプでなくなる場合がある.
 * </p>
 * 
 * @author Matsuura Y.
 * @param <TT> ターゲット行列の型パラメータ, {@link #target()} の戻り値型をサブタイプで限定する.
 * @param <IT> 逆行列の型パラメータ, {@link #inverse()} の戻り値型をサブタイプで限定する.
 */
abstract class SkeletalLUTypeSolver<TT extends EntryReadableMatrix, IT extends Matrix>
        extends InversionDeterminantableImplementation<TT, IT> implements LUTypeSolver {

    /**
     * 唯一のコンストラクタ.
     */
    SkeletalLUTypeSolver() {
        super();
    }

    /**
     * このソルバーの名前を返す.
     * 
     * <p>
     * このメソッドは, {@link #toString()} で返す文字列の作成を補助するために用意されており,
     * 外部から呼ばれることは想定されておらず,
     * 公開してはならない.
     * </p>
     * 
     * <p>
     * クラス定義に対して固定値であるべきである.
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
     * {@code %solverName[target:%matrix]}
     * </p>
     */
    @Override
    public String toString() {
        return String.format(
                "%s[target:%s]", this.solverName(), this.target());
    }

    /**
     * {@link LUTypeSolver.Executor} の骨格実装.
     * 
     * <p>
     * このクラスでは,
     * {@link #accepts(EntryReadableMatrix)},
     * {@link #apply(EntryReadableMatrix)},
     * {@link #apply(EntryReadableMatrix, double)}
     * メソッドの適切な実装を提供する.
     * </p>
     * 
     * <p>
     * {@link #accepts(EntryReadableMatrix)} の実装では,
     * 正方行列でない場合は無条件にrejectされる. <br>
     * さらに, 正方行列に対して抽象メソッド {@link #acceptsConcretely(EntryReadableMatrix)}
     * により検証される.
     * </p>
     * 
     * <p>
     * {@link #apply(EntryReadableMatrix)},
     * {@link #apply(EntryReadableMatrix, double)} の実装では,
     * ({@code epsilon} の検証と) {@link #accepts(EntryReadableMatrix)}
     * による行列の正当性の検証が行われ, <br>
     * 正当な行列に対して {@link #applyConcretely(EntryReadableMatrix, double)}
     * によって行列分解が実行される.
     * </p>
     * 
     * 
     * <hr>
     * 
     * <h2>使用上の注意</h2>
     * 
     * <p>
     * このクラスはインターフェースの骨格実装を提供するためのものであり,
     * 型として扱うべきではない. <br>
     * 具体的に, 次のような取り扱いは強く非推奨である.
     * </p>
     * 
     * <ul>
     * <li>このクラスを変数宣言の型として使う.</li>
     * <li>{@code instanceof} 演算子により, このクラスのサブタイプかを判定する.</li>
     * <li>インスタンスをこのクラスにキャストして使用する.</li>
     * </ul>
     * 
     * <p>
     * この骨格実装クラスの継承関係は積極的には維持されず,
     * このモジュールや関連モジュールの具象クラスが将来的にこのクラスのサブタイプでなくなる場合がある.
     * </p>
     * 
     * @author Matsuura Y.
     * @param <MT> 対応する行列の型
     * @param <ST> 出力される行列分解の型
     */
    abstract static class Executor<
            MT extends EntryReadableMatrix, ST extends LUTypeSolver>
            implements LUTypeSolver.Executor<MT> {

        /**
         * 唯一のコンストラクタ.
         */
        Executor() {
            super();
        }

        /**
         * 正方行列に対して, 受け入れ可能かの詳細を判定する.
         * 
         * <p>
         * このメソッドは, {@link #accepts(EntryReadableMatrix)}
         * の具体的処理を実装するための抽象メソッドである. <br>
         * {@link #accepts(EntryReadableMatrix)} がコールされたときに引数が正方行列であるかどうかが検証され,
         * 正方行列であることが確定した行列がこの抽象メソッドに渡される. <br>
         * このメソッドの実装では, 正方行列に対して追加の条件を判定する.
         * </p>
         * 
         * <p>
         * このメソッドの公開, サブクラスからのコールはほとんど全ての場合に不適切である.
         * </p>
         * 
         * @param matrix 正方行列であることが確定した行列(当然nullでない)
         * @return 判定結果
         */
        abstract MatrixStructureAcceptance acceptsConcretely(MT matrix);

        /**
         * 受け入れ可能であることが確定した行列に対して, 行列分解を実際に実行する.
         * 
         * <p>
         * このメソッドは, {@link #apply(EntryReadableMatrix, double)}
         * の具体的処理を実装するための抽象メソッドである. <br>
         * {@link #apply(EntryReadableMatrix, double)} がコールされたときに {@code matrix}
         * が
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
         * @return 行列分解
         */
        abstract Optional<ST> applyConcretely(MT matrix, double epsilon);

        /**
         * @throws NullPointerException {@inheritDoc }
         */
        @Override
        public final MatrixStructureAcceptance accepts(MT matrix) {
            if (!matrix.matrixDimension().isSquare()) {
                return MatrixRejectionConstant.REJECTED_BY_NOT_SQUARE.get();
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
