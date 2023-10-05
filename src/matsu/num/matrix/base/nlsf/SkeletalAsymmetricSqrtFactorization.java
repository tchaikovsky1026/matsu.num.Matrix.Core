/**
 * 2023.8.18
 */
package matsu.num.matrix.base.nlsf;

import java.util.function.Supplier;

import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.helper.value.DeterminantValues;
import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;
import matsu.num.matrix.base.lazy.InverseAndDeterminantStructure;

/**
 * {@link AsymmetricSqrtFactorization}の骨格実装.
 * 
 * @author Matsuura Y.
 * @version 15.0
 * @param <T> 紐づけられた行列の型パラメータ
 */
abstract class SkeletalAsymmetricSqrtFactorization<T extends Matrix> extends SkeletalLinearEquationSolving<T>
        implements AsymmetricSqrtFactorization<T> {

    //継承先のオーバーライドに依存するため, 遅延初期化される
    private final Supplier<LinearEquationSolving<Matrix>> asymmetricSqrtSupplier;

    /**
     * 新しいオブジェクトの作成
     */
    public SkeletalAsymmetricSqrtFactorization() {
        super();
        this.asymmetricSqrtSupplier = ImmutableLazyCacheSupplier.of(
                () -> this.calcAsymmetricSqrtSystem());
    }

    @Override
    final InverseAndDeterminantStructure<Matrix> calcInverseAndDeterminantStructure() {
        LinearEquationSolving<Matrix> sqrtMatrixStructure = this.asymmetricSqrtSupplier.get();
        return new InverseAndDeterminantStructure<Matrix>(
                new DeterminantValues(2 * sqrtMatrixStructure.logAbsDeterminant(), 1),
                Matrix.symmetrizedSquare(sqrtMatrixStructure.inverse().get().transpose()));
    }

    @Override
    public final LinearEquationSolving<Matrix> asymmetricSqrtSystem() {
        return this.asymmetricSqrtSupplier.get();
    }

    /**
     * 非対称平方根に関するシステムを作成する. 
     * 
     * @return 非対称平方根システム
     */
    abstract LinearEquationSolving<Matrix> calcAsymmetricSqrtSystem();
}
