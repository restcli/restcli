package uos.dev.restcli.parser;
%%

%public

%debug
%unicode
%state S_REQUEST_SEPARATOR, S_REQUEST_LINE, S_HEADER, S_BODY, S_MULTILE_PART, S_SCRIPT_HANDLER, S_SCRIPT_REFERENCE
%state S_MULTIPLE_PART_HEADER, S_MULTIPLE_PART_BODY

%{
private boolean hasRequestTarget = false;
private boolean isMultiplePart = false;
private int previousState = -1;
private String currentFieldName = null;
private boolean isNewPartRequired = false;

public int getPreviousState() {
  return previousState;
}

private void switchState(int newState) {
  previousState = yystate();
  yybegin(newState);
}

private void reset() {
    hasRequestTarget = false;
    isMultiplePart = false;
    currentFieldName = null;
    isNewPartRequired = false;
}

public boolean isMultiplePart() {
    return isMultiplePart;
}

public boolean isNewPartRequired() {
    return isNewPartRequired;
}

public void resetNewPartRequired() {
    isNewPartRequired = false;
}

private void throwError() throws ParserException {
    throw new ParserException("Error while parsing: " + yytext());
}

private Yytoken createTokenNormal(int type) {
  return new Yytoken(type, yytext());
}

private Yytoken createTokenTrimmed(int type) {
  return new Yytoken(type, yytext().trim());
}

private Yytoken createAndSaveFieldNameToken(int type) {
  String fieldName = yytext().trim();
  currentFieldName = fieldName;
  return new Yytoken(type, fieldName);
}

private Yytoken createFieldValueToken() {
  String fieldValueWithColonPrefix = yytext().trim();
  String fieldValue = fieldValueWithColonPrefix.replaceFirst(": *", "");
  boolean isContentTypeHeader = "Content-Type".equalsIgnoreCase(currentFieldName);
  if (isContentTypeHeader && yystate() == S_HEADER) {
    isMultiplePart = fieldValue.toLowerCase().contains("multipart/form-data;");
  }
  return new Yytoken(Yytoken.TYPE_FIELD_VALUE, fieldValue);
}

private static final void T(String text) {
  System.out.println(text);
}
%}

%yylexthrow ParserException

InputCharacter = [^\r\n]
Digit = [0-9]
Alpha = [a-zA-Z]

// Line terminators.
LineTerminator = [\r|\n|\r\n]
LineTail = {InputCharacter}* {LineTerminator}

// White space.
WhiteSpace = [ \t]
AnySpace = {LineTerminator} | {WhiteSpace} | [\f]
OptionalWhiteSpace = {WhiteSpace}*
RequiredWhiteSpace = {WhiteSpace}+

