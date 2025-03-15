package com.manolito.dashflow.loader;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TasksDataWarehouseLoaderTest {

    @Autowired
    private TasksDataWarehouseLoader loader;

    @Autowired
    private SparkSession spark;

    @Test
    public void testSaveData() {
        Dataset<Row> testData = spark.createDataFrame(
                List.of(
                        new org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema(
                                new Object[]{"test", true},
                                new org.apache.spark.sql.types.StructType(new org.apache.spark.sql.types.StructField[]{
                                        org.apache.spark.sql.types.DataTypes.createStructField("tool_name", org.apache.spark.sql.types.DataTypes.StringType, false),
                                        org.apache.spark.sql.types.DataTypes.createStructField("is_active", org.apache.spark.sql.types.DataTypes.BooleanType, false)
                                })
                        )
                ),
                new org.apache.spark.sql.types.StructType(new org.apache.spark.sql.types.StructField[]{
                        org.apache.spark.sql.types.DataTypes.createStructField("tool_name", org.apache.spark.sql.types.DataTypes.StringType, false),
                        org.apache.spark.sql.types.DataTypes.createStructField("is_active", org.apache.spark.sql.types.DataTypes.BooleanType, false)
                })
        );

        loader.save(testData, "tools");
        testData.show();

        Dataset<Row> loadedData = loader.loadDimension("tools", "Test Tool");
        assertTrue(loadedData.count() > 0, "Dados não foram salvos corretamente no banco de dados.");
    }
}