package sem;
import java.lang.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Semantics {

   /********************************************************/
   /*                        Tracing                       */
   /********************************************************/
   static int level = 0;
   static BufferedWriter traceFile;

   static void openTraceFile (String traceFileName) {
      if (traceFileName != null) {
         try {
            File file = new File(traceFileName);
            traceFile = new BufferedWriter(new FileWriter(file));
         } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
         }
      }
   }

   static void closeTraceFile () {
      if (traceFile != null) {
         try {
            traceFile.close();
         } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
         }
      }
   }

   static void TRACE(String x, ast.AST e, String y, boolean args) {
      if (traceFile != null) {
         try {
            for(int i=0; i<level; i++) {
               traceFile.write("   ");
            };
            traceFile.write(x + e.getClass().getSimpleName()); 
            traceFile.write(y); 
            traceFile.newLine();
         } catch (IOException ex) {
            System.out.println(ex.getMessage());
            System.exit(0);
         }
      }
   }

   static void ENTER(ast.AST e) {
      TRACE("<", e, ">", true);
      level++;
   }

   static void EXIT(ast.AST e) {
      level--;
      TRACE("</", e, ">", false);
   }

/********************************************************/
/*                        Semantics                     */
/********************************************************/

private enum DeclKind {
   NORMAL,
   FIELD,
   FORMAL
}

private static final class ExprInfo {
   final sym.TypeSy type;
   final boolean isConstant;
   final sym.Value value;
   final boolean isLValue;
   final boolean hasError;

   // expr attributes we need for the AST during type checking
   ExprInfo(sym.TypeSy type, boolean isConstant, sym.Value value, boolean isLValue, boolean hasError) {
      this.type = type;
      this.isConstant = isConstant;
      this.value = value;
      this.isLValue = isLValue;
      this.hasError = hasError;
   }
}

private static final class DeclsResult {
   final sym.SyTab scope;
   final sym.SyTab collected;

   // decl traversals return scope and a list for callers
   DeclsResult(sym.SyTab scope, sym.SyTab collected) {
      this.scope = scope;
      this.collected = collected;
   }
}

private static final class SemError {
   final int pos;
   final String message;
   final String argument;

   // record for emitting errors after sorting
   SemError(int pos, String message, String argument) {
      this.pos = pos;
      this.message = message;
      this.argument = argument;
   }

   String xml() {
      if (argument == null) {
         return "<SEMANTIC_ERROR pos=\"" + pos + "\" message=\"" + message + "\"/>";
      }
      return "<SEMANTIC_ERROR pos=\"" + pos + "\" message=\"" + message + "\" argument=\"" + argument + "\"/>";
   }
}

public Semantics(String traceFileName) {
    openTraceFile(traceFileName);
}


public static void SemanticAnalysis(ast.AST E) {
    if (E instanceof ast.PROGRAM) {
       // entry point of semantic analysis
       ArrayList<SemError> errors = new ArrayList<SemError>();
       PROGRAM((ast.PROGRAM) E, errors);
       emitSortedErrors(errors);
       closeTraceFile();
    } else {
       closeTraceFile();
      auxx.Error.Internal("SemanticAnalysis", "Node " + E.getClass().getName() + " unexpected.");
    };
}

private static void emitSortedErrors(ArrayList<SemError> errors) {
   Collections.sort(errors, new Comparator<SemError>() {
      public int compare(SemError a, SemError b) {
         return a.xml().compareTo(b.xml());
      }
   });
   emitErrorsRec(errors, 0);
}

private static void emitErrorsRec(ArrayList<SemError> errors, int index) {
   if (index >= errors.size()) {
      return;
   }
   System.err.println(errors.get(index).xml());
   emitErrorsRec(errors, index + 1);
}

// overloaded method for flexibility in error reporting
private static void error(ArrayList<SemError> errors, int pos, String msg) {
   errors.add(new SemError(pos, msg, null));
}

private static void error(ArrayList<SemError> errors, int pos, String msg, String arg) {
   errors.add(new SemError(pos, msg, arg));
}

// 6 simple helper methods
private static boolean isNoType(sym.TypeSy t) {
   return t == null || t == sym.Standard.NoType;
}

private static boolean sameType(sym.TypeSy a, sym.TypeSy b) {
   return !isNoType(a) && !isNoType(b) && a == b;
}

private static boolean isScalarType(sym.TypeSy t) {
   return t == sym.Standard.IntType ||
          t == sym.Standard.RealType ||
          t == sym.Standard.CharType ||
          t == sym.Standard.BoolType;
}

