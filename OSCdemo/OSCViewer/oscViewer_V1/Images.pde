// Image CLASS //////////////


int lastX = 0;
int Yposition = 120;
float offset = 0;
float scaleWidth;
float scaleHeight;
float imgSize;

float easing = 0.01;
float x;



public class TheImage {
  PVector center; // the center of the image
  PVector scale;  // the scale to draw the image
  PImage img;     // The actual image.
  
  
  
  // constructor
  TheImage( String filename  )
  {  
    img = loadImage( filename );
    scale = new PVector(1,1);
    setGallery( );
  }


  // place horizontally on screen
  void setGallery( )
  { 
     
   lastX += 280; 
   center = new PVector( lastX, 300);
   

   // resize image
 
      imgSize = 250;
      float scaleX = imgSize / img.width;
      scale.x = scaleX;
      scale.y = scaleX;  
    }


  /////// draw an individual image.
  
  void draw( )
  {
    

    pushMatrix( );
   
    offset = offset + input1;
    input1 = 0;
   
    float dx = offset - x;
    if(abs(dx) > 0.1) {
    x += dx * easing;
    }
   
     
    translate(center.x, center.y); 
   // translate(x*7, 0); 
  //  translate(topPos, 0);
    
   // float zoom = map(input2+20*6, 0, width, 0.1, 4.5);
   // scale(zoom);
  
      
    scaleWidth = img.width * scale.x;
    scaleHeight = img.height * scale.y;
    stroke(#CCCCCC);
    strokeWeight(4);
    strokeJoin(MITER);
    noFill();
  //  rect(0, 0, scaleWidth+4, scaleHeight );
    image( img, 0, Yposition, scaleWidth, scaleHeight);
  //  tint(0, 153, 204, 126);
    popMatrix( ); 
    
    
  }   
 
 
}


