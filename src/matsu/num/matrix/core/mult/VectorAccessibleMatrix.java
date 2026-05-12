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

import matsu.num.matrix.core.EntryReadableMatrix;
import matsu.num.matrix.core.Vector;

/**
 * 行ベクトル, 列ベクトルを取得可能な行列を表現するためのインターフェース.
 * 
 * <p>
 * <u><i>
 * パッケージ外には非公開としたい.
 * </i></u>
 * </p>
 * 
 * @author Matsuura Y.
 */
interface VectorAccessibleMatrix extends EntryReadableMatrix {

    /**
     * 与えられた行番号の行ベクトルを取得する.
     * 
     * @param index index
     * @return rowVector
     * @throws IndexOutOfBoundsException indexが範囲外
     */
    public abstract Vector rowVectorAt(int index);

    /**
     * 与えられた列番号の列ベクトルを取得する.
     * 
     * @param index index
     * @return columnVector
     * @throws IndexOutOfBoundsException indexが範囲外
     */
    public abstract Vector columnVectorAt(int index);
}
