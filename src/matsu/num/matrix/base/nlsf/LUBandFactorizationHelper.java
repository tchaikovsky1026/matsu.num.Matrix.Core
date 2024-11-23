/*
 * Copyright (c) 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.11.23
 */
package matsu.num.matrix.base.nlsf;

import matsu.num.matrix.base.BandMatrix;
import matsu.num.matrix.base.BandMatrixDimension;
import matsu.num.matrix.base.DiagonalMatrix;
import matsu.num.matrix.base.LowerUnitriangular;
import matsu.num.matrix.base.LowerUnitriangularBandMatrix;
import matsu.num.matrix.base.MatrixDimension;

/**
 * 帯行列LU分解のヘルパ. <br>
 * A = LDU.
 * 
 * <p>
 * 数値安定性を得るため, 与えられた行列Aを最初に定数倍してから分解する. <br>
 * その定数倍は対角行列Dに押し付ける.
 * </p>
 *
 * @author Matsuura Y.
 * @version 23.0
 */
final class LUBandFactorizationHelper {

    private final BandMatrixDimension bandMatrixDimension;
    private final double[] mxDiagonalEntry;
    private final double[] mxLowerEntry;
    private final double[] mxUpperEntry;
    private final double scale;

    private DiagonalMatrix mxD;
    private LowerUnitriangular mxL;
    private LowerUnitriangular mxUt;

    /**
     * @param matrix 受け入れ可能な行列
     * @param relativeEpsilon
     * @throws ProcessFailedException 行列が特異の場合, あるいはピボッティングが必要な場合
     */
    LUBandFactorizationHelper(final BandMatrix matrix, double relativeEpsilon) throws ProcessFailedException {
        this.scale = matrix.entryNormMax();
        if (this.scale == 0.0) {
            throw new ProcessFailedException("行列が特異(零行列である)");
        }
        this.bandMatrixDimension = matrix.bandMatrixDimension();
        this.mxDiagonalEntry = diagonalOfMatrixToArray(matrix);
        this.mxLowerEntry = lowerOfMatrixToArray(matrix);
        this.mxUpperEntry = upperOfMatrixToArray(matrix);
        this.factorize(relativeEpsilon);
        this.convertToEachMatrix();
    }

    DiagonalMatrix getMxD() {
        return this.mxD;
    }

    LowerUnitriangular getMxL() {
        return this.mxL;
    }

