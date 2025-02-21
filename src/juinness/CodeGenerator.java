//Created 2005-03-04
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

package juinness;

import java.lang.reflect.*;

import juinness.util.*;

import javax.media.j3d.*;
import java.util.List;
import java.util.*;
import java.awt.image.BufferedImage;
import com.sun.j3d.utils.geometry.*;


import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.FactoryConfigurationError;  
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;  
import org.xml.sax.SAXParseException;
import java.io.File;
import java.io.IOException;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;

import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

/**
 * Generates part of the Translator and Traverser related class files
 * based on java-templates and xml file
 *
 * @author Markus Ylikerälä and Maija Savolainen
 */
public class CodeGenerator extends DefaultHandler
{  
  private PrintWriter out;
  private PrintWriter out2;
  private String elmName;
  private String attr;
  private Class c;
  private List m = new Vector();
  private List supported =  new Vector();
  private Map hash = new HashMap();
  private Map para; 
  private List translator = new Vector();
  private StringBuffer traverserTmpl;
  private StringBuffer translatorTmpl;
  private StringBuffer traverserGenerated = new StringBuffer();

  
  public CodeGenerator(String location){
    try{
      BufferedReader in = 
	new BufferedReader(new FileReader("../models/TraverserTmpl.java"));
      traverserTmpl = read(in);

      in = 
	new BufferedReader(new FileReader("../models/TranslatorTmpl.java"));
      translatorTmpl = read(in);


      out = new PrintWriter(new FileWriter("../src/juinness/GeneratedTraverser.java"));


      out2 = new PrintWriter(new FileWriter("../src/juinness/GeneratedTranslator.java"));

      
      para = new HashMap();
      para.put("javax.media.j3d.Transform3D", "t3d");

      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse( new File(location), this);
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  private StringBuffer read(BufferedReader in)
    throws Exception
  {
    String str;
    StringBuffer tmpl = new StringBuffer();
    while((str = in.readLine()) != null){
      tmpl.append(str + "\n");
    }
    return tmpl;
  
  }

  public void startDocument()
    throws SAXException
  {
  }
  
  public void startElement(String namespaceURI,
			   String sName, // simple name (localName)
			   String qName, // qualified name
			   Attributes attrs)
    throws SAXException
  {
    try{
      String eName = sName; // element name
      if("".equals(eName)){
	eName = qName; // namespaceAware = false
      }

      elmName = eName;
      
      if("class".equals(elmName)){
	//Note that the method must be public otherwise
	//c.getMethod() does not find it and we should
	//instead traverse all the methods with all the parameters
	write("\n\n  public void parse(");	    
      }

      if(attrs != null) {
	for (int i = 0; i < attrs.getLength(); i++) {
	  String aName = attrs.getLocalName(i); // Attr name
	  if("".equals(aName)){
	    aName = attrs.getQName(i);
	  }
	  if("id".equals(aName)){
	    attr = attrs.getValue(i);
	    write(attr + " n){");	    
	    c = Class.forName(attr); 
	    supported.add(attr);
	  }
	  else if("target".equals(aName)){
	    translator.add(attrs.getValue(i));
	    translator.add(attr);
	    attr = attrs.getValue(i);
	  }	
	}
      }
    }
    catch(Exception e){
      throw new SAXException(e.toString()); 
    }    
  }
  
  public void endElement(String namespaceURI,
			 String sName, // simple name
			 String qName  // qualified name
			 )
    throws SAXException
  {
    Iterator itr = m.iterator();
    while(itr.hasNext()){
      Method met = (Method)itr.next();
      String params = null;
	
      Class[] parameterTypes = met.getParameterTypes();
      for(int k=0; k<parameterTypes.length; k++){
	params = parameterTypes[k].getName().trim();	
      }
	
      Class ret = met.getReturnType();
      if("java.util.Enumeration".equals(ret.getName())){
	write("\n\n    Enumeration enu = n." + met.getName() + "();" +
	      "\n    while(enu.hasMoreElements()){" +
	      "\n      invokeMethod(enu.nextElement());" +
	      "\n    }");
      }
      else if(params != null){
	Object obj = para.get(params);
	write("\n\n    n." + met.getName() + "("+ obj + ");");
	write("\n    parse(" + obj + ");");
      }
      else{
	write("\n\n    invokeMethod(n." + met.getName() + "());");
      }
    }
    m.clear();	  

    if("class".equals(qName)){
      write("\n    translator." + attr + "(n);");	    
      write("\n  }");
    }
  }
    
  public void endDocument()
    throws SAXException
  {
    if(supported.isEmpty() == false){
      write("\n\n  String[] supported = {");
      Iterator itr = supported.iterator();
      while(itr.hasNext()){
	write("\"" + itr.next() + "\", ");
      }
      write("\n  };");	    
    }
    
    String phrase = "//<%GENERATE%>";
    String str = "";
    int i;

    if(translator.isEmpty() == false){
      i = translatorTmpl.indexOf(phrase);
      if(i != -1){
	i += phrase.length();
	Iterator itr = translator.iterator();
	while(itr.hasNext()){      
	  Object method = itr.next();
	  Object param = itr.next();
	  str += "\n\n  void " +  method + "(" + param + " n){";
	  str += "\n   log.logMsg(\"J3D->M3G " + param + "\");";
	  str += "\n  }";
	}    
	translatorTmpl.insert(i, str);
	write2(translatorTmpl.toString());
      }
    }

    out2.flush();
    out2.close();
    
    i = traverserTmpl.indexOf(phrase);
    if(i != -1){
      i += phrase.length();
      traverserTmpl.insert(i, traverserGenerated.toString());
      out.write(traverserTmpl.toString());
    }

    out.flush();
    out.close();
  }

  public void characters(char buf[], int offset, int len)
    throws SAXException
  {
    try{
      String s = new String(buf, offset, len);
      if (!s.trim().equals("")){
	if("method".equals(elmName)){
	  Method[] met = c.getDeclaredMethods();
	  for(int i=0; i<met.length; i++){
	    if(met[i].getName().equals(s)){
	      m.add(met[i]);
	    }
	  }
	}
	else{
	  write(s);
	}
      }
    }
    catch(Exception e){
      throw new SAXException(e.toString()); 
    }
  }
  
  private void write(String str){
    System.err.print(str);
    traverserGenerated.append(str);
  }

  private void write2(String str){
    System.err.print(str);
    out2.write(str);
  }

  public static void main(String [] args) 
  {
    CodeGenerator gen = new CodeGenerator("../models/traverser.xml");
  }
}
