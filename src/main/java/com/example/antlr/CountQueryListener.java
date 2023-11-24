package com.example.antlr;

import com.example.antlr.gen.MySqlParser;
import com.example.antlr.gen.MySqlParserBaseListener;

import java.util.ArrayList;
import java.util.List;

public class CountQueryListener extends MySqlParserBaseListener {
    int startRuleCount = 0;
    int unionCount = 0;
    int innerJoin = 0;
//    // CREATE
//    @Override public void enterColumnCreateTable(MySqlParser.ColumnCreateTableContext ctx){
//        startRuleCount++;
//    }
//    @Override public void enterCreateDatabase(MySqlParser.CreateDatabaseContext ctx) { startRuleCount++; }
//
//    //INSERT
//    @Override public void enterInsertStatement(MySqlParser.InsertStatementContext ctx) { startRuleCount++; }
//
//    // UPDATE
//    @Override public void enterUpdateStatement(MySqlParser.UpdateStatementContext ctx) { startRuleCount++; } // multiple single 추가 ??
//
//    // DELETE
//    @Override public void enterDeleteStatement(MySqlParser.DeleteStatementContext ctx) { startRuleCount++; } // multiple single 추가 ??
//
//    // DROP
//    @Override public void enterDropTable(MySqlParser.DropTableContext ctx) { startRuleCount++; }  // tablespace trigger view user database index
//    @Override public void enterDropDatabase(MySqlParser.DropDatabaseContext ctx) { startRuleCount++; }

    // SELECT (Simple Select 포함)
    @Override
    public void enterQuerySpecification(MySqlParser.QuerySpecificationContext ctx){
        startRuleCount++;
         // System.out.println("enterQuerySpecification");
//        System.out.println(ctx.getChild(1).getText());
    }
    @Override
    public void enterQuerySpecificationNointo(MySqlParser.QuerySpecificationNointoContext ctx){
        startRuleCount++;
        // System.out.println("enterQuerySpecificationNOINTO");
        //System.out.println("enterQuerySpecification" + startRuleCount++);
//        System.out.println(ctx.getChild(1).getText());
    }


    // Union
//    @Override
//    public void enterUnionSelect(MySqlParser.UnionSelectContext ctx) { }{
////        System.out.println("inside enterUnionParentheis before" + unionCount);
//        unionCount++;
////        System.out.println("inside enterUnionParentheis before" + unionCount);
//        //System.out.println("enterUnionParenthesisSelect" + startRuleCount++);
////        System.out.println("union!!!");
//    }

    @Override public void enterUnionParenthesis(MySqlParser.UnionParenthesisContext ctx) {
        unionCount++;
    }

    @Override public void enterInsertStatement(MySqlParser.InsertStatementContext ctx) {
        //System.out.println("Enter insertStatement!");
    }

    @Override public void enterInnerJoin(MySqlParser.InnerJoinContext ctx) {
        innerJoin++;
    }

    public int[] getStartRuleCount(){
        int[] result = new int[2];

        System.out.println("unionCount is : " + unionCount);
        if(unionCount == 1){
            System.out.println("startRuleCount..CHeck?: " + startRuleCount);
            result[0] = -1;
            result[1] = startRuleCount;
            return result;
        }
        else if(innerJoin == 1){
            result[0] = -2;
            result[1] = startRuleCount;
            return result;
        }
        else{
            result[0] = 0;  //  일반 중첩된 Select 문, 단일 select문 , insert, create 등등
            result[1]= startRuleCount;
            return result;
        }
    }
    public int getUnionCount() {return unionCount; }
}
