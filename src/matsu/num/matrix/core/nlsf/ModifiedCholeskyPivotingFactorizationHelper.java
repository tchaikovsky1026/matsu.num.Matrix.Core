/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.23
 */
package matsu.num.matrix.core.nlsf;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.LowerUnitriangular;
import matsu.num.matrix.core.LowerUnitriangularMatrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.PermutationMatrix;

/**
 * 対称な部分ピボッティング付き修正Cholesky分解のヘルパ. <br>
 * A = PLML<sup>T</sup>P<sup>T</sup>.
 * 
 * <p>
 * 数値安定性を得るため, 与えられた行列Aを最初に定数倍してから分解する. <br>
 * その定数倍はブロック対角行列Mに押し付ける.
 * </p>
 *
 * @author Matsuura Y.
 * @version 23.0
 */
final class ModifiedCholeskyPivotingFactorizationHelper {

    //ピボット選択の閾値となるマジックナンバー
    private static final double ALPHA = 0.6403882032022076;

    private final MatrixDimension matrixDimension;
    private final double[] mxLowerEntry;
    private final boolean[] pivot22;
    private final double scale;

    private Block2OrderSymmetricDiagonalMatrix mxM;
    private LowerUnitriangular mxL;
    private PermutationMatrix mxP;

    /**
     * @param matrix
     * @param relativeEpsilon
     * @throws ProcessFailedException 行列が特異の場合
     */
    ModifiedCholeskyPivotingFactorizationHelper(final EntryReadableMatrix matrix, double relativeEpsilon)
            throws ProcessFailedException {
        this.scale = matrix.entryNormMax();
        if (this.scale == 0.0) {
            throw new ProcessFailedException("行列が特異(零行列である)");
        }
        this.matrixDimension = matrix.matrixDimension();
        this.mxLowerEntry = lowerSideOfMatrixToArray(matrix);
        this.pivot22 = new boolean[this.matrixDimension.rowAsIntValue() - 1];
        this.factorize(relativeEpsilon);
        this.convertToEachMatrix();
    }

    Block2OrderSymmetricDiagonalMatrix getMxM() {
        return this.mxM;
    }

    LowerUnitriangular getMxL() {
        return this.mxL;
    }

    PermutationMatrix getMxP() {
        return this.mxP;
    }

