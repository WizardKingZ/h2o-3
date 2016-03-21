package water.codegen.java;

import java.lang.reflect.Modifier;

import water.codegen.CodeGenerator;
import water.codegen.CodeGeneratorPipeline;
import water.codegen.JCodeSB;
import static water.util.ArrayUtils.append;

/**
 * FIXME:
 */
public class ClassCodeGenerator extends CodeGeneratorPipeline<ClassCodeGenerator> {

  /** Name of class to generate. */
  final String name;
  /** Class modifiers - e.g., "public static" */
  int modifiers;
  /** Extend given class */
  Class extendClass;
  /** Implements interface */
  Class[] interfaces;
  /** Annotation generators */
  CodeGenerator[] acgs;

  ClassCodeGenerator(String className) {
    this.name = className;
  }

  public ClassCodeGenerator withModifiers(int...modifiers) {
    for (int m : modifiers) {
      this.modifiers |= m;
    }
    return this;
  }

  public ClassCodeGenerator withImplements(Class ... interfaces) {
    this.interfaces = append(this.interfaces, interfaces);
    return this;
  }

  public ClassCodeGenerator withExtend(Class extendClass) {
    this.extendClass = extendClass;
    return this;
  }

  public ClassCodeGenerator withAnnotation(CodeGenerator... acgs) {
    this.acgs = append(this.acgs, acgs);
    return this;
  }

  public ClassCodeGenerator withAnnotation(final JCodeSB ... acgs) {
    if (acgs != null) {
      this.acgs = append(this.acgs, new CodeGenerator() {
        @Override
        public void generate(JCodeSB out) {
          for (JCodeSB acg : acgs) {
            out.p(acg).nl();
          }
        }
      });
    }
    return this;
  }

  public ClassCodeGenerator withCtor(MethodCodeGenerator... mcgs) {
    for (MethodCodeGenerator m : mcgs) {
      assert m.getReturnType() == void.class : "Declared method does not represent constructor! Method: " + m.name;
      assert m.name.equals(this.name) : "Name of constructor does not match name of class: " + m.name;
      add(m);
    }
    return this;
  }

  public ClassCodeGenerator withMethod(MethodCodeGenerator... mcgs) {
    for (MethodCodeGenerator m : mcgs) {
      add(m);
    }
    return this;
  }

  public ClassCodeGenerator withField(FieldCodeGenerator... fcgs) {
    for (FieldCodeGenerator fcg : fcgs) {
      add(fcg);
    }
    return this;
  }

  @Override
  public void generate(JCodeSB out) {
    // Generate class preamble
    genClassHeader(out);
    // Generate the body defined by a chain of code generators
    out.ii(2).i();
    super.generate(out);
    out.di(2).nl();
    // Close this class
    genClassFooter(out);
  }

  protected JCodeSB genClassHeader(JCodeSB sb) {
    // Generate annotations
    if (acgs != null) {
      for (CodeGenerator acg : acgs) {
        acg.generate(sb);
      }
    }
    // Starts to define class
    sb.p(Modifier.toString(modifiers)).p(" class ").p(name);
    if (extendClass != null) {
      sb.p(" extends ").pj(extendClass);
    }
    if (interfaces != null && interfaces.length > 0) {
      sb.p(" implements ").pj(interfaces[0]);
      for (int i = 1; i < interfaces.length; i++) {
        sb.p(", ").pj(interfaces[i]);
      }
    }
    sb.p(" {").nl();

    return sb;
  }

  protected JCodeSB genClassFooter(JCodeSB sb) {
    sb.p("} // End of class ").p(name).nl();
    return sb;
  }
}
