/*
 * Copyright © 2026 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.5.12
 */
package matsu.num.matrix.core.mult;

import java.util.Objects;

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.Matrix;
import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.UnitMatrix;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * 行列の乗算に関わるコンテナ.
 * 
 * <p>
 * このクラスはミュータブルであり, スレッドセーフでない. <br>
 * ただし, 最低限の不変式は維持されるようにする.
 * </p>
 * 
 * @author Matsuura Y.
 */
final class BaseMultiplyingContainer {

    private final MatrixDimension baseMatrixDimension;
    private final MatrixDimension leftSquareDimension;
    private final MatrixDimension rightSquareDimension;

    // ソースによる初期化時に使用される
    private final Object lock = new Object();
    private volatile EntryReadableMatrix volatileSrc;

    private volatile Vector[] leftSideColumnVectors = null;
    private volatile Vector[] rightSideRowVectors = null;

    /**
     * 非公開のコンストラクタ. <br>
     * 引数はnullであってはいけない.
     */
    private BaseMultiplyingContainer(EntryReadableMatrix src) {
        super();
        baseMatrixDimension = src.matrixDimension();
        leftSquareDimension = baseMatrixDimension.leftSquareDimension();
        rightSquareDimension = baseMatrixDimension.rightSquareDimension();

        volatileSrc = src;
    }

    /**
     * 左から行列を掛ける. <br>
     * サイズマッチした正方行列でなければならない.
     * 
     * @param matrix matrix
     * @throws MatrixFormatMismatchException 行列次元が不正の場合
     * @throws NullPointerException 引数がnullの場合
     */
    void operateLeftSide(Matrix matrix) {
        if (!matrix.matrixDimension().equals(leftSquareDimension)) {
            throw new MatrixFormatMismatchException(
                    "illegal matrix dimension: " + matrix.matrixDimension());
        }
        initilizeLeftSide();

        // leftColumnVec の初期化: null なら左演算用の単位行列で置き換え
        Vector[] currentLeftColumnVecs = leftSideColumnVectors;
        if (Objects.isNull(currentLeftColumnVecs)) {
            currentLeftColumnVecs =
                    createColumnVectorsFrom(UnitMatrix.matrixOf(leftSquareDimension));
        }

        // 行列積の実行
        int length = currentLeftColumnVecs.length;
        Vector[] updated = new Vector[length];
        for (int k = 0; k < length; k++) {
            updated[k] = matrix.operate(currentLeftColumnVecs[k]);
        }
        leftSideColumnVectors = updated;
    }

    /**
     * 右から行列を掛ける. <br>
     * サイズマッチした正方行列でなければならない.
     * 
     * @param matrix matrix
     * @throws MatrixFormatMismatchException 行列次元が不正の場合
     * @throws NullPointerException 引数がnullの場合
     */
    void operateRightSide(Matrix matrix) {
        if (!matrix.matrixDimension().equals(rightSquareDimension)) {
            throw new MatrixFormatMismatchException(
                    "illegal matrix dimension: " + matrix.matrixDimension());
        }
        initilizeRightSide();

        // rightRowVec の初期化: null なら右演算用の単位行列で置き換え
        Vector[] currentRightRowVecs = rightSideRowVectors;
        if (Objects.isNull(currentRightRowVecs)) {
            currentRightRowVecs =
                    createRowVectorsFrom(UnitMatrix.matrixOf(rightSquareDimension));
        }

        // 行列積の実行
        int length = currentRightRowVecs.length;
        Vector[] updated = new Vector[length];
        for (int j = 0; j < length; j++) {
            updated[j] = matrix.operateTranspose(currentRightRowVecs[j]);
        }
        rightSideRowVectors = updated;
    }

    /** 左列ベクトル側の初期化 */
    private void initilizeLeftSide() {
        if (Objects.isNull(volatileSrc)) {
            return;
        }
        synchronized (lock) {
            EntryReadableMatrix src = volatileSrc;
            if (Objects.isNull(src)) {
                return;
            }
            volatileSrc = null;
            leftSideColumnVectors = createColumnVectorsFrom(src);
        }
    }

