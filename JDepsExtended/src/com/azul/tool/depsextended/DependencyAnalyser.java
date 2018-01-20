package com.azul.tool.depsextended;

import static com.azul.tool.depsextended.DependencyAnalyser.allModules;
import static com.azul.tool.depsextended.DependencyAnalyser.appModules;
import static com.azul.tool.depsextended.DependencyAnalyser.debug;
import static com.azul.tool.depsextended.DependencyAnalyser.extractModuleName;
import static com.azul.tool.depsextended.DependencyAnalyser.jdepsPath;
import static com.azul.tool.depsextended.DependencyAnalyser.makeOutputFile;
import static com.azul.tool.depsextended.DependencyAnalyser.print;
import static com.azul.tool.depsextended.DependencyAnalyser.saveCharsets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;


/*
 *  Features to add:
 *  - Determine OS (properties)
 *  
 *  - Way to switch offon tasks and on from command line
 *  
 *  - list Dependent module
 *  - list classes with unresolved
 *  - list of unresolved 
 *  
 *  - locales
 *  - encodings
 *  - swing themes
 *  
 *  - headless?
 *  
 *  - guess version from java.so/dll
 *  
 *  
 *  Nice to have
 *  - Set application root path
 *  - Check for all modules to exist before analysing - prepend application root path to allow for relative classpaths
 *  
 *  Option strings for testing
 *   -appclasspath D:/Java/util/cassandra/apache-cassandra-3.10/lib/* -jre D:/Java/zulu6_64/
 *  
 */

public class DependencyAnalyser {

	public static boolean debug = false;
	
	public static LocalDateTime date = LocalDateTime.now();
	public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	public static String runName = date.format(formatter);
	
	//Options
	public static String activeJob = "default";
	public static String sampleOption = null;
	public static String appClasspath;
	public static File jreDir = null;
	public static boolean recursive = false;
	public static String saveCharsets = null;
	public static String saveLocales = null;
	
	//Internal
	static List<Task> tasks = new ArrayList<Task>();
	static Set<String> jobs = new LinkedHashSet<String>();
	
	//Working data
	static List<String> appModulePaths;
	static List<String> jreModulePaths;
	static String javaHomePath;
	static String wrkDir;
	static String jdepsPath;
	
	
	//Results
	static HashSet<Module> appModules = new HashSet<Module>();
	static HashSet<Module> jreModules = new HashSet<Module>();
	static HashSet<Module> allModules = new HashSet<Module>();

	
	public static void debug(String s)
	{
		System.out.print("DEBUG: ");
		System.out.println(s);
	}
	
	public static void print(String s)
	{
		if (debug) debug(s);
		System.out.println(s);
	}
	
	public void tasks()
	{
		//tasks.add(new TaskSample());
		tasks.add(new TaskFindJREinJDK());
		tasks.add(new TaskParseAppClasspath());
		tasks.add(new TaskCheckJRE());
		tasks.add(new TaskJREVersion());
		tasks.add(new TaskListJREModules());
		tasks.add(new TaskFindJdeps());
		tasks.add(new TaskClassDependencies());
		tasks.add(new TaskTestCharsets());
		tasks.add(new TaskTestLocales());
	
		//Setup
		for (Task task: tasks) {
			String s = task.getClass().getSimpleName();
			s = s.substring(4);
			task.setName(s);
		}
	}
	
	public void execute(String job) 
	{
		//Preparation
		wrkDir = System.getProperty("user.dir");
		if (debug)  debug("Working directory: " + wrkDir); 
		
		//Executing
		for( Task task : tasks ) {
			if (hasJob(task,job) ) {
				if (task.canRun(this)) {
					try { 
						if (debug) debug("Running task: " + task.getName()); 
						task.run(this);
					} catch (Exception e)
					{
						StringWriter writer = new StringWriter();
						e.printStackTrace(new PrintWriter(writer));
						print("Exception executing task: " + task.getName());
						print(writer.toString());
					}
				} else {
					if (debug) debug("Preconditions not met for: " + task.getName()); 
				}
			} else {
				if (debug) debug("Skipping task (not matching job): " + task.getName()); 
			}
		}
	}
	
	public void getJobs()
	{
		for( Task task : tasks ) {
			for ( String job: task.jobs() ) {
				if ( !jobs.contains(job)) jobs.add(job);
			}
		}
	}
	
	public void describeJobs()
	{
		for( String job : jobs ) {
			describeJob(job);
		}
	}
	
	public void describeJob(String job)
	{
		String tlist = "(";
		for( Task task : tasks ) {tlist+=task.getName();tlist+=" ";};
		tlist+= ")";
		print( "Job: \'" + job + "' DependencyAnalyser -job " + job + " " + tlist); 
		print( "Options:");
		for( Task task : tasks ) {
			if (hasJob(task,job)) {
				String options = task.getOptions();
				if (options!=null) print( "    " + options );
			}
		}
		print( "Tasks:");
		for( Task task : tasks ) {
			if (hasJob(task,job)) {
				print( "    " + task.getName() + ": " +  task.getDescription());
				print("");
			}
		}
	}
	
