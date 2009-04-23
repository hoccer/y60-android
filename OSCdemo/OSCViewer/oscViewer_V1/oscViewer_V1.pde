//import processing.opengl.*;

PVector SelectionCenter;
PVector SelectionCenter2;
PVector storeImage;

TheImage images[];
PImage imageIcon;
PImage cp;


ArrayList order;


float input1;
float input2;
int k = 0;
float cpos = 0;
color c = color(255, 102, 0);
color c2 = color(204, 204, 204);
float rMin = 1;
float rMax = 3;
float r = rMin;
int imageIndexFromOrange = 0;



boolean grow = true;
boolean storeEvent = false;






void setup( )
{
  size(1800, 800);
  frameRate(30);
  imageMode(CENTER);
  rectMode(CENTER);
  smooth();
  r = rMin;


  images = new TheImage[0];
  order = new ArrayList();
  imageIcon = loadImage("imageIcon.png");

  // load images
  String lines[] = loadStrings("images.txt");

  for( int i = 0; i < lines.length; i++)
  {
    images = (TheImage[])append( images, new TheImage( lines[i]));
    order.add( i );
  }




//// RECEIVER //////////////


Thread receiver = new Thread() {
    
        
    public void run() {

        try {
            
            while (true) {
              System.out.println("waiting for packets");
              byte[] buffer = new byte[2];
  
              // receive request
              DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
              DatagramSocket socket = new DatagramSocket(1999);
              socket.receive(packet);
              System.out.println("got a packet!");
              socket.close();
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



///


void draw( )
{

  background(0);
  for( int i = 0; i < order.size(); i++)
  {
    int offset = ((Integer) order.get( i )).intValue();
    images[offset].draw( );
    
  }
    
// Draw Icon image

  image(imageIcon, 280, 160, scaleWidth-20, scaleHeight+70);




    // draw frame NR.1 ///
    
    SelectionCenter = new PVector(280, 300);
  
    pushMatrix( );

    translate(SelectionCenter.x, SelectionCenter.y); 
    translate(cpos, 0);
    stroke(#FF9900);
    strokeWeight(1);
    strokeJoin(MITER);
    noFill();
    //rect(0, 0, scaleWidth, scaleHeight);


 rect(0, Yposition, scaleWidth+r-3, scaleHeight+r-3);
 noFill();
 for (int i = 1; i < 20; i++)
 {
   stroke(red(c), green(c), blue(c), 255/i);
   strokeWeight(1);
   rect(0, Yposition, scaleWidth+r+i-3, scaleHeight+r+i-3);
 }
 if ( grow ) 
 {
   r += 2/frameRate;
   if ( r > rMax ) grow = false;
 }
 else
 {
   r -= 2/frameRate;
   if ( r < rMin ) grow = true;
 } 

    popMatrix( );
  



  
  
  // draw frame NR. 2 -- controlled by TrackPad ///
    
    SelectionCenter2 = new PVector(280, 300);
    
    pushMatrix( );
  
  offset = offset + input1;
    input1 = 0;
   
    float dx = offset - x;
    if(abs(dx) > 0.1) {
    x += dx * easing;
    }
  
    translate(SelectionCenter2.x, SelectionCenter2.y); 
    translate(offset, 0);
   // translate(x*7, 0);
   // translate(0, 0);
    stroke(#CCCCCC);
    strokeWeight(1);
    strokeJoin(MITER);
    noFill();
    //rect(0, 0, scaleWidth, scaleHeight);


   rect(0, Yposition, scaleWidth+r-3, scaleHeight+r-3);
   noFill();
   for (int i = 1; i < 20; i++)
 {
   stroke(red(c2), green(c2), blue(c2), 255/i);
   strokeWeight(1);
   rect(0, Yposition, scaleWidth+r+i-3, scaleHeight+r+i-3);
 }
 if ( grow ) 
 {
   r += 2/frameRate;
   if ( r > rMax ) grow = false;
 }
 else
 {
   r -= 2/frameRate;
   if ( r < rMin ) grow = true;
 } 


      popMatrix( );
      
      
      
      
      
      
      
/// Store Event


if (storeEvent) { 
  
  
    storeImage = new PVector(280, 300);
    pushMatrix();
   
    translate(storeImage.x, storeImage.y); 
    translate(0, 0);
   
    
    images[imageIndexFromOrange].draw(); 

    
    popMatrix();
  
  

  } 



////////
      
  
  
  
}






void keyPressed()
{
  if ( keyCode == RIGHT )
  {
    println("RIGHT");
    cpos += scaleWidth+30;
    imageIndexFromOrange ++;
  }
  
  
  if ( keyCode == LEFT )
  {
    println("LEFT");
    cpos -= scaleWidth+30;
        imageIndexFromOrange --;
  }
 
 
 if (key == 'c' || key == 'C') {
    storeEvent = true;
    println("storeEvent");

 }
  
  }