private static boolean isReferenceType(sym.TypeSy t) {
   return t instanceof sym.RefType;
}

private static boolean isReadType(sym.TypeSy t) {
   return t == sym.Standard.IntType || t == sym.Standard.RealType || t == sym.Standard.CharType;
}

private static boolean isWriteType(sym.TypeSy t) {
   return isReadType(t) || t == sym.Standard.StringType;
}

private static boolean isSimpleCallDesignator(ast.DESIGNATOR d) {
   return (d instanceof ast.VARREF) && (((ast.VARREF) d).next instanceof ast.DESNULL);
}

// ensure we can resolve a type name and that it is a type, otherwise return NoType (to reduce cascading errors) and report an error
private static sym.TypeSy resolveTypeName(int pos, String typeName, sym.Env env, ArrayList<SemError> errors) {
   sym.Symbol sy = env.locateByName(typeName);
   if (sy == null) {
      error(errors, pos, "Identifier not declared", typeName);
      return sym.Standard.NoType;
   }
   if (!(sy instanceof sym.TypeSy)) {
      error(errors, pos, "Type identifier expected", typeName);
      return sym.Standard.NoType;
   }
   return (sym.TypeSy) sy;
}

private static ExprInfo attachExpr(ast.EXPRESSION e, ExprInfo info, sym.Env env) {
   // write attributes to ast node and return info
   e.inEnv = env;
   e.type = info.type;
   e.value = info.value;
   e.isConstant = info.isConstant;
   e.isLValue = info.isLValue;
   e.outEnv = env;
   return info;
}

private static ExprInfo analyzeDesignatorTail(ast.DESIGNATOR tail,
                                              ExprInfo base,
                                              sym.Env env,
                                              ArrayList<SemError> errors) {
   if (tail instanceof ast.DESNULL) { // base case
      tail.inEnv = env;
      tail.outEnv = env;
      return base;
   }

   if (tail instanceof ast.INDEX) { // array indexing
      ast.INDEX idx = (ast.INDEX) tail;
      idx.inEnv = env;
      ExprInfo idxInfo = analyzeExpr(idx.index, env, errors);
      if (!isNoType(idxInfo.type) && idxInfo.type != sym.Standard.IntType) {
         error(errors, idx.position, "Integer type expected");
      }

      ExprInfo result;
      if (base.type instanceof sym.ArrayType) {
         sym.ArrayType a = (sym.ArrayType) base.type;
         result = new ExprInfo(a.GetArrayElementType(), false, null, base.isLValue, base.hasError);
      } else if (isNoType(base.type)) {
         result = new ExprInfo(sym.Standard.NoType, false, null, false, true);
      } else {
         error(errors, idx.position, "Array variable expected");
         result = new ExprInfo(sym.Standard.NoType, false, null, false, true);
      }
      ExprInfo tailInfo = analyzeDesignatorTail(idx.next, result, env, errors);
      idx.outEnv = env;
      return tailInfo;
   }

   if (tail instanceof ast.FIELDREF) { // resolve field name
      ast.FIELDREF ref = (ast.FIELDREF) tail;
      ref.inEnv = env;
      ExprInfo result;
      if (base.type instanceof sym.RecordType) {
         sym.RecordType r = (sym.RecordType) base.type;
         sym.Symbol field = r.GetFields().locateByName(ref.ident);
         if (field == null) {
            error(errors, ref.position, "Field identifier not declared", ref.ident);
            result = new ExprInfo(sym.Standard.NoType, false, null, false, true);
         } else if (field instanceof sym.VariableSy) {
            sym.TypeSy t = ((sym.VariableSy) field).GetType();
            result = new ExprInfo(t, false, null, base.isLValue, base.hasError);
         } else {
            result = new ExprInfo(sym.Standard.NoType, false, null, false, true);
         }
      } else if (isNoType(base.type)) {
         result = new ExprInfo(sym.Standard.NoType, false, null, false, true);
      } else {
         error(errors, ref.position, "Record variable expected");
         result = new ExprInfo(sym.Standard.NoType, false, null, false, true);
      }
      ExprInfo tailInfo = analyzeDesignatorTail(ref.next, result, env, errors);
      ref.outEnv = env;
      return tailInfo;
   }

   return new ExprInfo(sym.Standard.NoType, false, null, false, true);
}

