/**
 * 2023.8.17
 */
package matsu.num.matrix.base.nlsf;

import java.util.Optional;
import java.util.function.Supplier;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;
import matsu.num.matrix.base.lazy.InverseAndDeterminantStructure;

/**
 * {@link LinearEquationSolving}の骨格実装.
 * 
 * @author Matsuura Y.
 * @version 15.0
 * @param <T> 紐づけられた行列の型パラメータ
 */
abstract class SkeletalLinearEquationSolving<T extends Matrix>
        implements LinearEquationSolving<T> {

    //継承先のオーバーライドメソッドに依存するため, 遅延初期化される
    private final Supplier<InverseAndDeterminantStructure<? extends Matrix>> invAndDetStructSupplier;

    /**
     * 新しいオブジェクトの作成.
     */
    public SkeletalLinearEquationSolving() {
        super();
        this.invAndDetStructSupplier = ImmutableLazyCacheSupplier.of(
                () -> this.calcInverseAndDeterminantStructure());
    }

    @Override
    public final Optional<? extends Matrix> inverse() {
        return this.invAndDetStructSupplier.get().inverseMatrix();
    }

    @Override
    public final double determinant() {
        return this.invAndDetStructSupplier.get().determinantValues().determinant();
    }

    @Override
    public final double logAbsDeterminant() {
        return this.invAndDetStructSupplier.get().determinantValues().logAbsDeterminant();
    }

    @Override
    public final int signOfDeterminant() {
        return this.invAndDetStructSupplier.get().determinantValues().sign();
    }

    /**
     * このオブジェクトの文字列説明表現を返す.
     * 
     * <p>
     * 文字列表現は明確には規定されていない(バージョン間の互換も担保されていない). <br>
     * おそらくは次のような表現であろう. <br>
     * {@code %分解方法[target: %matrix]}
     * </p>
     * 
     * @return 説明表現
     */
    @Override
    public String toString() {
        return LinearEquationSolving.toString(this);
    }

    /**
     * 行列式関連と逆行列を計算する. 
     * 
     * <p>
     * インターフェースの仕様により逆行列が存在することが確定しているため,
     * 逆行列は存在し, 符号の値は1もしくは-1である.
     * </p>
     * 
     * @return 行列式と逆行列の構造体
     */
    abstract InverseAndDeterminantStructure<? extends Matrix> calcInverseAndDeterminantStructure();

}
