package com.jstarcraft.rns.utility;

import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import com.jstarcraft.ai.environment.EnvironmentContext;
import com.jstarcraft.ai.environment.EnvironmentFactory;
import com.jstarcraft.ai.math.structure.MathCalculator;
import com.jstarcraft.ai.math.structure.matrix.DenseMatrix;
import com.jstarcraft.ai.math.structure.matrix.HashMatrix;
import com.jstarcraft.ai.math.structure.matrix.MatrixScalar;
import com.jstarcraft.ai.math.structure.matrix.SparseMatrix;

import it.unimi.dsi.fastutil.floats.Float2IntAVLTreeMap;
import it.unimi.dsi.fastutil.floats.Float2IntSortedMap;
import it.unimi.dsi.fastutil.longs.Long2FloatRBTreeMap;

public class SearchUtilityTestCase {

    @Test
    public void testPageRank() throws Exception {
        testPageRank(MathCalculator.SERIAL);
        testPageRank(MathCalculator.PARALLEL);
    }

    private void testPageRank(MathCalculator mode) throws Exception {
        EnvironmentContext context = EnvironmentFactory.getContext();
        Future<?> task = context.doTask(() -> {
            int dimension = 7;
            HashMatrix table = new HashMatrix(true, dimension, dimension, new Long2FloatRBTreeMap());
            table.setValue(0, 1, 0.5F);
            table.setValue(0, 2, 0.5F);

            table.setValue(2, 0, 0.3F);
            table.setValue(2, 1, 0.3F);
            table.setValue(2, 4, 0.3F);

            table.setValue(3, 4, 0.5F);
            table.setValue(3, 5, 0.5F);

            table.setValue(4, 3, 0.5F);
            table.setValue(4, 5, 0.5F);

            table.setValue(5, 3, 1F);

            table.setValue(6, 1, 0.5F);
            table.setValue(6, 3, 0.5F);
            SparseMatrix sparseMatrix = SparseMatrix.valueOf(dimension, dimension, table);
            DenseMatrix denseMatrix = DenseMatrix.valueOf(dimension, dimension);
            for (MatrixScalar scalar : sparseMatrix) {
                denseMatrix.setValue(scalar.getRow(), scalar.getColumn(), scalar.getValue());
            }

            Float2IntSortedMap sparseSort = new Float2IntAVLTreeMap();
            {
                int index = 0;
                for (float score : SearchUtility.pageRank(mode, dimension, sparseMatrix)) {
                    sparseSort.put(score, index++);
                }
            }
            Assert.assertArrayEquals(new int[] { 6, 0, 2, 1, 4, 5, 3 }, sparseSort.values().toIntArray());

            Float2IntSortedMap denseSort = new Float2IntAVLTreeMap();
            {
                int index = 0;
                for (float score : SearchUtility.pageRank(mode, dimension, denseMatrix)) {
                    denseSort.put(score, index++);
                }
            }
            Assert.assertArrayEquals(new int[] { 6, 0, 2, 1, 4, 5, 3 }, denseSort.values().toIntArray());

            Assert.assertTrue(sparseSort.equals(denseSort));
        });
        task.get();
    }

}