// resolve designator (variable/field/array ref), return type, l-value info, give AST scope/environment
private static ExprInfo analyzeDesignator(ast.DESIGNATOR e, sym.Env env, ArrayList<SemError> errors) {
   if (e instanceof ast.VARREF) {
      ast.VARREF v = (ast.VARREF) e;
      v.inEnv = env;
      sym.Symbol sy = env.locateByName(v.ident);

      ExprInfo base;
      if (sy == null) {
         error(errors, v.position, "Identifier not declared", v.ident);
         base = new ExprInfo(sym.Standard.NoType, false, null, false, true);
      } else if (sy instanceof sym.ConstSy) {
         sym.ConstSy c = (sym.ConstSy) sy;
         sym.Value vconst = c.GetConstantValue();
         if (c instanceof sym.EnumSy && c.GetType() == sym.Standard.BoolType && vconst instanceof sym.IntValue) {
            vconst = new sym.BoolValue(((sym.IntValue) vconst).getValue() != 0);
         }
         base = new ExprInfo(c.GetType(), true, vconst, false, false);
      } else if (sy instanceof sym.VariableSy) {
         sym.VariableSy var = (sym.VariableSy) sy;
         base = new ExprInfo(var.GetType(), false, null, true, false);
      } else if (sy instanceof sym.ProcedureSy) {
         base = new ExprInfo(sym.Standard.NoType, false, null, false, false);
      } else {
         error(errors, v.position, "Variable expected", v.ident);
         base = new ExprInfo(sym.Standard.NoType, false, null, false, true);
      }

      ExprInfo tailInfo = analyzeDesignatorTail(v.next, base, env, errors);
      v.outEnv = env;
      return attachExpr(e, tailInfo, env);
   }

   return attachExpr(e, new ExprInfo(sym.Standard.NoType, false, null, false, true), env);
}

private static sym.Value unaryConstValue(int op, sym.Value left, int pos) {
   if (left instanceof sym.IntValue) {
      return sym.IntValue.unaryEval(op, (sym.IntValue) left, pos);
   }
   if (left instanceof sym.RealValue) {
      return sym.RealValue.unaryEval(op, (sym.RealValue) left, pos);
   }
   if (left instanceof sym.BoolValue) {
      return sym.BoolValue.unaryEval(op, (sym.BoolValue) left, pos);
   }
   return null;
}

// evaluate binops for constants, return null if illegal operator
private static sym.Value binaryConstValue(int op, sym.Value left, sym.Value right, int pos) {
   if (left instanceof sym.IntValue && right instanceof sym.IntValue) {
      return sym.IntValue.binaryEval(op, (sym.IntValue) left, (sym.IntValue) right, pos);
   }
   if (left instanceof sym.RealValue && right instanceof sym.RealValue) {
      return sym.RealValue.binaryEval(op, (sym.RealValue) left, (sym.RealValue) right, pos);
   }
   if (left instanceof sym.CharValue && right instanceof sym.CharValue) {
      return sym.CharValue.binaryEval(op, (sym.CharValue) left, (sym.CharValue) right, pos);
   }
   if (left instanceof sym.BoolValue && right instanceof sym.BoolValue) {
      return sym.BoolValue.binaryEval(op, (sym.BoolValue) left, (sym.BoolValue) right, pos);
   }
   if (left instanceof sym.StringValue && right instanceof sym.StringValue) {
      return sym.StringValue.binaryEval(op, (sym.StringValue) left, (sym.StringValue) right, pos);
   }
   return null;
}

