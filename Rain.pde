

Collision ball;
ArrayList<Drop> rain = new ArrayList<Drop>();
void setup() {
    size(512, 512);
    background(255);
    ball = new Collision(width/2, width/2);

    rainLine();
}
void draw() {
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

void rainLine(){
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

    void createSegs(){
        for (int i = 0; i < segments; i++){
            PVector pt = new PVector(0, radius);
            pt.rotate(2*PI*float(i)/segments);
            pt = PVector.add(pt, loc);
            pts.add(pt);
        }
    }

    boolean collides(PVector pt){
        float dist = PVector.sub(pt, loc).mag();
        if (dist < radius){
            return true;
        }
        return false;
    }

    Verlet dropletpull(Verlet ver){
        PVector dir = PVector.sub(ver.curr, loc);
        if (dir.mag() < radius + 3){
            dir.normalize().setMag(0.001);
            ver.prev = PVector.add(ver.prev, dir);
            return ver;
        } 
        return ver;
        
    }

    PVector collisionpoint(Verlet ver){
        PVector normaldir = PVector.sub(ver.prev, ver.curr);
        normaldir.normalize();
        normaldir.setMag(0.01);
        PVector pt = ver.curr.copy();
        float dist = PVector.sub(pt, loc).mag();
        while (dist < radius){
            pt = PVector.add(pt, normaldir);
            dist = PVector.sub(pt, loc).mag();
        }
        return pt;
    }

    PVector collisionresponse(Verlet ver) { //returns new curr and prev difference
        PVector speed = PVector.sub(ver.curr, ver.prev);
        PVector flipover = PVector.sub(ver.curr, loc);
        flipover.normalize();
        float dot = speed.dot(flipover) * 2;
        flipover.mult(dot);
        PVector reflection = PVector.sub(speed, flipover);
        return reflection;
    }

    void display(){
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
    void push(PVector pt){
        pts.add(0, pt);
        if (pts.size() > s){
            pts.remove(pts.size()-1);
        }
    }
}

class Drop {
    Verlet loc;
    float bounce = 0.1;
    boolean finished = false;
    Stack stack;
    boolean bounced = false;
    int stacksize;
    public Drop(float x, float y) {
        loc = new Verlet(x, y);
        stacksize = int(random(5, 15));
        stack = new Stack(stacksize);
        bounce = random(0.1, 0.2);
    }

    void display(){
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

    void collision(){
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

    void move(){
        
        loc.move();
    }
}


class Verlet {
    PVector curr;
    PVector prev;
    float gravity = 0.02;
    float wind = 0.01;
    public Verlet(float x, float y){
        curr = new PVector(x, y);
        prev = curr.copy();
    }

    void move(){
        PVector diff = PVector.sub(curr, prev);
        prev = curr.copy();
        curr = PVector.add(curr, diff);
        curr.y += gravity;
        // curr.x -= wind;
    }

    float speed(){
        return PVector.sub(prev, curr).mag();
    }
}
