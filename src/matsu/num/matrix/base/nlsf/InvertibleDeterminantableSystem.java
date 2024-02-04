/**
 * 2024.1.19
 */
package matsu.num.matrix.base.nlsf;

import java.util.function.Supplier;

import matsu.num.matrix.base.Determinantable;
import matsu.num.matrix.base.Inversion;
import matsu.num.matrix.base.Invertible;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;

/**
 * <p>
 * 逆行列と行列式を同時に扱う仕組みを扱う. <br>
 * 実質的に不変であり, 全てのメソッドは関数的かつスレッドセーフである.
 * </p>
 * 
 * <p>
 * このクラスでは抽象メソッド {@linkplain #calcInverseDeterminantStruct()} を定義している.
 * <br>
 * 実装者は, このメソッドで逆行列と行列式を計算するように実装する. <br>
 * {@linkplain Invertible} と {@linkplain Determinantable}
 * のインターフェースの実装はこの抽象クラス内で実装されおり,
 * {@code final} である.
 * </p>
 * 
 * <p>
 * 逆行列や行列式の値が呼ばれたときに1度だけ
 * {@linkplain #calcInverseDeterminantStruct()}
 * が呼ばれ, 抽象クラス内ででキャッシュ化される.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 19.0
 * @param <IT> 逆行列の型パラメータ
 */
abstract class InvertibleDeterminantableSystem<IT extends Matrix>
        implements Determinantable, Inversion {

    //継承先のオーバーライドメソッドに依存するため, 遅延初期化される
    private final Supplier<InverstibleAndDeterminantStruct<? extends IT>> invAndDetStructSupplier;

    /**
     * 新しいオブジェクトの作成.
     */
    protected InvertibleDeterminantableSystem() {
        super();
        this.invAndDetStructSupplier = ImmutableLazyCacheSupplier.of(
                () -> this.calcInverseDeterminantStruct());
    }

    @Override
    public final IT inverse() {
        return this.invAndDetStructSupplier.get().inverseMatrix().get();
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
     * ターゲット行列に関する, 行列式と逆行列を計算する抽象メソッド.
     * 
     * <p>
     * 逆行列が存在することが確定しているため,
     * 逆行列は存在し, 符号の値は1もしくは-1である.
     * </p>
     * 
     * @return 行列式と逆行列の構造体
     */
    protected abstract InverstibleAndDeterminantStruct<? extends IT> calcInverseDeterminantStruct();

}
