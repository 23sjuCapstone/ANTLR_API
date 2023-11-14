package com.example.antlr;

import com.example.antlr.gen.MySqlParser;
import com.example.antlr.gen.MySqlParserBaseListener;
import com.example.antlrapi.dto.Condition;
import com.example.antlrapi.dto.SqlComponent;

import java.util.ArrayList;

public class ExtractComponentListener extends MySqlParserBaseListener {

    private SqlComponent sqlComponent = new SqlComponent();
    private Condition condition = new Condition();
    int flag = 0;

    public void extractComponent(MySqlParser.QuerySpecificationContext ctx){
        String keyword = "";
        ArrayList<String> columns = new ArrayList<>();
        ArrayList<String> tables = new ArrayList<>();

        keyword = ctx.SELECT().getText();

        int columnCnt = ctx.selectElements().getChildCount();
        for(int i=0; i < columnCnt; i++) {
            int index = 0;
            String str = ctx.selectElements().getChild(i).getText();
            if(str.equals(",")) continue;
            else {
                columns.add(str);
            }
        }

        int tableCnt = ctx.fromClause().tableSources().getChildCount();
        for(int i = 0; i < tableCnt; i++) {
            int index = 0;
            String str = ctx.fromClause().tableSources().getChild(i).getText();
            if(str.equals(",")) continue;
            else tables.add(str);
        }

        // From Clause
        if (ctx.fromClause().WHERE() != null){
            condition.setHaveCondition(true);
            condition.setType(ctx.fromClause().WHERE().getText());
            for (org.antlr.v4.runtime.tree.ParseTree child : ctx.fromClause().expression().children) {

                condition.setSubject(child.getChild(0).getText());
                condition.setOperator(child.getChild(1).getText());
                if(child.getChild(2).getText() == "("){
                    break;
                }
                else{
                    condition.setObject(child.getChild(2).getText());
                }


            }
        }

        sqlComponent = new SqlComponent(keyword, columns, tables, condition);
    }

    @Override
    public void enterQuerySpecification(MySqlParser.QuerySpecificationContext ctx) {
        if(flag == 0) {
            extractComponent(ctx);
            flag = 1;
        }
    }

    public SqlComponent returnComponent(){
        return sqlComponent;
    }

}
