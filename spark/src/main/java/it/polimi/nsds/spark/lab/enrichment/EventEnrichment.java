package it.polimi.nsds.spark.lab.enrichment;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.window;

/**
 * This code snippet exemplifies a typical scenario in event processing: merging
 * incoming events with some background knowledge.
<<<<<<< Updated upstream
 * <p>
 * A static dataset (read from a file) classifies products (associates products to the
 * class they belong to).  A stream of products is received from a socket.
 * <p>
=======
 *
 * A static dataset (read from a file) classifies products (associates products to the
 * class they belong to).  A stream of products is received from a socket.
 *
>>>>>>> Stashed changes
 * We want to count the number of products of each class in the stream. To do so, we
 * need to integrate static knowledge (product classification) and streaming data
 * (occurrences of products in the stream).
 */
public class EventEnrichment {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String socketHost = args.length > 1 ? args[1] : "localhost";
        final int socketPort = args.length > 2 ? Integer.parseInt(args[2]) : 9999;
        final String filePath = args.length > 3 ? args[3] : "./";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("EventEnrichment")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> productClassificationFields = new ArrayList<>();
        productClassificationFields.add(DataTypes.createStructField("product", DataTypes.StringType, false));
        productClassificationFields.add(DataTypes.createStructField("classification", DataTypes.StringType, false));
        final StructType productClassificationSchema = DataTypes.createStructType(productClassificationFields);

        final Dataset<Row> inStream = spark
                .readStream()
<<<<<<< Updated upstream
=======
                /*! Rate is for debugging there is ... that creates a map with the integer value, ... Time might be growing, just for debugging*/
>>>>>>> Stashed changes
                .format("rate")
                .option("rowsPerSecond", 1)
                .load();

        final Dataset<Row> productsClassification = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(productClassificationSchema)
                .csv(filePath + "files/enrichment/product_classification.csv");

<<<<<<< Updated upstream
        // TODO
        Dataset<Row> inStreamDF = inStream.toDF("product");

=======

        // TODO
        /*! Here we just change the columns names, for the join... */
        Dataset<Row> inStreamDF = inStream.toDF("timestamp", "product");

        /*! We query, creating a query and then using the stream commands we start it and print them as output in the console*/
>>>>>>> Stashed changes
        final StreamingQuery query = inStreamDF
                .join(productsClassification, productsClassification.col("product").equalTo(inStreamDF.col("product")))
                .groupBy(
                        window(col("timestamp"), "30 seconds", "10 seconds"),
                        productsClassification.col("classification")
                )
                .count()
<<<<<<< Updated upstream
=======
                /*! End of query and write to console + start the query*/
>>>>>>> Stashed changes
                .writeStream()
                .outputMode("update")
                .format("console")
                .start();
<<<<<<< Updated upstream
=======
        try {
            /*! We need to await the termination to see the terminal console while the task is running! */
            query.awaitTermination();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
>>>>>>> Stashed changes

        spark.close();
    }
}