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

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.m3g.*;

/**
 * For testing purposes of the converted model with the Juinness
 *
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class Demo extends MIDlet implements CommandListener
{
  private DemoCanvas canvas;
  private Command exitCmd;

  private Display display;
  
  public Demo()
  {
    display = Display.getDisplay(this);

    canvas = new DemoCanvas(this);
    exitCmd = new Command("Exit", Command.ITEM, 1);
    canvas.setCommandListener(this);
    canvas.addCommand(exitCmd);
    
    try{
      World world = null;
      String path = "/test.m3g";
      System.err.println("Creator: Loading path: " + path);	
      Object3D[] obj = Loader.load(path);
      
      for(int i=0; i<obj.length; i++){
	if(obj[i] instanceof World){
	  world = (World)obj[i];			    	
	  break;
	}
      }
      canvas.setScene(world);
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  public void destroyApp(boolean unconditional){
  }

  public void pauseApp(){
  }

  public void startApp(){
    display.setCurrent(canvas);
  }
  
  public void commandAction(Command cmd, Displayable disp)
  {
    if(cmd == exitCmd){
      try{
	destroyApp(false);
	notifyDestroyed();
      }
      catch(Exception e){
	e.printStackTrace();
      }
    }
  }

  private void log(String msg){
    System.err.println(msg);	    
  }
}