    /**
     * 広義下三角成分を配列へ.
     * 成分を配列に落とし込む際にスケールする.
     * 
     */
    private double[] lowerSideOfMatrixToArray(final EntryReadableMatrix matrix) {
        final int thisDimension = this.matrixDimension.rowAsIntValue();
        final int entrySize = (thisDimension * thisDimension + thisDimension) / 2;

        double[] outArray = new double[entrySize];
        int c = 0;
        for (int j = 0; j < thisDimension; j++) {
            for (int k = 0; k <= j; k++) {
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
        final double[] thisMxEntry = this.mxLowerEntry;

        final double[] thisMxUEntry_bk_0 = new double[thisDimension];
        final double[] thisMxUEntry_bk_1 = new double[thisDimension];

        int i = 0;
        while (i < thisDimension) {
            //ピボット選択アルゴリズムの選定
            boolean exePivot22 = false;
            final int in = (i * (i + 1)) / 2;
            if (i < thisDimension - 1) {
                double lambda_1 = 0.0;
                int r = i + 1;
                int jn = in;
                for (int j = i + 1; j < thisDimension; j++) {
                    jn += j;
                    double temp = Math.abs(thisMxEntry[jn + i]);
                    if (lambda_1 < temp) {
                        r = j;
                        lambda_1 = temp;
                    }
                }
                final double absAii = Math.abs(thisMxEntry[in + i]);
                if (absAii <= threshold && lambda_1 <= threshold) {
                    throw new ProcessFailedException("行列が特異");
                }
                if (absAii < ALPHA * lambda_1) {
                    final int rn = (r * (r + 1)) / 2;
                    double lambda_r = 0.0;
                    for (int k = i; k < r; k++) {
                        lambda_r = Math.max(lambda_r, Math.abs(thisMxEntry[rn + k]));
                    }
                    int kn = rn;
                    for (int k = r + 1; k < thisDimension; k++) {
                        kn += k;
                        lambda_r = Math.max(lambda_r, Math.abs(thisMxEntry[kn + r]));
                    }

                    if (absAii * lambda_r < ALPHA * lambda_1 * lambda_1) {
                        if (Math.abs(thisMxEntry[rn + r]) > ALPHA * lambda_r) {
                            this.swapRowsAndColumnsOfArray(i, r);
                            mxPBuilder.swapColumns(i, r);
                        } else {
                            if (i + 1 != r) {
                                this.swapRowsAndColumnsOfArray(i + 1, r);
                                mxPBuilder.swapColumns(i + 1, r);
                            }
                            exePivot22 = true;
                        }
                    }
                }
            }
            if (!exePivot22) {
                final double d = thisMxEntry[in + i];
                final double invD = 1 / d;
                //UのバックアップとLの計算
                int jn = in;
                for (int j = i + 1; j < thisDimension; j++) {
                    jn += j;
                    thisMxUEntry_bk_0[j] = thisMxEntry[jn + i];
                    thisMxEntry[jn + i] *= invD;
                }
                //前進消去
                jn = in;
                for (int j = i + 1; j < thisDimension; j++) {
                    jn += j;
                    final double l_j = thisMxEntry[jn + i];
                    for (int k = i + 1; k <= j; k++) {
                        thisMxEntry[jn + k] -= l_j * thisMxUEntry_bk_0[k];
                    }
                }
                i++;
            } else {
                final double m00 = thisMxEntry[in + i];
                final double m01 = thisMxEntry[in + i + i + 1];
                final double m11 = thisMxEntry[in + i + i + 2];
                final double invDet = 1 / (m00 * m11 - m01 * m01);
                //UのバックアップとLの計算(L=CM^{-1},U=C)
                int jn = in + i + 1;
                for (int j = i + 2; j < thisDimension; j++) {
                    jn += j;
                    final double c0 = thisMxEntry[jn + i];
                    final double c1 = thisMxEntry[jn + i + 1];
                    thisMxUEntry_bk_0[j] = c0;
                    thisMxUEntry_bk_1[j] = c1;
                    thisMxEntry[jn + i] = (m11 * c0 - m01 * c1) * invDet;
                    thisMxEntry[jn + i + 1] = (-m01 * c0 + m00 * c1) * invDet;
                }
                //前進消去(A -= CM^{-1}C^T)
                jn = in + i + 1;
                for (int j = i + 2; j < thisDimension; j++) {
                    jn += j;
                    for (int k = i + 2; k <= j; k++) {
                        thisMxEntry[jn + k] -= thisMxEntry[jn + i] * thisMxUEntry_bk_0[k]
                                + thisMxEntry[jn + i + 1] * thisMxUEntry_bk_1[k];
                    }
                }
                this.pivot22[i] = true;
                i += 2;
            }
        }

        //置換行列をビルド
        this.mxP = mxPBuilder.build();
    }

    /**
     * 分解されたmxEntryを行列オブジェクトに変換.
     *
     * @throws ProcessFailedException mxMが特異な場合
     */
    private void convertToEachMatrix() throws ProcessFailedException {
        final int thisDimension = this.matrixDimension.rowAsIntValue();
        final double[] thisMxEntry = this.mxLowerEntry;

        Block2OrderSymmetricDiagonalMatrix.Builder mxMBuilder = Block2OrderSymmetricDiagonalMatrix.Builder
                .zeroBuilder(this.matrixDimension);
        LowerUnitriangularMatrix.Builder mxLBuilder =
                LowerUnitriangularMatrix.Builder.unit(this.matrixDimension);

        int c = 0;
        for (int i = 0; i < thisDimension; i++) {
            for (int k = 0; k < i - 1; k++) {
                mxLBuilder.setValue(i, k, thisMxEntry[c]);
                c++;
            }
            if (i >= 1) {
                if (this.pivot22[i - 1]) {
                    //スケールをMに反映 
                    mxMBuilder.setSubDiagonal(i - 1, thisMxEntry[c] * this.scale);
                    c++;
                } else {
                    mxLBuilder.setValue(i, i - 1, thisMxEntry[c]);
                    c++;
                }
            }
            //スケールをMに反映 
            mxMBuilder.setDiagonal(i, thisMxEntry[c] * this.scale);
            c++;
        }

        this.mxM = mxMBuilder.build();
        this.mxL = mxLBuilder.build();

        //スケールの関係で特異になるかもしれないので, 正則判定
        if (this.mxM.signOfDeterminant() == 0) {
            throw new ProcessFailedException("行列が特異");
        }
    }

    /**
     * mxEntryの行列表示に関して, 2つの行を入れ替える.
     */
    private void swapRowsAndColumnsOfArray(int index1, int index2) {
        if (index1 == index2) {
            return;
        }

        double[] thisMxLowerEntry = this.mxLowerEntry;

        final int dimension = this.matrixDimension.rowAsIntValue();
        final int indMin = Math.min(index1, index2);
        final int indMax = Math.max(index1, index2);
        //4隅以外
        final int indMinN = (indMin * (indMin + 1)) / 2;
        final int indMaxN = (indMax * (indMax + 1)) / 2;
        int j = 0;
        for (; j < indMin; j++) {
            final double temp;
            temp = thisMxLowerEntry[indMinN + j];
            thisMxLowerEntry[indMinN + j] = thisMxLowerEntry[indMaxN + j];
            thisMxLowerEntry[indMaxN + j] = temp;
        }
        j++;
        for (; j < indMax; j++) {
            final int jn = (j * (j + 1)) / 2;
            final double temp = thisMxLowerEntry[jn + indMin];
            thisMxLowerEntry[jn + indMin] = thisMxLowerEntry[indMaxN + j];
            thisMxLowerEntry[indMaxN + j] = temp;
        }
        j++;
        for (; j < dimension; j++) {
            final int jn = (j * (j + 1)) / 2;
            final double temp = thisMxLowerEntry[jn + indMin];
            thisMxLowerEntry[jn + indMin] = thisMxLowerEntry[jn + indMax];
            thisMxLowerEntry[jn + indMax] = temp;
        }
        //4隅
        final double temp = thisMxLowerEntry[indMinN + indMin];
        thisMxLowerEntry[indMinN + indMin] = thisMxLowerEntry[indMaxN + indMax];
        thisMxLowerEntry[indMaxN + indMax] = temp;
    }
}
