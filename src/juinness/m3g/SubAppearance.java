//Created 2004-11-27
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

import javax.microedition.m3g.Appearance;

/**
 * SubAppearance is a decodator for the Appearance
 *
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class SubAppearance extends Appearance implements Sub
{  
  private SubTexture2D tex;

  public SubAppearance(){
  }

  public void setTexture(int index,
			 SubTexture2D texture){
    this.tex = texture; 
  }

  public SubTexture2D getSubTexture(int index){
    if(index == 0){
      return tex;
    }
    return null;
  }

  public int getObjectType(){
    return 3;
  }
}
