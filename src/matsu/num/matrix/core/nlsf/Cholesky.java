/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.23
 */
package matsu.num.matrix.core.nlsf;

import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.core.DiagonalMatrix;
import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.LowerUnitriangular;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.Symmetric;
import matsu.num.matrix.core.helper.value.DeterminantValues;
import matsu.num.matrix.core.helper.value.InverstibleAndDeterminantStruct;
import matsu.num.matrix.core.helper.value.MatrixRejectionConstant;
import matsu.num.matrix.core.validation.MatrixStructureAcceptance;

/**
 * 対称行列のCholesky分解を表す. <br>
 * これは, 正定値対称行列 A の A = LD<sup>1/2</sup>D<sup>1/2</sup>L<sup>T</sup>
 * の形での分解である. <br>
 * ただし,
 * D<sup>1/2</sup>: 正定値対角行列, L: 単位 (対角成分が1の) 下三角行列. <br>
 * A = BB<sup>T</sup> の分解として見ると, B = LD<sup>1/2</sup> である.
 * 
 * <p>
 * 行列が正定値であることが, 分解できることの必要十分条件である.
 * </p>
 * 
 * <p>
 * この行列分解が提供する逆行列には {@link Symmetric} が付与されている.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 23.0
 */
public final class Cholesky
        extends SkeletalSymmetrizedSquareTypeSolver<
                EntryReadableMatrix, Matrix, Matrix> {

    private static final double EPSILON_A = 1E-100;

    private final EntryReadableMatrix matrix;

    private final DiagonalMatrix mxSqrtD;
    private final LowerUnitriangular mxL;

    /**
     * この形式の行列分解を得るためのエグゼキュータを返す.
     * 
     * @return エグゼキュータ
     */
    public static Cholesky.Executor executor() {
        return Executor.INSTANCE;
    }

    /**
     * エグゼキュータから呼ばれる.
     *
     * @throws ProcessFailedException 行列が正定値でない場合
     */
    private Cholesky(final EntryReadableMatrix matrix, final double epsilon)
            throws ProcessFailedException {
        //ここで例外が発生する可能性がある
        CholeskyFactorizationHelper fact = new CholeskyFactorizationHelper(matrix, epsilon + EPSILON_A);

        this.matrix = matrix;

        this.mxL = fact.getMxL();
        this.mxSqrtD = fact.getMxSqrtD();
    }

    @Override
    public EntryReadableMatrix target() {
        return this.matrix;
    }

    /**
     * -
     * 
     * <p>
     * (外部からの呼び出し不可)
     * </p>
     * 
     * @return -
     */
    @Override
    final InversionDeterminantableImplementation<Matrix, Matrix> createAsymmetricSqrtSystem() {
        return new AsymmetricSqrtSystem(mxSqrtD, mxL);
    }

    /**
     * -
     * 
     * <p>
     * (外部からの呼び出し不可)
     * </p>
     * 
     * @return -
     */
    @Override
    String solverName() {
        return super.solverName();
    }

    /**
     * 対称行列のCholesky分解のエグゼキュータ.
     * 
     * <p>
     * {@code accepts} メソッドでrejectされる条件は,
     * {@link SolvingFactorizationExecutor} に加えて次のとおりである.
     * </p>
     * 
     * <ul>
     * <li>行列の有効要素数が大きすぎる場合 (後述)</li>
     * <li>対称行列でない場合</li>
     * </ul>
     * 
     * <p>
     * {@code apply} メソッドで空が返る条件は,
     * {@link SolvingFactorizationExecutor} に加えて次のとおりである.
     * </p>
     * 
     * <ul>
     * <li>正定値行列でない場合</li>
     * </ul>
     * 
     * <p>
     * このクラスのインスタンスは, {@link Cholesky#executor()} メソッドにより得ることができる. <br>
     * 実質的にシングルトンである.
     * </p>
     * 
     * <hr>
     * 
     * <p>
     * 有効要素数が大きすぎるかどうかは,
     * {@link MatrixDimension#isAccepedForDenseMatrix()}
     * に従う.
     * </p>
     * 
     */
    public static final class Executor
            extends SkeletalSolvingFactorizationExecutor<
                    EntryReadableMatrix, Cholesky> {

        private static final Executor INSTANCE = new Executor();

        /**
         * 内部から呼ばれる.
         */
        private Executor() {
            super();

            //シングルトンを強制
            if (Objects.nonNull(INSTANCE)) {
                throw new AssertionError();
            }
        }

        /**
         * -
         * 
         * <p>
         * (外部からの呼び出し不可)
         * </p>
         * 
         * @return -
         */
        @Override
        MatrixStructureAcceptance acceptsConcretely(EntryReadableMatrix matrix) {
            if (!(matrix instanceof Symmetric)) {
                return MatrixRejectionConstant.REJECTED_BY_NOT_SYMMETRIC.get();
            }

            return matrix.matrixDimension().isAccepedForDenseMatrix()
                    ? MatrixStructureAcceptance.ACCEPTED
                    : MatrixRejectionConstant.REJECTED_BY_TOO_MANY_ELEMENTS.get();
        }

        /**
         * -
         * 
         * <p>
         * (外部からの呼び出し不可)
         * </p>
         * 
         * @return -
         */
        @Override
        final Optional<Cholesky> applyConcretely(
                EntryReadableMatrix matrix, double epsilon) {
            try {
                return Optional.of(new Cholesky(matrix, epsilon));
            } catch (ProcessFailedException e) {
                return Optional.empty();
            }
        }
    }

    private static final class AsymmetricSqrtSystem
            extends InversionDeterminantableImplementation<Matrix, Matrix> {

        private final DiagonalMatrix mxSqrtD;
        private final LowerUnitriangular mxL;

        private final Matrix asymmSqrt;

        AsymmetricSqrtSystem(DiagonalMatrix mxSqrtD, LowerUnitriangular mxL) {
            super();
            this.mxSqrtD = mxSqrtD;
            this.mxL = mxL;

            /*
             * 非対称平方根 B を扱う.
             * A = B B^T であるので,
             * B = L D^{1/2}
             * となる.
             */
            this.asymmSqrt = Matrix.multiply(this.mxL, this.mxSqrtD);
        }

        @Override
        public Matrix target() {
            return this.asymmSqrt;
        }

        /**
         * -
         * 
         * <p>
         * (外部からの呼び出し不可)
         * </p>
         * 
         * @return -
         */
        @Override
        InverstibleAndDeterminantStruct<Matrix> createInverseDeterminantStruct() {
            /*
             * 非対称平方根 B に関する逆行列, 行列式を扱う.
             * B = L D^{1/2} であるので,
             * det B = det D^{1/2},
             * B^{-1} = D^{-1/2} L^{-1}
             * となる.
             */
            final Matrix asymmInvSqrt = Matrix.multiply(
                    this.mxSqrtD.inverse().get(),
                    this.mxL.inverse().get());

            final double logDetSqrtL = this.mxSqrtD.logAbsDeterminant();

            return new InverstibleAndDeterminantStruct<Matrix>(
                    new DeterminantValues(logDetSqrtL, 1),
                    asymmInvSqrt);
        }
    }
}
