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

    
}
boolean pause = false;
public void draw() {
    background(255);
    ball.display();

    for (Drop drop : rain){
        drop.move();
        drop.collision(ball);
        drop.display();
    }

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
    float segments = 200;
    ArrayList<PVector> pts = new ArrayList<PVector>();

    PVector loc;
    public Collision(float x, float y){
        loc = new PVector(x, y);
    }

    public boolean collide(PVector pt){
        float dist = PVector.sub(pt, loc).mag();
        if (dist < radius){
            return true;
        }
        return false;
    }

    public PVector collisionpt(Verlet ver){
        PVector direction = PVector.sub(ver.prev, ver.curr);
        direction.normalize().setMag(0.01f);

        PVector collpt = ver.curr.copy();
        float dist = PVector.sub(collpt, loc).mag();
        while (dist < radius){
            collpt = PVector.add(collpt, direction);
            dist = PVector.sub(collpt, loc).mag();
        }
        return collpt;
    }

    public PVector tangent(PVector pt) {
        PVector tan = PVector.sub(pt, loc);
        tan.rotate(PI/2);
        return tan;
    }

    public float angle(PVector pt){
        return PVector.angleBetween(pt, loc);
    }

    public void display(){
        ellipse(loc.x, loc.y, radius*2, radius*2);
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
    float bounce = 0.3f;
    boolean finished = false;
    Stack stack = new Stack(8);

    public Drop(float x, float y) {
        loc = new Verlet(x, y);
    }

    public void display(){

        stack.push(loc.curr.copy());

        strokeWeight(1);
        noFill();
        beginShape();
        for (PVector pt : stack.pts){
            curveVertex(pt.x, pt.y);
        }
        endShape();
    }

    public void move(){
        if (!finished){
            loc.move();
        } 
        
    }

    public void collision(Collision col){
        if (col.collide(loc.curr) && !finished){
            PVector collisionpt = col.collisionpt(loc);
            PVector tan = col.tangent(collisionpt);
            float angle = tan.heading();
            PVector currdiff = PVector.sub(collisionpt, loc.curr);
            PVector prevdiff = PVector.sub(loc.prev, collisionpt);
            if (loc.speed() > 1){
                currdiff.setMag(currdiff.mag()*bounce);
                prevdiff.setMag(prevdiff.mag()*bounce);
            } else {
                finished = true;
            }
            
            float currangle = PI - (angle*2);
            if (loc.curr.x > width/2){
                currdiff.rotate(angle);
                prevdiff.rotate(-currangle);
            } else {
                currdiff.rotate(-angle);
                prevdiff.rotate(-currangle);
            }
            
            loc.curr = PVector.add(currdiff, collisionpt);
            loc.prev = PVector.add(prevdiff, collisionpt);
        } else if (finished) {
            if (col.angle(loc.curr) > 30){
                loc.prev.y += 1;
            }
        }
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
        curr.x -= wind;
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
