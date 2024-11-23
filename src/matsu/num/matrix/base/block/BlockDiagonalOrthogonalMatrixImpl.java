/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.23
 */
package matsu.num.matrix.base.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.OrthogonalMatrix;
import matsu.num.matrix.base.SkeletalAsymmetricOrthogonalMatrix;
import matsu.num.matrix.base.Vector;
import matsu.num.matrix.base.VectorDimension;
import matsu.num.matrix.base.validation.ElementsTooManyException;
import matsu.num.matrix.base.validation.MatrixFormatMismatchException;

/**
 * {@link BlockDiagonalOrthogonalMatrix} の実装.
 * 
 * @author Matsuura Y.
 * @version 23.0
 */
final class BlockDiagonalOrthogonalMatrixImpl
        extends SkeletalAsymmetricOrthogonalMatrix<BlockDiagonalOrthogonalMatrix>
        implements BlockDiagonalOrthogonalMatrix {

    private final MatrixDimension matrixDimension;
    private final Collection<? extends OrthogonalMatrix> blockSeries;

    /**
     * 唯一の非公開のコンストラクタ. <br>
     * 引数チェックは行われていない. <br>
     * 与えるコレクションは外部から参照されていてはいけない.
     */
    private BlockDiagonalOrthogonalMatrixImpl(MatrixDimension dimension,
            Collection<? extends OrthogonalMatrix> blockSeries) {
        super();

        assert blockSeries.size() >= 2;

        this.matrixDimension = dimension;
        this.blockSeries = blockSeries;
    }

    @Override
    public MatrixDimension matrixDimension() {
        return this.matrixDimension;
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throwsh NullPointerException {@inheritDoc}
     */
    @Override
    public Vector operate(Vector operand) {
        if (!matrixDimension.rightOperable(operand.vectorDimension())) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "右から演算不可:matrix:%s, operand:%s",
                            matrixDimension, operand.vectorDimension()));
        }

        Vector[] splitted = this.splitOperable(operand);

        Vector[] operated = new Vector[splitted.length];
        {
            int i = 0;
            for (OrthogonalMatrix elementMx : this.blockSeries) {
                operated[i] = elementMx.operate(splitted[i]);
                i++;
            }
        }

        return this.merge(operated);
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throwsh NullPointerException {@inheritDoc}
     */
    @Override
    public Vector operateTranspose(Vector operand) {
        if (!matrixDimension.leftOperable(operand.vectorDimension())) {
            throw new MatrixFormatMismatchException(
                    String.format(
                            "左から演算不可:matrix:%s, operand:%s",
                            matrixDimension, operand.vectorDimension()));
        }

        Vector[] splitted = this.splitOperable(operand);

        Vector[] operated = new Vector[splitted.length];
        {
            int i = 0;
            for (OrthogonalMatrix elementMx : this.blockSeries) {
                operated[i] = elementMx.operateTranspose(splitted[i]);
                i++;
            }
        }

        return this.merge(operated);
    }

    /**
     * operandをブロック分割する. <br>
     * 構造は対称なので, 右から左からのどちらでも成立する.
     */
    private Vector[] splitOperable(Vector operand) {
        assert matrixDimension.rightOperable(operand.vectorDimension());

        Vector[] splitted = new Vector[this.blockSeries.size()];

        double[] arrOperand = operand.entryAsArray();
        int i = 0;
        int startIndex = 0;
        for (OrthogonalMatrix elementMx : this.blockSeries) {
            MatrixDimension blockDimension = elementMx.matrixDimension();
            int blockSize = blockDimension.columnAsIntValue();
            int endIndex = startIndex + blockSize;

            double[] entry = Arrays.copyOfRange(arrOperand, startIndex, endIndex);
            Vector.Builder vBuilder = Vector.Builder.zeroBuilder(blockDimension.rightOperableVectorDimension());
            vBuilder.setEntryValue(entry);
            splitted[i] = vBuilder.build();

            startIndex = endIndex;
            i++;
        }

        return splitted;
    }

    /**
     * 分割したベクトルを結合する. <br>
     * 合計サイズがこのMatrixの次元に一致しなければならない.
     */
    private Vector merge(Vector[] splitted) {
        VectorDimension entireDimension = this.matrixDimension.leftOperableVectorDimension();

        double[] mergedArray = new double[entireDimension.intValue()];
        int startIndex = 0;
        for (Vector elementVec : splitted) {
            double[] arrElementVec = elementVec.entryAsArray();
            int blockSize = arrElementVec.length;

            assert startIndex + blockSize <= mergedArray.length : "はみ出している";

            System.arraycopy(arrElementVec, 0, mergedArray, startIndex, blockSize);

            startIndex += blockSize;
        }

        assert startIndex == mergedArray.length : "合計サイズが一致しない";

        Vector.Builder vBuilder = Vector.Builder.zeroBuilder(entireDimension);
        vBuilder.setEntryValue(mergedArray);
        return vBuilder.build();
    }

    @Override
    public Collection<? extends OrthogonalMatrix> toSeries() {
        return new ArrayList<>(this.blockSeries);
    }

    /**
     * -
     * 
     * @return -
     * @deprecated (外部からの呼び出し不可)
     */
    @Deprecated
    @Override
    protected BlockDiagonalOrthogonalMatrix createTranspose() {
        List<OrthogonalMatrix> transposeList =
                this.blockSeries.stream()
                        .map(OrthogonalMatrix::transpose)
                        .toList();

        //dimensionは正方形だが, 意図の明確化のためdimension.transpose()とする
        return new TransposeAttachedBlockDiagonalOrthogonalMatrix(
                new BlockDiagonalOrthogonalMatrixImpl(
                        this.matrixDimension.transpose(), transposeList),
                this);
    }

    @Override
    public String toString() {
        return String.format(
                "Matrix[dim:%s, orthogonal]",
                this.matrixDimension());
    }

    /**
     * 1個以上の直交行列を対角ブロックに並べ,
     * 非対角ブロックに零行列を置いた, ブロック対角直交行列を返す.
     * 
     * @param first 左上ブロックの行列
     * @param following firstに続く行列, 左上から右下に向かって順番
     * @return ブロック対角直交行列
     * @throws ElementsTooManyException 全体のサイズが大きすぎる場合
     * @throws NullPointerException 引数にnullが含まれる場合
     */
    static OrthogonalMatrix matrixOf(
            OrthogonalMatrix first, OrthogonalMatrix... following) {

        Objects.requireNonNull(first);
        if (following.length == 0) {
            return first;
        }

        List<OrthogonalMatrix> rawBlockSeries = new ArrayList<>();
        rawBlockSeries.add(first);
        for (OrthogonalMatrix m : following) {
            rawBlockSeries.add(Objects.requireNonNull(m));
        }

        //ここで例外が発生する可能性がある
        MatrixDimension dimension = BlockDiagonalOrthogonalUtil.calcDimension(rawBlockSeries);

        Collection<OrthogonalMatrix> blockSeries = BlockDiagonalOrthogonalUtil.expand(rawBlockSeries);

        return new BlockDiagonalOrthogonalMatrixImpl(dimension, blockSeries);
    }

    private static final class TransposeAttachedBlockDiagonalOrthogonalMatrix
            implements BlockDiagonalOrthogonalMatrix {

        private final BlockDiagonalOrthogonalMatrix original;
        private final Optional<BlockDiagonalOrthogonalMatrix> opTranspose;

        TransposeAttachedBlockDiagonalOrthogonalMatrix(
                BlockDiagonalOrthogonalMatrix original,
                BlockDiagonalOrthogonalMatrix transpose) {
            super();
            this.original = original;
            this.opTranspose = Optional.of(transpose);
        }

        @Override
        public MatrixDimension matrixDimension() {
            return this.original.matrixDimension();
        }

        @Override
        public Vector operate(Vector operand) {
            return this.original.operate(operand);
        }

        @Override
        public Vector operateTranspose(Vector operand) {
            return this.original.operateTranspose(operand);
        }

        @Override
        public Collection<? extends OrthogonalMatrix> toSeries() {
            return this.original.toSeries();
        }

        @Override
        public BlockDiagonalOrthogonalMatrix transpose() {
            return this.opTranspose.get();
        }

        @Override
        public Optional<? extends BlockDiagonalOrthogonalMatrix> inverse() {
            return this.opTranspose;
        }

        @Override
        public String toString() {
            return this.original.toString();
        }
    }

}
