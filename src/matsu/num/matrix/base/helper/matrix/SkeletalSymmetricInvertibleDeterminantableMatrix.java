/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.4.4
 */
package matsu.num.matrix.base.helper.matrix;

import java.util.Optional;
import java.util.function.Supplier;

import matsu.num.matrix.base.Determinantable;
import matsu.num.matrix.base.Invertible;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.Symmetric;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * 逆行列と行列式が計算可能な行列に対する, 骨格実装を提供する. <br>
 * {@linkplain InverstibleAndDeterminantStruct} のキャッシュの仕組みを提供している.
 * 
 * @author Matsuura Y.
 * @version 21.0
 * @param <MT> thisのタイプ, transposeメソッドの戻り値に影響
 * @param <IT> inverseのタイプ
 */
public abstract class SkeletalSymmetricInvertibleDeterminantableMatrix<MT extends Matrix, IT extends Matrix>
        implements Matrix, Invertible, Determinantable, Symmetric {

    private final MT castedThis;

    //循環参照が生じるため, 逆行列は遅延初期化
    //逆行列と行列式はそれぞれの整合性のため, セットで扱う
    private Supplier<InverstibleAndDeterminantStruct<IT>> invAndDetStructSupplier;

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
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public final Vector operateTranspose(Vector operand) {
        return this.operate(operand);
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