	public boolean hasJob(Task task, String job)
	{
		for( String _job: task.jobs()) 
		{
			if (_job.equals(job)) return true;
		}
		return false;
	}

	
	public void analyse() throws Exception
	{
		/*
		 * Preparation
		 */
		if (jreDir==null && debug) debug("JRE not spefified. Performing application dependency analysis only."); 
		if (appClasspath==null && debug) debug("Application classpath not specified. Performing JRE analysis only."); 
		
		wrkDir = System.getProperty("user.dir");
		if (debug)  debug("Working directory: " + wrkDir); 
		
		/*
		 * Parse app classpath
		 */
		appModulePaths = parseClassPath(appClasspath);
		if (debug) for(String s: appModulePaths) debug("App module:" + s); 
		for(String s: appModulePaths) {
			File f = new File(s);
			if (!f.exists()) terminateWithError("Classpath element does not exist: " + appModulePaths);
		}
		
		/*
		 * Check for jre in JDK
		 */
		if (jreDir!=null)
		{
			//Check for JDK
			File checkJDK = new File(jreDir, "jre");
			if ( checkJDK.exists() ) {
				jreDir = checkJDK;
				if (debug)  debug("Found JDK switching to JRE: " + jreDir); 
			}
		}
		/*
		 *Common sense check that this is a JRE 
		 */
		if (jreDir!=null)
		{
			File checkrtjar = new File(jreDir, "lib/rt.jar");
			File checkmodules = new File(jreDir, "lib/modules");
			if ( !checkrtjar.exists() && !checkmodules.exists())
			{
				terminateWithError( jreDir + " does not seem to be a JRE. Neither " +  checkrtjar + " nor " + checkmodules + " exists!");
			}
			/*
			 * Traverse jre and find jars
			 */
			jreModulePaths = searchByExtension(jreDir, ".jar", true);
			if (debug) for(String s: jreModulePaths) debug("JRE module:" + s); 
		}	
		
		/*
		 * Try to guess Java version from java.dll
		 */
		if (jreDir!=null)
		{
			//TODO
		}
		
		/*
		 * Find Jdeps
		 */
		javaHomePath = System.getProperty("java.home");
		if (debug)  debug("Java Home: " + javaHomePath); 
		File javaHomeDir = new File(javaHomePath );
		
		File checkjdeps = new File(javaHomeDir, "bin/jdeps");
		if ( !checkjdeps.exists() )  {
		checkjdeps = new File(javaHomeDir, "bin/jdeps.exe");
		if ( !checkjdeps.exists() ) {
		checkjdeps = new File(javaHomeDir, "../bin/jdeps");
		if ( !checkjdeps.exists() ) {
		checkjdeps = new File(javaHomeDir, "../bin/jdeps.exe");
		if ( !checkjdeps.exists() ) {
			terminateWithError("Jdeps not found in currently used JRE/JDK " + javaHomePath );
		}}}}
		jdepsPath = checkjdeps.getAbsolutePath(); 
		if (debug)  debug("Using Jdeps in: " + jdepsPath); 
		
	
		/*
		//Resolve dependencies in app modules
		 * 
		 */
		//analyseModules();
		//resolveDependencies();
		
		/*
		//Look for encodings - extract list 
		 * 
		 */
		
		/*
		//Look for locales - extract list 
		 * 
		 */
		
		/*
		//look for headless lib
		 * 
		 */
		
		
	}
	
	public static void terminateWithError(String error)
	{
		print( error); 
		System.exit(1);
	}

	public static void printReport() throws Exception
	{
		//Scan for unresolved dependencies and report on them
		
		//Report on missing encodings
		
	}
	
	
	
	public static List<String> parseClassPath(String classpath)
	{
		String separator = System.getProperty("path.separator");
		List<String> modulepaths = new ArrayList<String>(16);
		//Dissecting the classpath
		StringTokenizer tokenizer = new StringTokenizer(classpath, separator);
		while (tokenizer.hasMoreTokens())
		{
			String path = tokenizer.nextToken();
			//Expanding wildcards
			if (path.endsWith("*")) {
				path = path.substring(0, path.length()-1);
				if (path.length() == 0) path = ".";
				modulepaths.addAll(searchByExtension(new File(path), ".jar", recursive ));
			} else {
				modulepaths.add(path);
			}
		}
		
		return modulepaths;
	}
	
	public static List<String> searchByExtension(File path, String ext, boolean recursive )
	{
		List<String> modulepaths = new ArrayList<String>(16);
		File[] files = path.listFiles();
		for (File f: files) {
			if (f.isDirectory()){
				modulepaths.addAll(searchByExtension(f, ext, recursive));
			} else {
				if (f.getName().endsWith(ext)) {
					modulepaths.add(f.getAbsolutePath());
				}
			}
		}
		return modulepaths;
	}
	/*
	public static void checkRtJar(Module module)
	{
		if (module.name.equals("rt.jar") || module.name.equals("modules"))
		{
			module.klasses.put("java.lang.Object", new Klass("java.lang.Object", module));
		}
	}
	*/
	
