package it.polimi.nsds.spark.lab.bank;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;

import static org.apache.spark.sql.functions.*;

/**
 * Bank example
 *
 * Input: csv files with list of deposits and withdrawals, having the following
 * schema ("person: String, account: String, amount: Int)
 *
 * Queries
 * Q1. Print the total amount of withdrawals for each person
 * Q2. Print the person with the maximum total amount of withdrawals
 * Q3. Print all the accounts with a negative balance
 * Q4. Print all accounts in descending order of balance
 *
 * The code exemplifies the use of SQL primitives.  By setting the useCache variable,
 * one can see the differences when enabling/disabling cache.
 */
public class Bank {
    private static final boolean useCache = true;

    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";
        final String appName = useCache ? "BankWithCache" : "BankNoCache";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName(appName)
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> mySchemaFields = new ArrayList<>();
        mySchemaFields.add(DataTypes.createStructField("person", DataTypes.StringType, true));
        mySchemaFields.add(DataTypes.createStructField("account", DataTypes.StringType, true));
        mySchemaFields.add(DataTypes.createStructField("amount", DataTypes.IntegerType, true));
        final StructType mySchema = DataTypes.createStructType(mySchemaFields);

        final Dataset<Row> deposits = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(mySchema)
                .csv(filePath + "files/bank/deposits.csv");

        final Dataset<Row> withdrawals = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(mySchema)
                .csv(filePath + "files/bank/withdrawals.csv");

        // Used in two different queries
        if (useCache) {
            withdrawals.cache();
        }

        // Q1. Total amount of withdrawals for each person
        System.out.println("Total amount of withdrawals for each person");
        final Dataset<Row> totalwithdrawls = withdrawals
                .groupBy("account")
                        .sum("amount")
                                .select("person","sum(amount)");


        // Q2. Person with the maximum total amount of withdrawals
        System.out.println("Person with the maximum total amount of withdrawals");
        final long sumWithdrawals = totalwithdrawls
                .agg(max("sum(amount)"))
                        .first()
                                .getLong(0);

        final Dataset<Row> maxWithdrawls = withdrawals
                .filter(withdrawals.col("sum(amount)").equalTo(totalwithdrawls));

        // Q3 Accounts with negative balance
        final Dataset<Row> totWithdrawls = withdrawals
                .groupBy("account")
                .sum("amount")
                .select("account","sum(amount)");

        final Dataset<Row> totDeposit = deposits
                .groupBy("account")
                        .sum("amount")
                                .select("account","sum(amount)");

        final Dataset<Row> negativeAccount = totWithdrawls
                .join(totDeposit, totDeposit.col("account").equalTo(totWithdrawls.col("account")),"left_outer")
                        .filter(totDeposit.col("sum(amount)").isNull().and(totWithdrawls.col("sum(amount)").gt(0))
                                .or(totDeposit.col("sum(amount)").lt(totWithdrawls.col("sum(amount)"))))
                                .select(totWithdrawls.col("account"));


        // Q4 Accounts in descending order of balance
        System.out.println("Accounts in descending order of balance");

        // TODO
        final Dataset<Row> totBalance = withdrawals
                .join(deposits, withdrawals.col("account").equalTo(deposits.col("account")),"outer");



        spark.close();

    }
}