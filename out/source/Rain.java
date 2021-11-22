import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Rain extends PApplet {



Collision ball;
ArrayList<Drop> rain = new ArrayList<Drop>();
public void setup() {
    
    background(255);
    ball = new Collision(width/2, width/2);

    rainLine();
}
public void draw() {
    background(255);
    // fill(255, 50);
    // rect(0, 0, width, height);
    for (Drop drop : rain){
        drop.move();
        drop.collision();
        drop.display();
    }
    ball.display();
    if (frameCount % 20 == 0){
        rainLine();
    }
}

public void rainLine(){
    for (int i = 1; i < 50; i++){
        Drop drop = new Drop(width*i/50, 5);
        rain.add(drop);
    }
}

class Collision {
    float radius = 100;
    float segments = 300;
    ArrayList<PVector> pts = new ArrayList<PVector>();

    PVector loc;
    public Collision(float x, float y){
        loc = new PVector(x, y);
        createSegs();
    }

    public void createSegs(){
        for (int i = 0; i < segments; i++){
            PVector pt = new PVector(0, radius);
            pt.rotate(2*PI*PApplet.parseFloat(i)/segments);
            pt = PVector.add(pt, loc);
            pts.add(pt);
        }
    }

    public boolean collides(PVector pt){
        float dist = PVector.sub(pt, loc).mag();
        if (dist < radius){
            return true;
        }
        return false;
    }

    public Verlet dropletpull(Verlet ver){
        PVector dir = PVector.sub(ver.curr, loc);
        if (dir.mag() < radius + 3){
            dir.normalize().setMag(0.001f);
            ver.prev = PVector.add(ver.prev, dir);
            return ver;
        } 
        return ver;
        
    }

    public PVector collisionpoint(Verlet ver){
        PVector normaldir = PVector.sub(ver.prev, ver.curr);
        normaldir.normalize();
        normaldir.setMag(0.01f);
        PVector pt = ver.curr.copy();
        float dist = PVector.sub(pt, loc).mag();
        while (dist < radius){
            pt = PVector.add(pt, normaldir);
            dist = PVector.sub(pt, loc).mag();
        }
        return pt;
    }

    public PVector collisionresponse(Verlet ver) { //returns new curr and prev difference
        PVector speed = PVector.sub(ver.curr, ver.prev);
        PVector flipover = PVector.sub(ver.curr, loc);
        flipover.normalize();
        float dot = speed.dot(flipover) * 2;
        flipover.mult(dot);
        PVector reflection = PVector.sub(speed, flipover);
        return reflection;
    }

    public void display(){
        // ellipse(loc.x, loc.y, radius*2, radius*2);
        for (PVector pt : pts){
            point(pt.x, pt.y);
        }
    }
}

class Stack {
    ArrayList<PVector> pts = new ArrayList<PVector>();
    int s;
    public Stack(int s){
        this.s = s;
    }
    public void push(PVector pt){
        pts.add(0, pt);
        if (pts.size() > s){
            pts.remove(pts.size()-1);
        }
    }
}

class Drop {
    Verlet loc;
    float bounce = 0.1f;
    boolean finished = false;
    Stack stack;
    boolean bounced = false;
    int stacksize;
    public Drop(float x, float y) {
        loc = new Verlet(x, y);
        stacksize = PApplet.parseInt(random(5, 15));
        stack = new Stack(stacksize);
        bounce = random(0.1f, 0.2f);
    }

    public void display(){
        fill(0);
        ellipse(loc.curr.x, loc.curr.y, 3, 3);
        // noFill();
        // strokeWeight(2);
        // beginShape();
        // for (PVector pt : stack.pts){
        //     curveVertex(pt.x, pt.y);
        // }
        // endShape();
    }

    public void collision(){
        if (ball.collides(loc.curr)){
            PVector reflection = ball.collisionresponse(loc);
            reflection.setMag(reflection.mag()*bounce);
            PVector collpt = ball.collisionpoint(loc);
            loc.curr = collpt;
            loc.prev = PVector.sub(collpt, reflection);
            bounced = true;
        } 
        loc = ball.dropletpull(loc);
        stack.push(loc.curr);
    }

    public void move(){
        
        loc.move();
    }
}


class Verlet {
    PVector curr;
    PVector prev;
    float gravity = 0.02f;
    float wind = 0.01f;
    public Verlet(float x, float y){
        curr = new PVector(x, y);
        prev = curr.copy();
    }

    public void move(){
        PVector diff = PVector.sub(curr, prev);
        prev = curr.copy();
        curr = PVector.add(curr, diff);
        curr.y += gravity;
        // curr.x -= wind;
    }

    public float speed(){
        return PVector.sub(prev, curr).mag();
    }
}
  public void settings() {  size(512, 512); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Rain" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
