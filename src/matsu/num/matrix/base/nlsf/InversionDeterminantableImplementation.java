/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.5
 */
package matsu.num.matrix.base.nlsf;

import java.util.function.Supplier;

import matsu.num.matrix.base.Determinantable;
import matsu.num.matrix.base.Inversion;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;

/**
 * {@link Inversion} と {@link Determinantable} を実装したクラスの骨格実装を扱う.
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
 * <p>
 * この抽象クラスは {@link #toString()} の適切な実装を提供しないので,
 * インスタンスを外部から参照できる場合はサブクラスでオーバーライドすることを推奨する.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 22.0
 * @param <TT> ターゲット行列の型パラメータ, {@link #target()} の戻り値型をサブタイプにゆだねる.
 * @param <IT> 逆行列の型パラメータ, {@link #inverse()} の戻り値型をサブタイプにゆだねる.
 */
abstract class InversionDeterminantableImplementation<TT extends Matrix, IT extends Matrix>
        implements Inversion, Determinantable {

    //継承先のオーバーライドメソッドに依存するため, 遅延初期化される
    private final Supplier<InverstibleAndDeterminantStruct<? extends IT>> invAndDetStructSupplier;

    /**
     * 唯一のコンストラクタ.
     */
    protected InversionDeterminantableImplementation() {
        super();
        this.invAndDetStructSupplier = ImmutableLazyCacheSupplier.of(
                () -> this.createInverseDeterminantStruct());
    }

    @Override
    public abstract TT target();

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
     * ターゲット行列に関する, 行列式と逆行列を計算する抽象メソッド. <br>
     * インスタンスが生成されてから一度だけ呼ばれる. <br>
     * 公開してはいけない.
     * 
     * <p>
     * 逆行列が存在することが確定しているため,
     * 逆行列は存在し, 符号の値は1もしくは-1である.
     * </p>
     * 
     * @return 行列式と逆行列の構造体
     */
    protected abstract InverstibleAndDeterminantStruct<IT> createInverseDeterminantStruct();
}