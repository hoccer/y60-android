
/* OSC LISTENER */

import oscP5.*;
import netP5.*;

float input1;
float input2;
  
//OscP5 oscP5;
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
  
  /* start oscP5, listening for incoming messages at port 12000 
  oscP5 = new OscP5(this,12000);
  myRemoteLocation = new NetAddress("127.0.0.1",12000);
  oscP5.plug(this,"x","/x");
  oscP5.plug(this, "y", "/y"); */

  Thread receiver = new Thread() {
    
    private DatagramSocket mSocket = null;
        
    public void run() {

        try {
            mSocket = new DatagramSocket(1999);
            
            while (true) {
              System.out.println("waiting for packets");
              byte[] buffer = new byte[2];
  
              // receive request
              DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
              mSocket.receive(packet);
              System.out.println("got a packet!");
              
              x(buffer[0]);
              y(buffer[1]);
            }

        } catch (IOException e) {
            
            System.out.println(e);
        }
    }
  };
  
  receiver.start();
  
  try {
    DatagramPacket packet  = new DatagramPacket(new byte[]{10,10}, 2, InetAddress.getByName("localhost"), 1999);
    DatagramSocket socket = new DatagramSocket();
    socket.send(packet);
    socket.disconnect();
  } catch (Exception x) {
    throw new RuntimeException(x);
  }
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
  mx[num-1] += input1;
  input1 = 0;
  my[num-1] += input2;
  input2 = 0;
  
  for(int i=0; i<num; i++) {
    ellipse(mx[i], my[i], i/2, i/2);
  }
}







