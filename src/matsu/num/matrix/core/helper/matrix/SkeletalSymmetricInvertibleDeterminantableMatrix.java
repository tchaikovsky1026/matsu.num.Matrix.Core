/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.4
 */
package matsu.num.matrix.core.helper.matrix;

import java.util.Optional;
import java.util.function.Supplier;

import matsu.num.matrix.core.Determinantable;
import matsu.num.matrix.core.Invertible;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.SkeletalSymmetricMatrix;
import matsu.num.matrix.core.Symmetric;
import matsu.num.matrix.core.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.core.lazy.ImmutableLazyCacheSupplier;

/**
 * 逆行列と行列式が計算可能な行列に対する, 骨格実装.
 * 
 * <p>
 * このクラスは, {@link SkeletalSymmetricMatrix} による {@link #transpose()} の実装に加え,
 * {@link #inverse()},
 * {@link #determinant()}, {@link #logAbsDeterminant()},
 * {@link #signOfDeterminant()}
 * の適切な実装を提供する. <br>
 * 初めてそれらの行列式, 逆行列関係のメソッドが呼ばれたときに,
 * 行列式逆行列構造体である {@link InverstibleAndDeterminantStruct} を
 * {@link #createInvAndDetWrapper()} によって生成, キャッシュし,
 * 以降はそのキャッシュから値を抽出して戻す.
 * </p>
 * 
 * @author Matsuura Y.
 * @param <MT> thisのタイプ, 再帰的ジェネリクスによりtransposeの戻り値型を具象クラスにゆだねる.
 * @param <IT> inverseのタイプ
 */
public abstract class SkeletalSymmetricInvertibleDeterminantableMatrix<
        MT extends SkeletalSymmetricInvertibleDeterminantableMatrix<MT, IT>,
        IT extends Matrix>
        extends SkeletalSymmetricMatrix<MT>
        implements Matrix, Invertible, Determinantable, Symmetric {

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
     * {@link #inverse()},
     * {@link #determinant()}, {@link #logAbsDeterminant()},
     * {@link #signOfDeterminant()}
     * を遅延初期化するために実装されるメソッドである. <br>
     * それらのどれかが初めて呼ばれたときに, 内部に持つキャッシュシステムから1度だけこのメソッドが呼ばれる. <br>
     * 公開してはいけない.
     * </p>
     * 
     * @return 行列式, 逆行列
     */
    protected abstract InverstibleAndDeterminantStruct<IT> createInvAndDetWrapper();
}