// could refactor this further into more helpers for each expression, but it works as it is so i'm leaving it
private static ExprInfo analyzeExpr(ast.EXPRESSION e, sym.Env env, ArrayList<SemError> errors) {
   if (e instanceof ast.DESIGNATOR) {
      return analyzeDesignator((ast.DESIGNATOR) e, env, errors);
   }

   if (e instanceof ast.INTLIT) {
      ast.INTLIT lit = (ast.INTLIT) e;
      lit.inEnv = env;
      ExprInfo info = new ExprInfo(sym.Standard.IntType, true, new sym.IntValue(Integer.parseInt(lit.literal)), false, false);
      lit.outEnv = env;
      return attachExpr(lit, info, env);
   }

   if (e instanceof ast.REALLIT) {
      ast.REALLIT lit = (ast.REALLIT) e;
      lit.inEnv = env;
      ExprInfo info = new ExprInfo(sym.Standard.RealType, true, new sym.RealValue(Float.parseFloat(lit.literal)), false, false);
      lit.outEnv = env;
      return attachExpr(lit, info, env);
   }

   if (e instanceof ast.CHARLIT) {
      ast.CHARLIT lit = (ast.CHARLIT) e;
      lit.inEnv = env;
      char value = lit.literal.length() > 0 ? lit.literal.charAt(0) : '\0';
      ExprInfo info = new ExprInfo(sym.Standard.CharType, true, new sym.CharValue(value), false, false);
      lit.outEnv = env;
      return attachExpr(lit, info, env);
   }

   if (e instanceof ast.STRINGLIT) {
      ast.STRINGLIT lit = (ast.STRINGLIT) e;
      lit.inEnv = env;
      ExprInfo info = new ExprInfo(sym.Standard.StringType, true, new sym.StringValue(lit.literal), false, false);
      lit.outEnv = env;
      return attachExpr(lit, info, env);
   }

   if (e instanceof ast.UNARY) {
      ast.UNARY u = (ast.UNARY) e;
      u.inEnv = env;
      ExprInfo left = analyzeExpr(u.left, env, errors);
      sym.TypeSy resultType = sym.Standard.NoType;
      boolean hadError = left.hasError;

      switch (u.op) { // unary operators
         case lexer.Token.MINUS:
            if (left.type == sym.Standard.IntType || left.type == sym.Standard.RealType) {
               resultType = left.type;
            } else if (!isNoType(left.type)) {
               error(errors, u.position, "Numeric type expected");
               hadError = true;
            }
            break;
         case lexer.Token.NOT:
            if (left.type == sym.Standard.BoolType) {
               resultType = sym.Standard.BoolType;
            } else if (!isNoType(left.type)) {
               error(errors, u.position, "Boolean type expected");
               hadError = true;
            }
            break;
         case lexer.Token.TRUNC:
            if (left.type == sym.Standard.RealType) {
               resultType = sym.Standard.IntType;
            } else if (!isNoType(left.type)) {
               error(errors, u.position, "Real type expected");
               hadError = true;
            }
            break;
         case lexer.Token.FLOAT:
            if (left.type == sym.Standard.IntType) {
               resultType = sym.Standard.RealType;
            } else if (!isNoType(left.type)) {
               error(errors, u.position, "Integer type expected");
               hadError = true;
            }
            break;
         default:
            // unreachable
            break;
      }

      boolean constant = left.isConstant;
      sym.Value value = null;
      if (constant && !hadError && !isNoType(resultType)) {
         value = unaryConstValue(u.op, left.value, u.position);
         if (value instanceof sym.ErrorValue) {
            error(errors, u.position, ((sym.ErrorValue) value).msg);
            hadError = true;
         }
      }
      ExprInfo info = new ExprInfo(resultType, constant, value, false, hadError);
      u.outEnv = env;
      return attachExpr(u, info, env);
   }

   if (e instanceof ast.BINARY) {
      ast.BINARY b = (ast.BINARY) e;
      b.inEnv = env;
      ExprInfo left = analyzeExpr(b.left, env, errors);
      ExprInfo right = analyzeExpr(b.right, env, errors);

      sym.TypeSy resultType = sym.Standard.NoType;
      boolean hadError = left.hasError || right.hasError;
      switch (b.op) {
         // arithmetic operators
         case lexer.Token.PLUS:
         case lexer.Token.MINUS:
         case lexer.Token.STAR:
         case lexer.Token.SLASH:
            if (left.type == sym.Standard.IntType && right.type == sym.Standard.IntType) {
               resultType = sym.Standard.IntType;
            } else if (left.type == sym.Standard.RealType && right.type == sym.Standard.RealType) {
               resultType = sym.Standard.RealType;
            } else if ((left.type == sym.Standard.IntType || left.type == sym.Standard.RealType) &&
                       (right.type == sym.Standard.IntType || right.type == sym.Standard.RealType)) {
               error(errors, b.position, "Type missmatch");
               hadError = true;
            } else if (!isNoType(left.type) && !isNoType(right.type)) {
               if (!(left.type == sym.Standard.IntType || left.type == sym.Standard.RealType)) {
                  error(errors, b.position, "Numeric type expected");
                  hadError = true;
               }
               if (!(right.type == sym.Standard.IntType || right.type == sym.Standard.RealType)) {
                  error(errors, b.position, "Numeric type expected");
                  hadError = true;
               }
            }
            break;

         case lexer.Token.PERCENT: // modulus
            if (left.type == sym.Standard.IntType && right.type == sym.Standard.IntType) {
               resultType = sym.Standard.IntType;
            } else if (!isNoType(left.type) && !isNoType(right.type)) {
               if (left.type != sym.Standard.IntType) {
                  error(errors, b.position, "Integer type expected");
                  hadError = true;
               }
               if (right.type != sym.Standard.IntType) {
                  error(errors, b.position, "Integer type expected");
                  hadError = true;
               }
            }
            break;

         case lexer.Token.AND:
         case lexer.Token.OR:
            if (left.type == sym.Standard.BoolType && right.type == sym.Standard.BoolType) {
               resultType = sym.Standard.BoolType;
            } else if (!isNoType(left.type) && !isNoType(right.type)) {
               if (left.type != sym.Standard.BoolType) {
                  error(errors, b.position, "Boolean type expected");
                  hadError = true;
               }
               if (right.type != sym.Standard.BoolType) {
                  error(errors, b.position, "Boolean type expected");
                  hadError = true;
               }
            }
            break;
         // comparison operators
         case lexer.Token.LT:
         case lexer.Token.LE:
         case lexer.Token.EQ:
         case lexer.Token.NE:
         case lexer.Token.GE:
         case lexer.Token.GT:
            if (!(isScalarType(left.type) || isReferenceType(left.type)) ||
                !(isScalarType(right.type) || isReferenceType(right.type))) {
               if (!isNoType(left.type) && !isNoType(right.type)) {
                  if (!(isScalarType(left.type) || isReferenceType(left.type))) {
                     error(errors, b.position, "Scalar or reference type expected");
                     hadError = true;
                  }
                  if (!(isScalarType(right.type) || isReferenceType(right.type))) {
                     error(errors, b.position, "Scalar or reference type expected");
                     hadError = true;
                  }
               }
            } else if (!sameType(left.type, right.type)) {
               if (!isNoType(left.type) && !isNoType(right.type)) {
                  error(errors, b.position, "Type missmatch");
                  hadError = true;
               }
            } else {
               resultType = sym.Standard.BoolType;
            }
            break;

         default:
            // unreachable
            break;
      }

      boolean constant = left.isConstant && right.isConstant;
      sym.Value value = null;
      if (constant && !hadError && !isNoType(resultType)) {
         value = binaryConstValue(b.op, left.value, right.value, b.position);
         if (value instanceof sym.ErrorValue) {
            error(errors, b.position, ((sym.ErrorValue) value).msg);
            hadError = true;
         }
      }

      ExprInfo info = new ExprInfo(resultType, constant, value, false, hadError);
      b.outEnv = env;
      return attachExpr(b, info, env);
   }

   if (e instanceof ast.ISA) {
      ast.ISA isa = (ast.ISA) e;
      isa.inEnv = env;
      ExprInfo left = analyzeExpr(isa.left, env, errors);
      sym.TypeSy rightType = resolveTypeName(isa.position, isa.typeName, env, errors);
      boolean constant = left.isConstant && !isNoType(rightType);
      sym.Value value = null;
      if (constant) {
         value = new sym.BoolValue(left.type == rightType);
      }
      ExprInfo info = new ExprInfo(sym.Standard.BoolType, constant, value, false, left.hasError);
      isa.outEnv = env;
      return attachExpr(isa, info, env);
   }

   return attachExpr(e, new ExprInfo(sym.Standard.NoType, false, null, false, true), env);
}

