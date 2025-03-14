package com.manolito.dashflow.transformer;

import org.apache.spark.sql.*;
import org.apache.spark.sql.types.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TaigaTransformerTest {

    private static SparkSession spark;
    private static TaigaTransformer transformer;

    @BeforeAll
    static void setUp() {
        spark = SparkSession.builder()
                .appName("Test")
                .master("local[*]")
                .getOrCreate();

        Dataset<Row> mockDatesDimension = spark.createDataFrame(List.of(), new StructType());
        transformer = new TaigaTransformer(mockDatesDimension);
    }

    @AfterAll
    static void tearDown() {
        if (spark != null) {
            spark.stop();
        }
    }

    @Test
    void testTransformUserStories() {
        List<Row> rawData = Arrays.asList(
                RowFactory.create(201, 1, 2, "UserStories1", "Details1", false),
                RowFactory.create(202, 1, 3, "UsersStories2", "Details2", true)
        );

        StructType schema = new StructType()
                .add("id", DataTypes.IntegerType)
                .add("project", DataTypes.IntegerType)
                .add("epic", DataTypes.IntegerType)
                .add("subject", DataTypes.StringType)
                .add("description", DataTypes.StringType)
                .add("is_closed", DataTypes.BooleanType);

        Dataset<Row> df = spark.createDataFrame(rawData, schema);
        Dataset<Row> result = transformer.transformUserStories(df);

        assertEquals(2, result.count());

        List<Row> rows = result.collectAsList();

        Row row1 = rows.get(0);
        assertEquals(201, Optional.ofNullable(row1.getAs("original_id")).get());
        assertEquals("UserStories1", row1.getAs("story_name"));

        Row row2 = rows.get(1);
        assertEquals(202, Optional.ofNullable(row2.getAs("original_id")).get());
        assertEquals("UsersStories2", row2.getAs("story_name"));

        assertEquals(DataTypes.IntegerType, result.schema().apply("original_id").dataType());
        assertEquals(DataTypes.StringType, result.schema().apply("story_name").dataType());
    }
}
