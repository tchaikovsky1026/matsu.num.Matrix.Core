/**
 * 2023.8.15
 */
package matsu.num.matrix.base.nlsf.helper.fact;

import matsu.num.matrix.base.BandMatrix;
import matsu.num.matrix.base.BandMatrixDimension;
import matsu.num.matrix.base.DiagonalMatrix;
import matsu.num.matrix.base.LowerUnitriangularBandBuilder;
import matsu.num.matrix.base.LowerUnitriangularEntryReadableMatrix;
import matsu.num.matrix.base.MatrixDimension;
import matsu.num.matrix.base.exception.ProcessFailedException;

/**
 * 帯行列LU分解のヘルパ. <br>
 * A = LDU. <br>
 * <br>
 * 数値安定性を得るため, 与えられた行列Aを最初に定数倍してから分解する. <br>
 * その定数倍は対角行列Dに押し付ける.
 *
 * @author Matsuura Y.
 * @version 15.0
 */
public final class LUBandFactorizationHelper {

    private final BandMatrixDimension bandMatrixDimension;
    private final double[] mxDiagonalEntry;
    private final double[] mxLowerEntry;
    private final double[] mxUpperEntry;
    private final double scale;

    private DiagonalMatrix mxD;
    private LowerUnitriangularEntryReadableMatrix mxL;
    private LowerUnitriangularEntryReadableMatrix mxUt;

    /**
     * @param matrix 
     * @param relativeEpsilon 
     * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(dim * lb > IntMax or dim * ub > IntMax)
     * @throws ProcessFailedException 行列が特異の場合, あるいはピボッティングが必要な場合, 極端な値を含み分解が完了できない場合
     */
    public LUBandFactorizationHelper(final BandMatrix matrix, double relativeEpsilon) {
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

    public DiagonalMatrix getMxD() {
        return this.mxD;
    }

    public LowerUnitriangularEntryReadableMatrix getMxL() {
        return this.mxL;
    }

    public LowerUnitriangularEntryReadableMatrix getMxUt() {
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
     * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(dim * lb > IntMax)
     */
    private double[] lowerOfMatrixToArray(final BandMatrix matrix) {

        final int thisDimension = this.bandMatrixDimension.dimension().rowAsIntValue();
        final int thisLowerBandWidth = this.bandMatrixDimension.lowerBandWidth();
        final long long_entrySize = (long) thisDimension * thisLowerBandWidth;
        if (long_entrySize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("サイズが大きすぎる");
        }

        double[] outArray = new double[(int) long_entrySize];
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
     * @throws IllegalArgumentException 行列の有効要素数が大きすぎる場合(dim * ub > IntMax)
     */
    private double[] upperOfMatrixToArray(final BandMatrix matrix) {

        final int thisDimension = this.bandMatrixDimension.dimension().rowAsIntValue();
        final int thisUpperBandWidth = this.bandMatrixDimension.upperBandWidth();
        final long long_entrySize = (long) thisDimension * thisUpperBandWidth;
        if (long_entrySize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("サイズが大きすぎる");
        }

        double[] outArray = new double[(int) long_entrySize];
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
    private void factorize(double threshold) {
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
     * @throws ProcessFailedException mxEntryに不正な値が入っている場合
     */
    private void convertToEachMatrix() {
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
        LowerUnitriangularBandBuilder mxLBuilder = LowerUnitriangularBandBuilder
                .unitBuilder(lowerBandMatrixDimension);
        LowerUnitriangularBandBuilder mxUtBuilder = LowerUnitriangularBandBuilder
                .unitBuilder(transposedUpperBandMatrixDimension);

        try {
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
        } catch (IllegalArgumentException e) {
            //setValueで不正値が入り込む可能性がある
            throw new ProcessFailedException("行列の成分に極端な値が含まれる");
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
