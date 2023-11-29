package it.polimi.nsds.spark.lab.friends;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;

/**
 * Implement an iterative algorithm that implements the transitive closure of friends
 * (people that are friends of friends of ... of my friends).
 *
 * Set the value of the flag useCache to see the effects of caching.
 */
public class FriendsComputation {
    private static final boolean useCache = true;

    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";
        final String appName = useCache ? "FriendsCache" : "FriendsNoCache";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName(appName)
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> fields = new ArrayList<>();
        fields.add(DataTypes.createStructField("person", DataTypes.StringType, false));
        fields.add(DataTypes.createStructField("friend", DataTypes.StringType, false));
        final StructType schema = DataTypes.createStructType(fields);

        final Dataset<Row> input = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(schema)
                .csv(filePath + "files/friends/friends.csv");

        // TODO
        boolean has_changed = true;
        long old_count = input.count();
        long new_count = 0;
        while(has_changed){
            Dataset<Row> i1 = input.as("i1");
            Dataset<Row> i2 = input.as("i2");
            Dataset<Row> closure = i1
                    .join(i2, i2.col("person").equalTo(i1.col("friend")))
                    .filter(i1.col("person").notEqual(i2.col("friend")))
                    .select(i1.col("person"),i2.col("friend"));
            input.union(closure).distinct();
            new_count = input.count();
            has_changed = new_count > old_count;
            input.cache();
        }


        spark.close();
    }
}
