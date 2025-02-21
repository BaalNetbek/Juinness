//Created 2004-10-21
//
//Copyright (C) 2004  Markus Ylikerälä and Maija Savolainen
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

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.m3g.*;

/**
 * For testing purposes of the converted model with the Juinness
 *
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class DemoCanvas extends Canvas
{
  private Demo midlet;
  private World world;
  private Graphics3D g3d;
  private int x;
  private int y;
  private int width;
  private int height;
  private Camera cam;
  private Group camGroup;
  private Transform trans;

  public DemoCanvas(Demo midlet){
    this.midlet = midlet;
  }

  public void setScene(World world)
  {    
    this.world = world;

    x = 0;
    y = 0;
    width = getWidth();
    height = getHeight();
    
    camGroup = new Group();
    
    Camera cam = new Camera();
    float aspectRatio = ((float)width) / ((float)height);
    cam.setPerspective(70.0f, aspectRatio, 0.1f, 50.0f);
    cam.postRotate(-10.0f, 1.0f, 0, 0);  

    trans = new Transform();
    trans.postTranslate(0.0f, 3.0f, 10.0f);
    camGroup.setTransform(trans);
    camGroup.addChild(cam);
    world.addChild(camGroup);
    world.setActiveCamera(cam);

    g3d = Graphics3D.getInstance();
  }

  protected void keyPressed(int keyCode)
  { 
    float offset = 0.1f;
    camGroup.getTransform(trans);
    int gameAction = getGameAction(keyCode);
    switch(gameAction){
    case Canvas.UP:    
      trans.postTranslate(0, 0, offset);
      break;
    case Canvas.DOWN:  
      trans.postTranslate(0, 0, -offset);
      break;
    case Canvas.LEFT:  
      trans.postTranslate(offset, 0, 0);
      break;
    case Canvas.RIGHT: 
      trans.postTranslate(-offset, 0, 0);
      break;
    default: 
      break;
    }
    camGroup.setTransform(trans);
    repaint();
  }
  
  protected void keyReleased(int keyCode)
  { 
  }

  public void paint(Graphics g){
    if(g.getClipWidth() != width || g.getClipHeight() != height ||
       g.getClipX() != x || g.getClipY() != y){
      g.setColor(0x00);
      g.fillRect(0, 0, getWidth(), getHeight());
    }
    
    if((g3d != null) && (world != null)){
      try{
	g3d.bindTarget(g);
	g3d.setViewport(x, y, width, height);
	g3d.render(world);
      }
      finally{
	g3d.releaseTarget();
      }
    }
  }  
}

