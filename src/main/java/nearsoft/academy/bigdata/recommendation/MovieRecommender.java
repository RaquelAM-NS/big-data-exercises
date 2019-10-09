package nearsoft.academy.bigdata.recommendation;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


public class MovieRecommender{
    private final String filename;
    private int reviews = 0;
    private long totalProd = 0;
    private long totalUsers = 0;
    HashMap<String, Long> productsTotal = new HashMap<>();
    HashMap<Long, String> productsTotalReverse = new HashMap<>();
    HashMap<String, Long> usersTotal = new HashMap<>();
    List<String> products = new ArrayList<>();
    List<String> users = new ArrayList<>();
    List<String> scores = new ArrayList<>();
    PrintWriter out = new PrintWriter(new FileWriter("movies.csv"));

    public MovieRecommender (String filename) throws IOException{
        this.filename = filename;
        getData();
    }

    public void getData(){
        try{
            String line;
            String idProduct="";
            String idUser="";
            String score;
            InputStream fileStream = new FileInputStream(filename);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
            BufferedReader buffered = new BufferedReader(decoder);

            while ((line = buffered.readLine()) != null){
                if(line.indexOf("product/productId:") != -1){
                    reviews++;
                    idProduct = line.replace("product/productId: ","");
                    if(!productsTotal.containsKey(idProduct)){
                        productsTotal.put(idProduct, totalProd);
                        totalProd++;
                    }
                }
                if(line.indexOf("review/userId:") != -1){
                    idUser = line.replace("review/userId: ","");
                    if(!usersTotal.containsKey(idUser)){
                        usersTotal.put(idUser, totalUsers);
                        totalUsers++;
                    }
                }
                if(line.indexOf("review/score") != -1){
                    score = line.replace("review/score: ","");
                    scores.add(score);
                    out.println(usersTotal.get(idUser) + "," +productsTotal.get(idProduct)+ "," + score);
                }
                
            }
            for (String idProd : productsTotal.keySet()) {
                productsTotalReverse.put(productsTotal.get(idProd),idProd);
            } 
        }
        catch(IOException e){
            System.out.println("Error loading file!");
        }
        finally{
            out.close();
        }

    }

    public int getTotalReviews(){
        return reviews;
    }

    public int getTotalProducts(){
        return productsTotal.size();
    }

    public int getTotalUsers(){
        return usersTotal.size();
    }

    public List<String> getRecommendationsForUser(String user) throws IOException, TasteException {
        DataModel model = new FileDataModel(new File("movies.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);


        List<String> RecommendedProducts = new ArrayList<String>();
        List<RecommendedItem> recommendations = recommender.recommend(usersTotal.get(user),3);
        for (RecommendedItem recommendation : recommendations) {
            RecommendedProducts.add(productsTotalReverse.get((long)recommendation.getItemID()));
        }
        return RecommendedProducts; 
    } 
}