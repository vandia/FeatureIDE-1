package de.ovgu.featureide.oscar.propertyusage.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import de.ovgu.featureide.oscar.IO.Console;
import de.ovgu.featureide.oscar.model.Feature;
import de.ovgu.featureide.oscar.model.FeatureType;
import de.ovgu.featureide.oscar.model.Node;
import oscar.OscarProperties;

public class HierarchyReconstruction {
	
	
	
	private static final Set<String> activeMarkers = new HashSet<String>(
			Arrays.asList(new String[] { "true", "yes", "no", "false", "on", "off" }));
	private static final String regx_split_list="[\\,\\|]";
	private static final Pattern regx_sep_list=Pattern.compile("\\w+"+regx_split_list+"\\w+");
	private static final String regx_split_key="[\\_\\-\\.]";
	//private static final String regx_split_key="[\\-\\.]";
	private static final Pattern regx_sep_key = Pattern.compile("\\w+"+regx_split_key+"\\w+");
	private static final String name_sep = "_";
	
	public static Feature getFDLHierarchy(OscarProperties op, Map<String, Integer[]> allPropMap, double threshold){
		
		//Create the Console to log the results
		final Console console = new Console();
		Feature base= new Feature("Base", true, FeatureType.ALL);
		base.setAbstract(true);
		
		for (String key:allPropMap.keySet()){
			try {
				int numerator=allPropMap.get(key)[1];
				int denominator=allPropMap.get(key)[0];
				double coef= (denominator > 0) ? ((1.0*numerator)/denominator) : 0.0;
				if (coef<threshold) continue;
				String value=op.getProperty(key);
				Feature current;
				if ((value == null) || (value.equals(""))){
					current=new Feature(key,false,FeatureType.ATOMIC);
				}else if (isBooleanProperty(value)){
					current=new Feature(key,Boolean.parseBoolean(value),FeatureType.ATOMIC);
				}else if (regx_sep_list.matcher(value).lookingAt()){ //the value is a list.
					current= new Feature(key, true, FeatureType.MORE_OF);
					current.setAbstract(true);
					for (String s: value.split(regx_split_list)){
						current.addHierarchy(new Feature(key+name_sep+s,true,FeatureType.ATOMIC));
					}					
				}else {
					current= new Feature(key, true, FeatureType.MORE_OF);
					current.setAbstract(true);
					current.addHierarchy(new Feature(key+name_sep+value,true,FeatureType.ATOMIC));
					//TO-DO: maybe mark this in red as to grab attention.
				}
				
				Feature father=base;
				if (regx_sep_key.matcher(key).lookingAt()){ //creating the fathers to current if it has fathers.
					String[] path=key.split(regx_split_key);
					Feature aux=null;
					for(int i=0; i < path.length - 1; i++){
						String name=key.substring(0,key.lastIndexOf(path[i])+path[i].length());
						aux=base.getFeature(name);
						if (aux==null){
							aux=new Feature(name,true,FeatureType.ALL);
							aux.setAbstract(true);
							father.addHierarchy(aux);
							father=aux;
						}else{
							aux.setType(FeatureType.ALL);
							father=aux;
						}
						
					}
				}
				father.addHierarchy(current);
				
			} catch (Exception e) {
				console.writeln("The property "+key+" could not be processed, error: "+ e.getMessage());
			}
			
		}
		base = cleanFDLHierarchy (base);
		return base;
		
	}
	
	private static Feature cleanFDLHierarchy (Feature root) {
		
		Stack<Node> stack = new Stack<Node>();
		Node init= new Node(null, root);
		stack.push(init);
		
		while(!stack.isEmpty()) {
			Node node = (Node)stack.pop();
			Feature father=node.getFather();
			Feature child=node.getChild();
			Feature succ;
			if ((father!=null) && (child!=null) && (child.isAbstract) && (child.getChildren().size()==1)){
				succ=child.getChildren().toArray(new Feature[1])[0];
				father.getChildren().remove(child);
				father.getChildren().add(succ);
				Node n=new Node(father, succ);
				stack.push(n);
				
			}
			for (Feature i:node.getSuccessors()){
				stack.push(new Node(node.getChild(),i));
			}
		}

		
		return root;
	}
	
	private static boolean isBooleanProperty(String val) {
		val = val==null ? null : val.trim();
		// if we're checking for positive value, any "active" one will do
		return (val != null && activeMarkers.contains(val.toLowerCase()));
	}
		
	

}