// walks decls, build symbol entries, thread scopes, report errors
private static DeclsResult analyzeDecls(ast.DECLS e,
                                        sym.Env outerEnv,
                                        sym.SyTab scope,
                                        sym.SyTab collected,
                                        int nextFormalNumber,
                                        DeclKind kind,
                                        ArrayList<SemError> errors) {
   e.inEnv = outerEnv.cons(scope);
   if (e instanceof ast.DECLNULL) {
      // pass up everything built to this point
      e.outEnv = outerEnv.cons(scope);
      return new DeclsResult(scope, collected);
   }

   ast.DECLARATION d = e.left;
   d.inEnv = outerEnv.cons(scope);

   if (scope.locateByName(d.ident) != null) {
      // keep walking to report downstream errors
      error(errors, e.position, "Multiple declaration", d.ident);
      d.outEnv = outerEnv.cons(scope);
      DeclsResult r = analyzeDecls(e.right, outerEnv, scope, collected, nextFormalNumber, kind, errors);
      e.outEnv = outerEnv.cons(r.scope);
      return r;
   }

   sym.Symbol newSymbol = null;
   sym.SyTab newScope = scope;
   sym.SyTab newCollected = collected;
   int newFormalNumber = nextFormalNumber;
   sym.Env env = outerEnv.cons(scope);

   // create symbol node for each decl
   if (d instanceof ast.VARDECL) {
      ast.VARDECL vd = (ast.VARDECL) d;
      sym.TypeSy type = resolveTypeName(vd.position, vd.typeName, env, errors);
      sym.VariableSy sy = new sym.VariableSy(vd.ident, vd.position, 0);
      sy.SetType(type);
      newSymbol = sy;
   } else if (d instanceof ast.FIELDDECL) {
      ast.FIELDDECL fd = (ast.FIELDDECL) d;
      sym.TypeSy type = resolveTypeName(fd.position, fd.typeName, env, errors);
      sym.FieldSy sy = new sym.FieldSy(fd.ident, fd.position, 0);
      sy.SetType(type);
      newSymbol = sy;
   } else if (d instanceof ast.FORMALDECL) {
      ast.FORMALDECL fd = (ast.FORMALDECL) d;
      sym.TypeSy type = resolveTypeName(fd.position, fd.typeName, env, errors);
      sym.FormalSy sy = new sym.FormalSy(fd.ident, fd.position, 0);
      sy.SetType(type);
      sy.SetFormalMode(fd.mode);
      sy.SetFormalNumber(nextFormalNumber);
      newSymbol = sy;
      newFormalNumber = nextFormalNumber + 1;
   } else if (d instanceof ast.CONSTDECL) {
      ast.CONSTDECL cd = (ast.CONSTDECL) d;
      sym.TypeSy declaredType = resolveTypeName(cd.position, cd.typeName, env, errors);
      if (!isNoType(declaredType) && !isScalarType(declaredType)) {
         error(errors, cd.position, "Scalar type expected");
      }
      ExprInfo exprInfo = analyzeExpr(cd.expr, env, errors);
      if (!isNoType(declaredType) && isScalarType(declaredType) &&
          !isNoType(exprInfo.type) && !sameType(declaredType, exprInfo.type)) {
         error(errors, cd.position, "Wrong expression type");
      }
      if (!exprInfo.hasError && !exprInfo.isConstant) {
         error(errors, cd.position, "Constant expression expected");
      }
      sym.ConstSy sy = new sym.ConstSy(cd.ident, cd.position, 0);
      sy.SetType(declaredType);
      sy.SetConstantValue(exprInfo.value);
      newSymbol = sy;
   } else if (d instanceof ast.ARRAYDECL) {
      ast.ARRAYDECL ad = (ast.ARRAYDECL) d;
      sym.TypeSy elemType = resolveTypeName(ad.position, ad.elementTypeName, env, errors);
      ExprInfo count = analyzeExpr(ad.count, env, errors);
      if (!isNoType(count.type) && count.type != sym.Standard.IntType) {
         error(errors, ad.position, "Integer expression expected");
      }
      if (!count.isConstant) {
         error(errors, ad.position, "Constant expression expected");
      }
      sym.ArrayType at = new sym.ArrayType(ad.ident, ad.position, 0);
      at.SetArrayElementType(elemType);
      if (count.value instanceof sym.IntValue) {
         at.SetArrayCount(((sym.IntValue) count.value).getValue());
      }
      newSymbol = at;
   } else if (d instanceof ast.RECORDDECL) {
      ast.RECORDDECL rd = (ast.RECORDDECL) d;
      sym.RecordType rt = new sym.RecordType(rd.ident, rd.position, 0);
      DeclsResult fieldResult = analyzeDecls(rd.fields, env, new sym.SyTab(), new sym.SyTab(), 1, DeclKind.FIELD, errors);
      rt.SetFields(fieldResult.scope);
      newSymbol = rt;
   } else if (d instanceof ast.PROCDECL) {
      ast.PROCDECL pd = (ast.PROCDECL) d;
      sym.ProcedureSy proc = new sym.ProcedureSy(pd.ident, pd.position, 0);
      sym.SyTab scopeWithProc = scope.insert(proc);
      sym.Env procOuterEnv = outerEnv.cons(scopeWithProc);

      // every proc gets its own pass for formals/ locals before walking
      DeclsResult formals = analyzeDecls(pd.formals, procOuterEnv, new sym.SyTab(), new sym.SyTab(), 1, DeclKind.FORMAL, errors);
      DeclsResult locals = analyzeDecls(pd.decls, procOuterEnv, formals.scope, new sym.SyTab(), 1, DeclKind.NORMAL, errors);
      proc.SetProcFormals(formals.collected);
      proc.SetProcLocals(locals.scope);

      sym.Env bodyEnv = procOuterEnv.cons(locals.scope);
      analyzeStats(pd.stats, bodyEnv, false, errors);
      newSymbol = proc;
   }

   if (newSymbol != null) {
      // put symbol on the AST node and thread it to both trackers
      d.symbol = newSymbol;
      d.scope = newScope;
      newScope = newScope.insert(newSymbol);
      newCollected = newCollected.insert(newSymbol);
   }

   d.outEnv = outerEnv.cons(newScope);
   // recursively call remaining declarations while threading the scope
   DeclsResult r = analyzeDecls(e.right, outerEnv, newScope, newCollected, newFormalNumber, kind, errors);
   e.outEnv = outerEnv.cons(r.scope);
   return r;
}

