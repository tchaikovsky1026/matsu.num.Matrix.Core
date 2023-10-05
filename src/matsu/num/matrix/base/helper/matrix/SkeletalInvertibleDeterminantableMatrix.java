/**
 * 2023.8.21
 */
package matsu.num.matrix.base.helper.matrix;

import java.util.Optional;
import java.util.function.Supplier;

import matsu.num.matrix.base.Determinantable;
import matsu.num.matrix.base.Inversion;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.SkeletalMatrix;
import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;
import matsu.num.matrix.base.lazy.InverseAndDeterminantStructure;

/**
 * 逆行列と行列式が計算可能な行列に対する, 骨格実装を提供する. <br>
 * {@linkplain InverseAndDeterminantStructure}のキャッシュの仕組みを提供している. 
 * 
 * @author Matsuura Y.
 * @version 15.1
 * @param <IT> 逆行列の型
 */
public abstract class SkeletalInvertibleDeterminantableMatrix<IT extends Matrix> extends SkeletalMatrix
        implements Matrix, Inversion, Determinantable {

    //循環参照が生じるため, 逆行列は遅延初期化
    //逆行列と行列式はそれぞれの整合性のため, セットで扱う
    private Supplier<InverseAndDeterminantStructure<IT>> invAndDetStructSupplier;

    /**
     * 骨格実装のコンストラクタ.
     */
    protected SkeletalInvertibleDeterminantableMatrix() {
        this.invAndDetStructSupplier = ImmutableLazyCacheSupplier.of(
                () -> this.createInvAndDetWrapper());
    }

    @Override
    public final Optional<IT> inverse() {
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
     * 一度だけ呼ばれる, <br> 
     * 逆行列, 行列式の生成. 
     * 
     * @return 行列式, 逆行列
     */
    protected abstract InverseAndDeterminantStructure<IT> createInvAndDetWrapper();

}
