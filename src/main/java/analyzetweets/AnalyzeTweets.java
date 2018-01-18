package analyzetweets;

import twitter4j.*;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Program to perform sentiment analysis on twitter tweets by fetching tweets using twitter public apis and latter
 * doing sentiment analysis over each of them using Stanford CoreNLP library.
 *
 * References used:
 * Twitter: https://developer.twitter.com/en/docs/basics/getting-started
 * Stanford CoreNLP: https://stanfordnlp.github.io/CoreNLP/
 */

public class AnalyzeTweets
{
    public static final String consumerKey = "[***YOUR CONSUMER KEY***]";
    public static final String consumerSecret = "[***CONSUMER SECRET***]";
    public static Twitter twitter;
    public static StanfordCoreNLP pipeline;

    public static OAuth2Token getOAuth2Token() throws TwitterException
    {
        ConfigurationBuilder configurationBuilder= new ConfigurationBuilder();
        configurationBuilder.setApplicationOnlyAuthEnabled(true);
        configurationBuilder.setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret);
        Configuration configuration = configurationBuilder.build();
        OAuth2Token token = new TwitterFactory(configuration).getInstance().getOAuth2Token();
        return token;
    }

    public static Twitter getTwitterObject() throws TwitterException
    {
        OAuth2Token token = getOAuth2Token();
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setApplicationOnlyAuthEnabled(true);
        configurationBuilder.setOAuthConsumerKey(consumerKey);
        configurationBuilder.setOAuthConsumerSecret(consumerSecret);
        configurationBuilder.setOAuth2TokenType(token.getTokenType());
        configurationBuilder.setOAuth2AccessToken(token.getAccessToken());
        Configuration configuration = configurationBuilder.build();
        Twitter twitter = new TwitterFactory(configuration).getInstance();
        return twitter;

    }

    public static List<String> getTweets(String query) throws TwitterException{
        List<String> list = new LinkedList<String>();
        Query q = new Query(query);
        QueryResult queryResult = twitter.search(q);
        List<Status> statusList = queryResult.getTweets();
        for(Status s : statusList)
            list.add(s.getText());
        return list;
    }

    public static int analyzeSentiment(String tweet) {

        int finalSentimentScore = 0;
        if (tweet != null && tweet.length() > 0){
            int longest = 0;
            Annotation annotation = pipeline.process(tweet);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)){
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                int sentimentScore = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (partText.length() > longest) {
                    finalSentimentScore = sentimentScore;
                    longest = partText.length();
                }
            }
        }
        return finalSentimentScore;
    }

    public static void main( String[] args ) throws Exception
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Query: ");
        String query = scanner.next();
        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
        pipeline = new StanfordCoreNLP(properties);
        twitter = getTwitterObject();
        List<String> tweetList = getTweets(query);
        for(String tweet : tweetList){
            System.out.println(tweet + ":" + analyzeSentiment(tweet));
        }
    }
}
