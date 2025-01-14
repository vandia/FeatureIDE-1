package de.ovgu.featureide.oscar.IO;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.apache.commons.lang.StringEscapeUtils;

import oscar.OscarProperties;
import oscar.Startup;

public class OscarPropertiesLoader {
	
	public static final String[] SEPS_LABELS={"key = value","key : value"};
	public static final String[] SEPS={"=",":"};
	
	
	public static final String COMMENT="^[#|!]+";
	
	
	public static OscarProperties loadOscarProperties(IFile properties, IProject project, IProject reportProj, String sep){
		Startup start = new Startup(properties, project);
		start.contextInitialized();
		
		final String SEP= getSep(sep);
		final String FILTER="^.+"+SEP+".+";
		
		String propFileName = properties.getLocation().toOSString()/*+File.separator+properties.getName()*/;
		final Pattern pattern = Pattern.compile(FILTER);
		System.out.println(propFileName);
		
		BufferedReader reader=null;
		StringBuilder contents = null;
		try {
			reader = new BufferedReader(new FileReader(propFileName));
			contents = new StringBuilder();
			while(reader.ready()) {
				String line = reader.readLine();
				Matcher m=pattern.matcher(line);
				if (m.matches()) {
					contents.append(line.replaceFirst(COMMENT, "")+"\n");
					
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		String tempFile = properties.getName().replace(properties.getFileExtension(),"tmp."+properties.getFileExtension());
		OscarProperties op = null;
		IFile tempIFile = reportProj.getFile(tempFile);

	    try {

			if (!tempIFile.exists()) {
			    byte[] bytes =  StringEscapeUtils.escapeXml(contents.toString()).getBytes();
			    InputStream source = new ByteArrayInputStream(bytes);
				tempIFile.create(source, IResource.NONE, null);
				
			}
	
			op = new OscarProperties(tempIFile);	
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if (tempIFile.exists()) {
				try {
					tempIFile.delete(true, null);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	    
	    return op;
	}


	private static String getSep(String sep) {
		// TODO Auto-generated method stub
		
		for (int i=0; i<  SEPS_LABELS.length; i++){
			if (SEPS_LABELS[i].equals(sep)) {
				return SEPS[i];
			}
				
		}
		
		return "";
	}

}