private static void analyzeActuals(ast.ACTUAL a,
                                   sym.ProcedureSy proc,
                                   int formalNumber,
                                   sym.Env env,
                                   ArrayList<SemError> errors) {
   a.inEnv = env;
   if (a instanceof ast.ACTUALNULL) {
      // no more actuals
      if (proc != null && sym.ProcedureSy.GetFormalParam(proc.GetProcFormals(), formalNumber) != null) {
         error(errors, a.position, "Too few actual parameters.");
      }
      a.outEnv = env;
      return;
   }

   ExprInfo expr = analyzeExpr(a.expr, env, errors);
   if (proc != null) {
      sym.FormalSy f = sym.ProcedureSy.GetFormalParam(proc.GetProcFormals(), formalNumber);
      if (f == null) {
         error(errors, a.position, "Too many actual parameters.");
      } else {
         // keep diagnosing after mismatch
         if (!isNoType(f.GetType()) && !isNoType(expr.type) && !sameType(f.GetType(), expr.type)) {
            error(errors, a.position, "Actual/formal parameter type missmatch.");
         }
         if ("VAR".equals(f.GetFormalMode()) && !expr.isLValue) {
            error(errors, a.position, "VAR formal parameter requires variable actual.");
         }
      }
   }

   // recurseively call list of actuals
   analyzeActuals(a.nextActual, proc, formalNumber + 1, env, errors);
   a.outEnv = env;
}