    /** 右行ベクトル側の初期化 */
    private void initilizeRightSide() {
        if (Objects.isNull(volatileSrc)) {
            return;
        }
        synchronized (lock) {
            EntryReadableMatrix src = volatileSrc;
            if (Objects.isNull(src)) {
                return;
            }
            volatileSrc = null;
            rightSideRowVectors = createRowVectorsFrom(src);
        }
    }

    /**
     * 結果を行列として返す.
     * 
     * @return 結果
     */
    BaseVectorAccessibleMatrix build() {
        // leftSide初期化を試みる -> どちらかはnullでない
        initilizeLeftSide();
        Vector[] currentLeftColumnVecs = leftSideColumnVectors;
        Vector[] currentRightRowVecs = rightSideRowVectors;

        if (Objects.isNull(currentLeftColumnVecs)) {
            return BaseVectorAccessibleMatrix
                    .fromRowVectors(baseMatrixDimension, currentRightRowVecs);
        }
        if (Objects.isNull(currentRightRowVecs)) {
            return BaseVectorAccessibleMatrix
                    .fromColumnVectors(baseMatrixDimension, currentLeftColumnVecs);
        }

        // 左右両方が埋まっている場合, シンプルなベクトル積を試みる
        assert currentLeftColumnVecs.length == currentRightRowVecs.length : "length mismatch";
        int row = baseMatrixDimension.rowAsIntValue();
        int column = baseMatrixDimension.columnAsIntValue();
        double[][] entry = new double[row][column];
        for (int i = 0, len = currentLeftColumnVecs.length; i < len; i++) {
            double[] leftColumnVec = currentLeftColumnVecs[i].entryAsArray();
            double[] rightRowVec = currentRightRowVecs[i].entryAsArray();
            for (int j = 0; j < row; j++) {
                double[] entry_j = entry[j];
                double leftColumnVec_j = leftColumnVec[j];
                for (int k = 0; k < column; k++) {
                    double v = entry_j[k];
                    v = v + leftColumnVec_j * rightRowVec[k];
                    entry_j[k] = EntryReadableMatrix.modified(v);
                }
            }
        }

        // 行ベクトルを生成
        Vector[] resultRowVectors = new Vector[row];
        for (int j = 0; j < row; j++) {
            var b = Vector.Builder.zeroBuilder(baseMatrixDimension.rightOperableVectorDimension());
            b.setEntryValue(entry[j]);
            resultRowVectors[j] = b.build();
        }

        return BaseVectorAccessibleMatrix.fromRowVectors(baseMatrixDimension, resultRowVectors);
    }

    /**
     * 与えた行列をベースとする, 乗算コンテナを作成する.
     * 
     * @param base ベース行列
     * @return コンテナ
     * @throws NullPointerException 引数がnullの場合
     */
    static BaseMultiplyingContainer basedOn(EntryReadableMatrix base) {
        return new BaseMultiplyingContainer(base);
    }

    /** 与えられた行列から, 行ベクトルたちを抽出する. */
    private static Vector[] createRowVectorsFrom(EntryReadableMatrix src) {
        MatrixDimension matrixDimension = src.matrixDimension();

        Vector[] out = new Vector[matrixDimension.rowAsIntValue()];
        VectorDimension vectorDimension = matrixDimension.rightOperableVectorDimension();
        for (int j = 0; j < out.length; j++) {
            double[] entry = new double[matrixDimension.columnAsIntValue()];
            for (int k = 0; k < entry.length; k++) {
                entry[k] = src.valueAt(j, k);
            }

            var b = Vector.Builder.zeroBuilder(vectorDimension);
            b.setEntryValue(entry);
            out[j] = b.build();
        }

        return out;
    }

    /** 与えられた行列から, 列ベクトルたちを抽出する. */
    private static Vector[] createColumnVectorsFrom(EntryReadableMatrix src) {
        MatrixDimension matrixDimension = src.matrixDimension();

        Vector[] out = new Vector[matrixDimension.columnAsIntValue()];
        VectorDimension vectorDimension = matrixDimension.leftOperableVectorDimension();
        for (int k = 0; k < out.length; k++) {
            double[] entry = new double[matrixDimension.rowAsIntValue()];
            for (int j = 0; j < entry.length; j++) {
                entry[j] = src.valueAt(j, k);
            }

            var b = Vector.Builder.zeroBuilder(vectorDimension);
            b.setEntryValue(entry);
            out[k] = b.build();
        }

        return out;
    }
}
