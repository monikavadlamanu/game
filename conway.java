package com.google.gson.reflect;

import javafx.application.Application;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.*;


public class conway extends Application {

   private static final int width = 500;
   private static final int height = 500;
   private static final int cellsize = 10;
  // private static Gson gson = new Gson();



   private static class Life{
       private final int reihe;
       private final int zeile;
       private Boolean[][] rules;
       private int[][] grid;
       private Random random=new Random();
       private final GraphicsContext graphics;

       public Life(int reihe, int zeile, GraphicsContext graphics){
           this.reihe =reihe;
           this.zeile =zeile;
           this.graphics=graphics;
           grid=new int[reihe][zeile];
           this.rules = new Boolean[][]{{false, false, false, true, false, false, false, false, false, false},
                   {false, false, true, true, false, false, false, false, false, false}};
       }

       public void init(){
           for(int i = 0; i< reihe; i++){
               for(int j = 0; j< zeile; j++){
                   grid[i][j]=random.nextInt(2);
               }
           }
           draw();
       }

       private void draw(){
           graphics.setFill(Color.YELLOW);
           graphics.fillRect(0,0,width,height);

           for(int i=0; i<grid.length;i++){
               for(int j=0; j<grid[i].length; j++){
                   if(grid[i][j]==1){
                       graphics.setFill(Color.gray(0.5,0.5));
                       graphics.fillRect(i*cellsize, j*cellsize,cellsize,cellsize);
                       graphics.setFill(Color.RED);
                       graphics.fillRect((i*cellsize)+1, (j*cellsize)+1, cellsize-2, cellsize-2);

                   }
                   else{
                       graphics.setFill(Color.gray(0.5,0.5));
                       graphics.fillRect(i*cellsize,j*cellsize,cellsize, cellsize);
                       graphics.setFill(Color.BLACK);
                       graphics.fillRect((i*cellsize)+1,(j*cellsize)+1,cellsize-2,cellsize-2);

                   }
               }
           }
       }

       private int countNeighbors(int i, int j){
           int sum=0;
           int iStart=i==0?0:-1;
           int iEnd=i==grid.length - 1 ? 0:1;
           int jStart=j==0?0:-1;
           int jEnd=j==grid[0].length - 1 ? 0:1;

           for (int k=iStart; k<=iEnd;k++){
               for(int l=jStart;l<=jEnd;l++){
                   sum+=grid[i+k][l+j];
               }
           }
           sum-=grid[i][j];

           return sum;
       }

       public void tick(){
           int[][] next=new int[reihe][zeile];
           for(int i = 0; i< reihe; i++){
               for(int j = 0; j< zeile; j++){
                   int nachbar= countNeighbors(i,j);

                   if(rules[grid[i][j]][nachbar] == true){
                       next[i][j] = 1;
                   }
               }
           }
           grid=next;
           draw();
       }

       public void safe() throws IOException {
           JsonArray to_safe = new JsonArray();
           Path pfad = Paths.get("C:\\Users\\Frodo\\IdeaProjects\\gameoflife\\only_safe_file_for_now.json");
           if(Files.exists(pfad) == false) {
               Files.createFile(pfad);
           }
           for(int i = 0; i<grid.length; i++){
               JsonArray helper = new JsonArray();
               for (int j = 0; j<grid[0].length; j++){
                   helper.add(grid[i][j]);
               }
               to_safe.add(helper);
           }
           Files.writeString(pfad, gson.toJson(to_safe));
       }

       public void load() throws IOException{
           int saved_grid[][];
           Path pfad = Paths.get("C:\\Users\\Frodo\\IdeaProjects\\gameoflife\\only_safe_file_for_now.json");
           if(Files.exists(pfad) == false) {
               return;
           }
           else {
               String array_string = Files.readString(pfad);
               saved_grid = gson.fromJson(array_string, new TypeToken<int[][]>(){}.getType());
               if (saved_grid.length == 0) {
                   return;
               }
           }
           grid = saved_grid;
           draw();
       }


   }



   public static void main(String[] args) {

       launch();
   }


   public void start(Stage primaryStage) {
       VBox root = new VBox(10);
       Scene scene = new Scene(root, width, height + 100);
       final Canvas canvas = new Canvas(width, height);

       final boolean[] leftclick = new boolean[1];

       leftclick[0] =false;
       scene.setOnMouseClicked(e->{
           if(e.getButton().equals(MouseButton.PRIMARY)){
               if(leftclick[0]){
                   leftclick[0] =false;

               }
               else leftclick[0] =true;
           }
       });





       Button reset = new Button("Reset");
       Button step = new Button("Step");
       Button run = new Button("Run");
       Button stop = new Button("Stop");
       Button safe_state = new Button("Safe");
       Button load_button = new Button("Load");
       Button terminate = new Button("Terminate");



       root.getChildren().addAll(canvas, new HBox(10, reset, step, run, stop, safe_state, load_button, terminate));
       primaryStage.setScene(scene);
       primaryStage.show();

       int rows = (int) Math.floor(height / cellsize);
       int cols = (int) Math.floor(width / cellsize);

       GraphicsContext graphics = canvas.getGraphicsContext2D();
       Life life = new Life(rows, cols, graphics);
       life.init();

       AnimationTimer Animation = new AnimationTimer() {
           private long lastUpdate=0;
           @Override
           public void handle(long now) {

               if ((now - lastUpdate) >= TimeUnit.MILLISECONDS.toNanos(100)) {
                   life.tick();
                   lastUpdate = now;
               }
           }
       };

       reset.setOnAction(l -> life.init());
       run.setOnAction(l -> Animation.start());
       step.setOnAction(l -> life.tick());
       stop.setOnAction(l -> Animation.stop());
       safe_state.setOnAction(l-> {
           try {
               life.safe();
           } catch (IOException e) {
               e.printStackTrace();
           }
       });
       load_button.setOnAction(l -> {
           try {
               life.load();
           } catch (IOException e) {
               e.printStackTrace();
           }
       });
       terminate.setOnAction(l -> {
           Stage stage = (Stage) terminate.getScene().getWindow();
           stage.close();
       });
   }


}