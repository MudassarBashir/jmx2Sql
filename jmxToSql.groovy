/**
 * jmxToSql.groovy
 * Created by Mudassar "Moe" Bashir on 11/3/2015.
 *
 * A groovy script which takes a *.jmx file (an xml file which is a JMeter test plan) and parses it to create
 * an SQL file. Assuming groovy is installed on the host system which intends to run this script, the user can
 * simply type 'groovy jmxToSql' to run it. The script assumes that the input *.jmx file is located in the same
 * directory as it. It produces the output *.sql file also in the same directory.
 *
 **/

println "jmxToSql: started."

//def outDirectory = ".\\"
def outDirectory = "C:\\Users\\hsmith\\JMXTESTS\\"

def path = outDirectory+"NGP_Test_Rest.jmx";

def jMeterTestPlan = new XmlSlurper().parse(new File(path))
def i = 0 // Just a counter to keep track of the number of test plans processed by this script

def outType = "rb"  // 'rb' or 'sql'

def outFile = new File(outDirectory+'importer-output.'+outType) // The file this script is going to create.

// Delete the file if it already exists so it's written anew rather than having new content
// appended to it every time this script is run.
outFile.delete()

// Main part of the script which processes the input XML via a .each groovy loop
jMeterTestPlan.'**'.findAll{ node-> node.name() == 'HTTPSamplerProxy' }
    .each {
        //outFile << "--> Begin test " + i + " -->\n"

        def  name = "dvd-" + i
        def info = it.@testname.toString().replaceAll(~/HTTP : DATAVIEW : /, "").replaceAll("'","\\\\'")
        def comments = it.'*'.find{commentsNode -> commentsNode.name() == 'stringProp' &&
                        commentsNode.@name == 'TestPlan.comments'}.text().replaceAll("'","\\\\'")
        def url = it.'*'.find{urlNode -> urlNode.name() == 'stringProp' &&
                        urlNode.@name == 'HTTPSampler.path'}.text()
        def method = it.'*'.find{methodNode -> methodNode.name() == 'stringProp' &&
                    methodNode.@name == 'HTTPSampler.method'}.text()
        def dvdJson = it.elementProp.collectionProp.elementProp.stringProp.find {dvdNode ->
                        dvdNode.@name == 'Argument.value'
                    }.text().replaceAll("'","\\\\'") 
        def dvd_name = it.parent().BeanShellPreProcessor.'*'.find{dvdNameNode -> dvdNameNode.name() == 'stringProp' &&
               dvdNameNode.@name == 'script'}.text()
               
        def params

        dvd_name = dvd_name =~ /\"DVD_NAME", \"(.*)\"/
        try {
            dvd_name = dvd_name[1][1]
            params = "{ \"DVD_NAME\": \"${dvd_name}\" }"
        }
        catch (Exception e) {
            dvd_name = name
        }

        switch (outType) {
        
        case "sql":
             outFile << "insert into dvd (name, info, parameters, body, autogen) values ('" + name + "', '" +
                    info + "', '" + params + "', '" + dvdJson + "', false);\n"
             //outFile << "insert into dvdTest (name, method) values ('" + name + "', '" + method + "');\n"
             break;
        case "rb":
             outFile << "Dvd.create(name:'${name}',info:'${info}',type:'sql',autoGen:'false',parameters:'${params}',body:'${dvdJson}')\n"
             break;
        }
               
        //outFile << "--< End test " + i + " --<\n\n"

        i++
   }

//.each{
//
//    println it.name()
//
//    def blockInfo
//
//    it.'**'.findAll{ node -> node.name() == 'BeanShellPreProcessor' }
//            .each{
//
//        println(">> "+ it)
//        blockInfo = it.@testname.toString()
//
//        ntext = it.text().toString()
//
//        println("ntext: '"+ntext+"'")
//
//        Matcher m = ntext =~ /\"DVD_NAME", \"(.*)\"/
//
//        println(":::"+m)
//        // println("::**"+m[0])
//        println(":::"+m.matches())
//
//    }
println "jmxToSql: finished."
