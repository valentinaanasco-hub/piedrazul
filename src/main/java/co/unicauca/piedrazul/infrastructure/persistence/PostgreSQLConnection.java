/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.infrastructure.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author santi
 */
public class PostgreSQLConnection {
        //Patron singleton
    private static Connection instance;
    
    public static Connection getConnection() throws SQLException{
        if(instance == null){
            String host = "localhost";
            String port = "5432";
            String db = "piedrazul_db";
            String user = "postgres";
            String pass = "1234";
            
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;
            try{
                instance = DriverManager.getConnection(url, user, pass);
            }catch(SQLException e){
                System.err.println("Error: Asegurese que Postgress esté encendido");
                throw e;
            }
                    
        }
        return instance;
    }
}