private static void analyzeStatement(ast.STATEMENT s,
                                     sym.Env env,
                                     boolean inLoop,
                                     ArrayList<SemError> errors) {
   s.inEnv = env;
   s.inLoop = inLoop;

   if (s instanceof ast.ASSIGN) { // assignment
      ast.ASSIGN a = (ast.ASSIGN) s;
      ExprInfo left = analyzeDesignator(a.left, env, errors);
      ExprInfo right = analyzeExpr(a.right, env, errors);
      if (!isNoType(left.type) && !isNoType(right.type) &&
          (!isScalarType(left.type) || !isScalarType(right.type))) {
         error(errors, a.position, "Scalar type expected");
      } else if (!isNoType(left.type) && !isNoType(right.type) && !sameType(left.type, right.type)) {
         error(errors, a.position, "Type missmatch in assignment statement.");
      }
      if (!left.hasError && !isNoType(left.type) && !left.isLValue) {
         error(errors, a.position, "Can't assign to a constant or expression.");
      }
   } else if (s instanceof ast.PROCCALL) { // procedure call
      ast.PROCCALL p = (ast.PROCCALL) s;
      sym.ProcedureSy proc = null;
      if (!isSimpleCallDesignator(p.des)) {
         // only bare identifiers indicate procedures, anything else is an error
         error(errors, p.position, "Identifier not declared");
      } else {
         ast.VARREF v = (ast.VARREF) p.des;
         sym.Symbol sy = env.locateByName(v.ident);
         if (sy == null) {
            error(errors, p.position, "Identifier not declared", v.ident);
         } else if (!(sy instanceof sym.ProcedureSy)) {
            error(errors, p.position, "Procedure identifier expected");
         } else {
            proc = (sym.ProcedureSy) sy;
         }
      }
      analyzeActuals(p.actuals, proc, 1, env, errors);
   } else if (s instanceof ast.WRITE) {
      ast.WRITE w = (ast.WRITE) s;
      ExprInfo expr = analyzeExpr(w.expr, env, errors);
      if (!isNoType(expr.type) && !isWriteType(expr.type)) {
         error(errors, w.position, "INTEGER, REAL, CHAR, STRING type expected");
      }
   } else if (s instanceof ast.READ) {
      ast.READ r = (ast.READ) s;
      ExprInfo expr = analyzeDesignator(r.des, env, errors);
      if (!isNoType(expr.type) && !isReadType(expr.type)) {
         error(errors, r.position, "INTEGER, REAL, CHAR type expected");
      }
      if (!expr.hasError && !isNoType(expr.type) && !expr.isLValue) {
         error(errors, r.position, "Can't read to a constant.");
      }
   } else if (s instanceof ast.WHILE) {
      ast.WHILE w = (ast.WHILE) s;
      ExprInfo expr = analyzeExpr(w.expr, env, errors);
      if (!isNoType(expr.type) && expr.type != sym.Standard.BoolType) {
         error(errors, w.position, "Boolean type expected");
      }
      analyzeStats(w.stats, env, inLoop, errors);
   } else if (s instanceof ast.REPEAT) {
      ast.REPEAT r = (ast.REPEAT) s;
      analyzeStats(r.stats, env, inLoop, errors);
      ExprInfo expr = analyzeExpr(r.expr, env, errors);
      if (!isNoType(expr.type) && expr.type != sym.Standard.BoolType) {
         error(errors, r.position, "Boolean type expected");
      }
   } else if (s instanceof ast.IF1) { // if without else
      ast.IF1 i = (ast.IF1) s;
      ExprInfo expr = analyzeExpr(i.expr, env, errors);
      if (!isNoType(expr.type) && expr.type != sym.Standard.BoolType) {
         error(errors, i.position, "Boolean type expected");
      }
      analyzeStats(i.then_, env, inLoop, errors);
   } else if (s instanceof ast.IF2) { // if with else
      ast.IF2 i = (ast.IF2) s;
      ExprInfo expr = analyzeExpr(i.expr, env, errors);
      if (!isNoType(expr.type) && expr.type != sym.Standard.BoolType) {
         error(errors, i.position, "Boolean type expected");
      }
      analyzeStats(i.then_, env, inLoop, errors);
      analyzeStats(i.else_, env, inLoop, errors);
   } else if (s instanceof ast.LOOP) {
      ast.LOOP l = (ast.LOOP) s;
      // pass down loop boolean
      analyzeStats(l.stats, env, true, errors);
   } else if (s instanceof ast.EXIT) {
      if (!inLoop) {
         error(errors, s.position, "EXIT only within LOOP");
      }
   }

   s.outEnv = env;
}

