package com.intern.createdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import org.apache.ibatis.jdbc.ScriptRunner;

public class Main {
    static String path = System.getProperty("user.dir") + "/createdb/src/main/java/resources/";

    public static void main(String[] args) throws SQLException, InterruptedException, IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(path+"config.properties"));

        final String URL = props.getProperty("URL");
        final String USER = props.getProperty("USER");
        final String PASS = props.getProperty("PASS");

        Scanner sc = new Scanner(System.in);
        Connection con = DriverManager.getConnection(URL, USER, PASS);

        System.out.print("DROP and CREATE? (Y/N): ");
        if (sc.next().equalsIgnoreCase("Y")) {
            ScriptRunner sr = new ScriptRunner(con);
            sr.setAutoCommit(true);
            sr.setStopOnError(true);

            // CREATE db
            Reader reader = new BufferedReader(new FileReader(path + "/create_db.sql"));
            try {
                sr.runScript(reader);
            } catch (Exception e) {
                System.err.println(e.toString());
                return;
            }

            con = DriverManager.getConnection(URL + "library", USER, PASS);
            sr = new ScriptRunner(con);

            // CREATE tables
            for (File file : new File(path + "tables/").listFiles()) {
                if (file.getName().equals("ending.sql"))
                    continue;
                reader = new BufferedReader(new FileReader(file));
                try {
                    sr.runScript(reader);
                } catch (Exception e) {
                    System.err.println(e.toString());
                    return;
                }
            }
            reader = new BufferedReader(new FileReader(path + "tables/ending.sql"));
            try {
                sr.runScript(reader);
            } catch (Exception e) {
                System.err.println(e.toString());
                return;
            }

            sr.setDelimiter(";;");
            // CREATE crud functions
            for (File file : new File(path + "functions/crud/").listFiles()) {
                reader = new BufferedReader(new FileReader(file));
                try {
                    sr.runScript(reader);
                } catch (Exception e) {
                    System.err.println(e.toString());
                    return;
                }
            }

            // INSERT data
            reader = new BufferedReader(new FileReader(path+"insert_data.sql"));
            try {
                sr.runScript(reader);
            } catch (Exception e) {
                System.err.println(e.toString());
                return;
            }            
        }

    }
}
