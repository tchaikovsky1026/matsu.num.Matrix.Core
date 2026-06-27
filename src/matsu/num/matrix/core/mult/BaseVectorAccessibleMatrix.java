/*
 * Copyright © 2026 Matsuura Y.
 * 
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */

/*
 * 2026.6.26
 */
package matsu.num.matrix.core.mult;

import java.util.Arrays;

import matsu.num.matrix.core.MatrixDimension;
import matsu.num.matrix.core.Vector;
import matsu.num.matrix.core.VectorDimension;
import matsu.num.matrix.core.helper.value.MatrixValidationSupport;
import matsu.num.matrix.core.validation.MatrixFormatMismatchException;

/**
 * {@link SkeletalVectorAccessibleMatrix} のベースとなるクラス.
 * 
 * @author Matsuura Y.
 */
final class BaseVectorAccessibleMatrix extends SkeletalVectorAccessibleMatrix {

    private final MatrixDimension matrixDimension;
    private final Vector[] rowVectors;
    private final Vector[] columnVectors;

    private volatile BaseVectorAccessibleMatrix transpose;
    private volatile double entryNormMax;

    /**
     * 公開されないコンストラクタ.
     * 引数はバリデーションされないので, 呼び出し元でチェックすること.
     * 
     * <p>
     * インスタンス生成時に, 必ず transpose と entryNormMax フィールドへの代入が行われなければならない.
     * </p>
     * 
     * @param matrixDimension
     * @param rowVectors
     * @param columnVectors
     */
    private BaseVectorAccessibleMatrix(MatrixDimension matrixDimension, Vector[] rowVectors, Vector[] columnVectors) {
        super();
        this.matrixDimension = matrixDimension;
        this.rowVectors = rowVectors;
        this.columnVectors = columnVectors;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public double valueAt(int row, int column) {
        MatrixValidationSupport.validateIndexInMatrix(matrixDimension, row, column);
        return rowVectors[row].valueAt(column);
    }

    @Override
    public double entryNormMax() {
        return entryNormMax;
    }

    @Override
    public MatrixDimension matrixDimension() {
        return matrixDimension;
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Vector operate(Vector operand) {
        MatrixValidationSupport.validateOperate(matrixDimension, operand.vectorDimension());

        double[] resultEntry = new double[matrixDimension.rowAsIntValue()];
        for (int j = 0, len = resultEntry.length; j < len; j++) {
            resultEntry[j] = rowVectors[j].dot(operand);
        }

        var b = Vector.Builder.zeroBuilder(matrixDimension.leftOperableVectorDimension());
        b.setEntryValue(resultEntry);
        return b.build();
    }

    /**
     * @throws MatrixFormatMismatchException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Vector operateTranspose(Vector operand) {
        MatrixValidationSupport.validateOperateTranspose(matrixDimension, operand.vectorDimension());

        double[] resultEntry = new double[matrixDimension.columnAsIntValue()];
        for (int j = 0, len = resultEntry.length; j < len; j++) {
            resultEntry[j] = columnVectors[j].dot(operand);
        }

        var b = Vector.Builder.zeroBuilder(matrixDimension.rightOperableVectorDimension());
        b.setEntryValue(resultEntry);
        return b.build();
    }

    @Override
    public BaseVectorAccessibleMatrix transpose() {
        assert transpose != null;
        return transpose;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public Vector rowVectorAt(int index) {
        return rowVectors[index];
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public Vector columnVectorAt(int index) {
        return columnVectors[index];
    }

    /**
     * 外部から呼ばれるstaticファクトリ: 行ベクトルたちから行列を生成する.
     * 
     * <p>
     * Vector の配列は内部でコピーされないので, 呼び出し元での扱いに注意すること
     * (参照を漏洩させない, 変更しない).
     * </p>
     * 
     * @param matrixDimension 行列次元
     * @param rowVectors 行ベクトルたち, 配列サイズが rows に一致し, ベクトル次元は columns に一致させる
     * @return インスタンス
     * @throws MatrixFormatMismatchException サイズミスマッチの場合
     * @throws NullPointerException 引数にnullを含む場合
     */
    static BaseVectorAccessibleMatrix fromRowVectors(MatrixDimension matrixDimension, Vector[] rowVectors) {

        // columnVectorsの生成時に引数がバリデーションされる
        return ofHelper(matrixDimension, rowVectors, calcColumnVectors(matrixDimension, rowVectors));
    }

    /**
     * 外部から呼ばれるstaticファクトリ: 列ベクトルたちから行列を生成する.
     * 
     * <p>
     * Vector の配列は内部でコピーされないので, 呼び出し元での扱いに注意すること
     * (参照を漏洩させない, 変更しない).
     * </p>
     * 
     * @param matrixDimension 行列次元
     * @param columnVectors 列ベクトルたち, 配列サイズが columns に一致し, ベクトル次元は rows に一致させる
     * @return インスタンス
     * @throws MatrixFormatMismatchException サイズミスマッチの場合
     * @throws NullPointerException 引数にnullを含む場合
     */
    static BaseVectorAccessibleMatrix fromColumnVectors(MatrixDimension matrixDimension, Vector[] columnVectors) {

        // columnVectorsの生成時に引数がバリデーションされる
        return ofHelper(matrixDimension, calcRowVectors(matrixDimension, columnVectors), columnVectors);
    }

    /**
     * 行ベクトルから列ベクトルを生成する.
     * 
     * @throws MatrixFormatMismatchException サイズミスマッチの場合
     */
    private static Vector[] calcColumnVectors(MatrixDimension matrixDimension, Vector[] rowVectors) {

        // rowVectorsは, 配列サイズがrows, ベクトル次元はcolumns
        if (rowVectors.length != matrixDimension.rowAsIntValue()) {
            throw new MatrixFormatMismatchException(
                    "illegal array.length: array.length = " + rowVectors.length +
                            ", matrixDimension.row = " + matrixDimension.rowAsIntValue());
        }
        for (Vector v : rowVectors) {
            if (!matrixDimension.rightOperable(v.vectorDimension())) {
                throw new MatrixFormatMismatchException(
                        "illegal vec.dimension: vec.dimension = " + v.vectorDimension() +
                                ", matrixDimension.column = " + matrixDimension.columnAsIntValue());
            }
        }

        return transposeVector(rowVectors);
    }

    /**
     * 列ベクトルから行ベクトルを生成する.
     * 
     * @throws MatrixFormatMismatchException サイズミスマッチの場合
     */
    private static Vector[] calcRowVectors(MatrixDimension matrixDimension, Vector[] columnVectors) {

        // columnVectorsは, 配列サイズがcolumns, ベクトル次元はrows
        if (columnVectors.length != matrixDimension.columnAsIntValue()) {
            throw new MatrixFormatMismatchException(
                    "illegal array.length: array.length = " + columnVectors.length +
                            ", matrixDimension.column = " + matrixDimension.columnAsIntValue());
        }
        for (Vector v : columnVectors) {
            if (!matrixDimension.leftOperable(v.vectorDimension())) {
                throw new MatrixFormatMismatchException(
                        "illegal vec.dimension: vec.dimension = " + v.vectorDimension() +
                                ", matrixDimension.rows = " + matrixDimension.rowAsIntValue());
            }
        }

        return transposeVector(columnVectors);
    }

    /**
     * src の転置を作成する.
     * ここに到達するまでに, バリデーションは完了していなくてはならない.
     */
    private static Vector[] transposeVector(Vector[] src) {
        int length = src[0].vectorDimension().intValue();
        VectorDimension dimension = VectorDimension.valueOf(src.length);

        Vector[] out = new Vector[length];
        for (int i = 0; i < length; i++) {
            double[] entry = new double[dimension.intValue()];
            for (int j = 0, len = entry.length; j < len; j++) {
                entry[j] = src[j].valueAt(i);
            }
            var b = Vector.Builder.zeroBuilder(dimension);
            b.setEntryValue(entry);
            out[i] = b.build();
        }

        return out;
    }

    /**
     * staticファクトリのヘルパ, 非公開.
     */
    private static BaseVectorAccessibleMatrix ofHelper(
            MatrixDimension matrixDimension, Vector[] rowVectors, Vector[] columnVectors) {

        // 転置行列を同時に生成する
        BaseVectorAccessibleMatrix out = new BaseVectorAccessibleMatrix(
                matrixDimension, rowVectors, columnVectors);
        BaseVectorAccessibleMatrix transpose = new BaseVectorAccessibleMatrix(
                matrixDimension.transpose(), columnVectors, rowVectors);

        // 転置行列どうしを結びつけ, 共通パラメータを初期化
        out.transpose = transpose;
        transpose.transpose = out;
        // 成分最大ノルムは等しい
        double entryNormMax = Arrays.stream(rowVectors)
                .mapToDouble(Vector::normMax)
                .max()
                .getAsDouble();
        out.entryNormMax = entryNormMax;
        transpose.entryNormMax = entryNormMax;

        return out;
    }
}