// Comments.
LineComment = {OptionalWhiteSpace}(#|\/\/){LineTail}

// Request separator.
RequestSeparator = ###{LineTail}

// Request.
RequestMethod = GET|HEAD|POST|PUT|DELETE|CONNECT|PATCH|OPTIONS|TRACE
RequestHttpVersion = HTTP\/{Digit}+\.{Digit}+

RequestTarget = [^\r\n\s]+

FieldName = [^\r\n:]+
// TODO: Support FieldValue in multiple lines.
FieldValue = {LineTail}

// Message body.
MessageLineText = {LineTail}
FilePath = {LineTail}
MessageLineFile = "<"{RequiredWhiteSpace}{FilePath}

MultiplePartBoundary = \-\-{LineTail}
// Response handler.
HandlerScript = (!(%}|###).)*
ResponseHandlerEmbededOpen = >{RequiredWhiteSpace}\{%
ResponseHandlerEmbededClose = %\}
ResponseHandlerScript = >{RequiredWhiteSpace}{FilePath}

// Response reference.
ResponseReference = <>{RequiredWhiteSpace}{FilePath}

FallbackCharacter = .
%%

{RequestSeparator}                         { reset();
                                             switchState(S_REQUEST_SEPARATOR);
                                             return createTokenTrimmed(Yytoken.TYPE_SEPARATOR);
                                           }

<YYINITIAL> {
  {AnySpace}+                              { T("Ignore any space in YYINITIAL"); }
  {FallbackCharacter}		                   { yypushback(1); switchState(S_REQUEST_LINE); }
}

<S_REQUEST_SEPARATOR>{
  {LineComment}                            { return createTokenNormal(Yytoken.TYPE_COMMENT); }
  {FallbackCharacter}                      { yypushback(1); switchState(S_REQUEST_LINE);}
}

<S_REQUEST_LINE> {
  {WhiteSpace}+                            { T("Ignore {WhiteSpace}+ in S_REQUEST_LINE"); }
  {RequestMethod} /{RequiredWhiteSpace}    { return createTokenTrimmed(Yytoken.TYPE_REQUEST_METHOD); }
  {RequestTarget}                          { hasRequestTarget = true; return createTokenTrimmed(Yytoken.TYPE_REQUEST_TARGET); }
  {RequiredWhiteSpace}{RequestHttpVersion} { return createTokenTrimmed(Yytoken.TYPE_REQUEST_HTTP_VERSION); }
  {LineTerminator}                         { if (!hasRequestTarget) throwError(); switchState(S_HEADER); }
  {FallbackCharacter} { throwError(); }
}

<S_HEADER> {
  {FieldName}/:                            { return createAndSaveFieldNameToken(Yytoken.TYPE_FIELD_NAME); }
  :{OptionalWhiteSpace}{FieldValue}        { return createFieldValueToken(); }
  {LineComment}                            { return createTokenNormal(Yytoken.TYPE_COMMENT); }
  {LineTerminator}|{AnySpace}+             { if (isMultiplePart) switchState(S_MULTILE_PART); else switchState(S_BODY); }
  {FallbackCharacter}                      { T("State S_HEADER fallback for: " + yytext());
                                             yypushback(1);
                                             switchState(YYINITIAL); }
                                           }

<S_BODY> {
  "<>"{LineTail}                           { T("State S_BODY but got <>.* => fallback to response reference"); }
  {LineComment}                            { return createTokenNormal(Yytoken.TYPE_COMMENT); }
  {MessageLineFile}                        { return createTokenNormal(Yytoken.TYPE_VALUE_FILE_REF); }
  {MessageLineText}                        { return createTokenNormal(Yytoken.TYPE_BODY_MESSAGE); }
  {FallbackCharacter}                      { T("State S_BODY falback for: " + yytext());
                                             yypushback(1);
                                             switchState(YYINITIAL);
                                           }
}

<S_MULTILE_PART> {
  "<>"{LineTail}                           { T("State S_BODY_MULTILE_PART but got <>.* => fallback to response reference"); }
  {LineComment}                            { return createTokenNormal(Yytoken.TYPE_COMMENT); }
  {MultiplePartBoundary}                   { isNewPartRequired = true; switchState(S_MULTIPLE_PART_HEADER); }
  {FallbackCharacter}                      { T("State S_BODY falback for: " + yytext());
                                             yypushback(1);
                                             switchState(YYINITIAL);
                                           }
}

<S_MULTIPLE_PART_HEADER>                   {
  {FieldName}/:                            { return createTokenTrimmed(Yytoken.TYPE_FIELD_NAME); }
  :{OptionalWhiteSpace}{FieldValue}        { return createFieldValueToken(); }
  {LineTerminator}|{AnySpace}+             { switchState(S_MULTIPLE_PART_BODY); }
  {FallbackCharacter}                      { throwError(); }
}

<S_MULTIPLE_PART_BODY> {
  "<>"{LineTail}                           { T("State S_BODY but got <>.* => fallback to response reference"); }
  {MultiplePartBoundary}                   { isNewPartRequired = true; switchState(S_MULTIPLE_PART_HEADER); }
  {LineComment}                            { return createTokenNormal(Yytoken.TYPE_COMMENT); }
  {MessageLineText}                        { return new Yytoken(Yytoken.TYPE_BODY_MESSAGE, yytext()); }
  {MessageLineFile}                        { return new Yytoken(Yytoken.TYPE_VALUE_FILE_REF, yytext()); }
  {FallbackCharacter}                      { yypushback(1); switchState(S_SCRIPT_HANDLER); }
}

<S_SCRIPT_HANDLER> {
  {ResponseHandlerScript}                  { switchState(S_SCRIPT_REFERENCE);
                                             return new Yytoken(Yytoken.TYPE_VALUE_FILE_REF, yytext());
                                           }
  {ResponseHandlerEmbededOpen}             { return new Yytoken(Yytoken.TYPE_OPEN_SCRIPT_HANDLER); }
  {ResponseHandlerEmbededClose}            { return new Yytoken(Yytoken.TYPE_CLOSE_SCRIPT_HANDLER); }
  {HandlerScript}                          { return new Yytoken(Yytoken.TYPE_HANDLER_SCRIPT, yytext()); }
  {FallbackCharacter}                      { yypushback(1); switchState(S_SCRIPT_HANDLER); }
}

<S_SCRIPT_REFERENCE> {
  {ResponseReference}                      { return new Yytoken(Yytoken.TYPE_VALUE_FILE_REF, yytext()); }
  {FallbackCharacter}                      { yypushback(1); switchState(YYINITIAL); }
}
