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
        final Dataset<Row> input2 =input; // Does this really copy?
        // SELECT T.person, T2.friend FROM T, T2 WHERE T.friend in T2.person
        // T.friend = T2.person. And... Want to add, if not exists, T.person, T2.friend
        // ALL TRANSFORMATION ON IMMUTABLE DATA! CREATING NEW DATABASE! STILL THERE! CACHE IT
        final Dataset<Row> friendsOfFriends = input
                .where(input.col("friend").and(input2.col("person"))).select(input.col("person"), input2.col("friend"));
        final Dataset<Row> toAdd = friendsOfFriends
                .drop()
                .where(friendsOfFriends.col("person").equalTo(input.col("person")).and(friendsOfFriends.col("friend").equalTo(input.col("friend"))));
        spark.close();
        input.show();
        toAdd.show();
    }
}
