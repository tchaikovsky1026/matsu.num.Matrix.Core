/*
 * Copyright © 2024 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
/*
 * 2024.4.4
 */
package matsu.num.matrix.core.helper.value;

import matsu.num.matrix.core.BandMatrixDimension;

/**
 * 帯行列上での位置属性(対角位置, 下側帯内, 上側帯内, 帯外, 行列外)を表す列挙型.
 * 
 * @author Matsuura Y.
 */
public enum BandDimensionPositionState {
    /**
     * 「位置属性」対角位置のシングルトン・インスタンス.
     */
    DIAGONAL,
    /**
     * 「位置属性」下側帯内のシングルトン・インスタンス.
     */
    LOWER_BAND,
    /**
     * 「位置属性」上側帯内のシングルトン・インスタンス.
     */
    UPPER_BAND,
    /**
     * 「位置属性」帯外, 行列内のシングルトン・インスタンス.
     */
    OUT_OF_BAND,
    /**
     * 「位置属性」行列外のシングルトン・インスタンス.
     */
    OUT_OF_MATRIX;

    /**
     * 与えられた行列indexの帯行列上位置を返す.
     *
     * @param rowIndex 行index
     * @param columnIndex 列index
     * @param bandMatrixDimension 帯行列構造
     * @return 帯行列上での位置
     */
    public static BandDimensionPositionState positionStateAt(int rowIndex, int columnIndex,
            BandMatrixDimension bandMatrixDimension) {
        if (!bandMatrixDimension.dimension().isValidIndexes(rowIndex, columnIndex)) {
            return OUT_OF_MATRIX;
        }
        if (rowIndex == columnIndex) {
            return DIAGONAL;
        } else if (columnIndex < rowIndex) {
            return columnIndex < rowIndex - bandMatrixDimension.lowerBandWidth() ? OUT_OF_BAND : LOWER_BAND;
        } else {
            return columnIndex > rowIndex + bandMatrixDimension.upperBandWidth() ? OUT_OF_BAND : UPPER_BAND;
        }
    }
}
