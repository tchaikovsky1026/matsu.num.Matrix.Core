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

import matsu.num.matrix.base.EntryReadableMatrix;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.helper.value.DeterminantValues;
import matsu.num.matrix.base.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.base.lazy.ImmutableLazyCacheSupplier;

/**
 * {@link SymmetrizedSquareTypeSolver} の骨格実装.
 * 
 * <p>
 * このクラスでは,
 * {@link #inverse()}, {@link #signOfDeterminant()},
 * {@link #determinant()}, {@link #logAbsDeterminant()},
 * {@link #asymmSqrt()}, {@link #inverseAsymmSqrt()}
 * メソッドの適切な実装を提供する. <br>
 * これらの戻り値は {@link #createAsymmetricSqrtSystem()}
 * メソッドにより一度だけ計算, キャッシュされ,
 * 以降はそのキャッシュを戻す.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 22.0
 * @param <TT> ターゲット行列の型パラメータ, {@link #target()} の戻り値型をサブタイプにゆだねる.
 * @param <ST> 非対称平方根行列の型パラメータ, {@link #asymmSqrt()} の戻り値型をサブタイプにゆだねる.
 * @param <SIT> 非対称平方根の逆行列の型パラメータ, {@link #inverseAsymmSqrt()} の戻り値型をサブタイプにゆだねる.
 */
abstract non-sealed class SkeletalSymmetrizedSquareTypeSolver<
        TT extends EntryReadableMatrix, ST extends Matrix, SIT extends Matrix>
        extends SkeletalLUTypeSolver<TT, Matrix> implements SymmetrizedSquareTypeSolver {

    //継承先のオーバーライドに依存するため, 遅延初期化される
    private final Supplier<InversionDeterminantableImplementation<ST, SIT>> asymmetricSqrtSupplier;

    /**
     * 唯一のコンストラクタ.
     */
    SkeletalSymmetrizedSquareTypeSolver() {
        super();
        this.asymmetricSqrtSupplier = ImmutableLazyCacheSupplier.of(
                () -> this.createAsymmetricSqrtSystem());
    }

    /**
     * -
     * 
     * @return -
     * @deprecated (サブクラスを含む, 外部からの呼び出し不可)
     */
    @Deprecated
    @Override
    final InverstibleAndDeterminantStruct<Matrix> createInverseDeterminantStruct() {
        InversionDeterminantableImplementation<ST, SIT> sqrtMatrixStructure =
                this.asymmetricSqrtSupplier.get();
        return new InverstibleAndDeterminantStruct<Matrix>(
                new DeterminantValues(2 * sqrtMatrixStructure.logAbsDeterminant(), 1),
                Matrix.symmetrizedSquare(sqrtMatrixStructure.inverse().transpose()));
    }

    @Override
    public final ST asymmSqrt() {
        return this.asymmetricSqrtSupplier.get().target();
    }

    @Override
    public final SIT inverseAsymmSqrt() {
        return this.asymmetricSqrtSupplier.get().inverse();
    }

    /**
     * 非対称平方根 B に関する行列式と逆行列システムを作成する抽象メソッド. <br>
     * インスタンスが生成されてから一度だけ呼ばれる. <br>
     * 公開してはいけない.
     * 
     * @return 非対称平方根行列に関する行列分解
     */
    abstract InversionDeterminantableImplementation<ST, SIT> createAsymmetricSqrtSystem();
}
