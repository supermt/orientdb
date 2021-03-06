/* Generated By:JJTree: Do not edit this line. OArrayConcatExpressionElement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.orientechnologies.orient.core.sql.parser;

public class OArrayConcatExpressionElement extends OExpression {
  public OArrayConcatExpressionElement(int id) {
    super(id);
  }

  public OArrayConcatExpressionElement(OrientSql p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(OrientSqlVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public OArrayConcatExpressionElement copy() {
    OArrayConcatExpressionElement result = new OArrayConcatExpressionElement(-1);
    result.singleQuotes = singleQuotes;
    result.doubleQuotes = doubleQuotes;
    result.isNull = isNull;
    result.rid = rid == null ? null : rid.copy();
    result.mathExpression = mathExpression == null ? null : mathExpression.copy();
    result.json = json == null ? null : json.copy();
    result.booleanValue = booleanValue;

    return result;
  }

}
/* JavaCC - OriginalChecksum=a37b12bac47f1771db27ce370d09f2f5 (do not edit this line) */