    LowerUnitriangular getMxUt() {
        return this.mxUt;
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
     * 
     */
    private double[] lowerOfMatrixToArray(final BandMatrix matrix) {

        final int thisDimension = this.bandMatrixDimension.dimension().rowAsIntValue();
        final int thisLowerBandWidth = this.bandMatrixDimension.lowerBandWidth();
        final int lower_entrySize = thisDimension * thisLowerBandWidth;

        double[] outArray = new double[lower_entrySize];
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
     * 狭義上三角成分を配列へ.
     * 成分を配列に落とし込む際にスケールする.
     * 
     */
    private double[] upperOfMatrixToArray(final BandMatrix matrix) {

        final int thisDimension = this.bandMatrixDimension.dimension().rowAsIntValue();
        final int thisUpperBandWidth = this.bandMatrixDimension.upperBandWidth();
        final int upper_entrySize = thisDimension * thisUpperBandWidth;

        double[] outArray = new double[upper_entrySize];
        for (int i = 0; i < thisDimension; i++) {
            final int shift = i * thisUpperBandWidth;
            for (int j = 0, l = Math.min(thisUpperBandWidth, thisDimension - i - 1); j < l; j++) {
                int r = i;
                int c = j + i + 1;
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
        final int thisLowerBandWidth = this.bandMatrixDimension.lowerBandWidth();
        final int thisUpperBandWidth = this.bandMatrixDimension.upperBandWidth();
        final int thisMinBandWidth = Math.min(thisLowerBandWidth, thisUpperBandWidth);
        final double[] thisDiagonalEntry = this.mxDiagonalEntry;
        final double[] thisLowerEntry = this.mxLowerEntry;
        final double[] thisUpperEntry = this.mxUpperEntry;
        int inl = -thisLowerBandWidth;
        int inu = -thisUpperBandWidth;
        for (int i = 0; i < thisDimension; i++) {
            inl += thisLowerBandWidth;
            inu += thisUpperBandWidth;
            //正則性チェック
            final double d = thisDiagonalEntry[i];
            if (Math.abs(d) <= threshold) {
                throw new ProcessFailedException("行列が特異,あるいはピボッティングが必要");
            }
            //Dの計算
            final double invD = 1 / d;
            //Lの計算
            for (int j = 0, l = Math.min(thisLowerBandWidth, thisDimension - i - 1); j < l; j++) {
                thisLowerEntry[inl + j] *= invD;
            }
            //前進消去
            for (int j = 0, l = Math.min(thisMinBandWidth, thisDimension - i - 1); j < l; j++) {
                thisDiagonalEntry[i + j + 1] -= thisLowerEntry[inl + j] * thisUpperEntry[inu + j];
            }
            int kp1n = 0;
            for (int k = 0; k < thisMinBandWidth; k++) {
                kp1n += thisLowerBandWidth;
                final double u_k = thisUpperEntry[inu + k];
                for (int j = 0, l = Math.min(thisLowerBandWidth - k - 1, thisDimension - i - k - 1); j < l; j++) {
                    thisLowerEntry[inl + kp1n + j] -= thisLowerEntry[inl + k + j + 1] * u_k;
                }
            }
            int jp1n = 0;
            for (int j = 0; j < thisMinBandWidth; j++) {
                jp1n += thisUpperBandWidth;
                final double l_j = thisLowerEntry[inl + j];
                for (int k = 0, l = Math.min(thisUpperBandWidth - j - 1, thisDimension - i - j - 1); k < l; k++) {
                    thisUpperEntry[inu + jp1n + k] -= l_j * thisUpperEntry[inu + j + k + 1];
                }
            }
            //Uの計算  
            for (int k = 0, l = Math.min(thisUpperBandWidth, thisDimension - i - 1); k < l; k++) {
                thisUpperEntry[inu + k] *= invD;
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
        final int thisLowerBandWidth = this.bandMatrixDimension.lowerBandWidth();
        final int thisUpperBandWidth = this.bandMatrixDimension.upperBandWidth();
        final BandMatrixDimension lowerBandMatrixDimension = BandMatrixDimension.of(
                thisMatrixDimension, thisLowerBandWidth, 0);
        final BandMatrixDimension transposedUpperBandMatrixDimension = BandMatrixDimension.of(
                thisMatrixDimension, thisUpperBandWidth, 0);

        final double[] thisDiagonalEntry = this.mxDiagonalEntry;
        final double[] thisLowerEntry = this.mxLowerEntry;
        final double[] thisUpperEntry = this.mxUpperEntry;

        DiagonalMatrix.Builder mxDBuilder = DiagonalMatrix.Builder.zeroBuilder(thisMatrixDimension);
        LowerUnitriangularBandMatrix.Builder mxLBuilder =
                LowerUnitriangularBandMatrix.Builder.unit(lowerBandMatrixDimension);
        LowerUnitriangularBandMatrix.Builder mxUtBuilder =
                LowerUnitriangularBandMatrix.Builder.unit(transposedUpperBandMatrixDimension);

        //対角成分
        //スケールを反映
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

        //狭義上三角成分
        for (int i = 0; i < thisDimension; i++) {
            final int shift = i * thisUpperBandWidth;
            for (int j = 0, l = Math.min(thisUpperBandWidth, thisDimension - i - 1); j < l; j++) {
                int r = j + i + 1;
                int c = i;
                mxUtBuilder.setValue(r, c, thisUpperEntry[shift + j]);
            }
        }

        this.mxD = mxDBuilder.build();
        this.mxL = mxLBuilder.build();
        this.mxUt = mxUtBuilder.build();

        //スケールの関係で特異になるかもしれないので, 正則判定
        if (this.mxD.signOfDeterminant() == 0) {
            throw new ProcessFailedException("行列が特異,あるいはピボッティングが必要");
        }
    }
}
