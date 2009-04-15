
/* OSC LISTENER */

import oscP5.*;
import netP5.*;

float input1;
float input2;
  
OscP5 oscP5;
NetAddress myRemoteLocation;


int num = 60;
float mx[] = new float[num];
float my[] = new float[num];

void setup() 
{
  size(200, 200);
  smooth();
  noStroke(); 
  fill(#ff00ae); 
  
  /* start oscP5, listening for incoming messages at port 12000 */
  oscP5 = new OscP5(this,12000);
  myRemoteLocation = new NetAddress("127.0.0.1",12000);
  oscP5.plug(this,"x","/x");
  oscP5.plug(this, "y", "/y");

  
}


// horizontal
void x(float Xvalue) {
  println("received an osc message:"+Xvalue);
  input1 = Xvalue;

  
}


// vertical
void y(float Yvalue) {
    println("received an osc message:"+Yvalue);
  input2 = Yvalue;
  
}



void draw() 

{
  background(0); 
  
  
  // Reads throught the entire array
  // and shifts the values to the left
  for(int i=1; i<num; i++) {
    mx[i-1] = mx[i];
    my[i-1] = my[i];
  } 
  
  
  // Add the new values to the end of the array
  mx[num-1] = input1;
  my[num-1] = input2;
  
  for(int i=0; i<num; i++) {
    ellipse(mx[i], my[i], i/2, i/2);
  }
}







