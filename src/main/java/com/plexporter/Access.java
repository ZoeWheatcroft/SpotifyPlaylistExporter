package com.plexporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.Scanner;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;


//class to access spotify and get json data
public class Access {
    
    public static String urlCallback = "http://[::1]:8888/callback";

    public Access(){

    }

    public String getApiKey(){
        String key = "";
        
        File txt = new File("SpotifyApiKey.txt");
        Scanner sc;
        try {
            sc = new Scanner(txt);
            key = sc.next();
            sc.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return key;
    }

    public String [] getClientInfo(){
        String info [] = {"", ""};
        
        File txt = new File("clientInfo.txt");
        Scanner sc;
        try {
            sc = new Scanner(txt);
            info[0] = sc.next();
            info[1] = sc.next();
            sc.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return info;
    }

    //connect to spotify w/ api key 
    public void connect() throws Exception
    {
        String key = getApiKey();
        System.out.println(key);

        // TODO: set up authorization and junk
        // HttpClient client = HttpClient.newHttpClient();
        // HttpRequest req = HttpRequest.newBuilder()
        //                     .uri(URI.create("https://example.com"))
        //                     .GET().build();
        
        // System.out.println("Status: " + client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode());
        
        CallbackServer server = new CallbackServer();
        server.begin();
        
        getAccessCode();

        //server.end();
        //swag!
    }


    public String getAccessCode() throws IOException, InterruptedException {
        String code = "poop balls"; // the access code that we're trying to get

        String clientId = getClientInfo()[0];
        String redirectUri = urlCallback;

        String state = generateState();

        String authUrl =
                "https://accounts.spotify.com/authorize?" +
                "response_type=code" +
                "&client_id=" + clientId +
                "&scope=" + "playlist-read-private" +
                "&redirect_uri=" + redirectUri +
                "&state=" + state;
            

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(authUrl))
                            .GET().build();


        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        System.out.println("Access code request returned: " + res.statusCode());

        

        //JSONObject resJson = new JSONObject(res.toString());

        //String code = resJson.get("access_code").toString();

        //System.out.println("Code is: " + code);

        return code;
    }

    public void getAccessToken(String[] args) throws Exception {

        String info [] = getClientInfo();
        String clientId = info[0];
        String clientSecret = info[1];

        // access code
        String code = "AQD123456789";
        // set where to callback
        String redirectUri = urlCallback;

        String basicAuth =
                Base64.getEncoder()
                        .encodeToString(
                                (clientId + ":" + clientSecret)
                                        .getBytes(StandardCharsets.UTF_8)
                        );

        String body =
                "grant_type=authorization_code" +
                "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(
                        redirectUri,
                        StandardCharsets.UTF_8
                );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                "https://accounts.spotify.com/api/token"
                        ))
                        .header("Authorization", "Basic " + basicAuth)
                        .header(
                                "Content-Type",
                                "application/x-www-form-urlencoded"
                        )
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        // ObjectMapper mapper = new ObjectMapper();

        // JsonNode json = mapper.readTree(response.body());

        // Json

        JSONObject json = new JSONObject(response.body());

        String accessToken = json.get("access_token").toString();

        System.out.println("Access Token:");
        System.out.println(accessToken);
    }



    // helper
    private static String generateState() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
