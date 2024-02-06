/**
 * 2024.2.5
 */
package matsu.num.matrix.base.nlsf;

import matsu.num.matrix.base.DiagonalMatrix;
import matsu.num.matrix.base.EntryReadableMatrix;
import matsu.num.matrix.base.LowerUnitriangular;
import matsu.num.matrix.base.LowerUnitriangularMatrix;
import matsu.num.matrix.base.Matrix;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.PermutationMatrix;

/**
 * <p>
 * ピボッティング付きLU分解のヘルパ. <br>
 * A = PLDU.
 * </p>
 * 
 * <p>
 * 数値安定性を得るため, 与えられた行列Aを最初に定数倍してから分解する. <br>
 * その定数倍は対角行列Dに押し付ける.
 * </p>
 * 
 * @author Matsuura Y.
 * @version 20.0
 */
final class LUPivotingFactorizationHelper {

    private final MatrixDimension matrixDimension;
    private final double[] mxEntry;
    private final double scale;

    private DiagonalMatrix mxD;
    private LowerUnitriangular mxL;
    private LowerUnitriangular mxUt;
    private PermutationMatrix mxP;

    /**
     * @param matrix 受け入れ可能な行列
     * @param relativeEpsilon
     * @throws ProcessFailedException 行列が特異の場合
     */
    public LUPivotingFactorizationHelper(final EntryReadableMatrix matrix, double relativeEpsilon)
            throws ProcessFailedException {
        this.scale = matrix.entryNormMax();
        if (this.scale == 0.0) {
            throw new ProcessFailedException("行列が特異(零行列である)");
        }
        this.matrixDimension = matrix.matrixDimension();
        this.mxEntry = matrixToArray(matrix);
        this.factorize(relativeEpsilon);
        this.convertToEachMatrix();
    }

    public static boolean acceptedSize(Matrix matrix) {
        final int dimension = matrix.matrixDimension().rowAsIntValue();
        final long long_entrySize = (long) dimension * dimension;
        return long_entrySize <= Integer.MAX_VALUE;
    }

    public DiagonalMatrix getMxD() {
        return this.mxD;
    }

    public LowerUnitriangular getMxL() {
        return this.mxL;
    }

    public LowerUnitriangular getMxUt() {
        return this.mxUt;
    }

    public PermutationMatrix getMxP() {
        return this.mxP;
    }

    /**
     * 配列にする際にスケールする.
     */
    private double[] matrixToArray(final EntryReadableMatrix matrix) {
        final int thisDimension = this.matrixDimension.rowAsIntValue();
        final int entrySize = thisDimension * thisDimension;

        double[] outArray = new double[entrySize];
        int c = 0;
        for (int j = 0; j < thisDimension; j++) {
            for (int k = 0; k < thisDimension; k++) {
                outArray[c] = matrix.valueAt(j, k) / this.scale;
                c++;
            }
        }
        return outArray;
    }

    /**
     * 行列を分解し, 同時に置換行列Pを決定する.
     *
     * @throws ProcessFailedException 行列が特異の場合
     */
    private void factorize(double threshold) throws ProcessFailedException {
        PermutationMatrix.Builder mxPBuilder = PermutationMatrix.Builder.unitBuilder(this.matrixDimension);

        final int thisDimension = this.matrixDimension.rowAsIntValue();
        final double[] thisMxEntry = this.mxEntry;

        int in = -thisDimension;
        for (int i = 0; i < thisDimension; i++) {
            in += thisDimension;
            //部分ピボット選択
            double maxValue = Math.abs(thisMxEntry[in + i]);
            int maxValueRow = i;
            int jn = i * thisDimension;
            for (int j = i + 1; j < thisDimension; j++) {
                jn += thisDimension;
                final double temp = Math.abs(thisMxEntry[jn + i]);
                if (maxValue < temp) {
                    maxValue = temp;
                    maxValueRow = j;
                }
            }
            if (maxValue <= threshold) {
                throw new ProcessFailedException("行列が特異");
            }
            if (maxValueRow != i) {
                this.swapRowsOfArray(i, maxValueRow);
                mxPBuilder.swapColumns(i, maxValueRow);
            }
            //Dの計算(対角成分自体はそのまま)
            final double invD = 1 / thisMxEntry[in + i];
            //Lの計算
            jn = i * thisDimension;
            for (int j = i + 1; j < thisDimension; j++) {
                jn += thisDimension;
                thisMxEntry[jn + i] *= invD;
            }
            //前進消去
            jn = i * thisDimension;
            for (int j = i + 1; j < thisDimension; j++) {
                jn += thisDimension;
                final double l_j = thisMxEntry[jn + i];
                for (int k = i + 1; k < thisDimension; k++) {
                    thisMxEntry[jn + k] -= l_j * thisMxEntry[in + k];
                }
            }
            //Uの計算
            for (int k = i + 1; k < thisDimension; k++) {
                thisMxEntry[in + k] *= invD;
            }
        }

        //置換行列をビルド
        mxP = mxPBuilder.build();
    }

    /**
     * 分解されたmxEntryを行列オブジェクトに変換.
     *
     * @throws IllegalArgumentException mxDが正則でない場合
     */
    private void convertToEachMatrix() throws ProcessFailedException {
        final int thisDimension = this.matrixDimension.rowAsIntValue();
        final double[] thisMxEntry = this.mxEntry;

        DiagonalMatrix.Builder mxDBuilder = DiagonalMatrix.Builder.zeroBuilder(this.matrixDimension);
        LowerUnitriangularMatrix.Builder mxLBuilder =
                LowerUnitriangularMatrix.Builder.unit(this.matrixDimension);
        LowerUnitriangularMatrix.Builder mxUtBuilder =
                LowerUnitriangularMatrix.Builder.unit(this.matrixDimension);

        int c = 0;
        for (int j = 0; j < thisDimension; j++) {
            for (int k = 0; k < j; k++) {
                mxLBuilder.setValue(j, k, thisMxEntry[c]);
                c++;
            }
            //対角成分はスケールを反映する
            mxDBuilder.setValue(j, thisMxEntry[c] * this.scale);
            c++;
            for (int k = j + 1; k < thisDimension; k++) {
                mxUtBuilder.setValue(k, j, thisMxEntry[c]);
                c++;
            }
        }

        this.mxD = mxDBuilder.build();
        this.mxL = mxLBuilder.build();
        this.mxUt = mxUtBuilder.build();

        //スケールの関係で特異になるかもしれないので, 正則判定
        if (this.mxD.signOfDeterminant() == 0) {
            throw new ProcessFailedException("行列が特異");
        }
    }

    /**
     * mxEntryの行列表示に関して, 2つの行を入れ替える.
     */
    private void swapRowsOfArray(int row1, int row2) {
        if (row1 == row2) {
            return;
        }

        final int thisDimension = this.matrixDimension.rowAsIntValue();
        final int rn1 = row1 * thisDimension;
        final int rn2 = row2 * thisDimension;
        for (int columnIndex = 0; columnIndex < thisDimension; columnIndex++) {
            final int i1 = rn1 + columnIndex;
            final int i2 = rn2 + columnIndex;
            final double temp = this.mxEntry[i1];
            this.mxEntry[i1] = this.mxEntry[i2];
            this.mxEntry[i2] = temp;
        }
    }
}
