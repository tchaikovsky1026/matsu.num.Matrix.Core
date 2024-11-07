/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.7
 */
package matsu.num.matrix.base.nlsf;

import matsu.num.matrix.base.BandMatrix;
import matsu.num.matrix.base.BandMatrixDimension;
import matsu.num.matrix.base.DiagonalMatrix;
import matsu.num.matrix.base.LowerUnitriangular;
import matsu.num.matrix.base.LowerUnitriangularBandMatrix;
import matsu.num.matrix.base.MatrixDimension;

/**
 * <p>
 * 帯行列の修正Cholesky分解のヘルパ. <br>
 * A = LDL<sup>T</sup>.
 * </p>
 * 
 * <p>
 * 数値安定性を得るため, 与えられた行列Aを最初に定数倍してから分解する. <br>
 * その定数倍は対角行列Dに押し付ける.
 * </p>
 *
 * @author Matsuura Y.
 * @version 22.2
 */
final class ModifiedCholeskyBandFactorizationHelper {

    private final BandMatrixDimension bandMatrixDimension;
    private final double[] mxDiagonalEntry;
    private final double[] mxLowerEntry;
    private final double scale;

    private DiagonalMatrix mxD;
    private LowerUnitriangular mxL;

    /**
     * @param matrix
     * @param relativeEpsilon
     * @throws ProcessFailedException 行列が特異の場合, ピボッティングが必要な場合
     */
    ModifiedCholeskyBandFactorizationHelper(final BandMatrix matrix, double relativeEpsilon)
            throws ProcessFailedException {
        this.scale = matrix.entryNormMax();
        if (this.scale == 0.0) {
            throw new ProcessFailedException("行列が特異(零行列である)");
        }

        this.bandMatrixDimension = matrix.bandMatrixDimension();
        this.mxDiagonalEntry = diagonalOfMatrixToArray(matrix);
        this.mxLowerEntry = lowerOfMatrixToArray(matrix);
        this.factorize(relativeEpsilon);
        this.convertToEachMatrix();
    }

    static boolean acceptedSize(BandMatrix matrix) {
        BandMatrixDimension bandMatrixDimension = matrix.bandMatrixDimension();
        final int thisDimension = bandMatrixDimension.dimension().rowAsIntValue();
        final int thisLowerBandWidth = bandMatrixDimension.lowerBandWidth();
        final long long_entrySize = (long) thisDimension * thisLowerBandWidth;

        return long_entrySize <= Integer.MAX_VALUE;
    }

    DiagonalMatrix getMxD() {
        return this.mxD;
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
     * 狭義下三角成分を配列へ.
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
     * @throws ProcessFailedException 行列が特異の場合, あるいはピボッティングが必要な場合
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
            if (Math.abs(d) <= threshold) {
                throw new ProcessFailedException("行列が特異あるいはピボッティングが必要");
            }
            //Dの計算
            final double invD = 1 / d;
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
                    thisMxLEntry[in + kp1n + j] -= thisMxLEntry[in + k + j + 1] * u_k;
                }
            }
        }
    }

    /**
     * 分解されたmxEntryを行列オブジェクトに変換.
     *
     * @throws ProcessFailedException mxDが特異な場合
     */
    private void convertToEachMatrix() throws ProcessFailedException {
        final MatrixDimension thisMatrixDimension = this.bandMatrixDimension.dimension();
        final int thisDimension = this.bandMatrixDimension.dimension().rowAsIntValue();
        final int thisLowerBandWidth = this.bandMatrixDimension.lowerBandWidth();
        final BandMatrixDimension lowerBandMatrixDimension = BandMatrixDimension.of(
                thisMatrixDimension, thisLowerBandWidth, 0);

        final double[] thisDiagonalEntry = this.mxDiagonalEntry;
        final double[] thisLowerEntry = this.mxLowerEntry;

        DiagonalMatrix.Builder mxDBuilder = DiagonalMatrix.Builder.zeroBuilder(thisMatrixDimension);
        LowerUnitriangularBandMatrix.Builder mxLBuilder =
                LowerUnitriangularBandMatrix.Builder.unit(lowerBandMatrixDimension);

        //対角成分
        //スケールを対角成分へ反映 
        for (int i = 0; i < thisDimension; i++) {
            mxDBuilder.setValue(i, thisDiagonalEntry[i] * this.scale);
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

        this.mxD = mxDBuilder.build();
        this.mxL = mxLBuilder.build();

        //スケールの関係で特異になるかもしれないので, 正則判定
        if (this.mxD.signOfDeterminant() == 0) {
            throw new ProcessFailedException("行列が特異あるいはピボッティングが必要");
        }
    }
}
