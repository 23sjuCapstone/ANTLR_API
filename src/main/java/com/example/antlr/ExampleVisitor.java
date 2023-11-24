package com.example.antlr;

import com.example.antlr.gen.MySqlParser;
import com.example.antlr.gen.MySqlParserBaseVisitor;
import com.example.antlrapi.dto.ColumnInfo;
import com.example.antlrapi.dto.StepSqlComponent;
import com.example.antlrapi.dto.TableInfo;
import com.ibm.icu.impl.UResource;
import org.antlr.v4.runtime.tree.ParseTree;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ExampleVisitor extends MySqlParserBaseVisitor {
    public static ArrayList<String> sqlQueue = new ArrayList<>();
    public static ArrayList<String> unionQueue = new ArrayList<>();
    public static ArrayList<StepSqlComponent> stepComponents = new ArrayList<>();


    public static int step = 0;

    public static void extractSelectComponent(MySqlParser.QuerySpecificationContext ctx, String sql) {
        ArrayList<TableInfo> usedTables = new ArrayList<>();
        ArrayList<ColumnInfo> selectedColumns = new ArrayList<>();
        ArrayList<ColumnInfo> conditionColumns = new ArrayList<>();
        ArrayList<String> conditions = new ArrayList<>();

        Boolean joinExists = null;
        ArrayList<ColumnInfo> joinedColumns = new ArrayList<>();
        ArrayList<String> on = new ArrayList<>();

        // 1. step, keyword, Sql
        step++;
        StepSqlComponent stepSqlComponent = new StepSqlComponent(step, "SELECT", sql);

        // 2. 사용된 테이블
        int tableSourceBaseCnt = ctx.fromClause().tableSources().getChild(0).getChildCount();
        if (tableSourceBaseCnt == 2) {  //  Inner Join 인 경우

            // join 테이블 저장
            TableInfo table = new TableInfo(ctx.fromClause().tableSources().getChild(0).getChild(0).getChild(0).getText(), ctx.fromClause().tableSources().getChild(0).getChild(0).getChild(2).getText());
            usedTables.add(table);
            // !!!!!!!!!!!!!!!!!InnerJoin예문, 서브쿼리 들어가는 곳 !!!!! !!!!!!!!!!!!!!!!!!!!!
            table = new TableInfo(ctx.fromClause().tableSources().getChild(0).getChild(1).getChild(2).getChild(0).getText(), ctx.fromClause().tableSources().getChild(0).getChild(1).getChild(2).getChild(2).getText());
            usedTables.add(table);

            // join 존재 여부
            joinExists = true;

            // joinSpec : 1. On 조건에 사용된 칼럼, 2. 전체 조건 Text
            ParseTree joinTreeCtx = ctx.fromClause().tableSources().getChild(0).getChild(1).getChild(3); // joinSpec:1 노드

            // 1. On 조건에 사용된 칼럼
            // 1) 등호 왼쪽 칼럼들
            String tbName = ctx.getChild(1).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getText();
            //System.out.println("right before : " + tbName);
            String colName = "";
            if (ctx.getChild(1).getChild(0).getChild(0).getChild(0).getChild(0).getChild(1) != null) {
                colName = ctx.getChild(1).getChild(0).getChild(0).getChild(0).getChild(0).getChild(1).getText().substring(1);
                joinedColumns.add(new ColumnInfo(tbName, colName));
            }

            // 2) 등호 오른쪽 칼럼들
            tbName = ctx.getChild(1).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getText();
            if (ctx.getChild(1).getChild(0).getChild(0).getChild(0).getChild(0).getChild(1) != null) {
                colName = ctx.getChild(1).getChild(0).getChild(0).getChild(0).getChild(0).getChild(1).getText().substring(1);
                joinedColumns.add(new ColumnInfo(tbName, colName));
            }

            // 2. On 전체 text
            String onExpression = "ON ";
            int childrenOn = joinTreeCtx.getChild(1).getChild(0).getChildCount();
            System.out.println(childrenOn);
            ParseTree onTree = joinTreeCtx.getChild(1).getChild(0);
            for (int i = 0; i < childrenOn; i++) {
                if (i == childrenOn - 1) {
                    onExpression += onTree.getChild(i).getText();
                } else {
                    onExpression += onTree.getChild(i).getText();
                    onExpression += " ";
                }
            }
            System.out.println("onExpression : " + onExpression);
            on.add(onExpression);
        } else {  // Join 없이 From 뒤에 테이블 ,로 구분해서 여러개 오는 경우
            int tableCnt = ctx.fromClause().tableSources().getChildCount();
            for (int i = 0; i < tableCnt; i++) {
                int index = 0;
                ParseTree tableSource = ctx.fromClause().tableSources().getChild(i);
                if (i == 1) System.out.println("Join Part : " + tableSource.getText());
                else if (tableSource.getText() == ",") continue;
                else {  // , 가 아닌 경우 (1. grade AS g, 2. person p,  3. id)
//                if(tableSource.getChild(0) != null) {
//                    System.out.println("tablesource type : " + tableSource.getChild(0).getClass().getName());
//                }
                    if (tableSource.getChild(0) != null) {
                        int aliasCnt = tableSource.getChild(0).getChildCount();

                        String tableName = tableSource.getChild(0).getChild(0).getText();
                        if (tableSource.getChild(0) instanceof MySqlParser.SubqueryTableItemContext) {
                            // tableSource.getChild(0).getClass().getName() == "com.example.antlr.gen.MySqlParser$SubqueryTableItemContext"
                            tableName = tableName.substring(1, tableName.length() - 1);
                        }
                        String alias = "";

                        if (aliasCnt == 3) {
                            alias = tableSource.getChild(0).getChild(2).getText();
                            usedTables.add(new TableInfo(tableName, alias));
                        } else if (aliasCnt == 2) {
                            alias = tableSource.getChild(0).getChild(1).getText();
                            usedTables.add(new TableInfo(tableName, alias));
                        } else {
                            usedTables.add(new TableInfo(tableName));
                        }
                    }
                }
            }
        }


        for (int i = 0; i < usedTables.size(); i++) {
            System.out.print("used table name : " + usedTables.get(i).getTableName());
            System.out.println(" / used table alias : " + usedTables.get(i).getAlias());
        }
        stepSqlComponent.setTables(usedTables);


        // 3. 선택된 칼럼
        if (ctx.selectElements().getText().equals("*")) {
            // System.out.println("in * column");
            selectedColumns.add(new ColumnInfo(ctx.fromClause().tableSources().getText(), "*"));
            System.out.println(ctx.fromClause().tableSources().getText());
        } else {
            int columnCnt = ctx.selectElements().getChildCount(); // selectElements 아래 분기 개수(, 포함)
            for (int i = 0; i < columnCnt; i++) {
                ParseTree columnSource = ctx.selectElements().getChild(i);
                if (columnSource.getText() == ",") continue;
                else {
                    if (columnSource.getChild(0) != null) {

                        String tableName = "";
                        String columnLable = "";
                        String alias = "";

                        int dotCnt = columnSource.getChildCount();
                        int tbCnt = columnSource.getChild(0).getChildCount();
                        if (dotCnt == 1) {  //  alias 없는 경우 (테이블 참조 유무 둘다 포함)
                            if (tbCnt == 1) {  // 테이블 참조 없이 칼럼 명만 있는 경우
                                columnLable = columnSource.getChild(0).getChild(0).getText();
                                if (columnSource.getChild(0).getChild(0) instanceof MySqlParser.ExpressionAtomPredicateContext) {
                                    columnLable = columnLable.substring(1, columnLable.length() - 1);
                                }
                            } else {  //  테이블 참조 있는 칼럼인 경우 (ex. c.cake)
                                tableName = columnSource.getChild(0).getChild(0).getText();
                                columnLable = columnSource.getChild(0).getChild(1).getText().substring(1);
                            }
                        } else {  //  alias 있는 경우(AS 유무 둘 다 포함)
                            if (dotCnt == 2) { // AS 없는 경우
//                            int aliasCnt = columnSource.getChild(0).getChildCount();
                                if (tbCnt == 2) {  // table 참조 있는 경우
                                    tableName = columnSource.getChild(0).getChild(0).getText();
                                    columnLable = columnSource.getChild(0).getChild(1).getText().substring(1);
                                    alias = columnSource.getChild(1).getText();
                                } else {  // table 참조 없는 경우
                                    columnLable = columnSource.getChild(0).getText();
                                    if (columnSource.getChild(0).getChild(0) instanceof MySqlParser.ExpressionAtomPredicateContext) {
                                        columnLable = columnLable.substring(1, columnLable.length() - 1);
                                    }
                                    alias = columnSource.getChild(1).getText();
                                }
                            } else {  //  dotCnt == 3 // AS 있는 경우
                                if (tbCnt == 2) {  // 테이블 참조 있는 경우
                                    tableName = columnSource.getChild(0).getChild(0).getText();
                                    columnLable = columnSource.getChild(0).getChild(1).getText().substring(1);
                                    alias = columnSource.getChild(2).getText();
                                } else {  //  테이블 참조 없는 경우
                                    columnLable = columnSource.getChild(0).getChild(0).getText();
                                    if (columnSource.getChild(0).getChild(0) instanceof MySqlParser.ExpressionAtomPredicateContext) {
                                        columnLable = columnLable.substring(1, columnLable.length() - 1);
                                    }
                                    alias = columnSource.getChild(2).getText();
                                }
                            }
                        }

                        selectedColumns.add(new ColumnInfo(tableName, columnLable, alias));
                    }
                }
            }
        }

        for (int i = 0; i < selectedColumns.size(); i++) {
            System.out.print("selectedColums Table Name : " + selectedColumns.get(i).getTableName());
            System.out.print(" / selectedColums Column Label : " + selectedColumns.get(i).getColumnLabel());
            System.out.println(" / selectedColums Alias  : " + selectedColumns.get(i).getAlias());
        }
        stepSqlComponent.setSelectedColumns(selectedColumns);


        // 4. 조건 칼럼
        if (ctx.fromClause().expression() != null) {
            ParseTree children = ctx.fromClause().expression().getChild(0);
            int childrenSize = children.getChildCount();
            for (int i = 0; i < childrenSize; i++) {
                String tableName = "unknown";
                String columnLable = "unknown";
                if(children.getChild(i) instanceof MySqlParser.ExpressionAtomPredicateContext){
//                    System.out.println("NNN : " + children.getChild(i).getChild(0).getChild(0).getClass().getName());
                    if( children.getChild(i).getChild(0).getChild(0) instanceof MySqlParser.FullColumnNameContext) {
                        if (children.getChild(i).getChild(0).getChild(0).getChildCount() == 2) {  //  테이블 참조 있는 경우
                            tableName = children.getChild(i).getChild(0).getChild(0).getChild(0).getText();
                            columnLable = children.getChild(i).getChild(0).getChild(0).getChild(1).getText().substring(1);
                        } else {  //  테이블 참조 없는 경우
                            columnLable = children.getChild(i).getChild(0).getChild(0).getChild(0).getText();
                        }
                        conditionColumns.add(new ColumnInfo(tableName, columnLable));
                    }
                    System.out.println("table and column : " + tableName + " " + columnLable);
                }

            }


            for (int i = 0; i < conditionColumns.size(); i++) {
                System.out.print("conditionColumn TableName : " + conditionColumns.get(i).getTableName());
                System.out.println(" / conditionColumn ColumnLable : " + conditionColumns.get(i).getColumnLabel());
            }

            stepSqlComponent.setConditionColumns(conditionColumns);


            // 5. Where절 전체 저장
            if (ctx.fromClause().WHERE() != null) {
                String whereExpression = "WHERE ";
                ParseTree childern2 = ctx.fromClause().expression().getChild(0);
                // System.out.println(childern2.getText());
                int childrenSize2 = childern2.getChildCount();
                for (int i = 0; i < childrenSize2; i++) {
                    if (i == childrenSize2 - 1) {
                        whereExpression += childern2.getChild(i).getText();
                    } else {
                        whereExpression += childern2.getChild(i).getText();
                        whereExpression += " ";
                    }
                }
                conditions.add(whereExpression);
                stepSqlComponent.setConditions(conditions);

                System.out.println("Where expression : " + whereExpression);
            }



            // 2. 3. 4. 저장된 컴포넌트 저장
            stepComponents.add(stepSqlComponent);
        }
    }

    public static void extractUnionComponent (MySqlParser.UnionStatementContext ctx, String sql) {
        step++;

        String unionQuery = "";
        String queryA = unionQueue.remove(0);
        String queryB = unionQueue.remove(0);
        unionQuery = queryA + " UNION " + queryB;

        StepSqlComponent stepSqlComponent = new StepSqlComponent(step, "UNION", unionQuery);
        stepComponents.add(stepSqlComponent);
    }

    @Override
    public String visitQuerySpecification (MySqlParser.QuerySpecificationContext ctx) {
        return "";
    }

    @Override
    public String visitUnionStatement (MySqlParser.UnionStatementContext ctx) {
        return "";
    }

}

