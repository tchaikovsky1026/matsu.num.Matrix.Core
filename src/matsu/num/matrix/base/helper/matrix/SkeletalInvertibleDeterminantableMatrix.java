/**
 * 2024.1.19
 */
package matsu.num.matrix.base.helper.matrix;

import java.util.Optional;
import java.util.function.Supplier;

import matsu.num.matrix.base.Determinantable;
import matsu.num.matrix.base.Invertible;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.SkeletalMatrix;
import matsu.num.matrix.base.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;

/**
 * 逆行列と行列式が計算可能な行列に対する, 骨格実装を提供する. <br>
 * {@linkplain InverstibleAndDeterminantStruct} のキャッシュの仕組みを提供している.
 * 
 * @author Matsuura Y.
 * @version 19.0
 * @param <IT> inverseのタイプ
 * @deprecated 使用されていないため,一時的にdeprecatedにしている.
 */
@Deprecated
public abstract class SkeletalInvertibleDeterminantableMatrix<IT extends Matrix>
        extends SkeletalMatrix
        implements Matrix, Invertible, Determinantable {

    //循環参照が生じるため, 逆行列は遅延初期化
    //逆行列と行列式はそれぞれの整合性のため, セットで扱う
    private Supplier<InverstibleAndDeterminantStruct<IT>> invAndDetStructSupplier;

    /**
     * 骨格実装のコンストラクタ.
     */
    protected SkeletalInvertibleDeterminantableMatrix() {
        super();
        this.invAndDetStructSupplier = ImmutableLazyCacheSupplier.of(
                () -> this.createInvAndDetWrapper());
    }

    @Override
    public final Optional<? extends IT> inverse() {
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
     * 逆行列と行列式の生成を行う抽象メソッド.
     * 
     * <p>
     * このメソッドは内部から呼ばれることが想定された内部から一度だけ呼ばれる. <br>
     * 公開してはいけない.
     * </p>
     * 
     * @return 行列式, 逆行列
     */
    protected abstract InverstibleAndDeterminantStruct<IT> createInvAndDetWrapper();

}
