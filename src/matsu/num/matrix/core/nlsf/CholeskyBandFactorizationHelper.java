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

import matsu.num.matrix.core.BandMatrix;
import matsu.num.matrix.core.BandMatrixDimension;
import matsu.num.matrix.core.DiagonalMatrix;
import matsu.num.matrix.core.LowerUnitriangular;
import matsu.num.matrix.core.LowerUnitriangularBandMatrix;
import matsu.num.matrix.core.MatrixDimension;

/**
 * 帯行列のCholesky分解のヘルパ. <br>
 * A = LD<sup>1/2</sup>D<sup>1/2</sup>L<sup>T</sup>.
 * 
 * <p>
 * 数値安定性を得るため, 与えられた行列Aを最初に定数倍してから分解する. <br>
 * その定数倍は対角行列Dに押し付ける.
 * </p>
 *
 * @author Matsuura Y.
 * @version 23.0
 */
final class CholeskyBandFactorizationHelper {

    private final BandMatrixDimension bandMatrixDimension;
    private final double[] mxDiagonalEntry;
    private final double[] mxLowerEntry;

    private final double scale;
    private DiagonalMatrix mxSqrtD;
    private LowerUnitriangular mxL;

    /**
     * @param matrix
     * @param relativeEpsilon
     * @throws ProcessFailedException 行列が正定値でない場合
     */
    CholeskyBandFactorizationHelper(final BandMatrix matrix, double relativeEpsilon) throws ProcessFailedException {
        this.scale = matrix.entryNormMax();
        if (this.scale == 0.0) {
            throw new ProcessFailedException("正定値でない(零行列である)");
        }

        this.bandMatrixDimension = matrix.bandMatrixDimension();
        this.mxDiagonalEntry = diagonalOfMatrixToArray(matrix);
        this.mxLowerEntry = lowerOfMatrixToArray(matrix);
        this.factorize(relativeEpsilon);
        this.convertToEachMatrix();
    }

    DiagonalMatrix getMxSqrtD() {
        return this.mxSqrtD;
    }

    LowerUnitriangular getMxL() {
        return this.mxL;
    }

    /**
     * 対角成分を配列へ.
     * 成分を配列に落とし込む際にスケールする.
     */
    private double[] diagonalOfMatrixToArray(final BandMatrix matrix) {

        final int thisDimension = this.bandMatrixDimension.dimension().rowAsIntValue();

        double[] outArray = new double[thisDimension];
        for (int j = 0; j < thisDimension; j++) {
            outArray[j] = matrix.valueAt(j, j) / this.scale;
        }
        return outArray;
    }

    /**
     * 非対角成分を配列へ.
     * 成分を配列に落とし込む際にスケールする.
     */
    private double[] lowerOfMatrixToArray(final BandMatrix matrix) {

        final int thisDimension = this.bandMatrixDimension.dimension().rowAsIntValue();
        final int thisLowerBandWidth = this.bandMatrixDimension.lowerBandWidth();
        final int entrySize = thisDimension * thisLowerBandWidth;

        double[] outArray = new double[entrySize];
        for (int i = 0; i < thisDimension; i++) {
            final int shift = i * thisLowerBandWidth;
            for (int j = 0, l = Math.min(thisLowerBandWidth, thisDimension - i - 1); j < l; j++) {
                int r = j + i + 1;
                int c = i;
                outArray[shift + j] = matrix.valueAt(r, c) / this.scale;
            }
        }
        return outArray;
    }

    /**
     * 行列を分解する.
     *
     * @throws ProcessFailedException 行列が正定値でない場合
     */
    private void factorize(double threshold) throws ProcessFailedException {
        final int thisDimension = this.bandMatrixDimension.dimension().rowAsIntValue();
        final int thisBandWidth = this.bandMatrixDimension.lowerBandWidth();
        final double[] thisMxDEntry = this.mxDiagonalEntry;
        final double[] thisMxLEntry = this.mxLowerEntry;

        final double[] thisMxUEntry_bk = new double[thisBandWidth];
        int in = -thisBandWidth;
        for (int i = 0; i < thisDimension; i++) {
            in += thisBandWidth;
            //正則性チェック
            final double d = thisMxDEntry[i];
            if (d <= threshold) {
                throw new ProcessFailedException("行列が正定値でない");
            }
            //Dの計算
            final double invD = 1 / d;
            thisMxDEntry[i] = Math.sqrt(d);
            //Lの計算
            for (int j = 0, l = Math.min(thisBandWidth, thisDimension - i - 1); j < l; j++) {
                thisMxUEntry_bk[j] = thisMxLEntry[in + j];
                thisMxLEntry[in + j] *= invD;
            }
            //前進消去
            for (int j = 0, l = Math.min(thisBandWidth, thisDimension - i - 1); j < l; j++) {
                thisMxDEntry[i + j + 1] -= thisMxLEntry[in + j] * thisMxUEntry_bk[j];
            }
            int kp1n = 0;
            for (int k = 0; k < thisBandWidth; k++) {
                kp1n += thisBandWidth;
                final double u_k = thisMxUEntry_bk[k];
                for (int j = 0, l = Math.min(thisBandWidth - k - 1, thisDimension - i - k - 1); j < l; j++) {
                    thisMxLEntry[in + kp1n + j] -= thisMxLEntry[in + k + 1 + j] * u_k;
                }
            }
        }
    }

    /**
     * 分解されたmxEntryを行列オブジェクトに変換.
     *
     * @throws ProcessFailedException mxDが正則にならない場合
     */
    private void convertToEachMatrix() throws ProcessFailedException {
        final MatrixDimension thisMatrixDimension = this.bandMatrixDimension.dimension();
        final int thisDimension = thisMatrixDimension.rowAsIntValue();
        final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();
        final BandMatrixDimension lowerBandMatrixDimension = BandMatrixDimension.of(
                thisMatrixDimension, thisLowerBandWidth, 0);

        final double[] thisSqrtDiagonalEntry = this.mxDiagonalEntry;
        final double[] thisLowerEntry = this.mxLowerEntry;

        DiagonalMatrix.Builder mxSqrtDBuilder = DiagonalMatrix.Builder.zeroBuilder(thisMatrixDimension);
        LowerUnitriangularBandMatrix.Builder mxLBuilder =
                LowerUnitriangularBandMatrix.Builder.unit(lowerBandMatrixDimension);

        //対角行列(sqrtD)にスケールを反映させる

        double sqrtScale = Math.sqrt(this.scale);
        //対角成分
        for (int i = 0; i < thisDimension; i++) {
            mxSqrtDBuilder.setValue(i, thisSqrtDiagonalEntry[i] * sqrtScale);
        }

        //狭義下三角成分
        for (int i = 0; i < thisDimension; i++) {
            final int shift = i * thisLowerBandWidth;
            for (int j = 0, l = Math.min(thisLowerBandWidth, thisDimension - i - 1); j < l; j++) {
                int r = j + i + 1;
                int c = i;
                mxLBuilder.setValue(r, c, thisLowerEntry[shift + j]);
            }
        }

        this.mxSqrtD = mxSqrtDBuilder.build();
        this.mxL = mxLBuilder.build();

        //スケールの関係で特異になるかもしれないので, 正則判定
        if (this.mxSqrtD.signOfDeterminant() == 0) {
            throw new ProcessFailedException("行列が正定値でない");
        }
    }
}
