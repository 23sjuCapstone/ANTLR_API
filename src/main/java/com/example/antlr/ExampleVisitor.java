package com.example.antlr;

import com.example.antlr.gen.MySqlParser;
import com.example.antlr.gen.MySqlParserBaseVisitor;
import com.example.antlrapi.dto.ColumnInfo;
import com.example.antlrapi.dto.StepSqlComponent;
import com.example.antlrapi.dto.TableInfo;
import com.ibm.icu.impl.UResource;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;

public class ExampleVisitor extends MySqlParserBaseVisitor {
    public static ArrayList<String> sqlQueue = new ArrayList<>();
    public static ArrayList<String> unionQueue = new ArrayList<>();
    public static ArrayList<StepSqlComponent> stepComponents = new ArrayList<>();


    public static int step = 0;
    public static void extractSelectComponent(MySqlParser.QuerySpecificationContext ctx, String sql){
        ArrayList<TableInfo> usedTables = new ArrayList<>();
        ArrayList<ColumnInfo> selectedColumns = new ArrayList<>();
        ArrayList<ColumnInfo> conditionColumns = new ArrayList<>();

        // 1. step, keyword, Sql
        step++;
        StepSqlComponent stepSqlComponent = new StepSqlComponent(step, "SELECT", sql);

        // 2. 사용된 테이블
        int tableCnt = ctx.fromClause().tableSources().getChildCount();
        for(int i = 0; i < tableCnt; i++) {
            int index = 0;
            ParseTree tableSource = ctx.fromClause().tableSources().getChild(i);
            if(tableSource.getText() == ",") continue;
            else{  // , 가 아닌 경우 (1. grade AS g, 2. person p,  3. id)
                if(tableSource.getChild(0) != null) {
                    int aliasCnt = tableSource.getChild(0).getChildCount();

                    String tableName = tableSource.getChild(0).getChild(0).getText();
                    String alias = "";

                    if(aliasCnt == 3) {
                        alias = tableSource.getChild(0).getChild(2).getText();
                        usedTables.add(new TableInfo(tableName, alias));
                    }
                    else if(aliasCnt == 2){
                        alias = tableSource.getChild(0).getChild(1).getText();
                        usedTables.add(new TableInfo(tableName, alias));
                    }
                    else{
                        usedTables.add(new TableInfo(tableName));
                    }
                }
            }
        }
//        for(int i=0;i<usedTables.size();i++) {
//            System.out.println(usedTables.get(i).getTableName());
//            System.out.println(usedTables.get(i).getAlias());
//        }
        stepSqlComponent.setTables(usedTables);

        // 3. 선택된 칼럼
        int columnCnt = ctx.selectElements().getChildCount();
        for(int i=0; i < columnCnt; i++) {
            ParseTree columnSource = ctx.selectElements().getChild(i);
            if(columnSource.getText() == ",") continue;
            else {
                if(columnSource.getChild(0) != null) {
                    int dotCnt = columnSource.getChild(0).getChildCount();

                    String tableName = "";
                    String columnLable = "";

                    if(dotCnt == 2) {  // 테이블명.칼럼명 인 경우
                        tableName = columnSource.getChild(0).getChild(0).getText();
                        columnLable = columnSource.getChild(0).getChild(1).getText().substring(1);
                        selectedColumns.add(new ColumnInfo(tableName, columnLable));
                    }
                    else{  // 그냥 칼럼 명인 경우
                        columnLable = columnSource.getChild(0).getChild(0).getText();
                        selectedColumns.add(new ColumnInfo(columnLable));
                    }
                }
            }
        }


        for(int i=0;i<selectedColumns.size();i++) {
            System.out.println(selectedColumns.get(i).getTableName());
            System.out.println(selectedColumns.get(i).getColumnLabel());
        }
        stepSqlComponent.setSelectedColumns(selectedColumns);



        stepComponents.add(stepSqlComponent);
    }

    @Override public String visitQuerySpecification(MySqlParser.QuerySpecificationContext ctx) { return ""; }


}
