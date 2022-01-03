package spoon.kotlin

import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import spoon.kotlin.compiler.SpoonKtEnvironment
import spoon.kotlin.compiler.ir.Empty
import spoon.kotlin.compiler.ir.IrGenerator
import spoon.kotlin.compiler.ir.IrTreeBuilder
import spoon.kotlin.compiler.ir.PsiImportBuilder
import spoon.kotlin.reflect.visitor.printing.DefaultKotlinPrettyPrinter
import spoon.kotlin.reflect.visitor.printing.DefaultPrinterAdapter
import spoon.reflect.CtModel
import spoon.reflect.declaration.CtCompilationUnit
import spoon.reflect.factory.FactoryImpl
import spoon.support.DefaultCoreFactory
import spoon.support.StandardEnvironment
import java.io.File
import java.util.concurrent.TimeUnit

/*
 * Example runner of KtSpoon.
 * This will build an instance of the Spoon metamodel of all .kt files in a directory and subdirectories, then print
 * them back into source code.
 *
 * Requires dependencies (including Kotlin stdlib) to be accessible as JARs at libLocation. 
 * They can be downloaded using the target project's build tool.
 * Such as "mvn dependency:copy-dependencies" for maven or adding a task in gradle:
   tasks.register<Copy>("downloadJars") {
     from(configurations.kotlinCompilerClasspath, configurations.kotlinCompilerPluginClasspath)
     into("lib")
   }
 */
class ExampleMain(val rootDir: String,
                  libLocation: String,
                  mainFolder: String? = "src/main",
                  mainSubfolder: String = "kotlin") {
    private val classPath: String

    init {
        val libDir = File(libLocation)
        val libs = libDir.listFiles()
        classPath = libs?.joinToString(separator = ";") { it.absolutePath } ?: ""
        println(classPath)
    }

    val dir = if(mainFolder == null) rootDir else "$rootDir/$mainFolder"
    val args = K2JVMCompilerArguments().also { // Show compiler where libs are
        if(classPath.isNotEmpty()) {
            it.classpath = classPath
        }
    }

    val setup: SpoonKtEnvironment = SpoonKtEnvironment(
           listOf(File("$dir/$mainSubfolder")),
            "ktspoon testing",
            args)

    fun run() {
        val factory = FactoryImpl(DefaultCoreFactory(), StandardEnvironment())
        val (irModule, context) = IrGenerator(setup).generateIrWithContext() 
        val builder = IrTreeBuilder(factory, context)
        val cus = ArrayList<CtCompilationUnit>()

        for(f in irModule.files) {
            cus.add(builder.visitFile(f, Empty(f)).resultSafe)
        }

        // Get imports from PSI, currently redundant as all types are printed with their
        // fully qualified name
        val importBuilder = PsiImportBuilder(factory)  
        importBuilder.build(irModule.files.map { context.sourceManager.getKtFile(it)!! })
        builder.factory.model.setBuildModelIsFinished<CtModel>(true);
        val prettyprinter = DefaultKotlinPrettyPrinter(DefaultPrinterAdapter())

        // Print each file in 'spooned' dir, with similar file structure as original 
        for(cu in cus) { 
            val file = cu.file.absolutePath.replace('\\', '/')
            val newFile = File("$rootDir/spooned/${file.substring(rootDir.length)}")
            newFile.parentFile.mkdirs()
            newFile.createNewFile()
            newFile.writeText(prettyprinter.prettyprint(cu))
        }
    }
}

fun main() {
   ExampleMain("<path-to-project-root>", "<path to dir of JARs of dependencies>").run()
}