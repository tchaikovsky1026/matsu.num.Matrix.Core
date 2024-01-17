/**
 * 2023.12.29
 */
package matsu.num.matrix.base.helper.matrix;

import java.util.Optional;
import java.util.function.Supplier;

import matsu.num.matrix.base.Determinantable;
import matsu.num.matrix.base.Inversion;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.helper.value.InverseAndDeterminantStruct;
import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;

/**
 * 逆行列と行列式が計算可能な行列に対する, 骨格実装を提供する. <br>
 * {@linkplain InverseAndDeterminantStruct} のキャッシュの仕組みを提供している.
 * 
 * @author Matsuura Y.
 * @version 18.2
 * @param <MT> thisのタイプ, target, transposeメソッドの戻り値に影響
 * @param <IT> inverseのタイプ
 */
public abstract class SkeletalSymmetricInvertibleDeterminantableMatrix<MT extends Matrix, IT extends Matrix>
        implements Matrix, Inversion, Determinantable, Symmetric {

    private final MT castedThis;

    //循環参照が生じるため, 逆行列は遅延初期化
    //逆行列と行列式はそれぞれの整合性のため, セットで扱う
    private Supplier<InverseAndDeterminantStruct<IT>> invAndDetStructSupplier;

    /**
     * 骨格実装のコンストラクタ.
     */
    protected SkeletalSymmetricInvertibleDeterminantableMatrix() {
        super();
        this.invAndDetStructSupplier = ImmutableLazyCacheSupplier.of(
                () -> this.createInvAndDetWrapper());

        /*
         * 警告抑制をしているが, ジェネリックキャストなので実行時は全て
         * Matrix に置き換えられ,
         * ClassCastException は発生しない.
         */
        @SuppressWarnings("unchecked")
        MT t = (MT) this;
        this.castedThis = t;
    }

    @Override
    public final MT target() {
        return this.castedThis;
    }

    @Override
    public MT transpose() {
        return this.castedThis;
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
    protected abstract InverseAndDeterminantStruct<IT> createInvAndDetWrapper();

}
