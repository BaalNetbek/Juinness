//Created 2004-12-03
//
//Copyright (C) 2004  Markus Yliker�l� and Maija Savolainen
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//http://www.gnu.org/copyleft/gpl.html

package juinness.m3g;

import javax.microedition.m3g.Image2D;
import javax.microedition.m3g.Texture2D;
import javax.microedition.m3g.FakeTexture2D;
import javax.microedition.m3g.Transformable;
import javax.microedition.m3g.Object3D;

/**
 * SubTexture2D is a decodator for the Texture2D
 *
 * There is a %"�&%#"�& bug in the JSR-184 that forbids to 
 * extends this from the Texture2D object
 *
 * Exception in thread "main" java.lang.UnsatisfiedLinkError: log2
 *        at com.nokia.phone.ri.m3g.Math.log2(Native Method)
 *        at javax.microedition.m3g.Texture2D.<init>(Unknown Source)
 *        at juinness.m3g.SubTexture2D.<init>(SubTexture2D.java:15)
 *
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class SubTexture2D extends FakeTexture2D implements Sub
//public class SubTexture2D extends Texture2D implements Sub
{  
  private Image2D image;
  private int blend = 0; //0xA1B2C3D4;

  public SubTexture2D(Image2D image){    
    super(image);
    this.image = image;
  }

  public SubTexture2D(){    
  }

  public void setImage(Image2D image){
    this.image = image;    
  }

  public Image2D getImage(){
    return image;
  }

  public int getWrappingS(){
    return Texture2D.WRAP_REPEAT;
  }

  public int getWrappingT(){
    return Texture2D.WRAP_REPEAT;
  }

  public int getBlending(){
    return Texture2D.FUNC_REPLACE;
  }

  public int getBlendColor(){
    return blend;
  }

  public int getLevelFilter(){
    return Texture2D.FILTER_BASE_LEVEL;
  }

  public int getImageFilter(){
    return Texture2D.FILTER_NEAREST;
  }

  public int getObjectType(){
    return 17;
  }
}
