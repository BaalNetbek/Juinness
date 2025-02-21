//Created 2004-10-24
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

package juinness.util;

import juinness.Exporter;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.zip.*;
import javax.microedition.m3g.*;
import java.util.*;

/**
 * Imports a M3G-file and interprets its content
 * this is used for development purposes only
 *
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class Checker
{
  private byte[] buf;
  private Inflater decompresser;
  private Util util;
  private long totFile;
  private Adler32 adler32;
  private Exporter exp;
  
  public Checker(){
    util = Util.getInstance();
    exp = new Exporter();
    decompresser = new Inflater();
  }

  private byte[] getFile(String path)
    throws Exception
  {
    File f = new File(path);
    RandomAccessFile in = new RandomAccessFile(f, "r");
    FileChannel ch = in.getChannel();
    long size = ch.size();
    util.setLog("tst_" + f.getName()+ ".txt");
    util.log("\nREAD " + size + " bytes from the: " + f.getName());
   
    buf = new byte[(int)size];
    in.read(buf, 0, buf.length);
    in.close();

    return buf;
  }

  public void importModel(String path)
    throws Exception
  {
    try{
      buf = getFile(path);
      
      MutableInteger offset = new MutableInteger(0);

      if(isFileIdentifier(buf, offset) == false){
	util.log("Invalid M3G file format");
	return;
      }

      util.log("\n*** M 3 G - FI L E  - B E G I N S ***");
      util.log(new String(buf, 0, offset.value));

      //Lex the rest of the file
      getSection(offset);
      util.log("*** M 3 G - FI L E  - E N D S ***");
    }
    finally{
      util.closeLog();
    }
  }

  /**
   * Checks the FileIdentifier as described in Section 5
   */
  private boolean isFileIdentifier(byte[] buf, MutableInteger offset)
  {
    byte[] data = util.getFileIdentifier();
    for(int i=0; i<data.length; i++){
      if(data[i] != buf[i]){
	return false;
      }
    }
    offset.value += data.length;
    return true;
  }

  public void getSection(MutableInteger offset){
    
    adler32 = new Adler32();
    MutableInteger k = new MutableInteger(1);
    while(true){
      util.log("\n*** S E C T I O N  - B E G I N S ***");
      util.log(" --------------");
      int begin = offset.value;

      boolean compression = util.bytesToBoolean(buf, offset);
      int tot = util.bytesToInt(buf, offset);
      int uncomp = util.bytesToInt(buf, offset);
      util.log("compressionScheme: " + compression);
      util.log("totalSectionLength: " + tot + "   offset: " + begin);
      util.log("uncompressedLength: " + uncomp);

      if(compression){
	//because the data is comressed it must first be decompressed
	decompresser.setInput(buf, offset.value, tot-13);
	byte[] obj = new byte[uncomp];
	try{
	  int resultLength = decompresser.inflate(obj);
	  util.log("# Decompresser ResultLength: " + resultLength);
	}
	catch(Exception e){
	  e.printStackTrace();
	}
	decompresser.reset();
	//interpret the data from the allocated array
	offset.value = 0;
	lex(obj, offset, offset.value + uncomp, k);
	offset.value = uncomp;
      }
      else{
	//interpret the data directly from the buf
	lex(buf, offset, offset.value + uncomp, k);
	offset.value = uncomp;
      }
      offset.value = begin + tot - 4;
      util.log("checksum: " + util.bytesToInt(buf, offset));
      util.log("Section ended at offset: " + offset);
      util.log("*** S E C T I O N  - E N D S ***");

      //Check if the end-of-the-file (EOF) is reached
      if(offset.value >= totFile){
	break;
      }
    }
  }

  /**
   * Lex all the ObjectStructures of the section
   * starting from the data index denoted with offset
   */
  private void lex(byte[] data, MutableInteger offset, int uncomp, 
		   MutableInteger k)
  {    
    while(uncomp - 5 > 0){
      int type = util.bytesToByte(data, offset);
      int length = util.bytesToInt(data, offset);
      util.log(" --------------");
      util.log(" objectType: " + type + 
	       "=(" + util.getObjectType(type) + ")  objectID: " + k);
      k.value++;
      util.log(" length: " + length + "   offset: " + offset);
      int begin = offset.value;
      long size = exp.show(type, data, offset, length);      
      if(type == 0){
	totFile = size;
      }

      if(offset.value - begin != length){
	util.log("\n\n\n\n\n\n*** **** *" +
		 "FATAL ERROR - COULD NOT CONSUME ALL OF THE BYTES");
	offset.value = begin + length;
      }
      util.log("#Consumed Bytes: " + offset.value + "/" + uncomp);      
      util.log(" *****************");      
      if(offset.value >= uncomp){
	break;
      } 
    }
  }

  public static void main(String[] args)
    throws Exception
  {  
    if(args.length < 1){
      System.err.println("Checker needs the path the the WTK apps directory");
      return;
    }

    Checker checker = new Checker();
    for(int i=1; i<args.length; i++){
      try{
	checker.importModel(args[0] + args[i]);    
      }
      catch(Exception e){
	e.printStackTrace();
      }
    }
  }
}