private static void analyzeStats(ast.STATS stats,
                                 sym.Env env,
                                 boolean inLoop,
                                 ArrayList<SemError> errors) {
   stats.inEnv = env;
   stats.inLoop = inLoop;
   if (stats instanceof ast.STATNULL) {
      stats.outEnv = env;
      return;
   }
   
   analyzeStatement(stats.left, env, inLoop, errors);
   analyzeStats(stats.right, env, inLoop, errors);
   stats.outEnv = env;
}

public static void PROGRAM(ast.PROGRAM E, ArrayList<SemError> errors) {
   ENTER(E);
   sym.Env std = sym.Standard.env(); // create base environment with standard decls
   E.inEnv = std; // for error reporting

   // decl pass before stats so subsequent lookups see proper scope
   DeclsResult decls = analyzeDecls(E.decls, std, new sym.SyTab(), new sym.SyTab(), 1, DeclKind.NORMAL, errors);
   sym.Env bodyEnv = std.cons(decls.scope); // combine before analyzing stats
   analyzeStats(E.stats, bodyEnv, false, errors); 
   E.outEnv = bodyEnv; // for errors
   EXIT(E);
}

   public static void main (String args[]) throws IOException{
      if (args.length < 1) {
          throw new IOException("Usage: luca_sem <input.luc> [trace.txt]");
      };

      String traceFile = null;
      if (args.length >= 2) {
          traceFile = args[1];
      };

      lexer.Lex scanner = new lexer.Lex(args[0]);
      parser.Parse parser = new parser.Parse(scanner);
      ast.PROGRAM ast = parser.program();
      sem.Semantics sem = new sem.Semantics(traceFile);
      sem.SemanticAnalysis(ast); // pass root of ast from parser

   }

}  
