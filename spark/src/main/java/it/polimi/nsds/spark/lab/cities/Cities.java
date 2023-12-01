package it.polimi.nsds.spark.lab.cities;

import org.apache.spark.api.java.JavaRDD;
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

<<<<<<< Updated upstream
=======
import static org.apache.spark.sql.functions.col;

>>>>>>> Stashed changes
public class Cities {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("SparkEval")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> citiesRegionsFields = new ArrayList<>();
        citiesRegionsFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesRegionsFields.add(DataTypes.createStructField("region", DataTypes.StringType, false));
        final StructType citiesRegionsSchema = DataTypes.createStructType(citiesRegionsFields);

        final List<StructField> citiesPopulationFields = new ArrayList<>();
        citiesPopulationFields.add(DataTypes.createStructField("id", DataTypes.IntegerType, false));
        citiesPopulationFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesPopulationFields.add(DataTypes.createStructField("population", DataTypes.IntegerType, false));
        final StructType citiesPopulationSchema = DataTypes.createStructType(citiesPopulationFields);

        final Dataset<Row> citiesPopulation = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesPopulationSchema)
                .csv(filePath + "files/cities/cities_population.csv");

        final Dataset<Row> citiesRegions = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesRegionsSchema)
                .csv(filePath + "files/cities/cities_regions.csv");

        // TODO: add code here if necessary
<<<<<<< Updated upstream

        final Dataset<Row> q1 = citiesRegions
                .join(citiesPopulation, citiesPopulation.col("id").equalTo(citiesRegions.col("city")), "inner")
                .groupBy(citiesPopulation.col("region"))
=======
        /*! Query 1, counts for each region the*/
        final Dataset<Row> q1 = citiesPopulation
                .join(citiesRegions, citiesPopulation.col("city").equalTo(citiesRegions.col("city")))
                .groupBy(citiesRegions.col("region"))
>>>>>>> Stashed changes
                .sum("population")
                .select("region", "sum(population)");

        q1.show();

<<<<<<< Updated upstream
        final Dataset<Row> mostPopulated = citiesRegions
                .join(citiesPopulation, citiesPopulation.col("id").equalTo(citiesRegions.col("city")), "inner")
                .groupBy(citiesPopulation.col("region"))
                .max("population")
                .select("region", "max(population)");

        final Dataset<Row> numCities = citiesRegions
                .groupBy("region")
                .count()
                .select("region","count");

        final Dataset<Row> mostPopulatedCity = citiesRegions
                .join(citiesPopulation, citiesPopulation.col("id").equalTo(citiesRegions.col("city")), "inner");


        final Dataset<Row> q2 =

=======
        final Dataset<Row> regionCityCount = citiesRegions
                .groupBy("region")
                .count()
                .select("region", "count");
        regionCityCount.show();

        //final Dataset<Row> regionCityCountAndMostPopulated =

        final Dataset<Row> q2_pre = citiesRegions
                .join(citiesPopulation, citiesPopulation.col("city").equalTo(citiesRegions.col("city")))
                .groupBy("region")
                .max("population")
                .select("region", "max(population)")
                ;

        final Dataset<Row> q2 = q2_pre
                .join(regionCityCount, regionCityCount.col("region").equalTo(q2_pre.col("region")))
                .select(regionCityCount.col("region").as("region_1"),col("max(population)"), col("count") );
        
>>>>>>> Stashed changes
        q2.show();

        // JavaRDD where each element is an integer and represents the population of a city
        JavaRDD<Integer> population = citiesPopulation.toJavaRDD().map(r -> r.getInt(2));
        // TODO: add code here to produce the output for query Q3

        // Bookings: the value represents the city of the booking
        final Dataset<Row> bookings = spark
                .readStream()
                .format("rate")
                .option("rowsPerSecond", 100)
                .load();

        final StreamingQuery q4 = null; // TODO query Q4

        try {
            q4.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }
}