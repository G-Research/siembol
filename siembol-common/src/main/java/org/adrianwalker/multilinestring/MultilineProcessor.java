/**
 * Code from https://github.com/adrianwalker/multiline-string
 * Based on Adrian Walker's blog posts:
 *  -http://www.adrianwalker.org/2011/12/java-multiline-string.html
 *  -http://www.adrianwalker.org/2018/08/java-910-multiline-string.html
 */
package org.adrianwalker.multilinestring;

import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import java.util.Set;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes({"org.adrianwalker.multilinestring.Multiline"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public final class MultilineProcessor extends AbstractProcessor {

  private JavacElements elementUtils;
  private TreeMaker maker;

  @Override
  public void init(final ProcessingEnvironment procEnv) {

    super.init(procEnv);

    JavacProcessingEnvironment javacProcessingEnv = (JavacProcessingEnvironment) procEnv;
    this.elementUtils = javacProcessingEnv.getElementUtils();
    this.maker = TreeMaker.instance(javacProcessingEnv.getContext());
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

    Set<? extends Element> fields = roundEnv.getElementsAnnotatedWith(Multiline.class);
    for (Element field : fields) {
      String docComment = elementUtils.getDocComment(field);
      if (null != docComment) {
        JCTree.JCVariableDecl fieldNode = (JCTree.JCVariableDecl) elementUtils.getTree(field);
        fieldNode.init = maker.Literal(docComment);
      }
    }

    return true;
  }
}
