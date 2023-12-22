/**
 * 2023.11.30
 */
package matsu.num.matrix.base;

import java.util.function.Supplier;

import matsu.num.matrix.base.helper.matrix.transpose.Transposition;
import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;

/**
 * {@link Matrix}の骨格実装. <br>
 * {@link Matrix#transpose()}の実装の提供が主な効果である.
 * 
 * <p>
 * {@link Matrix}は不変であるので, {@linkplain Matrix#transpose()}は関数的に振る舞う. <br>
 * この骨格実装は,
 * 初めて{@linkplain #transpose()}が呼ばれたときに転置行列を{@linkplain #createTranspose()}によって生成し,
 * 以後はそれを使いまわすキャッシュ仕組みを提供している.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 17.1
 */
public abstract class SkeletalMatrix implements Matrix {

    //転置行列を生成するサプライヤ
    private final Supplier<Matrix> transposeSupplier;

    /**
     * 骨格実装を生成する.
     */
    protected SkeletalMatrix() {

        if (this instanceof Symmetric) {
            this.transposeSupplier = () -> this;
            return;
        }

        this.transposeSupplier = ImmutableLazyCacheSupplier.of(() -> this.createTranspose());
    }

    @Override
    public final Matrix transpose() {
        return this.transposeSupplier.get();
    }

    /**
     * 自身の転置行列を生成する.
     * 
     * <p>
     * 骨格実装の{@linkplain #transpose()}を遅延初期化するために実装されるメソッドである. <br>
     * それが初めて呼ばれたときに, 内部に持つキャッシュシステムから1度だけ呼ばれる.
     * </p>
     * 
     * <p>
     * 実装としては, <br>
     * {@code this.}{@linkplain #createTranspose()}{@code .}{@linkplain #transpose()}{@code  == this}
     * <br>
     * を満たすことが望ましい.
     * </p>
     * 
     * @return 自身の転置行列
     */
    protected Matrix createTranspose() {
        return Transposition.instance().apply(this);
    }

    /**
     * このオブジェクトの文字列説明表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code Matrix[dim(%dimension)]}
     * </p>
     * 
     * @return 説明表現
     */
    @Override
    public String toString() {
        return Matrix.toString(this);
    }
}