	public static String extractModuleName(String modulePath)
	{ 
		String moduleName;
		File f = new File(modulePath);
		moduleName = f.getName();
		return moduleName;
	}
	
	public static File makeOutputFile(String moduleName)
	{ 
		File outFile = new File(wrkDir);
		outFile = new File(outFile, runName );
		outFile.mkdirs();
		File finalOutFile = new File(outFile, moduleName+".txt");
		int count = 0;
		while ( finalOutFile.exists() )
		{
			count++;
			finalOutFile = new File(outFile, moduleName+ "_"+ count+".txt");
		}
		
		return finalOutFile;
	}

	public static void analyseModule( String module, HashSet<Module> set1, HashSet<Module> set2 ) throws Exception
	{
		print("Analysing:" + module); 
		String moduleName = extractModuleName(module);
		File outPutFile =  makeOutputFile(moduleName);
		JDeps.execute(module, outPutFile.getAbsolutePath(), jdepsPath);
		Module mod = JDeps.parse(outPutFile, moduleName, module );
		//checkRtJar(mod);
		set1.add(mod);
		set2.add(mod);
	}
	
	public static Module findModuleByName( HashSet<Module> set, String name )
	{
		for ( Module module: set) {
			if (module.name.equals(name)) return module;
		}
		return null;
	}
	
	public static String findModuleByName( List<String> list, String name )
	{
		for( String path: list) {
			if ( path.endsWith(name) ) return path;
		}
		return null;
	}
	
	public static Module resolveKlass(String klassName )
	{
		for(Module module: allModules) 
		{
			Klass klass = module.klasses.get(klassName);
			if (klass != null ) return klass.parentModule;
		}
		return null;
	}
	
	public static void saveList(List<String> list, String saveCharsets) throws Exception {
		Writer outp = new FileWriter(saveCharsets);
		for(String s: list) {
			outp.write(s);
			outp.write("\n");
		}
		outp.close();
	}
	
	public static List<String> loadList(String fileName) throws Exception {
		List<String> list = new ArrayList<String>();
		BufferedReader in = new BufferedReader( new FileReader( fileName) ); 
		String line;
		while ( (line = in.readLine()) != null ) {
			list.add(line);
		}
		return list;
	}
	
	
	
	public static void main(String[] arg) throws Exception
	{
		System.out.println("Java Dependency Analyser");
		
		DependencyAnalyser ctx = new DependencyAnalyser();
		ctx.tasks();
		
		int i=0;
		while ( arg.length > i)
		{
			if ( arg[i].equals("-debug") )
			{
				i++;
				debug = true;
				if (debug) debug("Debug mode - on");
			} else if ( arg[i].equals("-job") )
			{
				i++;
				activeJob = arg[i].toLowerCase();
				i++;
			} else if ( arg[i].equals("-addtasks") )
			{
				i++;
				StringTokenizer tok = new StringTokenizer(arg[i],",");
				while ( tok.hasMoreTokens() )
				{
					boolean found = false;
					String token = tok.nextToken();
					for (Task task: tasks) { 
						if (task.getName().equals(token) ) 
						{
							found= true;
							task.addJob(activeJob); };
						}
					if (!found) print("Task not found: " + token);
				}
				i++;
			} else if ( arg[i].equals("-recursive") )
			{
				i++;
				recursive = true;
			} else if ( arg[i].equals("-savecharsets") )
			{
				i++;
				saveCharsets = arg[i];
				i++;
			} else if ( arg[i].equals("-savelocales") )
			{
				i++;
				saveLocales = arg[i];
				i++;
			} else if ( arg[i].equals("-appclasspath") )
			{
				i++;
				appClasspath = arg[i];
				i++;
				if (debug) debug("Param -appclasspath: " + appClasspath);
			} else if ( arg[i].equals("-jre") )
			{
				i++;
				String s = arg[i];
				if (s!=null) jreDir = new File(s);
				i++;
				if (debug) debug("Param -jre: " + jreDir);
			} else if ( arg[i].equals("-unittest") )
			{
				i++;
				String unittest = arg[i];
				i++;
				if (debug) debug("Param -unittest: " + unittest);
				if (unittest.equals("jdepsparse") ) {
					JDeps.parse(new File("D:\\azul\\activities\\20180113_JDeps_extended\\rt.txt"), "rt.jar","D:\\azul\\activities\\20180113_JDeps_extended\\rt.txt" );
				}
				System.exit(0);
			} 
			else
			{
				if (debug) debug(arg[i] + " - unknown option!");
				System.out.println( arg[i] + " - unknown option!");
				if (debug) debug("Terminating with error");
				System.exit(1);
			}
		}
		if (arg.length == 0 ) 
		{
			System.out.println();
			if (debug) debug("No options specified!");
			ctx.getJobs();
			ctx.describeJobs();
			System.exit(1);
		}
		
		ctx.execute(activeJob);
		ctx.printReport();
		
		if (debug) debug("Terminating regularely");
	}
}




















