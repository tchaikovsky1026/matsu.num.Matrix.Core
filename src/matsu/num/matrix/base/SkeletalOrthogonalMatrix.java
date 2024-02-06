/**
 * 2024.2.5
 */
package matsu.num.matrix.base;

import java.util.Optional;
import java.util.function.Supplier;

import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;

/**
 * <p>
 * {@linkplain OrthogonalMatrix} の骨格実装. <br>
 * {@linkplain OrthogonalMatrix#inverse()} の実装の提供が主な効果である.
 * </p>
 * 
 * <p>
 * この骨格実装は, {@linkplain #inverse()} メソッドの実装を提供し,
 * 同時にオーバーライドを禁止する. <br>
 * このメソッドにより返される逆行列の {@linkplain OrthogonalMatrix#inverse()} による戻り値
 * (すなわち, 逆行列の逆行列) は,
 * {@code this} と同一である.
 * </p>
 * 
 * <p>
 * {@linkplain OrthogonalMatrix#inverse()} は関数的に振る舞う. <br>
 * この骨格実装は,
 * 初めて {@linkplain #inverse()} または {@linkplain #transpose()}
 * が呼ばれたときに逆行列 (転置行列) を {@linkplain #createInverse()} によって生成し,
 * 以後はそれを使いまわすキャッシュの仕組みを提供している.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 20.0
 */
public abstract class SkeletalOrthogonalMatrix implements OrthogonalMatrix {

    //逆行列(転置行列)を生成するサプライヤ
    private final Supplier<Optional<? extends OrthogonalMatrix>> inverseSupplier;

    /**
     * 骨格実装を生成する唯一のコンストラクタ.
     */
    protected SkeletalOrthogonalMatrix() {
        super();
        this.inverseSupplier = ImmutableLazyCacheSupplier.of(() -> Optional.of(this.createInverse()));
    }

    @Override
    public final Optional<? extends OrthogonalMatrix> inverse() {
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
     * 骨格実装の{@linkplain #inverse()} と
     * {@linkplain #transpose()} を遅延初期化するために実装されるメソッドである.
     * <br>
     * それらのどちらかが初めて呼ばれたときに, 内部に持つキャッシュシステムから1度だけ呼ばれる. <br>
     * 公開してはいけない.
     * </p>
     * 
     * <p>
     * 実装としては, <br>
     * {@code this.createInverse().inverse().get() == this} <br>
     * を満たすことが望ましい
     * (逆行列の逆行列は自身と同一のインスタンスを指す).
     * </p>
     * 
     * @return 自身の逆行列
     */
    protected OrthogonalMatrix createInverse() {
        return (this instanceof Symmetric)
                ? this
                : new TransposedOrthogonal(this);
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

    /**
     * 直交行列の転置を扱う.
     */
    private static final class TransposedOrthogonal implements OrthogonalMatrix {

        private final Optional<OrthogonalMatrix> original;

        TransposedOrthogonal(OrthogonalMatrix matrix) {
            this.original = Optional.of(matrix);
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
        public Optional<? extends OrthogonalMatrix> inverse() {
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
