package com.example.antlr;

// (Youtube) Antlr Beginner Tutorial 2: Integrating Antrlr in Java Project

import com.example.antlrapi.dto.SqlComponent;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import com.example.antlr.gen.MySqlLexer;
import com.example.antlr.gen.MySqlParser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.antlr.ExampleVisitor.extractSelectComponent;

public class ParseProcessor {

    public static ParseTreeWalker walker = new ParseTreeWalker();
    // public static MySqlParser.RootContext tree;

    // public static SqlComponent sqlComponent = new SqlComponent();
    public static int[] count = new int[2];



//    static ArrayList<String> subquery = new ArrayList<>();
//    static ArrayList<String> subString = new ArrayList<>();

    static MySqlParser.RootContext tree = null;
    static ArrayList<Integer> idx = new ArrayList<>();
    static ArrayList<Integer> idx_ = new ArrayList<>();

    public static String getCommand(String sqlQuery){
        String[] words = sqlQuery.split(" ");
        String command = words[0];
        return command;
    }

    public static ArrayList<Integer> findStart(String sql){
        String regrex = "\\(\\s*SELECT";
        Pattern pattern = Pattern.compile(regrex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        while(matcher.find()){
            int index = matcher.start();
            idx.add(index);
            System.out.println(index);
        }

        System.out.println("Check idx " + idx.size());
        return idx;
    }
    static ArrayList<String> pullSubquery(ArrayList<Integer> idx, String sql){
        ArrayList<String> subquery = new ArrayList<>();
//
//        subquery = new ArrayList<>();
        Boolean isMine = true;
        Integer myIdx = 0;
        String query = "";

        for(int i=0;i<idx.size();i++){  // 첫 번째 sql에 대해서
            for(int j=idx.get(i) + 1;j<sql.length();j++){
                if (sql.charAt(j) == '('){
                    isMine = false;  // avg()
                }
                if(sql.charAt(j) == ')'){

                    if (isMine == false) {
                        isMine = true;
                    }
                    else{
                        myIdx = j;
                        break;
                    }
                }
            }
            if(myIdx != 0){
                query = sql.substring(idx.get(i), myIdx+1);
                String stringWithoutBracket = query.substring(1, query.length() - 1);
                String stringWithoutBlank = stringWithoutBracket.trim() + ";";
                subquery.add(stringWithoutBlank);
            }
        }

        System.out.println("Check !!" + subquery.size());
        return subquery;
    }
    public static ArrayList<String> findSubquery(String sql) {

        idx_ = findStart(sql);
        ArrayList<String> subString = pullSubquery(idx_, sql);
        return subString;
    }
//    public static ArrayList<String> SelectToComponents(String sql){
//        ArrayList<String> subqueryfound = findSubquery(sql);
//
//        int subquerySize = subqueryfound.size();
//        for(int i = 0; i < subquerySize; i++){
//
//            // step2함수는 SqlComponents 요소들 채워주는 용도
//            System.out.println("subquery Check ! : " + subqueryfound.get(i));
//            SqlComponent sqlcmpt = step2(subqueryfound.get(i));
//
//            sqlcmpt.setStep(i+1);
//            sqlcmpt.setSql(subqueryfound.get(i));
//            components.add(i, sqlcmpt);
//        }
//        return subqueryfound;
//    }
    public static int[] step1(String sqlQuery){
        // 파싱 준비
        CharStream charStream = CharStreams.fromString(sqlQuery);
        MySqlLexer mySqlLexer = new MySqlLexer(charStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(mySqlLexer);
        MySqlParser mySqlParser = new MySqlParser(commonTokenStream);

        // 트리 생성
        tree = mySqlParser.root(); // mysqlParser.시작룰(enterRule함수)

        // 쿼리 갯수 세어주는 리스너 생성
        CountQueryListener listener = new CountQueryListener();

        // 순회
        walker.walk(listener, tree);

        count = listener.getStartRuleCount();

        return count;

        // 단계별 요구 사항을 실행할 때마다 매번 파스 트리를 생성하는 과정을 거쳐야 하는가 > 그런가보다
    }

    public static SqlComponent step2(String sqlQuery) {
        System.out.println("step2 : "+ sqlQuery);
        // 파싱 준비
        CharStream charStream1 = CharStreams.fromString(sqlQuery);
        MySqlLexer mySqlLexer1 = new MySqlLexer(charStream1);
        CommonTokenStream commonTokenStream1 = new CommonTokenStream(mySqlLexer1);
        MySqlParser mySqlParser1 = new MySqlParser(commonTokenStream1);

        tree = mySqlParser1.root(); // ** !!

        ExtractComponentListener listener2 = new ExtractComponentListener();
        walker.walk(listener2, tree);  // tree는 1단계에서 사용한 tree를 사용해도 됨(새로 만들면 오류 남 ;; 왜 그런건지는 모르겠음 ;;), listener는 새로 만들기

        SqlComponent sqlComponent = listener2.returnComponent();

        return sqlComponent;
    }

        public static void step3 (String sqlQuery){

            // 파싱 준비 과정
            CharStream charStream = CharStreams.fromString(sqlQuery);
            MySqlLexer mySqlLexer = new MySqlLexer(charStream);
            CommonTokenStream commonTokenStream = new CommonTokenStream(mySqlLexer);
            MySqlParser mySqlParser = new MySqlParser(commonTokenStream);

            MySqlParser.QuerySpecificationContext parseTree = mySqlParser.querySpecification();  // mysqlParser.시작룰
            ExampleVisitor visitor = new ExampleVisitor();
            visitor.extractSelectComponent(parseTree, sqlQuery);

        }

    }