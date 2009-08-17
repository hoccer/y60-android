import controlP5.*;
import oscP5.*;
import netP5.*;
  
OscP5 oscP5;
NetAddress myRemoteLocation;

ControlP5 controlP5;

int myColorBackground = color(0,0,0);

int sliderValue = 100;
int slider = 100;

void setup() {
  size(400,400);
  
   oscP5 = new OscP5(this,14000);
   myRemoteLocation = new NetAddress("127.0.0.1",12000);
   
  
  controlP5 = new ControlP5(this);
 Slider s = controlP5.addSlider("slider",0,100,128,100,160,10,100);
 s = controlP5.addSlider("sliderValue",0,100,128,200,160,10,100);
}

void draw() {
  background(0);
 // fill(sliderValue);
 // rect(0,0,width,100);
  
  
}
  
  


void slider(float leftSliderValue) {
//  myColorBackground = color(leftSliderValue);
  
  OscMessage myMessage = new OscMessage("/x");
  myMessage.add(leftSliderValue); 
  oscP5.send(myMessage, myRemoteLocation); 
 
  println("a slider event. current value:"+leftSliderValue);
 
 
}


void sliderValue(float rightSliderValue) {
 // myColorBackground = color(rightSliderValue);
  
  OscMessage myMessage = new OscMessage("/y");
  myMessage.add(rightSliderValue); 
  oscP5.send(myMessage, myRemoteLocation); 
 
  println("a slider event. current value:"+rightSliderValue);
 
 
}


 
