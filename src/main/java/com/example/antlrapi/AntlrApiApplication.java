package com.example.antlrapi;

import com.example.antlr.ExampleVisitor;
import com.example.antlrapi.dto.Condition;
import com.example.antlrapi.dto.SqlComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;

import static com.example.antlr.ParseProcessor.*;


@SpringBootApplication
public class AntlrApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AntlrApiApplication.class, args);




//        String sql = "SELECT id FROM tb;";
//        ArrayList<SqlComponent> components = new ArrayList<>();



        String sql = "SELECT * FROM account WHERE balance >600;";
        step3(sql);


//        ArrayList<String> test = findSubquery(sql);
//        System.out.println("test size : " + test.size());
//
//        for(int i =0;i<test.size();i++){
//            System.out.println(test.get(i));
//        }

//        testApi(sql);


        //pullSubquery(sql);

//        int queryCount = step1(sql);
//        System.out.println(queryCount);
        // String sql = "SELECT first_name, last_name, age FROM employees;";
//        ArrayList<SqlComponent> components = step3(sql);
//        for(int i=0;i<components.size();i++) {
//            SqlComponent component= components.get(i);
//            System.out.println("step: " +component.getStep());
//            System.out.println("colums: " +component.getColumns());
//            System.out.println("tables: " +component.getTables());
//            System.out.println("condition: [ " );
//            Condition c = component.getCondition();
//            System.out.println("haveCondition " + c.isHaveCondition());
//            System.out.println("type " + c.getType());
//            System.out.println("subject " + c.getSubject());
//            System.out.println("operator " + c.getOperator());
//            System.out.println("object " + c.getObject() + " ]");


    }
}


