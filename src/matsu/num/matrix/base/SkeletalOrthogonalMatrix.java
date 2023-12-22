/**
 * 2023.12.4
 */
package matsu.num.matrix.base;

import java.util.Optional;
import java.util.function.Supplier;

import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;

/**
 * {@link OrthogonalMatrix}の骨格実装. <br>
 * {@link OrthogonalMatrix#inverse()}の実装の提供が主な効果である.
 * 
 * <p>
 * この骨格実装は, {@linkplain #inverse()}メソッドの実装を提供し,
 * 同時にオーバーライドを禁止する. <br>
 * このメソッドにより返される逆行列の{@linkplain OrthogonalMatrix#inverse()}による戻り値は,
 * {@code this}と同一である.
 * </p>
 * 
 * <p>
 * {@linkplain OrthogonalMatrix#inverse()}は関数的に振る舞う. <br>
 * この骨格実装は,
 * 初めて{@linkplain #inverse()}または{@linkplain #transpose()}が呼ばれたときに逆行列(転置行列)を{@linkplain #createInverse()}によって生成し,
 * 以後はそれを使いまわすキャッシュ仕組みを提供している.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 17.2
 */
public abstract class SkeletalOrthogonalMatrix implements OrthogonalMatrix {

    //逆行列(転置行列)を生成するサプライヤ
    private final Supplier<Optional<OrthogonalMatrix>> inverseSupplier;

    /**
     * 骨格実装を生成する.
     */
    protected SkeletalOrthogonalMatrix() {

        if (this instanceof Symmetric) {
            Optional<OrthogonalMatrix> optThis = Optional.of(this);
            this.inverseSupplier = () -> optThis;
            return;
        }

        this.inverseSupplier = ImmutableLazyCacheSupplier.of(() -> Optional.of(this.createInverse()));
    }

    @Override
    public final Optional<OrthogonalMatrix> inverse() {
        return this.inverseSupplier.get();
    }

    @Override
    public final OrthogonalMatrix transpose() {
        return this.inverseSupplier.get().get();
    }

    /**
     * 自身の転置行列を計算する.
     * 
     * <p>
     * 骨格実装の{@linkplain #inverse()}と{@linkplain #transpose()}を遅延初期化するために実装されるメソッドである.
     * <br>
     * それらのどちらかが初めて呼ばれたときに, 内部に持つキャッシュシステムから1度だけ呼ばれる. <br>
     * このメソッドのアクセス修飾子をOverride先で{@code public}にしてはならない.
     * </p>
     * 
     * <p>
     * 実装としては, <br>
     * {@code this.createInverse().inverse().get() == this} <br>
     * を満たすことが望ましい.
     * </p>
     * 
     * @return 自身の逆行列
     */
    protected OrthogonalMatrix createInverse() {
        return new TransposedOrthogonal(this);
    }

    /**
     * このオブジェクトの文字列説明表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code OrthogonalMatrix[dim(%dimension)]}
     * </p>
     * 
     * @return 説明表現
     */
    @Override
    public String toString() {
        return OrthogonalMatrix.toString(this);
    }

    private static final class TransposedOrthogonal implements OrthogonalMatrix {

        private final Optional<OrthogonalMatrix> original;

        TransposedOrthogonal(OrthogonalMatrix matrix) {
            this.original = Optional.of(matrix);
        }

        @Override
        public OrthogonalMatrix target() {
            return this.original.get().inverse().get();
        }

        @Override
        public MatrixDimension matrixDimension() {
            //直交行列は正方行列だが,転置を意識するためにtransposeを加える
            return this.original.get().matrixDimension().transpose();
        }

        @Override
        public Vector operate(Vector operand) {
            return this.original.get().operateTranspose(operand);
        }

        @Override
        public Vector operateTranspose(Vector operand) {
            return this.original.get().operate(operand);
        }

        @Override
        public Optional<OrthogonalMatrix> inverse() {
            return this.original;
        }

        @Override
        public final OrthogonalMatrix transpose() {
            return this.original.get();
        }

        @Override
        public String toString() {
            return OrthogonalMatrix.toString(this);
        }

    }
}
