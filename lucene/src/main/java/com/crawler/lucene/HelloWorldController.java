package com.crawler.lucene;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;  
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.IOException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.json.JSONArray;
import org.json.JSONObject;


@RestController
@CrossOrigin("*")  
public class HelloWorldController 
{
    private static ArrayList<Tweet> tweets = new ArrayList<Tweet>();
    private static IndexWriter writer;

    public static String search(Path dir, String q) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
        String res = "";
        IndexReader rdr = DirectoryReader.open(FSDirectory.open(dir));
        IndexSearcher is = new IndexSearcher(rdr);
        QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
        Query query = parser.parse(q); 
        TopDocs hits = is.search(query, 1000);
        System.out.println("Searching");
        Integer top = 0;
        List<ScoreDoc> documents = Arrays.asList(hits.scoreDocs);

        //Custom Ranking Equation
        // tf.idf * 0.7 + like_count * 0.3 + follower_count * 0.2
        for(ScoreDoc scoreDoc : documents){
            Document doc = is.doc(scoreDoc.doc);
            Integer likeCount = Integer.parseInt(doc.get("like_count"));
            Integer follower_count = Integer.parseInt(doc.get("follower_count"));
            Float newScore = (float)(scoreDoc.score* 0.6 + likeCount * 0.2 + follower_count*0.2);
            scoreDoc.score = newScore;
        }
        
        Collections.sort(documents, new Comparator<ScoreDoc>() {
            public int compare(ScoreDoc o1, ScoreDoc o2)
            {
                return Float.compare(o1.score,o2.score);
            }
        });
        //Take top 10 tweets with new scores
        for(ScoreDoc scoreDoc : documents) {
            if(top == 10){
                System.out.println(res);
                System.out.println("Done");
                return res;
            }
            top++;
            Document doc = is.doc(scoreDoc.doc);
            res += doc.get("id") + ":::";
        }
        System.out.println(res);
        System.out.println("Done");
        return res;
    }

    private static void indexTweet(Tweet t) throws Exception {
        Document doc = getDocument(t);
        writer.addDocument(doc);
    }

    public static IndexWriter getIndexWriter(Path dir) throws IOException {
        Directory indexDir = FSDirectory.open(dir);
        IndexWriterConfig luceneConfig = new IndexWriterConfig(new StandardAnalyzer());
        return(new IndexWriter(indexDir, luceneConfig));
    }

    protected static Document getDocument(Tweet t) throws Exception {
        Document doc = new Document();
        doc.add(new TextField("contents", t.getText(), Field.Store.YES));
        doc.add(new TextField("like_count", t.getLikeCount(), Field.Store.YES));
        doc.add(new TextField("follower_count", t.getFollowerCount(), Field.Store.YES));
        doc.add(new StringField("id", t.getId(), Field.Store.YES));
        return doc;
    }

    public static void parseTweets(){

        String filePath = "/Users/edwardzhu/Desktop/CS172-Project/lucene/src/main/java/com/crawler/sample_data.json";
        try {
            String fileString = Files.readString(Paths.get(filePath));
            JSONArray test = new JSONArray(fileString);
            for(int i = 0; i < test.length(); i++){
                JSONObject obj = test.getJSONObject(i);
                String id = (String)((JSONObject) obj.get("data")).get("id");
                String created_at = (String)((JSONObject)obj.get("data")).get("created_at");
                int like_count = (Integer)((JSONObject)((JSONObject) obj.get("data")).get("public_metrics")).get("like_count");
                int follower_count = 0;
                JSONArray follower_count_array = (JSONArray)((((JSONObject)(obj.get("includes"))).get("users")));
                if(follower_count_array.length() > 0){
                    follower_count = (Integer)(((JSONObject) (((JSONObject) follower_count_array.get(0)).get("public_metrics"))).get("followers_count"));
                }
                String text = (String)((JSONObject) obj.get("data")).get("text");
                Tweet tweet = new Tweet(created_at,id, like_count, text, follower_count);
                tweets.add(tweet);
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    @RequestMapping("/")  
    public String hello()   
    {  
        return "Hello User";  
    }  

    @RequestMapping("/getTweets")
    public static String getTweetsByQuery(@RequestParam(name="name", required = false, defaultValue = "fruit") String query) throws Exception
    {
        String res = "hello";
        try {
            System.out.println("Query: " + query);
            writer = getIndexWriter(Paths.get("/Users/edwardzhu/Desktop/lucene/src/main/java/com/crawler/lucene/Index2"));
            parseTweets();
            for(int i = 0; i < tweets.size(); i++){
                indexTweet(tweets.get(i));
            }
            res = search(Paths.get("/Users/edwardzhu/Desktop/lucene/src/main/java/com/crawler/lucene/Index"), query);
            writer.close();
            }
        catch(Exception e){
            e.printStackTrace();
        }
        return res;
    }
} 
