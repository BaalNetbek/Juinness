//Created 2004-11-19
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
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package juinness.m3g;

import javax.microedition.m3g.Image2D;

/**
 * SubImage2D is a decodator for the Image2D
 *
 * @author Markus Yliker�l� and Maija Savolainen
 */
public class SubImage2D extends Image2D implements Sub
{
  private byte[] pixels;
  private byte[] palette;

  public SubImage2D(int format, int width, int height, 
		    byte[] image, byte[] palette){
    super(format, width, height, image, palette);
    this.pixels = image;
    this.palette = palette;
  }    

  public int getObjectType(){
    return 10;
  }

  public byte[] getPixels(){
    return pixels;
  }

  public byte[] getPalette(){
    return palette;
  }
}
